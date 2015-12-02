package org.molgenis.compute;

import java.util.Set;

/**
 * Created by hvbyelas on 12/15/14.
 */
public class GeneratedScript
{
	private String name;
	private String stepName;
	private String script;
	private Set<String> previousTasks;

	public Set<String> getPreviousTasks()
	{
		return previousTasks;
	}

	public void setPreviousTasks(Set<String> previousTasks)
	{
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

	public String getStepName()
	{
		return stepName;
	}

	public void setStepName(String stepName)
	{
		this.stepName = stepName;
	}

	public String getScript()
	{
		return script;
	}

	public void setScript(String script)
	{
		this.script = script;
	}
}
