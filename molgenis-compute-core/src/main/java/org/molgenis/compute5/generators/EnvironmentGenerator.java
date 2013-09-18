package org.molgenis.compute5.generators;

import java.io.*;
import java.util.*;

import com.sun.tools.javac.util.Pair;
import org.apache.log4j.Logger;
import org.molgenis.compute5.model.*;
import org.molgenis.util.tuple.WritableTuple;

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
		String output = "#\n## User parameters\n#\n";

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

			for (WritableTuple wt : compute.getParameters().getValues())
			{
				// retrieve index and value for that index
				Integer index = null;
				String value = null;
				for (String col : wt.getColNames())
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
					String assignment = parameter + "[" + index + "]=\"" + value + "\"\n";

					environment.put(parameter + "[" + index + "]", value);
					output += assignment;
				}
			}
		}
		
		return output;
	}

	private List<String> findRelatedSteps(String parameter)
	{
		List<String> relatedSteps = new ArrayList<String>();

		for(Pair<String, String> pair : arrayOfParameterSteps)
		{
			if(pair.fst.equalsIgnoreCase(parameter))
				relatedSteps.add(pair.snd);
		}

		return relatedSteps;
	}

	private boolean isFoundAsOutput(String parameter, WritableTuple wt)
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
			String prefixedEnvironment = "";
			String[] lines = strUserEnvironment.split(System.getProperty("line.separator"));
			for(int i = 0; i < lines.length; i++)
			{
				String line = lines[i];
				if(line.startsWith("#"))
					prefixedEnvironment += line + "\n";
				else
					prefixedEnvironment += GLOBAL_PREFIX + line + "\n";
			}

			compute.setUserEnvironment(prefixedEnvironment);
			//end fix

			BufferedWriter output = new BufferedWriter(new FileWriter(env, true));
			output.write(prefixedEnvironment);
			output.close();

		return environment;
	}
}
