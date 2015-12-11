package org.molgenis.compute.generators.impl;

import static com.google.common.collect.Iterables.contains;

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
import org.molgenis.compute.model.Context;
import org.molgenis.compute.model.FoldParameters;
import org.molgenis.compute.model.Input;
import org.molgenis.compute.model.Output;
import org.molgenis.compute.model.Parameters;
import org.molgenis.compute.model.Protocol;
import org.molgenis.compute.model.Step;
import org.molgenis.compute.model.Task;
import org.molgenis.compute.model.TaskInfo;
import org.molgenis.compute.model.Workflow;
import org.molgenis.compute.model.impl.DataEntity;

public class TaskGenerator
{
	private static final Logger LOG = Logger.getLogger(TaskGenerator.class);

	private Context context;
	private List<DataEntity> globalParameters = new ArrayList<DataEntity>();
	private HashMap<String, List<String>> newEnvironment = new HashMap<String, List<String>>();

	private ScriptGenerator scriptGenerator;

	public TaskGenerator(Context context, ScriptGenerator scriptGenerator)
	{
		this.context = context;
		this.scriptGenerator = scriptGenerator;

		setGlobalParameters();
	}

	public List<TaskInfo> generate() throws IOException
	{
		List<TaskInfo> taskInfos = new ArrayList<TaskInfo>();

		Workflow workflow = context.getWorkflow();

		for (Step step : workflow.getSteps())
		{
			// map global to local parameters
			// e.g. input -> in
			List<DataEntity> localParameters = mapGlobalToLocalParameters(step, workflow);

			// Collapse parameter values
			localParameters = collapseOnTargets(step, localParameters);

			// add the output templates/values + generate step ids
			localParameters = addResourceValues(step, localParameters);

			// add step ids as
			// (i) taskId = name_id
			// (ii) taskIndex = id
			localParameters = addStepIds(localParameters, step);

			// Generate the scripts for each task in this step.
			// Add TaskInfo objects to the taskInfos list.
			taskInfos.addAll(scriptGenerator.generateTaskScripts(generateTasks(step, localParameters, workflow,
					context.getComputeProperties(), context.getMapUserEnvironment()), step.getName()));

			// uncollapse
			localParameters = TupleUtils.uncollapse(localParameters);

			// add local input/output parameters to the global parameters
			// e.g. out -> step1.out
			addLocalToGlobalParameters(step, localParameters);
		}

		return taskInfos;
	}

	private List<DataEntity> mapGlobalToLocalParameters(Step step, Workflow workflow) throws IOException
	{
		List<DataEntity> localParameters = new ArrayList<DataEntity>();

		// Loop through all the global parameters
		for (DataEntity globalParameter : globalParameters)
		{
			// Include row number to enable uncollapse
			DataEntity local = new DataEntity(Parameters.ID_COLUMN, globalParameter.get(Parameters.ID_COLUMN));

			// Loop through all the inputs from this particular step
			for (Input input : step.getProtocol().getInputs())
			{
				String localName = input.getName();
				String globalName = step.getLocalGlobalParameterMap().get(localName);

				// check the mapping, give error if missing
				if (globalName == null)
				{
					globalName = localName;
				}

				// Check wether the the list of attributes contains the input name
				Iterable<String> attributes = globalParameter.getAttributeNames();
				if (contains(attributes, globalName))
				{
					local.set(localName, globalParameter.get(globalName));
				}
				else if (contains(attributes, Parameters.USER_PREFIX + globalName))
				{
					local.set(localName, globalParameter.get(Parameters.USER_PREFIX + globalName));
				}
				else
				{
					LOG.warn("Parameter [" + localName + "] is not known at design time");
				}
			}

			localParameters.add(local);
		}

		return localParameters;
	}

	private List<DataEntity> collapseOnTargets(Step step, List<DataEntity> localParameters)
	{
		List<String> targets = new ArrayList<String>();

		// For every input, add its name to the list of targets if its of a list type
		for (Input input : step.getProtocol().getInputs())
		{
			// If the input type does not equal to a list
			if (!Parameters.LIST_INPUT.equals(input.getType()))
			{
				targets.add(input.getName());
			}
		}

		// No values from user_*, so do not collapse
		if (targets.size() == 0)
		{
			return localParameters;
		}
		else
		{
			List<DataEntity> collapsed = TupleUtils.collapse(localParameters, targets);
			return collapsed;
		}
	}

	private List<DataEntity> addResourceValues(Step step, List<DataEntity> localParameters)
	{
		for (DataEntity localParamater : localParameters)
		{
			// add parameters for resource management:
			DataEntity defaultResourcesMap = globalParameters.get(0);

			setProtocolQue(step, localParamater, defaultResourcesMap);
			setProtocolNodes(step, localParamater, defaultResourcesMap);
			setProtocolPpn(step, localParamater, defaultResourcesMap);
			setProtocolWalltime(step, localParamater, defaultResourcesMap);
			setProtocolMemory(step, localParamater, defaultResourcesMap);

			// add protocol parameters
			step.getProtocol().getOutputs().forEach(output -> localParamater.set(output.getName(), output.getValue()));
		}

		return localParameters;
	}

