package org.molgenis.compute.model;

import java.util.HashMap;
import java.util.Map;

import org.molgenis.compute.ComputeProperties;
import org.molgenis.compute.generators.impl.BatchAnalyser;
import org.molgenis.compute.model.impl.FoldParametersImpl;
import org.molgenis.compute.model.impl.WorkflowImpl;

public class Context
{
	private WorkflowImpl workflowImpl;
	private Parameters parameters;
	private Iterable<Task> tasks;
	private ComputeProperties computeProperties;
	private String userEnvironment;

	HashMap<String, String> mapUserEnvironment = null;
	private FoldParametersImpl foldParameters;

	private BatchAnalyser batchAnalyser = null;

	public Context(ComputeProperties computeProperties)
	{
		this.setComputeProperties(computeProperties);
	}

	public Iterable<Task> getTasks()
	{
		return tasks;
	}

	public void setTasks(Iterable<Task> tasks)
	{
		this.tasks = tasks;
	}

	public WorkflowImpl getWorkflow()
	{
		return workflowImpl;
	}

	public void setWorkflow(WorkflowImpl workflowImpl)
	{
		this.workflowImpl = workflowImpl;
	}

	public Parameters getParameters()
	{
		return parameters;
	}

	public void setParameters(Parameters parameters)
	{
		this.parameters = parameters;
	}

	public ComputeProperties getComputeProperties()
	{
		return computeProperties;
	}

	public void setComputeProperties(ComputeProperties computeProperties)
	{
		this.computeProperties = computeProperties;
	}

	public void setUserEnvironment(String environment)
	{
		this.userEnvironment = environment;
	}

	public String getUserEnvironment()
	{
		return this.userEnvironment;
	}

	public HashMap<String, String> getMapUserEnvironment()
	{
		return mapUserEnvironment;
	}

	public void setMapUserEnvironment(HashMap<String, String> mapUserEnvironment)
	{
		this.mapUserEnvironment = mapUserEnvironment;
	}

	public void setFoldParameters(FoldParametersImpl foldParameters)
	{
		this.foldParameters = foldParameters;
	}

	public FoldParametersImpl getFoldParameters()
	{
		return foldParameters;
	}

	public void createBatchAnalyser(String batchVariable, int batchNumber)
	{
		batchAnalyser = new BatchAnalyser(batchVariable, batchNumber);
	}

	public int getBatchNumber(Map<String, Object> map)
	{
		return batchAnalyser.getBatchNum(map);
	}

	public int getBatchesSize()
	{
		return batchAnalyser.getBatchesSize();
	}
}