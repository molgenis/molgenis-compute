package org.molgenis.compute5.generators;

import java.io.*;
import java.util.*;


import org.apache.log4j.Logger;
import org.molgenis.compute5.model.*;
import org.molgenis.data.support.MapEntity;
import org.molgenis.util.Pair;

public class EnvironmentGenerator
{
	/**
	 * Returns initial environment with all user params that are used somewhere in this workflow
	 * @param compute
	 * @return
	 */

	public static final String GLOBAL_PREFIX = "global_";

	private HashMap<String, String> environment = new HashMap<String, String>();
	private List<Step> steps = null;
	private Workflow workflow = null;

	//for error handling
	private ArrayList<Pair<String, String>> arrayOfParameterSteps = new ArrayList<Pair<String, String>>();

	private static final Logger LOG = Logger.getLogger(EnvironmentGenerator.class);


	public String getEnvironmentAsString(Compute compute) throws Exception
	{
		workflow = compute.getWorkflow();

		// put header 'adding user params' in environment
		StringBuilder output = new StringBuilder(1024);
		output.append("#\n## User parameters\n#\n");

		// user parameters that we want to put in environment
		HashSet<String> userInputParamSet = new HashSet<String>();

		steps = compute.getWorkflow().getSteps();

		// first collect all user parameters that are used in this workflow
		for (Step step : steps)
		{
			Map<String, String> pmap = step.getParametersMapping();

			Iterator<String> itValue = pmap.values().iterator();
			while (itValue.hasNext())
			{
				String value = itValue.next();

				arrayOfParameterSteps.add(new Pair(value, step.getName()));

				if(!workflow.parameterHasStepPrefix(value))
				{
					userInputParamSet.add(value);
				}
			}

			List<String> autoMappedParameters = step.getAutoMappedParameters();

			for(String s : autoMappedParameters)
			{
				arrayOfParameterSteps.add(new Pair(s, step.getName()));
			}

			userInputParamSet.addAll(autoMappedParameters);

		}

		// get all parameters from parameters.csv
		for (String parameter: userInputParamSet)
		{
			String userParameter = Parameters.USER_PREFIX + parameter;

			for (MapEntity wt : compute.getParameters().getValues())
			{
				// retrieve index and value for that index
				Integer index = null;
				String value = null;
				for (String col : wt.getAttributeNames())
				{
					if (col.equals(userParameter))
						value = wt.getString(col);
					if (col.equals(Parameters.USER_PREFIX + Task.TASKID_COLUMN))
						index = wt.getInt(col);
				}

				if (value == null)
				{

					if(!isFoundAsOutput(parameter, wt))
					{
						List<String> relatedSteps = findRelatedSteps(parameter);
						throw new Exception("Parameter '" + parameter +
								"' used in steps " + relatedSteps.toString() +
								"does not have value in the parameters (.csv, .properties) files ");
					}
					else
					{
						LOG.warn("Variable [" + index + "] has run time value");
					}

				}
				else
				{
					StringBuilder assignment = new StringBuilder();
					assignment.append(parameter).append("[").append(index).append("]=\"")
							.append(value).append("\"\n");

					environment.put(parameter + "[" + index + "]", value);
					output.append(assignment.toString());
				}
			}
		}
		
		return output.toString();
	}

	private List<String> findRelatedSteps(String parameter)
	{
		List<String> relatedSteps = new ArrayList<String>();

		for(Pair<String, String> pair : arrayOfParameterSteps)
		{
			if(pair.getA().equalsIgnoreCase(parameter))
				relatedSteps.add(pair.getB());
		}

		return relatedSteps;
	}

	private boolean isFoundAsOutput(String parameter, MapEntity wt)
	{
		boolean isRunTime = false;

		for(Step step: workflow.getSteps())
		{
			Set<Output> outputs = step.getProtocol().getOutputs();
			for(Output output : outputs)
			{
				//first search for auto.mapping
				String name = output.getName();
				if(name.equalsIgnoreCase(parameter))
				{
					boolean canBeKnown = checkIfVariableCanbeKnown(step.getName(), parameter);

					if(canBeKnown)
					{
						wt.set("user_" + parameter, step.getName() + "_" + parameter);
						return true;
					}
				}

				//else search for step.mapping
				name = step.getName() + Parameters.STEP_PARAM_SEP_PROTOCOL + output.getName();
				if(name.equalsIgnoreCase(parameter))
				{
					boolean canBeKnown = checkIfVariableCanbeMapped(step.getName(), parameter);

					if(canBeKnown)
					{
						wt.set("user_" + parameter, step.getName() + Parameters.STEP_PARAM_SEP_SCRIPT + parameter);
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean checkIfVariableCanbeKnown(String previousStepName, String parameterName)
	{
		for(Step step: workflow.getSteps())
		{
			for(Input input: step.getProtocol().getInputs())
			{
				if(input.getName().equalsIgnoreCase(parameterName))
				{
					//this step has input named parameterName
					Set<String> previousSteps = step.getPreviousSteps();
					if(previousSteps.contains(previousStepName))
					{
						//this step has previous step with output parameterName, so it can be known at run time
						input.setKnownRunTime(true);
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean checkIfVariableCanbeMapped(String previousStepName, String parameterName)
	{
		boolean isRunTimeVariable = false;
		String key = null;

		for(Step step: workflow.getSteps())
		{
			if(step.getPreviousSteps().contains(previousStepName))
			{
				Map<String, String> parameterMappings = step.getParametersMapping();

				for (Map.Entry<String, String> entry : parameterMappings.entrySet())
				{
					key = entry.getKey();
					String value = entry.getValue();

					if(value.equalsIgnoreCase(parameterName))
					{
						isRunTimeVariable = true;
						break;
					}
				}
			}

			if(isRunTimeVariable)
			for(Input input: step.getProtocol().getInputs())
			{
				if(input.getName().equalsIgnoreCase(key))
				{
						input.setKnownRunTime(true);
						return isRunTimeVariable;
				}
			}
		}
		return isRunTimeVariable;
	}


	public HashMap<String, String> generate(Compute compute, String workDir) throws Exception
	{
		Parameters.ENVIRONMENT_FULLPATH = workDir + File.separator + Parameters.ENVIRONMENT;
		
		File env = new File(Parameters.ENVIRONMENT_FULLPATH);
		env.delete();

			// give user environment to compute
			String strUserEnvironment = getEnvironmentAsString(compute);

			// create new environment file
			env.createNewFile();

			//start global prefix fix
			StringBuilder prefixedEnvironment = new StringBuilder();
			String[] lines = strUserEnvironment.split(System.getProperty("line.separator"));
			for(int i = 0; i < lines.length; i++)
			{
				String line = lines[i];
				if(line.startsWith("#"))
					prefixedEnvironment.append(line).append('\n');
				else
					prefixedEnvironment.append(GLOBAL_PREFIX).append(line).append('\n');
			}

			String strPrefixed = prefixedEnvironment.toString();

			compute.setUserEnvironment(strPrefixed);
			//end fix

			BufferedWriter output = new BufferedWriter(new FileWriter(env, true));
			output.write(strPrefixed);
			output.close();

		return environment;
	}
}
