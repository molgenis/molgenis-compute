package org.molgenis.compute.model.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.molgenis.compute.model.Input;
import org.molgenis.compute.model.Output;
import org.molgenis.compute.model.Parameters;
import org.molgenis.compute.model.Step;
import org.molgenis.compute.model.Workflow;
import org.molgenis.util.Pair;

import com.google.gson.Gson;

public class WorkflowImpl implements Workflow
{
	private List<Step> steps = new ArrayList<Step>();

	// for error handling
	private ArrayList<Pair<String, String>> arrayOfParameterSteps = new ArrayList<Pair<String, String>>();

	@Override
	public Set<String> getUserParameters()
	{
		Set<String> result = new HashSet<String>();
		for (Step step : steps)
		{
			for (String value : step.getParametersMapping().values())
			{
				if (value.startsWith(Parameters.USER_PREFIX))
				{
					result.add(value);
				}
			}
		}
		return result;
	}

	@Override
	public List<Step> getSteps()
	{
		return steps;
	}

	@Override
	public void addStep(Step step)
	{
		this.steps.add(step);
	}

	@Override
	public Step getStep(String stepName)
	{
		for (Step step : steps)
		{
			if (stepName.equals(step.getName())) return step;
		}
		return null;
	}

	@Override
	public boolean parameterHasStepPrefix(String parameter)
	{
		for (Step step : steps)
		{
			if (parameter.contains(step.getName() + Parameters.STEP_PARAM_SEP_SCRIPT)) return true;
		}
		return false;
	}

	public String toString()
	{
		String result = new Gson().toJson(this);
		return "workflow=" + result;
	}

	@Override
	public Set<String> getUserInputParams()
	{
		// user parameters that we want to put in environment
		HashSet<String> userInputParamSet = new LinkedHashSet<String>();

		// first collect all user parameters that are used in this workflow
		for (Step step : steps)
		{
			Map<String, String> parameterMap = step.getParametersMapping();
			for (String parameter : parameterMap.values())
			{
				arrayOfParameterSteps.add(new Pair<String, String>(parameter, step.getName()));

				if (!parameterHasStepPrefix(parameter))
				{
					userInputParamSet.add(parameter);
				}
			}

			List<String> autoMappedParameters = step.getAutoMappedParameters();

			for (String autoMappedParameter : autoMappedParameters)
			{
				arrayOfParameterSteps.add(new Pair<String, String>(autoMappedParameter, step.getName()));
			}

			userInputParamSet.addAll(autoMappedParameters);

		}

		return userInputParamSet;
	}

	@Override
	public String findMatchingOutput(String unknownGlobalParameterName, DataEntity dataEntity) throws Exception
	{
		for (Step step : steps)
		{
			Set<Output> outputs = step.getProtocol().getOutputs();
			for (Output output : outputs)
			{
				// first search for auto.mapping
				String outputName = output.getName();
				if (outputName.equalsIgnoreCase(unknownGlobalParameterName))
				{
					if (checkIfVariableCanbeKnown(step, unknownGlobalParameterName))
					{
						return step.getName() + "_" + unknownGlobalParameterName;
					}
				}

				// else search for step.mapping
				outputName = step.getName() + Parameters.STEP_PARAM_SEP_PROTOCOL + output.getName();
				if (outputName.equalsIgnoreCase(unknownGlobalParameterName))
				{
					if (checkIfVariableCanbeMapped(step.getName(), unknownGlobalParameterName))
					{
						return step.getName() + Parameters.STEP_PARAM_SEP_SCRIPT + unknownGlobalParameterName;
					}
				}
			}
		}

		List<String> relatedSteps = findRelatedSteps(unknownGlobalParameterName);
		throw new Exception("Parameter '" + unknownGlobalParameterName + "' used in steps " + relatedSteps.toString()
				+ " does not have a value in any of the parameter files ");
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
	
	
	private boolean checkIfVariableCanbeKnown(Step previousStep, String parameterName)
	{
		for (Step step : steps)
		{
			for (Input input : step.getProtocol().getInputs())
			{
				if (input.getName().equalsIgnoreCase(parameterName))
				{
					// this step has input named parameterName
					Set<String> previousSteps = step.getPreviousSteps();
					if (previousSteps.contains(previousStep.getName()))
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

		for (Step step : steps)
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
}
