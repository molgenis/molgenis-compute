package org.molgenis.compute.db.cloudexecutor;

import org.molgenis.compute.db.executor.ComputeExecutorPilotDB;
import org.molgenis.compute.runtime.ComputeTask;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Properties;

/**
 * Created by hvbyelas on 5/19/14.
 */
public class CloudCurlBuilder
{
	private static final String SERVER_IP = "server_ip";
	private static final String SERVER_PORT = "server_port";

	private String serverIP, serverPort;

	@Autowired
	private CloudManager cloudManager;

	private static final String JOB_ID = "jobid";

	private String curlStartedTemplate = "curl -s -S -u ${"+ CloudManager.API_USER +"}:" +
			"${"+ CloudManager.API_PASS +"} -F jobid=${" + JOB_ID + "} -F serverid=${serverid}" +
			" -F status=" + CloudService.STATUS_STARTED + " -F backend=${backend} " +
			"http://${IP}:${PORT}/api/cloud\n";

	private String curlFinishedTemplate = "curl -s -S -u ${"+ CloudManager.API_USER +"}:" +
			"${"+ CloudManager.API_PASS +"} -F jobid=${" + JOB_ID + "} -F serverid=${serverid}" +
			" -F status=" + CloudService.STATUS_FINISHED + " -F backend=${backend} " +
			"-F log_file=@log.log " +
			"http://${IP}:${PORT}/api/cloud\n"+
			"rm -f log.log\n";


	public String buildScript(ComputeTask task, CloudServer server)
	{
		StringBuilder sb = new StringBuilder();

		Hashtable<String, String> values = new Hashtable<String, String>();
		//
		values.put(CloudManager.API_USER, cloudManager.getApiUser());
		values.put(CloudManager.API_PASS, cloudManager.getApiPass());
		values.put(JOB_ID, task.getId() + "");
		values.put("serverid", server.getId());

		String backend = task.getComputeRun().getComputeBackend().getName();
		values.put("backend", backend);

		readServerProperties();
		values.put("IP", serverIP);
		values.put("PORT", serverPort);

		String prefix = ComputeExecutorPilotDB.weaveFreemarker(curlStartedTemplate, values);
		String postfix = ComputeExecutorPilotDB.weaveFreemarker(curlFinishedTemplate, values);

		//for testing
		sb.append(prefix);

		String script = task.getComputeScript();
		sb.append(script);

		sb.append(postfix);

		return sb.toString();
	}

	private void readServerProperties()
	{
		Properties prop = new Properties();
		InputStream input = null;

		try
		{
			input = new FileInputStream(".openstack.properties");
			prop.load(input);

			serverIP = prop.getProperty(SERVER_IP);
			serverPort = prop.getProperty(SERVER_PORT);

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
