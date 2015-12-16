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
import org.molgenis.compute.model.StringStore;
import org.molgenis.compute.model.Task;
import org.molgenis.compute.model.TaskInfo;
import org.molgenis.compute.model.Workflow;
import org.molgenis.compute.model.impl.DataEntity;

import com.google.common.collect.Lists;

public class TaskGenerator
{
	private static final Logger LOG = Logger.getLogger(TaskGenerator.class);

	private Context context;
	private List<DataEntity> globalParameters = new ArrayList<DataEntity>();

	private ScriptGenerator scriptGenerator;
	private StringStore stringStore;

	public TaskGenerator(Context context, ScriptGenerator scriptGenerator, StringStore stringStore)
	{
		this.context = context;
		this.scriptGenerator = scriptGenerator;
		this.stringStore = stringStore;

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

			// add the output templates/values
			localParameters = addResourceValues(step, localParameters);

			// add step ids as
			// (i) taskId = name_id
			// (ii) taskIndex = id
			localParameters = addStepIds(localParameters, step);

			// Generate the scripts for each task in this step.
			// Add TaskInfo objects to the taskInfos list.
			taskInfos.addAll(scriptGenerator.generateTaskScripts(generateTasks(step, localParameters), step.getName()));

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
			if (!(input.getType() == Input.Type.LIST))
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

	private List<Task> generateTasks(Step step, List<DataEntity> localParameters) throws IOException
	{
		return Lists.transform(localParameters, target -> generateTask(step, target));
	}

	private Task generateTask(Step step, DataEntity dataEntity)
	{
		Task task = new Task(dataEntity.getString(Task.TASKID_COLUMN));
		ComputeProperties computeProperties = context.getComputeProperties();
		Workflow workflow = context.getWorkflow();

		try
		{
			Map<String, Object> dataEntityValues = dataEntity.getValueMap();
			String valueWORKDIR = globalParameters.get(0).getString("user_WORKDIR");
			if (valueWORKDIR != null) dataEntityValues.put("WORKDIR", stringStore.intern(valueWORKDIR));
			else dataEntityValues.put("WORKDIR", "UNDEFINED");
			// remember parameter values

			if (computeProperties.errorMailAddr != null)
				dataEntityValues.put(Parameters.ERROR_MESSAGE_ADDR, computeProperties.errorMailAddr);

			// for this step: store which target-ids go into which job
			for (Integer id : dataEntity.getIntList(Parameters.ID_COLUMN))
			{
				step.setJobName(id, stringStore.intern(task.getName()));
			}

			StringBuilder scriptBuilder = new StringBuilder();
			scriptBuilder.append("\n#\n## Generated header\n#\n");

			// now source the task's parameters from each prevStep.env on
			// which this task depends

			scriptBuilder.append("\n# Assign values to the parameters in this script\n");
			scriptBuilder.append("\n# Set taskId, which is the job name of this task");
			scriptBuilder.append("\ntaskId=\"").append(task.getName()).append("\"\n");

			scriptBuilder.append("\n# Make compute.properties available");
			scriptBuilder.append("\nrundir=\"").append(computeProperties.runDir).append("\"");
			scriptBuilder.append("\nrunid=\"").append(computeProperties.runId).append("\"");
			scriptBuilder.append("\nworkflow=\"").append(computeProperties.workFlow).append("\"");
			scriptBuilder.append("\nparameters=\"").append(computeProperties.parametersString()).append("\"");
			scriptBuilder.append("\nuser=\"").append(computeProperties.molgenisuser).append("\"");
			scriptBuilder.append("\ndatabase=\"").append(computeProperties.database).append("\"");
			scriptBuilder.append("\nbackend=\"").append(computeProperties.backend).append("\"");
			scriptBuilder.append("\nport=\"").append(computeProperties.port).append("\"");
			scriptBuilder.append("\ninterval=\"").append(computeProperties.interval).append("\"");
			scriptBuilder.append("\npath=\"").append(computeProperties.path).append("\"\n");

			for (String previousStepName : step.getPreviousSteps())
			{ // we have jobs on which we depend in this prev step
				Step prevStep = workflow.getStep(previousStepName);
				for (Integer id : dataEntity.getIntList(Parameters.ID_COLUMN))
				{
					String prevJobName = prevStep.getJobName(id);

					// prevent duplicate work
					if (!task.getPreviousTasks().contains(prevJobName))
					{
						// for this task: add task dependencies
						task.getPreviousTasks().add(prevJobName);

						// source its environment
						scriptBuilder.append(Parameters.SOURCE_COMMAND).append(" ")
								.append(Parameters.ENVIRONMENT_DIR_VARIABLE).append(File.separator).append(prevJobName)
								.append(Parameters.ENVIRONMENT_EXTENSION).append("\n");
					}
				}
			}

			scriptBuilder.append("\n\n# Connect parameters to environment\n");

			// now couple input parameters to parameters in sourced environment
			List<String> presentStrings = new ArrayList<String>();
			List<Input> listInputsToFoldNew = new ArrayList<Input>();
			Map<String, String> filters = new LinkedHashMap<String, String>();

			// Loops through all the inputs,
			for (Input input : step.getProtocol().getInputs())
			{
				// If input equals a list, is not a combined list, and the parameters consist of multiple parameter
				// files
				if (input.getType() == Input.Type.LIST && !input.isCombineLists()
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
					String localParameterName = input.getName();

					List<String> rowIndex = dataEntity.getList(Parameters.ID_COLUMN);
					for (int i = 0; i < rowIndex.size(); i++)
					{
						Object rowIndexObject = rowIndex.get(i);
						String rowIndexString = rowIndexObject.toString();

						String value = null;
						String parameterMapping = step.getParametersMapping().get(localParameterName);
						if (parameterMapping != null)
						{
							// parameter is mapped locally
							value = parameterMapping;
							if (input.isKnownRunTime()) value = value.replace(Parameters.STEP_PARAM_SEP_PROTOCOL,
									Parameters.STEP_PARAM_SEP_SCRIPT);
							else value = EnvironmentGenerator.GLOBAL_PREFIX + value;

							String left = null;
							if (input.getType() == Input.Type.STRING)
							{
								left = localParameterName;
								if (presentStrings.contains(left)) continue;
								else presentStrings.add(left);
							}
							else left = localParameterName + "[" + i + "]";

							String right = value + "[" + rowIndexString + "]";

							if (right.startsWith(EnvironmentGenerator.GLOBAL_PREFIX))
							{
								right = right.substring(EnvironmentGenerator.GLOBAL_PREFIX.length());
								String realValue = context.getParameters().getValues()
										.get(Integer.parseInt(rowIndexString))
										.getString(value.replaceFirst("global_", "user_"));
								scriptBuilder.append(left).append("=").append("\"").append(realValue).append("\"\n");
								filters.put(left, realValue);
								dataEntityValues.put(stringStore.intern(left), stringStore.intern(realValue));
							}
							else
							{
								// leave old style (runtime parameter)
								scriptBuilder.append(left).append("=${").append(value).append("[")
										.append(rowIndexString).append("]}\n");
							}
						}
						else
						{
							if (step.hasParameter(localParameterName))
							{
								value = localParameterName;

								Object oValue = dataEntityValues.get(localParameterName);

								if (oValue instanceof String)
								{
									if (input.isKnownRunTime())
									{
										value = localParameterName;
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

								String left = null;
								if (input.getType() == Input.Type.STRING)
								{
									left = localParameterName;
									if (presentStrings.contains(left)) continue;
									else presentStrings.add(left);
								}
								else left = localParameterName + "[" + i + "]";

								String right = value + "[" + rowIndexString + "]";
								if (right.startsWith(EnvironmentGenerator.GLOBAL_PREFIX))
								{
									right = right.substring(EnvironmentGenerator.GLOBAL_PREFIX.length());
									String realValue = context.getParameters().getValues().get(Integer.parseInt(rowIndexString))
											.getString(value.replaceFirst("global_", "user_"));
									scriptBuilder.append(left).append("=").append("\"").append(realValue)
											.append("\"\n");
									filters.put(left, realValue);
								}
								else
								{
									// leave old style (runtime parameter)
									scriptBuilder.append(left).append("=${").append(value).append("[")
											.append(rowIndexString).append("]}\n");
								}
							}
						}
					}
				}
			}

			Map<String, List<String>> collapsedEnvironment = foldIntoHeaderAndSetEnvironment(listInputsToFoldNew,
					filters, scriptBuilder);

			scriptBuilder.append("\n# Validate that each 'value' parameter has only identical values in its list\n")
					.append("# We do that to protect you against parameter values that might not be correctly set at runtime.\n");

			for (Input input : step.getProtocol().getInputs())
			{
				if (!(input.getType() == Input.Type.LIST))
				{
					String inputName = input.getName();

					scriptBuilder.append("if [[ ! $(IFS=$'\\n' sort -u <<< \"${").append(inputName)
							.append("[*]}\" | wc -l | sed -e 's/^[[:space:]]*//') = 1 ]]; then echo \"Error in Step '")
							.append(step.getName()).append("': input parameter '").append(inputName)
							.append("' is an array with different values. Maybe '").append(inputName)
							.append("' is a runtime parameter with 'more variable' values than what was folded on generation-time?\" >&2; exit 1; fi\n");
				}
			}
			scriptBuilder.append("\n#\n## Start of your protocol template\n#\n\n");


			// now we check if protocol is shell or freemarker template
			if (step.getProtocol().getType().equalsIgnoreCase(Protocol.TYPE_FREEMARKER) || computeProperties.weave)
			{
				String weavedScript = weaveProtocol(step.getProtocol(), dataEntity, collapsedEnvironment);
				scriptBuilder.append(weavedScript);
			}
			else {
				if (step.getProtocol().getType().equalsIgnoreCase(Protocol.TYPE_SHELL)){
					LOG.warn("STEP [" + step.getName() + "] has protocol [" + step.getProtocol().getName()
							+ "]with unknown type");
				}
				scriptBuilder.append(step.getProtocol().getTemplate());
			}

			// append footer that appends the task's parameters to
			// environment of this task
			String myEnvironmentFile = Parameters.ENVIRONMENT_DIR_VARIABLE + File.separator + task.getName()
					+ Parameters.ENVIRONMENT_EXTENSION;
			scriptBuilder.append("\n#\n## End of your protocol template\n#\n");
			scriptBuilder.append("\n# Save output in environment file: '" + myEnvironmentFile
					+ "' with the output vars of this step\n");

			Iterator<Output> itOutput = step.getProtocol().getOutputs().iterator();
			while (itOutput.hasNext())
			{
				String parameterName = itOutput.next().getName();
				if (dataEntityValues.containsKey(parameterName))
				{
					// If parameter not set at runtime then ERROR
					String line = "if [[ -z \"$" + parameterName + "\" ]]; then echo \"In step '" + step.getName()
							+ "', parameter '" + parameterName + "' has no value! Please assign a value to parameter '"
							+ parameterName + "'." + "\" >&2; exit 1; fi\n";

					// Else set parameters at right indexes.
					// Explanation: if param file is collapsed in this
					// template, then we should not output a single
					// value but a list of values because next step may
					// be run in uncollapsed fashion

					List<String> rowIndex = dataEntity.getList(Parameters.ID_COLUMN);
					for (int i = 0; i < rowIndex.size(); i++)
					{
						Object rowIndexObject = rowIndex.get(i);
						String rowIndexString = rowIndexObject.toString();
						line += "echo \"" + step.getName() + Parameters.STEP_PARAM_SEP_SCRIPT + parameterName + "["
								+ rowIndexString + "]=\\\"${" + parameterName + "[" + i + "]}\\\"\" >> "
								+ myEnvironmentFile + "\n";
					}

					scriptBuilder.append(line);
				}

			}
			scriptBuilder.append("\necho \"\" >> " + myEnvironmentFile + "\nchmod 755 " + myEnvironmentFile + "\n");
			scriptBuilder.append("\n");

			task.setScript(scriptBuilder.toString());
			task.setStepName(step.getName());
			task.setParameters(dataEntityValues);

			if (computeProperties.batchOption != null)
			{
				int batchNum = context.getBatchNumber(dataEntityValues);
				task.setBatchNumber(batchNum);
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException("Generation of protocol '" + step.getProtocol().getName() + "' failed: "
					+ e.getMessage() + ".\nParameters used: " + dataEntity);
		}
		return task;
	}

	private HashMap<String, List<String>> foldIntoHeaderAndSetEnvironment(List<Input> inputs,
			Map<String, String> filters, StringBuilder parameterHeader)
	{
		HashMap<String, List<String>> collapsedEnvironment = new LinkedHashMap<>();

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
				collapsedEnvironment.put(inputName, values);
			}
			else if (numberOfFilesContainingParameter == 0)
			{
				LOG.warn("PARAMETER [" + input.getName() + "] is not found in design time files, "
						+ "maybe it is the run time list parameter");
			}
		}

		return collapsedEnvironment;
	}

	private String weaveProtocol(Protocol protocol, DataEntity target, Map<String, List<String>> collapsedEnvironment)
	{
		String template = protocol.getTemplate();
		Hashtable<String, String> values = new Hashtable<String, String>();

		for (Input input : protocol.getInputs())
		{
			if (input.isKnownRunTime()) continue;
			if (input.getType() == Input.Type.STRING)
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
			else if (input.getType() == Input.Type.LIST)
			{
				String name = input.getName();

				List<String> arrayList = null;
				if (collapsedEnvironment.containsKey(name)) arrayList = collapsedEnvironment.get(name);
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
