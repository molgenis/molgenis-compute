package org.molgenis.compute.db.api;

import java.util.Collections;
import java.util.List;

import org.molgenis.compute.model.Task;

public class CreateRunRequest
{
	private final String runName;
	private final String backendUrl;
	private final Long pollDelay;
	private final List<Task> tasks;
	private final String environment;
	private final String userName;
	private final String submitScript;

	public CreateRunRequest(String runName, String backendUrl, Long pollDelay, List<Task> tasks, String environment,
							String userName, String submitScript)
	{
		this.runName = runName;
		this.backendUrl = backendUrl;
		this.pollDelay = pollDelay;
		this.tasks = tasks;
		this.environment = environment;
		this.userName = userName;
		this.submitScript = submitScript;
	}

	public String getRunName()
	{
		return runName;
	}

	public String getBackendUrl()
	{
		return backendUrl;
	}

	public Long getPollDelay()
	{
		return pollDelay;
	}

	public List<Task> getTasks()
	{
		return Collections.unmodifiableList(tasks);
	}

	public String getEnvironment()
	{
		return environment;
	}

    public String getUserName()
    {
        return userName;
    }

	public String getSubmitScript()
	{
		return submitScript;
	}
}
