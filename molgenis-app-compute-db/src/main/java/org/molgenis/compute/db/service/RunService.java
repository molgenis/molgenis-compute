package org.molgenis.compute.db.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.molgenis.compute.db.ComputeDbException;
import org.molgenis.compute.db.executor.Scheduler;
import org.molgenis.compute.db.pilot.MolgenisPilotService;
import org.molgenis.compute.runtime.ComputeBackend;
import org.molgenis.compute.runtime.ComputeParameterValue;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.compute5.db.api.RunStatus;
import org.molgenis.compute5.model.Task;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * ComputeRun service facade
 *
 * @author erwin
 */
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS, value = "request")
@Component
public class RunService
{
	private static final Logger LOG = Logger.getLogger(RunService.class);
	private static final long DEFAULT_POLL_DELAY = 30000;

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private DataService dataService;

	public ComputeRun create(String name, String backendName, Long pollDelay,
							 List<Task> tasks, String userEnvironment, String userName)
	{
		Iterable<ComputeBackend> backends = dataService.findAll(ComputeBackend.ENTITY_NAME, new QueryImpl()
				.eq(ComputeBackend.NAME, backendName));
		if (!backends.iterator().hasNext())
		{
			throw new ComputeDbException("Unknown backend with name [" + backendName + "]");
		}

		ComputeBackend backend = backends.iterator().next();

		Iterable<MolgenisUser> users = dataService.findAll(MolgenisUser.ENTITY_NAME, new QueryImpl()
				.eq(MolgenisUser.USERNAME, userName));
		if (!users.iterator().hasNext())
		{
			throw new ComputeDbException("Unknown user with name [" + userName + "]");
		}

		MolgenisUser user = users.iterator().next();

		Iterable<ComputeBackend> runs = dataService.findAll(ComputeRun.ENTITY_NAME, new QueryImpl()
				.eq(ComputeRun.NAME, name));

		if (runs.iterator().hasNext())
		{
			throw new ComputeDbException("Run with name [" + name + "] already exists");
		}

		ComputeRun run = new ComputeRun();
		run.setComputeBackend(backend);
		run.setName(name);
		run.setPollDelay(pollDelay == null ? DEFAULT_POLL_DELAY : pollDelay);
		run.setUserEnvironment(userEnvironment);
		run.setOwner(user);
		dataService.add(ComputeRun.ENTITY_NAME, run);

		// Add tasks to db
		for (Task task : tasks)
		{
			ComputeTask computeTask = new ComputeTask();
			computeTask.setName(task.getName());
			computeTask.setComputeRun(run);
			computeTask.setInterpreter("bash");
			computeTask.setStatusCode(MolgenisPilotService.TASK_GENERATED);
			computeTask.setComputeScript(task.getScript());
			dataService.add(ComputeTask.ENTITY_NAME, computeTask);

			if (LOG.isDebugEnabled())
			{
				LOG.debug("Added task [" + task.getName() + "]");
			}
		}

		// Set prev tasks
		for (Task task : tasks)
		{
			ComputeRun computeRun = dataService.findOne(ComputeRun.ENTITY_NAME, new QueryImpl()
					.eq(ComputeRun.NAME, run.getName()));

			Iterable<ComputeTask> computeTasks = dataService.findAll(ComputeTask.ENTITY_NAME, new QueryImpl()
					.eq(ComputeTask.COMPUTERUN, computeRun).and()
					.eq(ComputeTask.NAME, task.getName()));

			ComputeTask computeTask = computeTasks.iterator().next();

			List<ComputeTask> prevTasks = new ArrayList<ComputeTask>();
			for (String prevTaskName : task.getPreviousTasks())
			{


				Iterable<ComputeTask> prevComputeTasks = dataService.findAll(ComputeTask.ENTITY_NAME, new QueryImpl()
						.eq(ComputeTask.COMPUTERUN, computeRun).and()
						.eq(ComputeTask.NAME, prevTaskName));


				if (!prevComputeTasks.iterator().hasNext())
				{
					throw new ComputeDbException("Previous task [" + prevTaskName + "]  not found");
				}

				ComputeTask prevTask = prevComputeTasks.iterator().next();

				prevTasks.add(prevTask);
			}

			if (!prevTasks.isEmpty())
			{
				computeTask.setPrevSteps(prevTasks);
			}
			dataService.update(ComputeTask.ENTITY_NAME, computeTask);

			if (LOG.isDebugEnabled())
			{
				LOG.debug("Set prevSteps for [" + task.getName() + "]");
			}
		}

		// Add parameters
		for (Task task : tasks)
		{

			Iterable<ComputeTask> computeTasks2 = dataService.findAll(ComputeTask.ENTITY_NAME, new QueryImpl()
					.eq(ComputeTask.COMPUTERUN, run).and()
					.eq(ComputeTask.NAME, task.getName()));

			ComputeTask computeTask = computeTasks2.iterator().next();


			for (Map.Entry<String, Object> param : task.getParameters().entrySet())
			{
				ComputeParameterValue computeParameterValue = new ComputeParameterValue();
				computeParameterValue.setComputeTask(computeTask);
				computeParameterValue.setName(param.getKey());
				if (param.getValue() != null)
				{
					computeParameterValue.setValue(param.getValue().toString());
				}

				dataService.add(ComputeParameterValue.ENTITY_NAME, computeParameterValue);

				if (LOG.isDebugEnabled())
				{
					LOG.debug("Added parameter [" + param.getKey() + "]");
				}
			}
		}

		LOG.info("New run [" + name + "] is created");

		return run;
	}

