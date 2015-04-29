package org.molgenis.compute5.model.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.molgenis.compute5.model.Parameters;
import org.molgenis.compute5.model.Step;
import org.molgenis.compute5.model.Workflow;

import com.google.gson.Gson;

public class WorkflowImpl implements Workflow
{
	private List<Step> steps = new ArrayList<Step>();

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

}
