package org.molgenis.compute.generators.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.molgenis.compute.ComputeProperties;
import org.molgenis.compute.model.Compute;
import org.molgenis.compute.model.FoldParameters;
import org.molgenis.compute.model.Input;
import org.molgenis.compute.model.Output;
import org.molgenis.compute.model.Parameters;
import org.molgenis.compute.model.Protocol;
import org.molgenis.compute.model.Step;
import org.molgenis.compute.model.Task;
import org.molgenis.compute.model.Workflow;
import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;

public class TaskGenerator
{
	private List<MapEntity> globalParameters = null;
	private HashMap<String, String> environment = null;
	private Workflow workflow = null;

	private static final Logger LOG = Logger.getLogger(TaskGenerator.class);
	private Compute compute;
	private StringBuilder parameterHeader = null;

	private HashMap<String, List<String>> newEnvironment = new HashMap<String, List<String>>();

	public List<Task> generate(Compute compute) throws IOException
	{
		this.compute = compute;
		workflow = compute.getWorkflow();
		Parameters parameters = compute.getParameters();
		ComputeProperties computeProperties = compute.getComputeProperties();
		environment = compute.getMapUserEnvironment();

		List<Task> result = new ArrayList<Task>();

		globalParameters = parameters.getValues();
		for (Step step : workflow.getSteps())
		{
			// map global to local parameters
			List<MapEntity> localParameters = mapGlobalToLocalParameters(globalParameters, step);

			// collapse parameter values
			localParameters = collapseOnTargets(localParameters, step);

			// add the output templates/values + generate step ids
			localParameters = addResourceValues(step, localParameters);

			// add step ids as
			// (i) taskId = name_id
			// (ii) taskIndex = id
			localParameters = addStepIds(localParameters, step);

			List<Task> tasks = generateTasks(step, localParameters, workflow, computeProperties);
			// generate the tasks from template, add step id
			result.addAll(tasks);

			// uncollapse
			localParameters = TupleUtils.uncollapse(localParameters, Parameters.ID_COLUMN);
			// add local input/output parameters to the global parameters
			addLocalToGlobalParameters(step, localParameters);

		}

		return result;
	}

