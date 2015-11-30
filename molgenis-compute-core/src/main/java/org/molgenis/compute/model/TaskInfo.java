package org.molgenis.compute.model;

import java.util.HashSet;
import java.util.Set;

public class TaskInfo
{
	// reference to previousTasks (i.e. outputs from previous tasks this task depends on)
	private Set<String> previousTasks = new HashSet<String>();
	private String name;

	public TaskInfo(String name, Set<String> previousTasks)
	{
		this.name = name;
		this.previousTasks = previousTasks;
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
}
