package org.molgenis.compute.db.cloudexecutor;

import org.apache.log4j.Logger;
import org.molgenis.compute.runtime.ComputeBackend;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hvbyelas
 * Date: 5/7/14
 * Time: 4:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class CloudExecutor implements Runnable
{
	private static final Logger LOG = Logger.getLogger(CloudExecutor.class);

	@Autowired
	private DataService dataService;

	@Autowired
	private CloudManager cloudManager;

	private ComputeRun run = null;


	public CloudExecutor(ComputeRun run)
	{
		this.run = run;
	}

	private void evaluateTasks(Iterable<ComputeTask> generatedTasks)
	{
		for(ComputeTask task : generatedTasks)
		{
			boolean isReady = true;
			List<ComputeTask> prevSteps = task.getPrevSteps();
			for (ComputeTask prev : prevSteps)
			{
				if (!prev.getStatusCode().equalsIgnoreCase("done")) isReady = false;
			}

			if (isReady)
			{
				LOG.info(">>> TASK [" + task.getName() + "] is ready for execution");

				task.setStatusCode("ready");
				dataService.update(ComputeTask.ENTITY_NAME, task);
			}
		}

	}

	@Override
	public void run()
	{
		Iterable<ComputeTask> generatedTasks = dataService.findAll(ComputeTask.ENTITY_NAME, new QueryImpl()
				.eq(ComputeTask.COMPUTERUN, run).and()
				.eq(ComputeTask.STATUSCODE, "generated"), ComputeTask.class);

		//evaluateTasks(generatedTasks);

	}
}