	private void setProtocolMemory(Step step, DataEntity localParamater, DataEntity defaultResourcesMap)
	{
		if (step.getProtocol().getMemory() == null)
		{
			String memory = (String) defaultResourcesMap.get("user_" + Parameters.MEMORY);
			if (memory != null)
			{
				localParamater.set(Parameters.MEMORY, memory);
			}
			else
			{
				localParamater.set(Parameters.MEMORY, step.getProtocol().getDefaultMemory());
			}
		}
		else
		{
			localParamater.set(Parameters.MEMORY, step.getProtocol().getMemory());
		}
	}

	private void setProtocolWalltime(Step step, DataEntity localParamater, DataEntity defaultResourcesMap)
	{
		if (step.getProtocol().getWalltime() == null)
		{
			String walltime = (String) defaultResourcesMap.get("user_" + Parameters.WALLTIME);
			if (walltime != null)
			{
				localParamater.set(Parameters.WALLTIME, walltime);
			}
			else
			{
				localParamater.set(Parameters.WALLTIME, step.getProtocol().getDefaultWalltime());
			}
		}
		else
		{
			localParamater.set(Parameters.WALLTIME, step.getProtocol().getWalltime());
		}
	}

	private void setProtocolPpn(Step step, DataEntity localParamater, DataEntity defaultResourcesMap)
	{
		if (step.getProtocol().getPpn() == null)
		{
			String ppn = (String) defaultResourcesMap.get("user_" + Parameters.PPN);
			if (ppn != null)
			{
				localParamater.set(Parameters.PPN, ppn);
			}
			else
			{
				localParamater.set(Parameters.PPN, step.getProtocol().getDefaultPpn());
			}
		}
		else
		{
			localParamater.set(Parameters.PPN, step.getProtocol().getPpn());
		}
	}

	private void setProtocolNodes(Step step, DataEntity localParamater, DataEntity defaultResourcesMap)
	{
		if (step.getProtocol().getNodes() == null)
		{
			String nodes = (String) defaultResourcesMap.get("user_" + Parameters.NODES);
			if (nodes != null)
			{
				localParamater.set(Parameters.NODES, nodes);
			}
			else
			{
				localParamater.set(Parameters.NODES, step.getProtocol().getDefaultNodes());
			}
		}
		else
		{
			localParamater.set(Parameters.NODES, step.getProtocol().getNodes());
		}
	}

	private void setProtocolQue(Step step, DataEntity localParamater, DataEntity defaultResourcesMap)
	{
		if (step.getProtocol().getQueue() == null)
		{
			String queue = (String) defaultResourcesMap.get("user_" + Parameters.QUEUE);
			if (queue != null)
			{
				localParamater.set(Parameters.QUEUE, queue);
			}
			else
			{
				localParamater.set(Parameters.QUEUE, step.getProtocol().getDefaultQueue());
			}
		}
		else
		{
			localParamater.set(Parameters.QUEUE, step.getProtocol().getQueue());
		}
	}

