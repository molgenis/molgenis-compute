package org.molgenis.compute.db.cloudexecutor;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created with IntelliJ IDEA.
 * User: hvbyelas
 * Date: 5/9/14
 * Time: 3:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class RemoteExecutor
{
	public static boolean executeCommandRemote(String IP, String SSHPASS, String command)
	{
		System.out.println("execute command remotely");

		try {
			JSch jsch = new JSch();

			String user = "root";
			String host = IP;
			int port = 22;
			String privateKey = ".ssh/id_rsa";

			jsch.addIdentity(privateKey, SSHPASS);
			System.out.println("identity added ");

			Session session = jsch.getSession(user, host, port);
			System.out.println("session created.");

			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);

			session.connect();
			System.out.println("session connected.....");

			System.out.println("execute command ...");
			ChannelExec channelExec = (ChannelExec)session.openChannel("exec");

			InputStream in = channelExec.getInputStream();

			System.out.println(command);
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

			System.out.println("done!");
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

}