	private List<Task> generateTasks(Step step, List<MapEntity> localParameters, Workflow workflow,
			ComputeProperties computeProperties) throws IOException
	{
		List<Task> tasks = new ArrayList<Task>();

		for (MapEntity target : localParameters)
		{
			Task task = new Task(target.getString(Task.TASKID_COLUMN));

			try
			{
				Map<String, Object> map = TupleUtils.toMap(target);
				//
				String valueWORKDIR = globalParameters.get(0).getString("user_WORKDIR");
				if (valueWORKDIR != null) map.put("WORKDIR", valueWORKDIR);
				else map.put("WORKDIR", "UNDEFINED");
				// remember parameter values

				if (computeProperties.errorMailAddr != null) map.put(Parameters.ERROR_MESSAGE_ADDR,
						computeProperties.errorMailAddr);

				// for this step: store which target-ids go into which job
				for (Integer id : target.getIntList(Parameters.ID_COLUMN))
				{
					step.setJobName(id, task.getName());
				}

				parameterHeader = new StringBuilder();
				parameterHeader.append("\n#\n## Generated header\n#\n");

				// now source the task's parameters from each prevStep.env on
				// which this task depends

				parameterHeader.append("\n# Assign values to the parameters in this script\n");
				parameterHeader.append("\n# Set taskId, which is the job name of this task");
				parameterHeader.append("\ntaskId=\"").append(task.getName()).append("\"\n");

				parameterHeader.append("\n# Make compute.properties available");
				parameterHeader.append("\nrundir=\"").append(computeProperties.runDir).append("\"");
				parameterHeader.append("\nrunid=\"").append(computeProperties.runId).append("\"");
				parameterHeader.append("\nworkflow=\"").append(computeProperties.workFlow).append("\"");
				parameterHeader.append("\nparameters=\"").append(computeProperties.parametersString()).append("\"");
				parameterHeader.append("\nuser=\"").append(computeProperties.molgenisuser).append("\"");
				parameterHeader.append("\ndatabase=\"").append(computeProperties.database).append("\"");
				parameterHeader.append("\nbackend=\"").append(computeProperties.backend).append("\"");
				parameterHeader.append("\nport=\"").append(computeProperties.port).append("\"");
				parameterHeader.append("\ninterval=\"").append(computeProperties.interval).append("\"");
				parameterHeader.append("\npath=\"").append(computeProperties.path).append("\"");

				for (String previousStepName : step.getPreviousSteps())
				{ // we have jobs on which we depend in this prev step
					Step prevStep = workflow.getStep(previousStepName);
					for (Integer id : target.getIntList(Parameters.ID_COLUMN))
					{
						String prevJobName = prevStep.getJobName(id);

						// prevent duplicate work
						if (!task.getPreviousTasks().contains(prevJobName))
						{
							// for this task: add task dependencies
							task.getPreviousTasks().add(prevJobName);

							// source its environment
							parameterHeader.append(Parameters.SOURCE_COMMAND).append(" ")
									.append(Parameters.ENVIRONMENT_DIR_VARIABLE).append(File.separator)
									.append(prevJobName).append(Parameters.ENVIRONMENT_EXTENSION).append("\n");
						}
					}
				}

				parameterHeader.append("\n\n# Connect parameters to environment\n");

				// now couple input parameters to parameters in sourced
				// environment
				List<String> presentStrings = new ArrayList<String>();
				List<Input> listInputsToFoldNew = new ArrayList<Input>();
				Map<String, String> filters = new LinkedHashMap<String, String>();
				for (Input input : step.getProtocol().getInputs())
				{
					if (input.getType().equalsIgnoreCase(Parameters.LIST_INPUT) && !input.isCombineLists()
							&& compute.getParametersContainer().isMultiParameterFiles())
					{
						// a new way of folding
						// take list of parameters from initial parameter list, where values are the same as for eachOne
						listInputsToFoldNew.add(input);
						continue;
					}
					// good all folding
					else
					{
						String parameterName = input.getName();

						List<String> rowIndex = target.getList(Parameters.ID_COLUMN);
						for (int i = 0; i < rowIndex.size(); i++)
						{
							Object rowIndexObject = rowIndex.get(i);
							String rowIndexString = rowIndexObject.toString();

							String value = null;
							String parameterMapping = step.getParametersMapping().get(parameterName);
							if (parameterMapping != null)
							{
								// parameter is mapped locally
								value = parameterMapping;

								if (input.isKnownRunTime()) value = value.replace(Parameters.STEP_PARAM_SEP_PROTOCOL,
										Parameters.STEP_PARAM_SEP_SCRIPT);
								else value = EnvironmentGenerator.GLOBAL_PREFIX + value;

								String type = input.getType();

								String left = null;
								if (type.equalsIgnoreCase(Input.TYPE_STRING))
								{
									left = parameterName;
									if (presentStrings.contains(left)) continue;
									else presentStrings.add(left);
								}
								else left = parameterName + "[" + i + "]";

								String right = value + "[" + rowIndexString + "]";
								if (right.startsWith(EnvironmentGenerator.GLOBAL_PREFIX))
								{
									right = right.substring(EnvironmentGenerator.GLOBAL_PREFIX.length());
									String realValue = environment.get(right);
									parameterHeader.append(left).append("=").append("\"").append(realValue)
											.append("\"\n");
									filters.put(left, realValue);
									map.put(left, realValue);
								}
								else
								{
									// leave old style (runtime parameter)
									parameterHeader.append(left).append("=${").append(value).append("[")
											.append(rowIndexString).append("]}\n");
								}
							}
							else
							{
								if (step.hasParameter(parameterName))
								{
									value = parameterName;

									Object oValue = map.get(parameterName);

									if (oValue instanceof String)
									{
										if (input.isKnownRunTime())
										{
											value = parameterName;
											value = value.replaceFirst(Parameters.UNDERSCORE,
													Parameters.STEP_PARAM_SEP_SCRIPT);
										}
										else
										{
											value = EnvironmentGenerator.GLOBAL_PREFIX + value;
										}
									}
									else if (oValue instanceof ArrayList)
									{
										if (input.isKnownRunTime())
										{
											value = ((ArrayList<String>) oValue).get(i).toString();
											value = value.replaceFirst(Parameters.UNDERSCORE,
													Parameters.STEP_PARAM_SEP_SCRIPT);
										}
										else
										{
											value = EnvironmentGenerator.GLOBAL_PREFIX + value;
										}
									}

									String type = input.getType();

									String left = null;
									if (type.equalsIgnoreCase(Input.TYPE_STRING))
									{
										left = parameterName;
										if (presentStrings.contains(left)) continue;
										else presentStrings.add(left);
									}
									else left = parameterName + "[" + i + "]";

									String right = value + "[" + rowIndexString + "]";
									if (right.startsWith(EnvironmentGenerator.GLOBAL_PREFIX))
									{
										right = right.substring(EnvironmentGenerator.GLOBAL_PREFIX.length());
										String realValue = environment.get(right);
										parameterHeader.append(left).append("=").append("\"").append(realValue)
												.append("\"\n");
										filters.put(left, realValue);
									}
									else
									{
										// leave old style (runtime parameter)
										parameterHeader.append(left).append("=${").append(value).append("[")
												.append(rowIndexString).append("]}\n");
									}
								}
							}
						}
					}
				}

				foldNew(listInputsToFoldNew, filters);

				parameterHeader
						.append("\n# Validate that each 'value' parameter has only identical values in its list\n")
						.append("# We do that to protect you against parameter values that might not be correctly set at runtime.\n");

				for (Input input : step.getProtocol().getInputs())
				{
					boolean isList = Parameters.LIST_INPUT.equals(input.getType());
					if (!isList)
					{
						String p = input.getName();

						parameterHeader
								.append("if [[ ! $(IFS=$'\\n' sort -u <<< \"${")
								.append(p)
								.append("[*]}\" | wc -l | sed -e 's/^[[:space:]]*//') = 1 ]]; then echo \"Error in Step '")
								.append(step.getName())
								.append("': input parameter '")
								.append(p)
								.append("' is an array with different values. Maybe '")
								.append(p)
								.append("' is a runtime parameter with 'more variable' values than what was folded on generation-time?\" >&2; exit 1; fi\n");
					}
				}
				parameterHeader.append("\n#\n## Start of your protocol template\n#\n\n");

				String script = step.getProtocol().getTemplate();

				// now we check if protocol is shell or freemarker template
				if (step.getProtocol().getType().equalsIgnoreCase(Protocol.TYPE_FREEMARKER) || computeProperties.weave)
				{
					String weavedScript = weaveProtocol(step.getProtocol(), newEnvironment, environment, target);
					script = parameterHeader.toString() + weavedScript;
				}
				else if (step.getProtocol().getType().equalsIgnoreCase(Protocol.TYPE_SHELL)) script = parameterHeader
						.toString() + script;
				else
				{
					script = parameterHeader.toString() + script;
					LOG.warn("STEP [" + step.getName() + "] has protocol [" + step.getProtocol().getName()
							+ "]with unknown type");
				}

				// append footer that appends the task's parameters to
				// environment of this task
				String myEnvironmentFile = Parameters.ENVIRONMENT_DIR_VARIABLE + File.separator + task.getName()
						+ Parameters.ENVIRONMENT_EXTENSION;
				script = script + "\n#\n## End of your protocol template\n#\n";
				script = script + "\n# Save output in environment file: '" + myEnvironmentFile
						+ "' with the output vars of this step\n";

				Iterator<String> itParam = map.keySet().iterator();
				while (itParam.hasNext())
				{
					String p = itParam.next();

					// add to environment only if this is an output
					// iterate through outputs to check that
					Iterator<Output> itOutput = step.getProtocol().getOutputs().iterator();
					while (itOutput.hasNext())
					{
						Output o = itOutput.next();
						if (o.getName().equals(p))
						{
							// we've found a match

							// If parameter not set then ERROR
							String line = "if [[ -z \"$" + p + "\" ]]; then echo \"In step '" + step.getName()
									+ "', parameter '" + p + "' has no value! Please assign a value to parameter '" + p
									+ "'." + "\" >&2; exit 1; fi\n";

							// Else set parameters at right indexes.
							// Explanation: if param file is collapsed in this
							// template, then we should not output a single
							// value but a list of values because next step may
							// be run in uncollapsed fashion

							List<String> rowIndex = target.getList(Parameters.ID_COLUMN);
							for (int i = 0; i < rowIndex.size(); i++)
							{
								Object rowIndexObject = rowIndex.get(i);
								String rowIndexString = rowIndexObject.toString();
								line += "echo \"" + step.getName() + Parameters.STEP_PARAM_SEP_SCRIPT + p + "["
										+ rowIndexString + "]=\\\"${" + p + "[" + i + "]}\\\"\" >> "
										+ myEnvironmentFile + "\n";
							}

							script += line;
						}
					}
				}
				
				task.setScript(script);
				task.setStepName(step.getName());
				task.setParameters(map);

				if (computeProperties.batchOption != null)
				{
					int batchNum = compute.getBatchNumber(map);
					task.setBatchNumber(batchNum);
				}

			}
			catch (Exception e)
			{
				throw new IOException("Generation of protocol '" + step.getProtocol().getName() + "' failed: "
						+ e.getMessage() + ".\nParameters used: " + target);
			}

			tasks.add(task);
		}
		return tasks;
	}

