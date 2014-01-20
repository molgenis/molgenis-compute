package org.molgenis.compute.db.pilot;

import com.google.common.io.CharStreams;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.molgenis.compute.runtime.ComputeBackend;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.compute.runtime.Pilot;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.security.runas.RunAsSystem;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.ApplicationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import javax.servlet.http.Part;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA. User: georgebyelas Date:
 * /usr/local/mysql/bin/mysql -u molgenis -pmolgenis compute < $HOME/compute.sql
 * 20/07/2012 Time: 16:53 To change this template use File | Settings | File
 * Templates.
 */
@Controller
@RequestMapping("/api/pilot")
public class MolgenisPilotService
{

	private static final Logger LOG = Logger.getLogger(MolgenisPilotService.class);

	public static final String TASK_GENERATED = "generated";
	public static final String TASK_READY = "ready";
	public static final String TASK_RUNNING = "running";
	public static final String TASK_FAILED = "failed";
	public static final String TASK_DONE = "done";
	public static final String TASK_CANCELLED = "cancelled";


    public static final String PILOT_ID = "pilotid";

    public static final String PILOT_SUBMITTED = "submitted";
    public static final String PILOT_USED = "used";
    public static final String PILOT_EXPIRED = "expired";
    public static final String PILOT_FAILED = "failed";
    public static final String PILOT_DONE = "done";

	public static final String LOG_DIR = "log";
	private static final String ERR_EXTENSION = ".err";
	private static final String LOG_EXTENSION = ".log";

	private static final String IS_CANCELLED = "cancelled";
	private static final String IS_NOT_CANCELLED = "not_cancelled";

	@Autowired
	private DataService dataService;

	public MolgenisPilotService()
	{
		createLogDir();
	}

	private void createLogDir()
	{
		File theDir = new File(LOG_DIR);

		if (!theDir.exists())
		{
			boolean result = theDir.mkdir();
			if (result)
				LOG.info("LOG DIR created");
			else
				LOG.info("CANNOT create LOG DIR");
		}
	}