	private List<Task> generateTasks(Step step, List<DataEntity> localParameters, Workflow workflow,
			ComputeProperties computeProperties, HashMap<String, String> environment) throws IOException
	{
		List<Task> tasks = new ArrayList<Task>();
		StringBuilder parameterHeader = null;
		for (DataEntity target : localParameters)
		{
			Task task = new Task(target.getString(Task.TASKID_COLUMN));

			try
			{
				Map<String, Object> map = target.getValueMap();
				String valueWORKDIR = globalParameters.get(0).getString("user_WORKDIR");
				if (valueWORKDIR != null) map.put("WORKDIR", valueWORKDIR);
				else map.put("WORKDIR", "UNDEFINED");
				// remember parameter values

				if (computeProperties.errorMailAddr != null)
					map.put(Parameters.ERROR_MESSAGE_ADDR, computeProperties.errorMailAddr);

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
				parameterHeader.append("\npath=\"").append(computeProperties.path).append("\"\n");

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

				// now couple input parameters to parameters in sourced environment
				List<String> presentStrings = new ArrayList<String>();
				List<Input> listInputsToFoldNew = new ArrayList<Input>();
				Map<String, String> filters = new LinkedHashMap<String, String>();

				// Loops through all the inputs,
				for (Input input : step.getProtocol().getInputs())
				{
					// If input equals a list, is not a combined list, and the parameters consist of multiple parameter
					// files
					if (input.getType().equalsIgnoreCase(Parameters.LIST_INPUT) && !input.isCombineLists()
							&& context.getFoldParameters().isMultiParameterFiles())
					{
						// a new way of folding takes a list of parameters from initial parameter list, where values are
						// the same as for eachOne
						listInputsToFoldNew.add(input);
						continue;
					}

					else
					{
						// Start folding
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

				parameterHeader = foldIntoHeaderAndSetEnvironment(listInputsToFoldNew, filters, parameterHeader);

				parameterHeader
						.append("\n# Validate that each 'value' parameter has only identical values in its list\n")
						.append("# We do that to protect you against parameter values that might not be correctly set at runtime.\n");

				for (Input input : step.getProtocol().getInputs())
				{
					boolean isList = Parameters.LIST_INPUT.equals(input.getType());
					if (!isList)
					{
						String inputName = input.getName();

						parameterHeader.append("if [[ ! $(IFS=$'\\n' sort -u <<< \"${").append(inputName)
								.append("[*]}\" | wc -l | sed -e 's/^[[:space:]]*//') = 1 ]]; then echo \"Error in Step '")
								.append(step.getName()).append("': input parameter '").append(inputName)
								.append("' is an array with different values. Maybe '").append(inputName)
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
				else if (step.getProtocol().getType().equalsIgnoreCase(Protocol.TYPE_SHELL))
					script = parameterHeader.toString() + script;
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

				Iterator<Output> itOutput = step.getProtocol().getOutputs().iterator();
				while (itOutput.hasNext())
				{
					String parameterName = itOutput.next().getName();
					if (map.containsKey(parameterName))
					{
						// If parameter not set at runtime then ERROR
						String line = "if [[ -z \"$" + parameterName + "\" ]]; then echo \"In step '" + step.getName()
								+ "', parameter '" + parameterName
								+ "' has no value! Please assign a value to parameter '" + parameterName + "'."
								+ "\" >&2; exit 1; fi\n";

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
							line += "echo \"" + step.getName() + Parameters.STEP_PARAM_SEP_SCRIPT + parameterName + "["
									+ rowIndexString + "]=\\\"${" + parameterName + "[" + i + "]}\\\"\" >> "
									+ myEnvironmentFile + "\n";
						}

						script += line;
					}

				}
				script = appendToEnv(script, "", myEnvironmentFile);
				script += "\n";

				task.setScript(script);
				task.setStepName(step.getName());
				task.setParameters(map);

				if (computeProperties.batchOption != null)
				{
					int batchNum = context.getBatchNumber(map);
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

	private StringBuilder foldIntoHeaderAndSetEnvironment(List<Input> inputs, Map<String, String> filters,
			StringBuilder parameterHeader)
	{
		for (Input input : inputs)
		{
			FoldParameters foldParameters = context.getFoldParameters();
			String inputName = input.getName();
			int numberOfFilesContainingParameter = foldParameters.numberOfFilesContainingParameter(inputName);

			if (numberOfFilesContainingParameter == 1)
			{
				List<String> filteredParameterValues = foldParameters.getFilteredParameterValues(inputName, filters);

				List<String> values = new ArrayList<String>();
				for (int i = 0; i < filteredParameterValues.size(); i++)
				{
					String parameterValue = filteredParameterValues.get(i);
					parameterHeader.append(String.format("%s[%d]=\"%s\"", inputName, i, parameterValue)).append('\n');
					values.add(parameterValue);
				}
				newEnvironment.put(inputName, values);
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

		return parameterHeader;
	}

	private String weaveProtocol(Protocol protocol, HashMap<String, List<String>> newEnvironment,
			HashMap<String, String> environment, DataEntity target)
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
					StringBuilder strList = new StringBuilder("");
					for (String s : arrayList)
					{
						strList.append('"');
						strList.append(s);
						strList.append('"').append(' ');
					}
					name = formFreemarker(name);
					name = addQuotes(name);
					values.put(name, strList.toString().trim());
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

	private String appendToEnv(String script, String string, String thisFile)
	{
		String appendString = "echo \"" + string + "\" >> " + thisFile + "\n" + "chmod 755 " + thisFile + "\n";

		return script + "\n" + appendString;
	}

	private List<DataEntity> addStepIds(List<DataEntity> localParameters, Step step)
	{
		int stepId = 0;
		for (DataEntity target : localParameters)
		{
			String name = step.getName() + "_" + stepId;
			target.set(Task.TASKID_COLUMN, name);
			target.set(Task.TASKID_INDEX_COLUMN, stepId++);
		}
		return localParameters;
	}

	private void addLocalToGlobalParameters(Step step, List<DataEntity> localParameters)
	{
		for (int i = 0; i < localParameters.size(); i++)
		{
			DataEntity local = localParameters.get(i);

			for (String localName : local.getAttributeNames())
			{
				if (!localName.contains(Parameters.UNDERSCORE))
				{
					DataEntity tuple = globalParameters.get(i);
					tuple.set(step.getName() + Parameters.UNDERSCORE + localName, local.get(localName));
				}
			}
		}
	}

	private void setGlobalParameters()
	{
		globalParameters = context.getParameters().getValues();
	}
}