	/**
	 * Start pilots
	 *
	 * @param runName
	 * @param username
	 * @param password
	 */
	public void start(String runName, String username, String password)
	{
		ComputeRun run = dataService.findOne(ComputeRun.ENTITY_NAME, new QueryImpl()
				.eq(ComputeRun.NAME, runName));

		if (run == null)
		{
			throw new ComputeDbException("Unknown run name [" + runName + "]");
		}

		run.setIsSubmittingPilots(true);
		dataService.update(ComputeRun.ENTITY_NAME, run);
		scheduler.schedule(run.getName(), username, password);

	}

	/**
	 * Stop database polling
	 *
	 * @param runName
	 */
	public void stop(String runName)
	{
		ComputeRun run = dataService.findOne(ComputeRun.ENTITY_NAME, new QueryImpl()
				.eq(ComputeRun.NAME, runName));
		if (run == null)
		{
			throw new ComputeDbException("Unknown run name [" + runName + "]");
		}

		LOG.debug(">> In RunService:stop");
		scheduler.unschedule(run.getId());

		run.setIsSubmittingPilots(false);
		dataService.update(ComputeRun.ENTITY_NAME, run);
	}

	/**
	 * Activate run for execution
	 *
	 * @param runName
	 */
	public void activate(String runName)
	{

		ComputeRun run = dataService.findOne(ComputeRun.ENTITY_NAME, new QueryImpl()
				.eq(ComputeRun.NAME, runName));
		if (run == null)
		{
			throw new ComputeDbException("Unknown run name [" + runName + "]");
		}

		run.setIsActive(true);
		dataService.update(ComputeRun.ENTITY_NAME, run);

	}

	/**
	 * Inactivate run for execution
	 *
	 * @param runName
	 */
	public void inactivate(String runName)
	{

		ComputeRun run = dataService.findOne(ComputeRun.ENTITY_NAME, new QueryImpl()
				.eq(ComputeRun.NAME, runName));
		if (run == null)
		{
			throw new ComputeDbException("Unknown run name [" + runName + "]");
		}

//		scheduler.unschedule(run.getId());
		run.setIsActive(false);
		run.setIsSubmittingPilots(false);
		dataService.update(ComputeRun.ENTITY_NAME, run);

	}


	/**
	 * Check is a run is currently running
	 *
	 * @param runName
	 * @return
	 */
	public boolean isRunning(String runName)
	{
		ComputeRun run = dataService.findOne(ComputeRun.ENTITY_NAME, new QueryImpl()
				.eq(ComputeRun.NAME, runName));
		if (run == null)
		{
			throw new ComputeDbException("Unknown run name [" + runName + "]");
		}

		return run.getIsActive();
	}

	/**
	 * Check is a run is currently submitting pilots
	 *
	 * @param runName
	 * @return
	 */
	public boolean isSubmitting(String runName)
	{

		ComputeRun run = dataService.findOne(ComputeRun.ENTITY_NAME, new QueryImpl()
				.eq(ComputeRun.NAME, runName));
		if (run == null)
		{
			throw new ComputeDbException("Unknown run name [" + runName + "]");
		}

		return run.getIsSubmittingPilots();
	}

	/**
	 * Check is a run is complete
	 *
	 * @param runName
	 * @return
	 */
	public boolean isComplete(String runName)
	{
		ComputeRun run = dataService.findOne(ComputeRun.ENTITY_NAME, new QueryImpl()
				.eq(ComputeRun.NAME, runName));
		if (run == null)
		{
			throw new ComputeDbException("Unknown run name [" + runName + "]");
		}

		return run.getIsDone();
	}

	/**
	 * Check is a run is cancelled
	 *
	 * @param runName
	 * @return
	 */
	public boolean isCancelled(String runName)
	{
		ComputeRun run = dataService.findOne(ComputeRun.ENTITY_NAME, new QueryImpl()
				.eq(ComputeRun.NAME, runName));
		if (run == null)
		{
			throw new ComputeDbException("Unknown run name [" + runName + "]");
		}

		return run.getIsCancelled();
	}