	@RunAsSystem
	@RequestMapping(method = RequestMethod.POST, headers = "Content-Type=multipart/form-data")
	public synchronized void analysePilotCall(HttpServletRequest request,
											  @RequestParam String pilotid,
											  @RequestParam(required = false) String host,
											  @RequestParam String status,
											  @RequestParam(required = false) String backend,
											  @RequestParam(required = false) Part log_file,
											  @RequestParam(required = false) Part output_file,
											  HttpServletResponse response
											 ) throws IOException
	{

		LOG.debug(">> In handleRequest!");


		if (status.equalsIgnoreCase("started"))
		{
			LOG.info("Checking pilot ID in start");
			String pilotID = pilotid;

			Iterable<Pilot> pilots = dataService.findAll(Pilot.ENTITY_NAME, new QueryImpl()
					.eq(Pilot.VALUE, pilotID)
					.and().eq(Pilot.STATUS, PILOT_SUBMITTED));

			System.out.println(">>>>>>>>>>>>>>>>>>>> " + pilotID + " @ " + host + " @ " + backend);

			Pilot pilot = null;
			if(pilots.iterator().hasNext())
			{
				LOG.info("Pilot value is correct [" + pilotID + "] in start");
				pilot = pilots.iterator().next();
				pilot.setStatus(PILOT_USED);
				dataService.update(Pilot.ENTITY_NAME, pilot);
			}
			else
			{
				LOG.warn("MALICIOUS PILOT [ " + pilotID + " ] in start");
				return;
			}

			Iterable<ComputeRun> computeRuns = dataService.findAll(ComputeRun.ENTITY_NAME, new QueryImpl()
					.eq(ComputeBackend.NAME, pilot.getComputeRun().getName()));

			if(computeRuns.iterator().hasNext())
            {
                ComputeRun computeRun = computeRuns.iterator().next();
                int pilotsStarted = computeRun.getPilotsStarted();
                computeRun.setPilotsStarted(pilotsStarted + 1);
                dataService.update(ComputeRun.ENTITY_NAME, computeRun);
            }
            else
            {
                LOG.error("No ComputeRun found for [" + pilot.getComputeRun().getName() + "]");
            }

            LOG.info("Looking for task to execute for host [" + backend + "]");


			//first we look for tasks, that from the run of the pilot
			Iterable<ComputeTask> tasks = findRunTasksReadyForRun(pilot);

			if((!tasks.iterator().hasNext()))
			{
				LOG.info("No tasks to start for compute run [" + pilot.getComputeRun().getName() + "]");
				tasks = findRunTasksReadyForBackend(backend);
			}

			if (!tasks.iterator().hasNext())
			{
				LOG.info("No tasks to start for host [" + backend + "]");
			}
			else
			{
				ComputeTask task = tasks.iterator().next();

				// we add task id to the run listing to identify task when
				// it is done
				ScriptBuilder sb = ApplicationContextProvider.getApplicationContext().getBean(ScriptBuilder.class);
				String serverAddress = request.getScheme() +
								"://" + request.getServerName() +
								":" + request.getServerPort();
				String serverPath = request.getServletPath();
				String taskScript = sb.build(task, serverAddress, serverPath, pilotID);

				LOG.info("Script for task [" + task.getName() + "] of run [ " + task.getComputeRun().getName() + "]:\n"
						+ taskScript);

				// change status to running
				task.setStatusCode(MolgenisPilotService.TASK_RUNNING);
				dataService.update(ComputeTask.ENTITY_NAME, task);

				pilot.setComputeTask(task);
				dataService.update(Pilot.ENTITY_NAME, pilot);

				// send response
				PrintWriter pw = response.getWriter();
				try
				{
					pw.write(taskScript);
					pw.flush();
				}
				finally
				{
					IOUtils.closeQuietly(pw);
				}
			}
		}
		else if(status.equalsIgnoreCase("is_cancel"))
		{
			String pilotID = pilotid;
			LOG.info("Checking if pilot " + pilotID + " is cancelled");

			Iterable<Pilot> pilots = dataService.findAll(Pilot.ENTITY_NAME, new QueryImpl()
					.eq(Pilot.VALUE, pilotID)
					.and().eq(Pilot.STATUS, PILOT_USED));

			String runCancelStatus = IS_NOT_CANCELLED;
			if(pilots.iterator().hasNext())
			{
				LOG.info("Pilot value is correct");
				Pilot pilot = pilots.iterator().next();
				ComputeRun run = pilot.getComputeRun();
				boolean isCancelled = run.getIsCancelled();
				if(isCancelled)
					runCancelStatus = IS_CANCELLED;
			}
			else
			{
				LOG.warn("MALICIOUS PILOT [ " + pilotID + " ] in cancellation check");
				return;
			}

			PrintWriter pw = response.getWriter();
			try
			{
				pw.write(runCancelStatus);
				pw.flush();
			}
			finally
			{
				IOUtils.closeQuietly(pw);
			}
		}
		else
		{

			LOG.info("Checking pilot ID in report");
			String pilotID = pilotid;

			Iterable<Pilot> pilots = dataService.findAll(Pilot.ENTITY_NAME, new QueryImpl()
					.eq(Pilot.VALUE, pilotID)
					.and().eq(Pilot.STATUS, PILOT_DONE));

			if((pilots.iterator().hasNext()) && status.equalsIgnoreCase("nopulse"))
			{
				LOG.info("Job is already reported back");
			}
			else
			{

				pilots = dataService.findAll(Pilot.ENTITY_NAME, new QueryImpl()
						.eq(Pilot.VALUE, pilotID)
						.and().eq(Pilot.STATUS, PILOT_USED));

				if(pilots.iterator().hasNext())
				{
					LOG.info("Pilot value is correct in report");
				}
				else
				{
					LOG.warn("MALICIOUS PILOT [ " + pilotID + " ] in report");
					return;
				}
			}

			Pilot pilot_withTask = null;
			pilots = dataService.findAll(Pilot.ENTITY_NAME, new QueryImpl()
					.eq(Pilot.VALUE, pilotID));
			if(pilots.iterator().hasNext())
			{
				pilot_withTask = pilots.iterator().next();
				ComputeTask task_from_pilot = pilot_withTask.getComputeTask();
				if(task_from_pilot == null)
				{
					//the reporting pilot is empty
					return;
				}
			}

			InputStream inputStream = log_file.getInputStream();

			StringWriter writer = new StringWriter();
			IOUtils.copy(inputStream, writer, "UTF-8");
			String logFileContent = writer.toString();
			LogFileParser logfile = new LogFileParser(logFileContent);
			String taskName = logfile.getTaskName();
			String runName = logfile.getRunName();
			List<String> logBlocks = logfile.getLogBlocks();
			String runInfo = StringUtils.join(logBlocks, "\n");

			ComputeRun computeRun = dataService.findOne(ComputeRun.ENTITY_NAME, new QueryImpl()
					.eq(ComputeRun.NAME, runName));

			Iterable<ComputeTask> tasks = dataService.findAll(ComputeTask.ENTITY_NAME, new QueryImpl()
					.eq(ComputeTask.NAME, taskName)
					.and().eq(ComputeTask.COMPUTERUN, computeRun));

			if (!tasks.iterator().hasNext())
			{
				LOG.warn("No task found for TASKNAME [" + taskName + "] of RUN [" + runName + "]");
				return;
			}

			ComputeTask task = tasks.iterator().next();

			if (status.equalsIgnoreCase("done"))
			{

				Iterable<Pilot> pilotList = dataService.findAll(Pilot.ENTITY_NAME, new QueryImpl()
						.eq(Pilot.COMPUTETASK, task));
				Pilot pilot = null;
				if(pilotList.iterator().hasNext())
				{
					pilot = pilots.iterator().next();
					pilot.setStatus(PILOT_DONE);
					dataService.update(Pilot.ENTITY_NAME, pilot);
				}
				else
				{
					LOG.warn("There is no pilot, which got TASK [" + task.getName() + "] of RUN [" + task.getComputeRun().getName() + "]");
				}


				LOG.info(">>> task [" + taskName + "] of run [" + runName + "] is finished");
				if (task.getStatusCode().equalsIgnoreCase(TASK_RUNNING) ||
						task.getStatusCode().equalsIgnoreCase(TASK_CANCELLED))
				{
					if (task.getStatusCode().equalsIgnoreCase(TASK_RUNNING))
						task.setStatusCode(TASK_DONE);
					task.setRunLog(logFileContent);
					task.setRunInfo(runInfo);

					String logFile = LOG_DIR + "/" + taskName + LOG_EXTENSION;
					writeToFile(logFile, logFileContent);

					if(output_file != null)
					{
						inputStream = log_file.getInputStream();
						writer = new StringWriter();
						IOUtils.copy(inputStream, writer, "UTF-8");
						String output = writer.toString();

						task.setOutputEnvironment(output);
					}
				}
				else
				{
					LOG.warn("from done: something is wrong with task [" + taskName + "] of run [" + runName
							+ "] status should be [running] but is [" + task.getStatusCode() + "]");
				}
			}
			else if (status.equalsIgnoreCase("pulse"))
			{
				if (task.getStatusCode().equalsIgnoreCase(TASK_RUNNING))
				{
					LOG.info(">>> pulse from task [" + taskName + "] of run [" + runName + "]");
					task.setRunLog(logFileContent);
					task.setRunInfo(runInfo);
				}
			}
			else if (status.equalsIgnoreCase("nopulse"))
			{
				//TODO sometimes there is no pulse received, but later job reports back itself
				//todo improve pulse-task management

				if (task.getStatusCode().equalsIgnoreCase(TASK_RUNNING))
				{
					Pilot pilot = pilots.iterator().next();
					pilot.setStatus(PILOT_FAILED);
					dataService.update(Pilot.ENTITY_NAME, pilot);

					LOG.info(">>> no pulse from task [" + taskName + "] of run [" + runName + "]");
					task.setRunLog(logFileContent);
					task.setRunInfo(runInfo);
					task.setStatusCode("failed");

					inputStream = log_file.getInputStream();

					writer = new StringWriter();
					IOUtils.copy(inputStream, writer, "UTF-8");
					logFileContent = writer.toString();
					task.setFailedLog(logFileContent);

					String errFile = LOG_DIR + "/" + taskName + ERR_EXTENSION;
					writeToFile(errFile, logFileContent);

				}
				else if (task != null && task.getStatusCode().equalsIgnoreCase(TASK_DONE))
				{
					LOG.info("double check: job is finished & no pulse from it for task [" + taskName + "] of run ["
							+ runName + "]");
				}

			}

			dataService.update(ComputeTask.ENTITY_NAME, task);
		}

	}

