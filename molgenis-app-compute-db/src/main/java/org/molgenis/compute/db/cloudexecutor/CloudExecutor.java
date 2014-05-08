package org.molgenis.compute.db.cloudexecutor;

import com.google.common.collect.Iterables;
import org.apache.log4j.Logger;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hvbyelas
 * Date: 5/8/14
 * Time: 9:49 AM
 * To change this template use File | Settings | File Templates.
 */

public class CloudExecutor
{
	@Autowired
	private DataService dataService;

	@Autowired
	private CloudManager cloudManager;


	private static final Logger LOG = Logger.getLogger(CloudThread.class);

	@RunAsSystem
	public void executeRun(ComputeRun run)
	{
		Iterable<ComputeTask> generatedTasks = dataService.findAll(ComputeTask.ENTITY_NAME, new QueryImpl()
				.eq(ComputeTask.COMPUTERUN, run).and()
				.eq(ComputeTask.STATUSCODE, "generated"), ComputeTask.class);

		int size = Iterables.size(generatedTasks);
		int i = 0;

		//evaluateTasks(generatedTasks);

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

}