	private void foldNew(List<Input> inputs, Map<String, String> filters)
	{
		for (Input input : inputs)
		{
			FoldParameters originalParameters = compute.getParametersContainer();
			int numberOfFilesContainingParameter = originalParameters.numberOfFilesContainingParameter(input.getName());

			if (numberOfFilesContainingParameter == 1)
			{
				String name = input.getName();
				List<String> filteredParameterValues = originalParameters.getFilteredParameterValues(name, filters);

				List<String> values = new ArrayList<String>();
				for (int i = 0; i < filteredParameterValues.size(); i++)
				{
					String value = filteredParameterValues.get(i);
					parameterHeader.append(String.format("%s[%d]=\"%s\"", name, i, value)).append('\n');
					values.add(value);
				}
				newEnvironment.put(name, values);
			}
			else if (numberOfFilesContainingParameter > 1)
			{
				LOG.error("PARAMETER [" + input.getName() + "] comes is a list, which "
						+ "requires simple way of folding, but comes from several parameter files");
			}
			else if (numberOfFilesContainingParameter == 0)
			{
				LOG.warn("PARAMETER [" + input.getName() + "] does not found in design time files, "
						+ "maybe it is the run time list parameter");
			}
		}
	}

	private String weaveProtocol(Protocol protocol, HashMap<String, List<String>> newEnvironment,
			HashMap<String, String> environment, MapEntity target)
	{
		String template = protocol.getTemplate();
		Hashtable<String, String> values = new Hashtable<String, String>();

		for (Input input : protocol.getInputs())
		{
			if (input.isKnownRunTime()) continue;
			if (input.getType().equalsIgnoreCase(Parameters.STRING))
			{
				String name = input.getName();
				String value = (String) target.get(name);
				if (value.equalsIgnoreCase(Parameters.NOTAVAILABLE))
				{
					// run time value and to prevent weaving
					value = formFreemarker(name);
				}
				name = formFreemarker(name);
				values.put(name, value);
			}
			else if (input.getType().equalsIgnoreCase(Parameters.LIST_INPUT))
			{
				String name = input.getName();

				List<String> arrayList = null;
				if (newEnvironment.containsKey(name)) arrayList = newEnvironment.get(name);
				else arrayList = (ArrayList<String>) target.get(name);

				name += FreemarkerUtils.LIST_SIGN;

				if (checkIfAllAvailable(arrayList))
				{
					String strList = "";
					for (String s : arrayList)
					{
						s = addQuotes(s);
						strList += s + " ";
					}
					strList = strList.trim();
					name = formFreemarker(name);
					name = addQuotes(name);
					values.put(name, strList);
				}
				else
				{
					String value = formFreemarker(name);
					name = formFreemarker(name);
					name = addQuotes(name);
					value = addQuotes(value);
					values.put(name, value);
				}
			}
		}

		String result = FreemarkerUtils.weaveWithoutFreemarker(template, values);
		return result;
	}

