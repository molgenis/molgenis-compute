package org.molgenis.compute.db.cloudexecutor;

import com.jcraft.jsch.*;
import com.jcraft.jsch.Logger;
import org.apache.log4j.*;

import java.io.*;
import java.util.List;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: hvbyelas
 * Date: 5/9/14
 * Time: 3:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class RemoteExecutor
{
	public static final String EXECUTION_DIR_FLAG = "exec_dir";
	public String EXECUTION_DIR = null;
	public static final String SUBMIT_COMMAND_FLAG = "submit_command";

	public String SUBMIT_COMMAND = null;

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(RemoteExecutor.class);

	public RemoteExecutor()
	{
		readUserProperties();
	}

	public static boolean executeCommandRemote(String IP, String SSHPASS, String serverUserName, String command)
	{
		LOG.info("execute command remotely");

		try {

			Thread.sleep(90000);

			JSch jsch = new JSch();

			String user = serverUserName;
			String host = IP;
			int port = 22;
			String privateKey = ".ssh/id_rsa";

			jsch.addIdentity(privateKey, SSHPASS);
			LOG.info("identity added ");

			Session session = jsch.getSession(user, host, port);
			LOG.info("session created.");

			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);

			session.connect();
			LOG.info("session connected.....");

			LOG.info("execute command ...");
			ChannelExec channelExec = (ChannelExec)session.openChannel("exec");

			InputStream in = channelExec.getInputStream();

			LOG.info(command);
			channelExec.setCommand(command);
			channelExec.connect();

			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line;
			int index = 0;

			while ((line = reader.readLine()) != null)
			{
				LOG.info(++index + " : " + line);
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

	public boolean executeCommandsRemote(String IP, String SSHPASS, String serverUserName, List<String> commands)
	{
		LOG.info("execute command remotely");

		try {

			Thread.sleep(90000);

			JSch jsch = new JSch();

			String user = serverUserName;
			String host = IP;
			int port = 22;
			String privateKey = ".ssh/id_rsa";

			jsch.addIdentity(privateKey, SSHPASS);
			LOG.info("identity added ");

			Session session = jsch.getSession(user, host, port);
			LOG.info("session created.");

			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);

			session.connect();
			LOG.info("session connected.....");

			LOG.info("execute commands ...");
			ChannelExec channelExec = (ChannelExec)session.openChannel("exec");

			InputStream in = channelExec.getInputStream();

			for(String command : commands)
			{
				LOG.info(command);
				channelExec.setCommand(command);
				channelExec.connect();

				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String line;
				int index = 0;

				while ((line = reader.readLine()) != null)
				{
					LOG.info(++index + " : " + line);
				}

				channelExec.disconnect();
			}
			session.disconnect();

			LOG.info("done!");
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

	public boolean transferScriptAndRun(String IP, String SSHPASS,
											   String serverUsername,
									  		   String script, String jobID)
	{
		LOG.info("Submitting job " + jobID);

		try {

			Thread.sleep(90000);

			JSch jsch = new JSch();

			String user = serverUsername;
			String host = IP;
			int port = 22;
			String privateKey = ".ssh/id_rsa";

			jsch.addIdentity(privateKey, SSHPASS);
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

			ChannelSftp c = (ChannelSftp) channel;

			LOG.info("script transfering...");
			InputStream is = new ByteArrayInputStream(script.getBytes());
			c.put(is, EXECUTION_DIR +"/script.sh");

			c.exit();
			LOG.info("done");

			LOG.info("executing ...");
			ChannelExec channelExec = (ChannelExec)session.openChannel("exec");

			channelExec.setCommand(SUBMIT_COMMAND);
			channelExec.connect();
			channelExec.start();

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
		return false;
	}

	//temporary for testing
	public static void main(String[] args)
	{
		new RemoteExecutor().transferScriptAndRun("makeit", "makeit",
				"makeit", "echo LALA \n" +
				"sleep 60", "testManual");
	}

	private void readUserProperties()
	{
		Properties prop = new Properties();
		InputStream input = null;

		try
		{
			LOG.info("Working Directory = " +
					System.getProperty("user.dir"));

			input = new FileInputStream(".openstack.properties");

			// load a properties file
			prop.load(input);

			EXECUTION_DIR = prop.getProperty(EXECUTION_DIR_FLAG);
			SUBMIT_COMMAND = prop.getProperty(SUBMIT_COMMAND_FLAG);
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
