package org.molgenis.compute.generators.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.molgenis.compute.model.Context;
import org.molgenis.compute.model.Input;
import org.molgenis.compute.model.Output;
import org.molgenis.compute.model.Parameters;
import org.molgenis.compute.model.Step;
import org.molgenis.compute.model.Task;
import org.molgenis.compute.model.impl.WorkflowImpl;
import org.molgenis.data.support.MapEntity;
import org.molgenis.util.Pair;

/**
 * Returns initial environment with all user params that are used somewhere in this workflow
 * 
 * @param compute
 * @return
 */

public class EnvironmentGenerator
{
	private static final Logger LOG = Logger.getLogger(EnvironmentGenerator.class);

	public static final String GLOBAL_PREFIX = "global_";

	private HashMap<String, String> environment = new HashMap<String, String>();
	private List<Step> steps = null;
	private WorkflowImpl workflowImpl = null;

	// for error handling
	private ArrayList<Pair<String, String>> arrayOfParameterSteps = new ArrayList<Pair<String, String>>();

	public String getEnvironmentAsString(Context compute) throws Exception
	{
		workflowImpl = compute.getWorkflow();

		// put header 'adding user params' in environment
		StringBuilder output = new StringBuilder(1024);
		output.append("#\n## User parameters\n#\n");

		// user parameters that we want to put in environment
		HashSet<String> userInputParamSet = new LinkedHashSet<String>();

		steps = compute.getWorkflow().getSteps();

		// first collect all user parameters that are used in this workflow
		for (Step step : steps)
		{
			Map<String, String> pmap = step.getParametersMapping();

			Iterator<String> itValue = pmap.values().iterator();
			while (itValue.hasNext())
			{
				String value = itValue.next();

				arrayOfParameterSteps.add(new Pair<String, String>(value, step.getName()));

				if (!workflowImpl.parameterHasStepPrefix(value))
				{
					userInputParamSet.add(value);
				}
			}

			List<String> autoMappedParameters = step.getAutoMappedParameters();

			for (String s : autoMappedParameters)
			{
				arrayOfParameterSteps.add(new Pair<String, String>(s, step.getName()));
			}

			userInputParamSet.addAll(autoMappedParameters);

		}

		// get all parameters from parameters.csv
		for (String parameter : userInputParamSet)
		{
			String userParameter = Parameters.USER_PREFIX + parameter;

			for (MapEntity parameterValues : compute.getParameters().getValues())
			{
				// retrieve index and value for that index
				Integer index = null;
				String value = null;
				for (String col : parameterValues.getAttributeNames())
				{
					if (col.equals(userParameter)) value = parameterValues.getString(col);
					if (col.equals(Parameters.USER_PREFIX + Task.TASKID_COLUMN)) index = parameterValues.getInt(col);
				}

				if (value == null)
				{

					if (!isFoundAsOutput(parameter, parameterValues))
					{
						List<String> relatedSteps = findRelatedSteps(parameter);
						throw new Exception("Parameter '" + parameter + "' used in steps " + relatedSteps.toString()
								+ "does not have value in the parameters (.csv, .properties) files ");
					}
					else
					{
						LOG.warn("Variable [" + index + "] has run time value");
					}

				}
				else
				{
					StringBuilder assignment = new StringBuilder();
					assignment.append(parameter).append("[").append(index).append("]=\"").append(value).append("\"\n");

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

		for (Pair<String, String> pair : arrayOfParameterSteps)
		{
			if (pair.getA().equalsIgnoreCase(parameter)) relatedSteps.add(pair.getB());
		}

		return relatedSteps;
	}

	private boolean isFoundAsOutput(String parameter, MapEntity wt)
	{
		for (Step step : workflowImpl.getSteps())
		{
			Set<Output> outputs = step.getProtocol().getOutputs();
			for (Output output : outputs)
			{
				// first search for auto.mapping
				String name = output.getName();
				if (name.equalsIgnoreCase(parameter))
				{
					boolean canBeKnown = checkIfVariableCanbeKnown(step.getName(), parameter);

					if (canBeKnown)
					{
						wt.set("user_" + parameter, step.getName() + "_" + parameter);
						return true;
					}
				}

				// else search for step.mapping
				name = step.getName() + Parameters.STEP_PARAM_SEP_PROTOCOL + output.getName();
				if (name.equalsIgnoreCase(parameter))
				{
					boolean canBeKnown = checkIfVariableCanbeMapped(step.getName(), parameter);

					if (canBeKnown)
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
		for (Step step : workflowImpl.getSteps())
		{
			for (Input input : step.getProtocol().getInputs())
			{
				if (input.getName().equalsIgnoreCase(parameterName))
				{
					// this step has input named parameterName
					Set<String> previousSteps = step.getPreviousSteps();
					if (previousSteps.contains(previousStepName))
					{
						// this step has previous step with output parameterName, so it can be known at run time
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

		for (Step step : workflowImpl.getSteps())
		{
			if (step.getPreviousSteps().contains(previousStepName))
			{
				Map<String, String> parameterMappings = step.getParametersMapping();

				for (Map.Entry<String, String> entry : parameterMappings.entrySet())
				{
					key = entry.getKey();
					String value = entry.getValue();

					if (value.equalsIgnoreCase(parameterName))
					{
						isRunTimeVariable = true;
						break;
					}
				}
			}

			if (isRunTimeVariable) for (Input input : step.getProtocol().getInputs())
			{
				if (input.getName().equalsIgnoreCase(key))
				{
					input.setKnownRunTime(true);
					return isRunTimeVariable;
				}
			}
		}
		return isRunTimeVariable;
	}

	public HashMap<String, String> generate(Context compute, String workDir) throws Exception
	{
		Parameters.ENVIRONMENT_FULLPATH = workDir + File.separator + Parameters.ENVIRONMENT;

		File env = new File(Parameters.ENVIRONMENT_FULLPATH);
		env.delete();

		// give user environment to compute
		String strUserEnvironment = getEnvironmentAsString(compute);

		// create new environment file
		env.createNewFile();

		// start global prefix fix
		StringBuilder prefixedEnvironment = new StringBuilder();
		String[] lines = strUserEnvironment.split(System.getProperty("line.separator"));
		for (int i = 0; i < lines.length; i++)
		{
			String line = lines[i];
			if (line.startsWith("#")) prefixedEnvironment.append(line).append('\n');
			else prefixedEnvironment.append(GLOBAL_PREFIX).append(line).append('\n');
		}

		String strPrefixed = prefixedEnvironment.toString();

		compute.setUserEnvironment(strPrefixed);
		// end fix

		BufferedWriter output = new BufferedWriter(new FileWriter(env, true));
		output.write(strPrefixed);
		output.close();

		return environment;
	}
}