	/**
	 * Get the status of all tasks of a run
	 *
	 * @param runName
	 * @return
	 */
	public RunStatus getStatus(String runName)
	{
		ComputeRun run = dataService.findOne(ComputeRun.ENTITY_NAME, new QueryImpl()
				.eq(ComputeRun.NAME, runName));
		if (run == null)
		{
			throw new ComputeDbException("Unknown run name [" + runName + "]");
		}

		int generated = getTaskStatusCount(run, MolgenisPilotService.TASK_GENERATED);
		int ready = getTaskStatusCount(run, MolgenisPilotService.TASK_READY);
		int running = getTaskStatusCount(run, MolgenisPilotService.TASK_RUNNING);
		int failed = getTaskStatusCount(run, MolgenisPilotService.TASK_FAILED);
		int done = getTaskStatusCount(run, MolgenisPilotService.TASK_DONE);
		int cancelled = getTaskStatusCount(run, MolgenisPilotService.TASK_CANCELLED);

		int submitted = run.getPilotsSubmitted();
		int started = run.getPilotsStarted();

		boolean status = false;

		if ((generated == 0) && (ready == 0) && (running == 0) && (failed == 0) && (cancelled == 0))
		{
			status = true;
			run.setIsDone(true);
			dataService.update(ComputeRun.ENTITY_NAME, run);
		}
		return new RunStatus(generated, ready, running, failed, done, cancelled, submitted, started, status);
	}

	/**
	 * Resubmit all failed tasks of a run
	 *
	 * @param runName
	 * @return the number of resubmitted failed tasks
	 */
	public int resubmitFailedTasks(String runName)
	{
		LOG.info("Resubmit failed tasks for run [" + runName + "]");

		ComputeRun run = dataService.findOne(ComputeRun.ENTITY_NAME, new QueryImpl()
				.eq(ComputeRun.NAME, runName));

		Iterable<ComputeTask> tasks = dataService.findAll(ComputeTask.ENTITY_NAME, new QueryImpl()
				.eq(ComputeTask.STATUSCODE, MolgenisPilotService.TASK_FAILED).and()
				.eq(ComputeTask.COMPUTERUN, run));

		if (!tasks.iterator().hasNext())
		{
			return 0;
		}

		int numberOfTasks = 0;
		for (ComputeTask task : tasks)
		{
			numberOfTasks++;
			// mark job as generated
			// entry to history is added by ComputeTaskDecorator
			task.setStatusCode("generated");
			task.setRunLog(null);
			task.setRunInfo(null);

			LOG.info("Task [" + task.getName() + "] changed from failed to generated");
		}

		dataService.update(ComputeTask.ENTITY_NAME, tasks);

		return numberOfTasks;
	}

	/**
	 * Remove a run from the dashboard (not from the database)
	 *
	 * @param runName
	 */
	public void removeFromDashboard(String runName)
	{
		ComputeRun run = dataService.findOne(ComputeRun.ENTITY_NAME, new QueryImpl()
				.eq(ComputeRun.NAME, runName));
		if (run == null)
		{
			throw new ComputeDbException("Unknown run name [" + runName + "]");
		}

		run.setShowInDashboard(false);
		dataService.update(ComputeRun.ENTITY_NAME, run);
	}

	private int getTaskStatusCount(ComputeRun run, String status)
	{
		Iterable<ComputeTask> computeTasks =
				dataService.findAll(ComputeTask.ENTITY_NAME, new QueryImpl().eq(ComputeTask.COMPUTERUN, run).and()
						.eq(ComputeTask.STATUSCODE, status));
		return ((Collection<?>) computeTasks).size();
	}

	/**
	 * Cancel a run: this action stops saving result files for the whole run; the files created until this action
	 * are not removed, but they will be overwritten in the next run; if it will take place
	 *
	 * @param runName
	 */
	public void cancel(String runName)
	{
		ComputeRun run = dataService.findOne(ComputeRun.ENTITY_NAME, new QueryImpl()
				.eq(ComputeRun.NAME, runName));
		if (run == null)
		{
			throw new ComputeDbException("Unknown run name [" + runName + "]");
		}

		run.setIsCancelled(true);
		dataService.update(ComputeRun.ENTITY_NAME, run);

		List<String> statuses = new ArrayList<String>();
		statuses.add(MolgenisPilotService.TASK_RUNNING);
		statuses.add(MolgenisPilotService.TASK_GENERATED);
		statuses.add(MolgenisPilotService.TASK_READY);

		Query q = new QueryImpl()
				.eq(ComputeTask.COMPUTERUN, run.getName())
				.and().in(ComputeTask.STATUSCODE, statuses);


		Iterable<ComputeTask> listTask = dataService
				.findAll(ComputeTask.ENTITY_NAME, q);

		for (ComputeTask task : listTask)
		{
			task.setStatusCode(MolgenisPilotService.TASK_CANCELLED);
			dataService.update(ComputeTask.ENTITY_NAME, task);
		}

	}
}
