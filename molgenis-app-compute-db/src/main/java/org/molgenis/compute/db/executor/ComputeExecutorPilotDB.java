package org.molgenis.compute.db.executor;

import java.io.*;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.molgenis.compute.db.ComputeDbException;
import org.molgenis.compute5.sysexecutor.SysCommandExecutor;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.util.ApplicationUtil;

import javax.servlet.ServletContext;

/**
 * Created with IntelliJ IDEA. User: georgebyelas Date: 22/08/2012 Time: 14:26
 * To change this template use File | Settings | File Templates.
 */
public class ComputeExecutorPilotDB implements ComputeExecutor
{
	public static final int SSH_PORT = 22;
	private static final Logger LOG = Logger.getLogger(ComputeExecutorPilotDB.class);

	private String backendUrl;
	private String username;
	private String password;
	private int sshPort;
	private DataService dataService;

	private ExecutionHost executionHost = null;

	public ComputeExecutorPilotDB(DataService dataService, String backendUrl, String username, String password, int sshPort)
	{
		this.backendUrl = backendUrl;
		this.username = username;
		this.password = password;
		this.sshPort = sshPort;
		this.dataService = dataService;
	}

	@Override
	public void executeTasks(ComputeRun computeRun)
	{
		try
		{
			this.executionHost = new ExecutionHost(dataService, backendUrl, username, password, sshPort);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		if (computeRun == null)
			throw new IllegalArgumentException("ComputRun is null");

		try
		{
			ComputeRun computeRunDB = dataService.findOne(ComputeRun.ENTITY_NAME, new QueryImpl()
					.eq(ComputeRun.NAME, computeRun.getName()));
			Iterable<ComputeTask> generatedTasks = dataService.findAll(ComputeTask.ENTITY_NAME, new QueryImpl()
					.eq(ComputeTask.COMPUTERUN, computeRunDB).and()
					.eq(ComputeTask.STATUSCODE, "generated"));

			int size =  ((Collection<?>)generatedTasks).size();

			LOG.info("Nr of tasks with status [generated]: [" + size + "]");

			evaluateTasks(generatedTasks);

			Iterable<ComputeTask> readyTasks = dataService.findAll(ComputeTask.ENTITY_NAME, new QueryImpl()
					.eq(ComputeTask.COMPUTERUN, computeRunDB).and()
					.eq(ComputeTask.STATUSCODE, "generated"));

			while (readyTasks.iterator().hasNext())
			{
				ComputeTask task = readyTasks.iterator().next();
				LOG.info("Task ready: [" + task.getName() + "]");
			}

			if(computeRun.getIsSubmittingPilots())
				while(readyTasks.iterator().hasNext())
				{
					if (computeRun.getComputeBackend().getHostType().equalsIgnoreCase("localhost"))
					{
						submitPilotLocalhost(computeRun.getComputeBackend().getCommand());
					}
					else
					{
						LOG.info("Executing command [" + computeRun.getComputeBackend().getCommand() + "] on backend ["
								+ computeRun.getComputeBackend().getBackendUrl() + "]");

						if (executionHost == null)
						{
							executionHost = new ExecutionHost(dataService, computeRun.getComputeBackend().getBackendUrl(), username,
									password, SSH_PORT);
						}

						//generate unique pilot and its submission command
						String pilotID = String.valueOf(UUID.randomUUID());

						InputStream inStreamJDL = getClass().getClassLoader().getResourceAsStream("templates/maverick.jdl.ftl");
						InputStream inStreamSH = getClass().getClassLoader().getResourceAsStream("templates/maverick.sh.ftl");

						StringWriter writer = new StringWriter();
						IOUtils.copy(inStreamJDL, writer);
						String jdlTemplate = writer.toString();

						writer = new StringWriter();
						IOUtils.copy(inStreamSH, writer);
						String shTemplate = writer.toString();

						String comTemplate = computeRun.getComputeBackend().getCommand();

						Hashtable<String, String> values = new Hashtable<String, String>();

						values.put("pilotid", pilotID);

						String serverIP = getServerIP();
						values.put("SERVER", serverIP);

						String command = weaveFreemarker(comTemplate, values);
						String jdl = weaveFreemarker(jdlTemplate, values);
						String sh = weaveFreemarker(shTemplate, values);

						executionHost.submitPilot(computeRun,
													command, pilotID, sh, jdl, computeRun.getOwner());
					}
				}
		}
		catch (IOException e)
		{

			LOG.error("IOException executing tasks", e);
			throw new ComputeDbException("DatabaseException executing tasks", e);
		}
		finally
		{
			if (executionHost != null)
			{
				executionHost.close();
			}

		}
	}

	private String getServerIP()
	{
		String result = null;
		try
		{
			result = Inet4Address.getLocalHost().getHostAddress();
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
		return result;
	}

	private void submitPilotLocalhost(String command)
	{
		LOG.info("Execution command [" + command + "] ...");

		SysCommandExecutor localExecutor = new SysCommandExecutor();
		try
		{
			localExecutor.runCommand(command);
		}
		catch (Exception e)
		{
			LOG.error("Exception executing command [" + command + "] on localhost", e);
			throw new ComputeDbException("Exception executing command [" + command + "] on localhost", e);
		}

		String cmdError = localExecutor.getCommandError();
		String cmdOutput = localExecutor.getCommandOutput();

		LOG.info("Command error output:\n" + cmdError);
		LOG.info("Command output:\n" + cmdOutput);
	}

	private void evaluateTasks(Iterable<ComputeTask> generatedTasks)
	{

		while(generatedTasks.iterator().hasNext())
		{
			ComputeTask task = generatedTasks.iterator().next();
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

    public String weaveFreemarker(String strTemplate, Hashtable<String, String> values)
    {
        Configuration cfg = new Configuration();

        Template t = null;
        StringWriter out = new StringWriter();
        try
        {
            t = new Template("name", new StringReader(strTemplate), cfg);
            t.process(values, out);
        }
        catch (TemplateException e)
        {
            //e.printStackTrace();
        }
        catch (IOException e)
        {
            //e.printStackTrace();
        }

        return out.toString();
    }

}
