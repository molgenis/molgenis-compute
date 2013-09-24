package org.molgenis.compute.db.pilot;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.molgenis.compute.runtime.ComputeBackend;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.compute.runtime.Pilot;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.ApplicationUtil;
import org.molgenis.util.tuple.HttpServletRequestTuple;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.ParseException;
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

	@RequestMapping(method = RequestMethod.POST, headers = "Content-Type=multipart/form-data")
	public synchronized void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ParseException,
			DatabaseException, IOException
	{

		LOG.debug(">> In handleRequest!");
		HttpServletRequestTuple tuple = new HttpServletRequestTuple(request);

//		small code to read parameters directly from request
//		BufferedReader reader = request.getReader();
//		String test = CharStreams.toString(reader);

		if ("started".equals(tuple.get("status").toString()))
		{
			LOG.info("Checking pilot ID");
			String pilotID = (String) tuple.get(PILOT_ID);
			List<Pilot> pilots = ApplicationUtil.getDatabase().query(Pilot.class).eq(Pilot.VALUE, pilotID)
					.and().eq(Pilot.STATUS, PILOT_SUBMITTED).find();

			System.out.println(">>>>>>>>>>>>>>>>>>>> " + pilotID + " @ " + tuple.get("host"));

			Pilot pilot = null;
			if(pilots.size() > 0)
			{
				LOG.info("Pilot value is correct");
				pilot = pilots.get(0);
				pilot.setStatus(PILOT_USED);
				ApplicationUtil.getDatabase().update(pilot);
			}
			else
			{
				LOG.warn("MALICIOUS PILOT [ " + pilotID + " ] in start");
				return;
			}

			String backend = (String) tuple.get("backend");

			List<ComputeRun> computeRuns = ApplicationUtil.getDatabase().query(ComputeRun.class)
					.equals(ComputeBackend.NAME, pilot.getComputeRun().getName()).find();

			if(computeRuns.size() > 0)
            {
                ComputeRun computeRun = computeRuns.get(0);
                int pilotsStarted = computeRun.getPilotsStarted();
                computeRun.setPilotsStarted(pilotsStarted + 1);
                ApplicationUtil.getDatabase().update(computeRun);
            }
            else
            {
                LOG.error("No ComputeRun found for [" + pilot.getComputeRun().getName() + "]");
            }

            LOG.info("Looking for task to execute for host [" + backend + "]");

			List<ComputeTask> tasks = findRunTasksReady(backend);

			if (tasks.isEmpty())
			{
				LOG.info("No tasks to start for host [" + backend + "]");
			}
			else
			{
				ComputeTask task = tasks.get(0);

				// we add task id to the run listing to identify task when
				// it is done
				ScriptBuilder sb = ApplicationContextProvider.getApplicationContext().getBean(ScriptBuilder.class);
				String serverAddress = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
				String serverPath = request.getServletPath();
				String taskScript = sb.build(task, serverAddress, serverPath, pilotID);

				LOG.info("Script for task [" + task.getName() + "] of run [ " + task.getComputeRun().getName() + "]:\n"
						+ taskScript);

				// change status to running
				task.setStatusCode(MolgenisPilotService.TASK_RUNNING);
				ApplicationUtil.getDatabase().update(task);

				pilot.setComputeTask(task);
				ApplicationUtil.getDatabase().update(pilot);

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
		else if("is_cancel".equals(tuple.get("status").toString()))
		{
			String pilotID = (String) tuple.get(PILOT_ID);
			LOG.info("Checking if pilot " + pilotID + " is cancelled");
			List<Pilot> pilots = ApplicationUtil.getDatabase().query(Pilot.class).eq(Pilot.VALUE, pilotID)
					.and().eq(Pilot.STATUS, PILOT_SUBMITTED).find();

			String runCancelStatus = IS_NOT_CANCELLED;
			if(pilots.size() > 0)
			{
				LOG.info("Pilot value is correct");
				Pilot pilot = pilots.get(0);
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

			LOG.info("Checking pilot ID");
			String pilotID = (String) tuple.get(PILOT_ID);

			List<Pilot> pilots = ApplicationUtil.getDatabase().query(Pilot.class).eq(Pilot.VALUE, pilotID)
					.and().eq(Pilot.STATUS, PILOT_DONE).find();

			if((pilots.size() > 0) && tuple.get("status").toString().equalsIgnoreCase("nopulse"))
			{
				LOG.info("Job is already reported back");
			}
			else
			{
				pilots = ApplicationUtil.getDatabase().query(Pilot.class).eq(Pilot.VALUE, pilotID)
						.and().eq(Pilot.STATUS, PILOT_USED).find();

				if(pilots.size() > 0)
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
			pilots = ApplicationUtil.getDatabase().query(Pilot.class).eq(Pilot.VALUE, pilotID).find();
			if(pilots.size() > 0)
			{
				pilot_withTask = pilots.get(0);
				ComputeTask task_from_pilot = pilot_withTask.getComputeTask();
				if(task_from_pilot == null)
				{
					//the reporting pilot is empty
					return;
				}
			}

			File file = tuple.getFile("log_file");
			String logFileContent = FileUtils.readFileToString(file);
			LogFileParser logfile = new LogFileParser(logFileContent);
			String taskName = logfile.getTaskName();
			String runName = logfile.getRunName();
			List<String> logBlocks = logfile.getLogBlocks();
			String runInfo = StringUtils.join(logBlocks, "\n");

			List<ComputeTask> tasks = ApplicationUtil.getDatabase().query(ComputeTask.class).eq(ComputeTask.NAME, taskName)
					.and().eq(ComputeTask.COMPUTERUN_NAME, runName).find();

			if (tasks.isEmpty())
			{
				LOG.warn("No task found for TASKNAME [" + taskName + "] of RUN [" + runName + "]");
				return;
			}

			ComputeTask task = tasks.get(0);

			if ("done".equals(tuple.get("status").toString()))
			{

				List<Pilot> pilotList = ApplicationUtil.getDatabase().query(Pilot.class).eq(Pilot.COMPUTETASK, task).find();
				Pilot pilot = null;
				if(pilotList.size() > 0)
				{
					pilot = pilots.get(0);
					pilot.setStatus(PILOT_DONE);
					ApplicationUtil.getDatabase().update(pilot);
				}
				else
				{
					LOG.warn("There is no pilot, which got TASK [" + task.getName() + "] of RUN [" + task.getComputeRun().getName() + "]");
				}


				LOG.info(">>> task [" + taskName + "] of run [" + runName + "] is finished");
				if (task.getStatusCode().equalsIgnoreCase(TASK_RUNNING))
				{
					task.setStatusCode(TASK_DONE);
					task.setRunLog(logFileContent);
					task.setRunInfo(runInfo);

					String logFile = LOG_DIR + "/" + taskName + LOG_EXTENSION;
					writeToFile(logFile, logFileContent);

					File output = tuple.getFile("output_file");
					if(output != null)
					{
						task.setOutputEnvironment(FileUtils.readFileToString(output));
					}
				}
				else
				{
					LOG.warn("from done: something is wrong with task [" + taskName + "] of run [" + runName
							+ "] status should be [running] but is [" + task.getStatusCode() + "]");
				}
			}
			else if ("pulse".equals(tuple.get("status").toString()))
			{
				if (task.getStatusCode().equalsIgnoreCase(TASK_RUNNING))
				{
					LOG.info(">>> pulse from task [" + taskName + "] of run [" + runName + "]");
					task.setRunLog(logFileContent);
					task.setRunInfo(runInfo);
				}
			}
			else if ("nopulse".equals(tuple.get("status").toString()))
			{
				//TODO sometimes there is no pulse received, but later job reports back itself
				//todo improve pulse-task management

				if (task.getStatusCode().equalsIgnoreCase(TASK_RUNNING))
				{
					Pilot pilot = pilots.get(0);
					pilot.setStatus(PILOT_FAILED);
					ApplicationUtil.getDatabase().update(pilot);

					LOG.info(">>> no pulse from task [" + taskName + "] of run [" + runName + "]");
					task.setRunLog(logFileContent);
					task.setRunInfo(runInfo);
					task.setStatusCode("failed");

					File failedLog = tuple.getFile("failed_log_file");
					if (failedLog != null)
					{
						task.setFailedLog(FileUtils.readFileToString(failedLog));

						String errFile = LOG_DIR + "/" + taskName + ERR_EXTENSION;
						writeToFile(errFile, FileUtils.readFileToString(failedLog));
					}
				}
				else if (task != null && task.getStatusCode().equalsIgnoreCase(TASK_DONE))
				{
					LOG.info("double check: job is finished & no pulse from it for task [" + taskName + "] of run ["
							+ runName + "]");
				}
			}

			ApplicationUtil.getDatabase().update(task);
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

	private List<ComputeTask> findRunTasksReady(String backendName) throws DatabaseException
	{
       List<ComputeRun> runs = ApplicationUtil.getDatabase().query(ComputeRun.class)
				.eq(ComputeRun.COMPUTEBACKEND_NAME, backendName)
                .and().eq(ComputeRun.ISACTIVE, true).find();

        return ApplicationUtil.getDatabase().query(ComputeTask.class)
				.equals(ComputeTask.STATUSCODE, MolgenisPilotService.TASK_READY).in(ComputeTask.COMPUTERUN, runs).find();
	}
}
