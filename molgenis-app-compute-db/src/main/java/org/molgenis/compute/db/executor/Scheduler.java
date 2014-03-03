package org.molgenis.compute.db.executor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;
import org.molgenis.compute.db.ComputeDbException;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.SecurityUtils;
import org.molgenis.security.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;

/**
 * Schedule and unschedule pilot jobs
 *
 * @author erwin
 */
public class Scheduler
{
	private final TaskScheduler taskScheduler;
	private final Map<Integer, ScheduledFuture<?>> scheduledJobs = new HashMap<Integer, ScheduledFuture<?>>();
	private final ComputeExecutor computeExecutor;
	private final DataService dataService;
	private static final Logger LOG = Logger.getLogger(Scheduler.class);

	@Autowired
	public Scheduler(DataService dataService, TaskScheduler taskScheduler, ComputeExecutor computeExecutor)
	{
		this.taskScheduler = taskScheduler;
		this.dataService = dataService;
		this.computeExecutor = computeExecutor;
	}

	public synchronized void schedule(String runName, String username, String password)
	{
		ComputeRun computeRun =  dataService.findOne(ComputeRun.ENTITY_NAME, new QueryImpl()
				.eq(ComputeRun.NAME, runName));

		if (scheduledJobs.containsKey(computeRun.getId()))
		{
			throw new ComputeDbException("Run " + computeRun.getName() + " already running");
		}

		ExecutionHost executionHost = null;
		try
		{
			executionHost = new ExecutionHost(dataService, computeRun.getComputeBackend().getBackendUrl(), username,
					password, ComputeExecutorPilotDB.SSH_PORT);
		}
		catch (IOException e)
		{
			throw new ComputeDbException(e);
		}

		ComputeJob job = new ComputeJob(computeExecutor, computeRun.getName(), username, password);

		ScheduledFuture<?> future = taskScheduler.scheduleWithFixedDelay(job, computeRun.getPollDelay());

		//to try
		computeRun.setIsActive(true);
		computeRun.setIsSubmittingPilots(true);

		scheduledJobs.put(computeRun.getId(), future);
	}

	public synchronized boolean isRunning(Integer computeRunId)
	{
		return scheduledJobs.containsKey(computeRunId);
	}

	public synchronized void unschedule(Integer computeRunId)
	{
		LOG.debug(">> In scheduler:unschedule");

		if (!isRunning(computeRunId))
		{
			throw new ComputeDbException("Not running");
		}

		ScheduledFuture<?> future = scheduledJobs.get(computeRunId);
		future.cancel(false);
		scheduledJobs.remove(computeRunId);
	}
}