	private String addQuotes(String str)
	{
		return "\"" + str + "\"";
	}

	private String formFreemarker(String str)
	{
		return "${" + str + "}";
	}

	private boolean checkIfAllAvailable(List<String> arrayList)
	{
		for (String s : arrayList)
		{
			if (s.equalsIgnoreCase(Parameters.NOTAVAILABLE)) return false;
		}
		return true;
	}

	private List<MapEntity> addStepIds(List<MapEntity> localParameters, Step step)
	{
		int stepId = 0;
		for (MapEntity target : localParameters)
		{
			String name = step.getName() + "_" + stepId;
			target.set(Task.TASKID_COLUMN, name);
			target.set(Task.TASKID_INDEX_COLUMN, stepId++);
		}
		return localParameters;
	}

	private void addLocalToGlobalParameters(Step step, List<MapEntity> localParameters)
	{
		for (int i = 0; i < localParameters.size(); i++)
		{
			MapEntity local = localParameters.get(i);

			for (String localName : local.getAttributeNames())
			{
				if (!localName.contains(Parameters.UNDERSCORE))
				{
					MapEntity tuple = globalParameters.get(i);
					tuple.set(step.getName() + Parameters.UNDERSCORE + localName, local.get(localName));
				}
			}
		}
	}

	private List<MapEntity> addResourceValues(Step step, List<MapEntity> localParameters)
	{
		for (MapEntity target : localParameters)
		{
			// add parameters for resource management:
			Entity defaultResousesMap = globalParameters.get(0);

			// choices to get value for resources
			// 1. get from protocol
			// 2. get default from parameters file
			// 3. get default from protocol file

			if (step.getProtocol().getQueue() == null)
			{
				String queue = (String) defaultResousesMap.get("user_" + Parameters.QUEUE);
				if (queue != null) target.set(Parameters.QUEUE, queue);
				else target.set(Parameters.QUEUE, step.getProtocol().getDefaultQueue());
			}
			else target.set(Parameters.QUEUE, step.getProtocol().getQueue());

			if (step.getProtocol().getNodes() == null)
			{
				String nodes = (String) defaultResousesMap.get("user_" + Parameters.NODES);
				if (nodes != null) target.set(Parameters.NODES, nodes);
				else target.set(Parameters.NODES, step.getProtocol().getDefaultNodes());
			}
			else target.set(Parameters.NODES, step.getProtocol().getNodes());

			if (step.getProtocol().getPpn() == null)
			{
				String ppn = (String) defaultResousesMap.get("user_" + Parameters.PPN);
				if (ppn != null) target.set(Parameters.PPN, ppn);
				else target.set(Parameters.PPN, step.getProtocol().getDefaultPpn());
			}
			else target.set(Parameters.PPN, step.getProtocol().getPpn());

			if (step.getProtocol().getWalltime() == null)
			{
				String walltime = (String) defaultResousesMap.get("user_" + Parameters.WALLTIME);
				if (walltime != null) target.set(Parameters.WALLTIME, walltime);
				else target.set(Parameters.WALLTIME, step.getProtocol().getDefaultWalltime());
			}
			else target.set(Parameters.WALLTIME, step.getProtocol().getWalltime());

			if (step.getProtocol().getMemory() == null)
			{
				String memory = (String) defaultResousesMap.get("user_" + Parameters.MEMORY);
				if (memory != null) target.set(Parameters.MEMORY, memory);
				else target.set(Parameters.MEMORY, step.getProtocol().getDefaultMemory());
			}
			else target.set(Parameters.MEMORY, step.getProtocol().getMemory());

			// add protocol parameters
			for (Output o : step.getProtocol().getOutputs())
			{
				target.set(o.getName(), o.getValue());
			}
		}

		return localParameters;
	}

