package org.molgenis.compute;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hvbyelas on 12/12/14.
 */
public class CommandLineRunContainer
{
	private String sumbitScript;
	private List<GeneratedScript> tasks = new ArrayList<GeneratedScript>();

	public String getSumbitScript()
	{
		return sumbitScript;
	}

	public void setSumbitScript(String sumbitScript)
	{
		this.sumbitScript = sumbitScript;
	}

	public List<GeneratedScript> getTasks()
	{
		return tasks;
	}

	public void addTask(GeneratedScript task)
	{
		tasks.add(task);
	}
}
