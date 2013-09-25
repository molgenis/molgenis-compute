package org.molgenis.compute.db.executor;

import org.molgenis.compute.runtime.ComputeRun;

public class ComputeJob implements Runnable
{
	private final ComputeExecutor executor;
	private final ComputeRun computeRun;


	public ComputeJob(ComputeExecutor executor, ComputeRun computeRun)
	{
		this.executor = executor;
		this.computeRun = computeRun;
	}

	@Override
	public void run()
	{
		computeRun.setIsActive(true);
		computeRun.setIsSubmittingPilots(true);
		executor.executeTasks(computeRun);
	}
}