	private List<MapEntity> collapseOnTargets(List<MapEntity> localParameters, Step step)
	{

		List<String> targets = new ArrayList<String>();

		for (Input i : step.getProtocol().getInputs())
		{
			boolean initialized = true;

			boolean isList = Parameters.LIST_INPUT.equals(i.getType());

			if (!isList && initialized) targets.add(i.getName());
		}

		if (0 == targets.size()) // no values from user_*, so do not collapse
		{
			return localParameters;
		}
		else
		{
			List<MapEntity> collapsed = TupleUtils.collapse(localParameters, targets);
			return collapsed;
		}
	}

	private List<MapEntity> mapGlobalToLocalParameters(List<MapEntity> globalParameters, Step step) throws IOException
	{
		List<MapEntity> localParameters = new ArrayList<MapEntity>();

		for (Entity global : globalParameters)
		{
			MapEntity local = new MapEntity();

			// include row number for later to enable uncollapse
			local.set(Parameters.ID_COLUMN, global.get(Parameters.ID_COLUMN));

			// check and map
			for (Input i : step.getProtocol().getInputs())
			{
				// check the mapping, give error if missing
				String localName = i.getName();
				String globalName = step.getLocalGlobalParameterMap().get(localName);

				// appending "user_" if needed
				String parameterNameWithPrefix = null;
				if (globalName == null)
				{
					// automapping
					globalName = localName;
				}

				boolean found = false;
				for (String col : global.getAttributeNames())
				{
					if (!workflow.parameterHasStepPrefix(globalName)) parameterNameWithPrefix = Parameters.USER_PREFIX
							+ globalName;
					else parameterNameWithPrefix = globalName;

					if (col.equals(parameterNameWithPrefix))
					{
						found = true;
						break;
					}
				}

				if (!found)
				{
					LOG.warn("Parameter [" + localName + "] is unknown at design time");
				}
				else local.set(localName, global.get(parameterNameWithPrefix));
			}

			localParameters.add(local);
		}

		return localParameters;
	}

	/**
	 * Analyze lists in workflow protocols and determine whether these lists should be combined or not
	 * 
	 * @param workflow
	 */
	public void determineCombineLists(Workflow workflow)
	{
		for (Step step : workflow.getSteps())
		{
			Protocol protocol = step.getProtocol();

			// calculate how many separated lists we have
			int size = 0;
			for (Input input : protocol.getInputs())
			{
				if (input.getType().equalsIgnoreCase(Input.TYPE_LIST))
				{
					size++;
				}
			}

			if (size > 1) for (Input input : protocol.getInputs())
			{
				if (input.getType().equalsIgnoreCase(Input.TYPE_LIST) && !input.isCombinedListsNotation())
				{
					input.setCombineLists(false);
				}
			}
		}

	}
}
