package org.molgenis.compute.db.executor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;
import org.molgenis.compute.db.ComputeDbException;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.data.DataService;
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
	private DataService dataService = null;
	private static final Logger LOG = Logger.getLogger(Scheduler.class);

	@Autowired
	public Scheduler(DataService dataService, TaskScheduler taskScheduler, ComputeExecutor computeExecutor)
	{
		this.taskScheduler = taskScheduler;
		this.dataService = dataService;
		this.computeExecutor = computeExecutor;
	}

	public synchronized void schedule(ComputeRun run, String username, String password)
	{
		if (scheduledJobs.containsKey(run.getId()))
		{
			throw new ComputeDbException("Run " + run.getName() + " already running");
		}

		ExecutionHost executionHost = null;
		try
		{
			executionHost = new ExecutionHost(dataService, run.getComputeBackend().getBackendUrl(), username,
					password, ComputeExecutorPilotDB.SSH_PORT);
		}
		catch (IOException e)
		{
			throw new ComputeDbException(e);
		}

//
//		ComputeExecutor executor = new ComputeExecutorPilotDB(dataService, run.getComputeBackend().getBackendUrl(), username,
//				password, ComputeExecutorPilotDB.SSH_PORT);

		ComputeJob job = new ComputeJob(computeExecutor, run, username, password);

		ScheduledFuture<?> future = taskScheduler.scheduleWithFixedDelay(job, run.getPollDelay());

		System.out.println(SecurityUtils.getCurrentUsername());
		System.out.println(SecurityUtils.currentUserIsSu());

		scheduledJobs.put(run.getId(), future);
	}

	public synchronized boolean isRunning(Integer computeRunId)
	{
		return scheduledJobs.containsKey(computeRunId);
	}

	public synchronized void unschedule(Integer computeRunId)
	{
		System.out.println(SecurityUtils.getCurrentUsername());
		System.out.println(SecurityUtils.currentUserIsSu());
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
