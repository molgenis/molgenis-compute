package org.molgenis.compute.db.clusterexecutor;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.molgenis.compute.db.cloudexecutor.CloudManager;
import org.molgenis.compute.db.cloudexecutor.CloudServer;
import org.molgenis.compute.db.cloudexecutor.CloudService;
import org.molgenis.compute.db.pilot.MolgenisPilotService;
import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.compute.runtime.ComputeVM;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created with IntelliJ IDEA.
 * User: hvbyelas
 * Date: 5/7/14
 * Time: 1:27 PM
 * To change this template use File | Settings | File Templates.
 */


@Controller
@RequestMapping("/api/cluster")
public class ClusterService
{
	private static final Logger LOG = Logger.getLogger(ClusterService.class);


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
											  @RequestParam(required = false) Part out_log_file,
											  @RequestParam(required = false) Part err_log_file,
											  HttpServletResponse response
	) throws IOException
	{
		LOG.info(">> Callback from server [ " + serverid + " ] with running job [ " + jobid + " ] " +
				"and status [" + status + "]");


		ComputeTask computeTask = dataService.findOne(ComputeTask.ENTITY_NAME, new QueryImpl()
				.eq(ComputeTask.ID, jobid), ComputeTask.class);


		if(computeTask == null)
		{
			LOG.warn("Compute Task with ID [" + jobid + "] does not exist in database");
			return;
		}

		if (status.equalsIgnoreCase(CloudService.STATUS_STARTED))
		{
			LOG.info(">> Job [ " + jobid + " ] is started");

			//here, task can call back, when DB is not updated yet
			if(computeTask.getStatusCode().equalsIgnoreCase(MolgenisPilotService.TASK_SUBMITTED) ||
					computeTask.getStatusCode().equalsIgnoreCase(MolgenisPilotService.TASK_GENERATED))
			{

				computeTask.setStatusCode(MolgenisPilotService.TASK_RUNNING);
				dataService.update(ComputeTask.ENTITY_NAME, computeTask);

			}
			else
				LOG.warn("Compute Task [" + computeTask.getId() + " : " + computeTask.getName() + "] has a wrong status in started");

		}
		else if(status.equalsIgnoreCase(CloudService.STATUS_FINISHED))
		{
			LOG.info(">> Job [ " + jobid + " ] is finished");

			if(computeTask.getStatusCode().equalsIgnoreCase(MolgenisPilotService.TASK_RUNNING))
			{
				computeTask.setStatusCode(MolgenisPilotService.TASK_DONE);

				String logFileContent = readLog(out_log_file);
				String errFileContent = readLog(err_log_file);

				if(logFileContent != null)
				{
					computeTask.setRunLog(logFileContent);
				}

				if(errFileContent != null)
				{
					computeTask.setFailedLog(errFileContent);
				}
				dataService.update(ComputeTask.ENTITY_NAME, computeTask);

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

}
