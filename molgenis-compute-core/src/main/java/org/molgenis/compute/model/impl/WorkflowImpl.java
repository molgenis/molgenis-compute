package org.molgenis.compute.model.impl;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.molgenis.compute.model.Parameters;
import org.molgenis.compute.model.Step;
import org.molgenis.compute.model.Workflow;

import com.google.gson.Gson;

public class WorkflowImpl implements Workflow
{
	private List<Step> steps = new LinkedList<Step>();

	@Override
	public Set<String> getUserParameters()
	{
		Set<String> result = new LinkedHashSet<String>();
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
