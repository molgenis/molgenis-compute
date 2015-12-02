package org.molgenis.compute;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hvbyelas on 12/12/14.
 */
public class CommandLineRunContainer
{
	private String submitScript;
	private List<GeneratedScript> tasks = new ArrayList<GeneratedScript>();

	public String getSubmitScript()
	{
		return submitScript;
	}

	public void setSubmitScript(String submitScript)
	{
		this.submitScript = submitScript;
	}

	public List<GeneratedScript> getTaskScripts()
	{
		return tasks;
	}

	public void addTaskScript(GeneratedScript taskScript)
	{
		tasks.add(taskScript);
	}
}
