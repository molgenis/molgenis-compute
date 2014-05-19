package org.molgenis.compute.db.cloudexecutor;

import com.jcraft.jsch.*;
import com.jcraft.jsch.Logger;
import org.apache.log4j.*;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: hvbyelas
 * Date: 5/9/14
 * Time: 3:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class RemoteExecutor
{
	public static final String EXECUTION_DIR = "/storage";
	//public String SUBMIT_COMMAND = "bash /tmp/script.sh > /tmp/log.log";
	public static String SUBMIT_COMMAND = "bash " + EXECUTION_DIR + "/script.sh > " + EXECUTION_DIR + "/log.log &";
//	public static String SUBMIT_COMMAND = "bash -l " + EXECUTION_DIR + "/script.sh 2>&1 | tee -a " + EXECUTION_DIR + "/log.log &";

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(RemoteExecutor.class);


	public static boolean executeCommandRemote(String IP, String SSHPASS, String serverUserName, String command)
	{
		LOG.info("execute command remotely");

		try {
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
				System.out.println(++index + " : " + line);
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
		return false;
	}

	public static boolean transferScriptAndRun(String IP, String SSHPASS,
											   String serverUsername,
									  		   String script, String jobID)
	{
		LOG.info("Submitting job " + jobID);

		try {
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
		return false;
	}

	//temporary for testing
	public static void main(String[] args)
	{
		transferScriptAndRun("makeit", "makeit",
				"makeit", "echo LALA \n" +
				"sleep 60", "testManual");
	}


}
