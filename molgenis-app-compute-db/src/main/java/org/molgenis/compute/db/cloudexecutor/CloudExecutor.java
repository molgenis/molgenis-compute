package org.molgenis.compute.db.cloudexecutor;

import org.apache.log4j.Logger;
import org.molgenis.compute.db.pilot.MolgenisPilotService;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;

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

	@Autowired
	private CloudCurlBuilder builder;


	private static final Logger LOG = Logger.getLogger(CloudThread.class);

	@RunAsSystem
	public void executeRun(ComputeRun run)
	{
		Iterable<ComputeTask> generatedTasks = dataService.findAll(ComputeTask.ENTITY_NAME, new QueryImpl()
				.eq(ComputeTask.COMPUTERUN, run).and()
				.eq(ComputeTask.STATUSCODE, MolgenisPilotService.TASK_GENERATED), ComputeTask.class);

		evaluateTasks(generatedTasks);

		Iterable<ComputeTask> readyTasks = dataService.findAll(ComputeTask.ENTITY_NAME, new QueryImpl()
				.eq(ComputeTask.COMPUTERUN, run).and()
				.eq(ComputeTask.STATUSCODE, MolgenisPilotService.TASK_READY), ComputeTask.class);

		for(ComputeTask computeTask : readyTasks)
		{
			CloudServer server = cloudManager.getAvailServer();
			if(server != null)
			{
				LOG.info("Server [" + server.getId() + "] is available");
				server.setCurrentJobID(computeTask.getId());
				executeTaskOnServer(computeTask, server);

			}
			else
			{
				LOG.info("There are no available servers to execute tasks in the moment.");
				LOG.info("All ["+ cloudManager.getTotalNumberServers() + "] are busy.");
			}
		}

	}

	private void executeTaskOnServer(ComputeTask computeTask, CloudServer server)
	{
		//here ssh script submission
		String script = builder.buildScript(computeTask, server);
		System.out.println("-----------------------------------");
		System.out.println(script);
		System.out.println("-----------------------------------");
		boolean isSuccess = false;

		script = script.replaceAll("\r", "");

		while(!isSuccess)
		{
			//String IP, String SSHPASS,
			//String serverUsername,
			//String script, String jobID
			isSuccess = RemoteExecutor.transferScriptAndRun(server.getFloatingIpExtern(), cloudManager.getSshPass(),
															cloudManager.getServerUsername(),
															script, computeTask.getName() + "_" + computeTask.getId());
		}
		computeTask.setStatusCode(MolgenisPilotService.TASK_SUBMITTED);
		dataService.update(ComputeTask.ENTITY_NAME, computeTask);
		//here move result + clear server
		server.setInUse(true);


	}

	private void evaluateTasks(Iterable<ComputeTask> generatedTasks)
	{
		for(ComputeTask task : generatedTasks)
		{
			boolean isReady = true;
			List<ComputeTask> prevSteps = task.getPrevSteps();
			for (ComputeTask prev : prevSteps)
			{
				if (!prev.getStatusCode().equalsIgnoreCase(MolgenisPilotService.TASK_DONE)) isReady = false;
			}

			if (isReady)
			{
				LOG.info(">>> TASK [" + task.getName() + "] is ready for execution");

				task.setStatusCode(MolgenisPilotService.TASK_READY);
				dataService.update(ComputeTask.ENTITY_NAME, task);
				CrudRepository repo = dataService.getCrudRepository(ComputeTask.ENTITY_NAME);
				repo.flush();
			}
		}

	}

}
