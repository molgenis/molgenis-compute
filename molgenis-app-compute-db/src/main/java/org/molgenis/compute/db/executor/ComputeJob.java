package org.molgenis.compute.db.executor;

import org.molgenis.compute.runtime.ComputeRun;

public class ComputeJob implements Runnable
{
	private final ComputeExecutor executor;
	private final String computeRun;
	private String username;
	private String password;

	public ComputeJob(ComputeExecutor executor, String computeRun, String username, String password)
	{
		this.executor = executor;
		this.computeRun = computeRun;
		this.username = username;
		this.password = password;
	}

	@Override
	public void run()
	{
		try
		{
//			computeRun.setIsActive(true);
//			computeRun.setIsSubmittingPilots(true);
			executor.executeTasks(computeRun, username, password);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
