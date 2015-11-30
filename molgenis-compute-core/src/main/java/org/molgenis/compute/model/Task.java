package org.molgenis.compute.model;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;

/**
 * Generated tasks from steps, with the inputs/outpus prefilled. Includes data dependency graph via previousTasks.
 */
public class Task
{
	public static final String TASKID_INDEX_COLUMN = "taskIdIndex";
	public static String TASKID_COLUMN = "taskId";

	private String name;

	// reference to previousTasks (i.e. outputs from previous tasks this task depends on)
	private Set<String> previousTasks = new HashSet<String>();

	// copy of the local input/outputs used
	private Map<String, Object> parameters = new LinkedHashMap<String, Object>();

	// the body of the script (backend independent)
	private String script;

	// working directory (i.e. the directory on the shared storage for this
	// workflow run)
	private String workdir;
	private String stepName;

	private int batchNumber = -1;

	public Task(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Set<String> getPreviousTasks()
	{
		return previousTasks;
	}

	public void setPreviousTasks(Set<String> previousTasks)
	{
		this.previousTasks = previousTasks;
	}

	public String toString()
	{
		return new Gson().toJson(this);
	}

	public String getScript()
	{
		return script;
	}

	public void setScript(String script)
	{
		this.script = script;
	}

	public Map<String, Object> getParameters()
	{
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters)
	{
		this.parameters = parameters;
	}

	public String getWorkdir()
	{
		return workdir;
	}

	public void setWorkdir(String workdir)
	{
		this.workdir = workdir;
	}

	public void setStepName(String stepName)
	{
		this.stepName = stepName;
	}

	public String getStepName()
	{
		return stepName;
	}

	public int getBatchNumber()
	{
		return batchNumber;
	}

	public void setBatchNumber(int batchNumber)
	{
		this.batchNumber = batchNumber;
	}
}