	private void writeToFile(String filename, String text)
	{
		File file = new File(filename);
		if (!file.exists())
		{
			try
			{
				file.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		try
		{
			FileOutputStream out = new FileOutputStream(file, false);
			out.write(text.getBytes());
			out.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private Iterable<ComputeTask> findRunTasksReadyForRun(Pilot pilot)
	{
		ComputeRun computeRun = dataService.findOne(ComputeRun.ENTITY_NAME, new QueryImpl()
				.eq(ComputeRun.NAME, pilot.getComputeRun().getName()));

		if(computeRun.getIsActive())
			return dataService.findAll(ComputeTask.ENTITY_NAME, new QueryImpl()
				.eq(ComputeTask.STATUSCODE, MolgenisPilotService.TASK_READY)
				.and().eq(ComputeTask.COMPUTERUN, computeRun));
		else
			return new ArrayList<ComputeTask>();
	}

	private Iterable<ComputeTask> findRunTasksReadyForBackend(String backendName)
	{
		ComputeBackend computeBackend = dataService.findOne(ComputeBackend.ENTITY_NAME, new QueryImpl()
				.eq(ComputeBackend.NAME, backendName));


		Iterable<ComputeRun> runs = dataService.findAll(ComputeRun.ENTITY_NAME, new QueryImpl()
				.eq(ComputeRun.COMPUTEBACKEND, computeBackend)
				.and().eq(ComputeRun.ISACTIVE, true));

        return dataService.findAll(ComputeTask.ENTITY_NAME, new QueryImpl()
				.eq(ComputeTask.STATUSCODE, MolgenisPilotService.TASK_READY).in(ComputeTask.COMPUTERUN, runs));
	}
}
