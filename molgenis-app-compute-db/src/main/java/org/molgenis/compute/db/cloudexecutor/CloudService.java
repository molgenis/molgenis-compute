package org.molgenis.compute.db.cloudexecutor;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.molgenis.compute.db.pilot.MolgenisPilotService;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.compute.runtime.ComputeVM;
import org.molgenis.compute.runtime.Pilot;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hvbyelas
 * Date: 5/7/14
 * Time: 1:27 PM
 * To change this template use File | Settings | File Templates.
 */


@Controller
@RequestMapping("/api/cloud")
public class CloudService
{
	private static final Logger LOG = Logger.getLogger(CloudService.class);

	public static final String STATUS_STARTED = "started";
	public static final String STATUS_FINISHED = "finished";
	public static final String STATUS_FAILED = "failed";


	@Autowired
	private DataService dataService;

	@Autowired
	private CloudManager cloudManager;

	@RunAsSystem
	@RequestMapping(method = RequestMethod.POST, headers = "Content-Type=multipart/form-data")
	public synchronized void analyseCloudCall(HttpServletRequest request,
											  @RequestParam String jobid,
											  @RequestParam String status,
											  @RequestParam String serverid,
											  @RequestParam String backend,
											  @RequestParam(required = false) Part log_file,
											  HttpServletResponse response
	) throws IOException
	{
		LOG.info(">> Callback from server [ " + serverid + " ] with running job [ " + jobid + " ] " +
				"and status [" + status + "]");


		ComputeTask computeTask = dataService.findOne(ComputeTask.ENTITY_NAME, new QueryImpl()
				.eq(ComputeTask.ID, jobid), ComputeTask.class);

		ComputeVM computeVM = dataService.findOne(ComputeVM.ENTITY_NAME, new QueryImpl()
				.eq(ComputeVM.SERVERID, serverid), ComputeVM.class);


		if(computeTask == null)
		{
			LOG.warn("Compute Task with ID [" + jobid + "] does not exist in database");
			return;
		}

		if(computeVM == null)
		{
			LOG.warn("Compute VM with ID [" + serverid + "] does not exist in database");
			return;
		}

		if (status.equalsIgnoreCase(STATUS_STARTED))
		{
			LOG.info(">> Job [ " + jobid + " ] is started");
			if(computeTask.getStatusCode().equalsIgnoreCase(MolgenisPilotService.TASK_SUBMITTED))
			{

				computeTask.setStatusCode(MolgenisPilotService.TASK_RUNNING);
				dataService.update(ComputeTask.ENTITY_NAME, computeTask);

			}
			else
				LOG.warn("Compute Task [" + computeTask.getId() + " : " + computeTask.getName() + "] has a wrong status in started");

		}
		else if(status.equalsIgnoreCase(STATUS_FINISHED))
		{
			LOG.info(">> Job [ " + jobid + " ] is finished");
			releaseServer(serverid, jobid);

			if(computeTask.getStatusCode().equalsIgnoreCase(MolgenisPilotService.TASK_RUNNING))
			{
				computeTask.setStatusCode(MolgenisPilotService.TASK_DONE);

				String logFileContent = readLog(log_file);

				if(logFileContent != null)
				{
					computeTask.setRunLog(logFileContent);
					dataService.update(ComputeTask.ENTITY_NAME, computeTask);
				}

				updateRunHistory(computeTask, computeVM, status);
			}
		}
		else if(status.equalsIgnoreCase(STATUS_FAILED))
		{
			LOG.info(">> Job [ " + jobid + " ] is failed");
			releaseServer(serverid, jobid);

			if (computeTask.getStatusCode().equalsIgnoreCase(MolgenisPilotService.TASK_RUNNING) ||
					computeTask.getStatusCode().equalsIgnoreCase(MolgenisPilotService.TASK_SUBMITTED))
			{
				computeTask.setStatusCode(MolgenisPilotService.TASK_FAILED);

				String logFileContent = readLog(log_file);

				if (logFileContent != null)
				{
					computeTask.setFailedLog(logFileContent);
				}
				dataService.update(ComputeTask.ENTITY_NAME, computeTask);
				updateRunHistory(computeTask, computeVM, status);
			}
		}
		else
			LOG.warn("Compute Task [" + computeTask.getId() + " : " + computeTask.getName() + "] has a wrong status in finished");

	}

	private String readLog(Part log_file)
	{
		if(log_file != null)
		{
			try
			{
				InputStream inputStream = log_file.getInputStream();
				StringWriter writer = new StringWriter();
				IOUtils.copy(inputStream, writer, "UTF-8");
				return writer.toString();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}

	private void updateRunHistory(ComputeTask computeTask, ComputeVM computeVM, String status)
	{
		if(status.equalsIgnoreCase(STATUS_FINISHED))
		{
			List<ComputeTask> tasks = computeVM.getFinishedComputeTask();
			tasks.add(computeTask);
			computeVM.setFinishedComputeTask(tasks);
		}
		else if(status.equalsIgnoreCase(STATUS_FAILED))
		{
			List<ComputeTask> tasks = computeVM.getFailedComputeTask();
			tasks.add(computeTask);
			computeVM.setFailedComputeTask(tasks);
		}
		dataService.update(ComputeVM.ENTITY_NAME, computeVM);

	}

	private void releaseServer(String serverid, String jobid)
	{
		for(CloudServer server : cloudManager.getCloudServers())
		{
			if(server.getId().equalsIgnoreCase(serverid))
			{
				server.setInUse(false);
				server.addFinishedJob(jobid);
			}
		}
	}

}
