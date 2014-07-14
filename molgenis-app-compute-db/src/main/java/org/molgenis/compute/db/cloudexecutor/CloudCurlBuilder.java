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
	@Autowired
	private CloudManager cloudManager;

	private static final String JOB_ID = "jobid";

	private String curlStartedTemplate = "curl -s -S -u ${"+ CloudManager.API_USER +"}:" +
			"${"+ CloudManager.API_PASS +"} -F jobid=${" + JOB_ID + "} -F serverid=${serverid}" +
			" -F status=" + CloudService.STATUS_STARTED + " -F backend=${backend} " +
			"http://${IP}:8080/api/cloud\n";

	private String curlFinishedTemplate = "curl -s -S -u ${"+ CloudManager.API_USER +"}:" +
			"${"+ CloudManager.API_PASS +"} -F jobid=${" + JOB_ID + "} -F serverid=${serverid}" +
			" -F status=" + CloudService.STATUS_FINISHED + " -F backend=${backend} " +
			"-F log_file=@log.log " +
			"http://${IP}:8080/api/cloud\n";


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

		String ip = readServerIPProperty();
		values.put("IP", ip);

		String prefix = ComputeExecutorPilotDB.weaveFreemarker(curlStartedTemplate, values);
		String postfix = ComputeExecutorPilotDB.weaveFreemarker(curlFinishedTemplate, values);

		//for testing
		sb.append("echo start\n");

		sb.append(prefix);

		sb.append("echo \"after curl\"\n");

		String script = task.getComputeScript();
		sb.append(script);

		sb.append(postfix);

		//for testing
		sb.append("echo finish\n");


		return sb.toString();
	}

	private String readServerIPProperty()
	{
		String serverIP = null;

		Properties prop = new Properties();
		InputStream input = null;

		try
		{
			input = new FileInputStream(".openstack.properties");
			prop.load(input);
			serverIP = prop.getProperty(SERVER_IP);

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

		return serverIP;
	}


}
