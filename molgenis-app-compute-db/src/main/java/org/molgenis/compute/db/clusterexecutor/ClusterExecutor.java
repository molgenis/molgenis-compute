package org.molgenis.compute.db.clusterexecutor;

import com.jcraft.jsch.*;
import com.jcraft.jsch.Logger;
import org.apache.log4j.*;
import org.molgenis.compute.db.pilot.MolgenisPilotService;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Created by hvbyelas on 10/14/14.
 */
public class ClusterExecutor
{
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ClusterManager.class);

	private static final String SLURM_CANCEL = "scancel ";
	private static final String PBS_CANCEL = "qdel ";

	public static final String SLURM = "slurm";
	private static final String PBS = "pbs";

	private List<String> idList = new ArrayList<String>();

	@Autowired
	private DataService dataService;

	@Autowired
	private ClusterCurlBuilder builder;

	private ComputeRun run = null;

	public boolean submitRun(ComputeRun run, String username, String password)
	{
		System.out.println("SUBMIT Run :" + run.getName());
		this.run = run;

		String runName = run.getName();
		String clusterRoot = run.getComputeBackend().getRootDir();
		String runDir = clusterRoot + runName;

		boolean prepared = prepareRun(run, username, password, runDir);

		if(prepared)
		{
			boolean submitted = submit(run, username, password, runDir);
			return submitted;
		}
		else
		{
			LOG.error("Error in preparing ComputeRun");
			return false;
		}
	}

	public boolean prepareRun(ComputeRun run, String username, String password, String runDir)
	{
		LOG.info("Prepare Run: " + run.getName());

		try
		{
			Thread.sleep(90000);

			JSch jsch = new JSch();

			String user = username;
			String host = run.getComputeBackend().getBackendUrl();
			int port = 22;
			String privateKey = ".ssh/id_rsa";

			jsch.addIdentity(privateKey, password);
			LOG.info("identity added ");

			Session session = jsch.getSession(user, host, port);

			LOG.info("session created.");

			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);

			session.connect();
			LOG.info("session connected.....");

			Channel channel = session.openChannel("sftp");
			channel.setInputStream(System.in);
			channel.setOutputStream(System.out);
			channel.connect();
			LOG.info("shell channel connected....");

			ChannelSftp channelSftp = (ChannelSftp) channel;
			ChannelExec channelExec = (ChannelExec)session.openChannel("exec");

//			InputStream answer = channelExec.getInputStream();

			LOG.info("create run directory...");

			channelExec.setCommand("mkdir " + runDir);
			channelExec.connect();
			channelExec.disconnect();

//			give some time to create directory
			TimeUnit.SECONDS.sleep(1);


			LOG.info("scripts transferring...");
			InputStream is = new ByteArrayInputStream(run.getSubmitScript().getBytes());
			channelSftp.put(is, runDir + "/submit.sh");

			Iterable<ComputeTask> generatedTasks = dataService.findAll(ComputeTask.ENTITY_NAME, new QueryImpl()
					.eq(ComputeTask.COMPUTERUN, run).and()
					.eq(ComputeTask.STATUSCODE, MolgenisPilotService.TASK_GENERATED), ComputeTask.class);

			for(ComputeTask task : generatedTasks)
			{
				String taskName = task.getName();
				String builtScript = builder.buildScript(task);
				is = new ByteArrayInputStream(builtScript.getBytes());
				channelSftp.put(is, runDir + "/" + taskName +".sh");
			}

			channelSftp.exit();
//			TimeUnit.SECONDS.sleep(1);
//
//			String command = "cd " + runDir + "; sh submit.sh";
//			channelExec.setCommand(command);
//			channelExec.connect();
//
//			System.out.println("Status: " + channelExec.getExitStatus());
//
//			BufferedReader reader = new BufferedReader(new InputStreamReader(answer));
//			String line;
//
//			int index = 0;
//
//			while ((line = reader.readLine()) != null)
//			{
//				LOG.info(++index + " : " + line);
//			}
//
//			channelExec.disconnect();
			session.disconnect();

			LOG.info("... run [" + run.getName() + "] is prepared");
			return true;
		}
		catch (JSchException e)
		{
			e.printStackTrace();
		}
		catch (SftpException e)
		{
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//		}

		return false;
	}


	public boolean submit(ComputeRun run, String username, String password, String runDir)
	{
		try
		{
			Thread.sleep(90000);

			JSch jsch = new JSch();

			String user = username;
			String host = run.getComputeBackend().getBackendUrl();
			int port = 22;
			String privateKey = ".ssh/id_rsa";

			jsch.addIdentity(privateKey, password);
			LOG.info("identity added ");

			Session session = jsch.getSession(user, host, port);

			LOG.info("session created.");

			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);

			session.connect();
			LOG.info("session connected.....");

			Channel channel = session.openChannel("sftp");
			channel.setInputStream(System.in);
			channel.setOutputStream(System.out);
			channel.connect();
			LOG.info("shell channel connected....");

			ChannelExec channelExec = (ChannelExec)session.openChannel("exec");

			InputStream answer = channelExec.getInputStream();

			LOG.info("submitting ...");

			String command = "cd " + runDir + "; sh submit.sh";
			channelExec.setCommand(command);
			channelExec.connect();

			BufferedReader reader = new BufferedReader(new InputStreamReader(answer));
			String line;
			idList.clear();

			while ((line = reader.readLine()) != null)
			{
				LOG.info(line);
				idList.add(line);
			}

			channelExec.disconnect();
			session.disconnect();

			updateDatabaseWithTaskIDs(idList);

			LOG.info("run [" + run.getName() + "] is submitted");
			return true;
		}
		catch (JSchException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	private void updateDatabaseWithTaskIDs(List<String> idList)
	{
		for(String str: idList)
		{
			int index = str.indexOf(":");
			if(index > 0)
			{
				String idJob = str.substring(0, index);
				String idSub = str.substring(index + 1);

				ComputeTask task = dataService.findOne(ComputeTask.ENTITY_NAME, new QueryImpl()
						.eq(ComputeTask.COMPUTERUN, run).and()
						.eq(ComputeTask.NAME, idJob), ComputeTask.class);

				task.setSubmittedID(idSub);

				if(task.getStatusCode().equalsIgnoreCase(MolgenisPilotService.TASK_GENERATED))
					task.setStatusCode(MolgenisPilotService.TASK_SUBMITTED);

				dataService.update(ComputeTask.ENTITY_NAME, task);

			}
		}
	}


	public boolean cancelRun(ComputeRun run, String username, String password)
	{
		System.out.println("Canceling Run [" + run.getName() + "]");

		try
		{
			Thread.sleep(90000);

			JSch jsch = new JSch();

			String user = username;
			String host = run.getComputeBackend().getBackendUrl();
			int port = 22;
			String privateKey = ".ssh/id_rsa";

			jsch.addIdentity(privateKey, password);
			LOG.info("identity added ");

			Session session = jsch.getSession(user, host, port);

			LOG.info("session created.");

			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);

			session.connect();
			LOG.info("session connected.....");

			Channel channel = session.openChannel("sftp");
			channel.setInputStream(System.in);
			channel.setOutputStream(System.out);
			channel.connect();
			LOG.info("shell channel connected....");

			ChannelExec channelExec = (ChannelExec)session.openChannel("exec");

			InputStream answer = channelExec.getInputStream();

			LOG.info("cancelling jobs ...");

			Iterable<ComputeTask> tasks = dataService.findAll(ComputeTask.ENTITY_NAME, new QueryImpl()
					.eq(ComputeTask.COMPUTERUN, run), ComputeTask.class);

			String schedulerType = run.getComputeBackend().getScheduler();

			for(ComputeTask task : tasks)
			{
				String command ="";
				if(schedulerType.equalsIgnoreCase(SLURM))
					command = SLURM_CANCEL + task.getSubmittedID();
				else if(schedulerType.equalsIgnoreCase(PBS))
					command = PBS_CANCEL + task.getSubmittedID();
				else
				    LOG.error("Unsupported scheduler type [" + schedulerType + "]");

				channelExec.setCommand(command);
				channelExec.connect();

				BufferedReader reader = new BufferedReader(new InputStreamReader(answer));
				String line;

				while ((line = reader.readLine()) != null)
				{
					LOG.info(line);
				}

				task.setStatusCode(MolgenisPilotService.TASK_CANCELLED);
				dataService.update(ComputeTask.ENTITY_NAME, task);
			}

			channelExec.disconnect();
			session.disconnect();

			updateDatabaseWithTaskIDs(idList);

			LOG.info("run [" + run.getName() + "] is cancelled");
			return true;
		}
		catch (JSchException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		return false;

	}
}
