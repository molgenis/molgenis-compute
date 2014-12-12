package org.molgenis.compute5;

import java.util.Hashtable;

/**
 * Created by hvbyelas on 12/12/14.
 */
public class CommandLineRunContainer
{
	private String sumbitScript;
	private Hashtable<String, String> tasks = new Hashtable<String, String>();

	public String getSumbitScript()
	{
		return sumbitScript;
	}

	public void setSumbitScript(String sumbitScript)
	{
		this.sumbitScript = sumbitScript;
	}

	public Hashtable<String, String> getTasks()
	{
		return tasks;
	}

	public void addTask(String name, String script)
	{
		tasks.put(name, script);
	}
}
