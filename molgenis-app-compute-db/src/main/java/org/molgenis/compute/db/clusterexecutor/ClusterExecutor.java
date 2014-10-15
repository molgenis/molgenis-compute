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
import java.util.Properties;

/**
 * Created by hvbyelas on 10/14/14.
 */
public class ClusterExecutor
{
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ClusterManager.class);

	private static final String CLUSTER_DIR = "cluster_dir";

	private String CLUSTER_ROOT_DIR;
	private String SUBMIT_COMMAND = "sh submit.sh";

	@Autowired
	private DataService dataService;

	public boolean submitRun(ComputeRun run, String username, String password)
	{
		System.out.println("SUBMIT");
		System.out.println("Run :" + run.getName());
		System.out.println("User:" + username);
		System.out.println("Pass:" + password);

		LOG.info("Submitting Compute Run: " + run.getName());

		try {

			Thread.sleep(90000);

			readUserProperties();

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

			LOG.info("create run directory...");

			String runName = run.getName();
			String runDir = CLUSTER_ROOT_DIR + runName;

			channelExec.setCommand("mkdir " + runDir);
			channelExec.connect();
			channelExec.start();

			channelExec.disconnect();

			//give some time to create directory
			Thread.sleep(5);

			LOG.info("scripts transferring...");
			System.out.println(run.getSubmitScript());
			InputStream is = new ByteArrayInputStream(run.getSubmitScript().getBytes());
			System.out.println("file:"+runDir +"/submit.sh");
			channelSftp.put(is, runDir + "/submit.sh");

			Iterable<ComputeTask> generatedTasks = dataService.findAll(ComputeTask.ENTITY_NAME, new QueryImpl()
					.eq(ComputeTask.COMPUTERUN, run).and()
					.eq(ComputeTask.STATUSCODE, MolgenisPilotService.TASK_GENERATED), ComputeTask.class);

			for(ComputeTask task : generatedTasks)
			{
				String taskName = task.getName();
				is = new ByteArrayInputStream(task.getComputeScript().getBytes());
				channelSftp.put(is, runDir + "/" + taskName +".sh");
			}

			channelSftp.exit();
			LOG.info("done");

			//give some time to save files
			Thread.sleep(10);

			LOG.info("submitting ...");

			InputStream answer = channelExec.getInputStream();
			InputStream error = channelExec.getErrStream();


			channelExec.setCommand("sh " + runDir + "/submit.sh");
			channelExec.connect();
			channelExec.start();

			BufferedReader reader = new BufferedReader(new InputStreamReader(answer));
			String line;

			while ((line = reader.readLine()) != null)
			{
				LOG.info(line);
			}

			reader = new BufferedReader(new InputStreamReader(error));

			while ((line = reader.readLine()) != null)
			{
				LOG.info("ERROR:"+line);
			}

			channelExec.disconnect();
			session.disconnect();

			LOG.info("done!");
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
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public boolean cancelRun(ComputeRun run, String username, String password)
	{
		System.out.println("CANCEL");
		System.out.println("Run :" + run.getName());
		System.out.println("User:" + username);
		System.out.println("Pass:" + password);
		return true;
	}

	private void readUserProperties()
	{
		Properties prop = new Properties();
		InputStream input = null;

		try
		{
			input = new FileInputStream(".cluster.properties");

			// load a properties file
			prop.load(input);

			CLUSTER_ROOT_DIR = prop.getProperty(CLUSTER_DIR);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			if (input != null)
			{
				try
				{
					input.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
