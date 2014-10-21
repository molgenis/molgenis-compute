package org.molgenis.compute.db.cloudexecutor;

import org.apache.commons.io.IOUtils;
import org.molgenis.compute.db.executor.ComputeExecutorPilotDB;
import org.molgenis.compute.runtime.ComputeTask;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Properties;

/**
 * Created by hvbyelas on 5/19/14.
 */
public class CloudCurlBuilder
{

	private String serverIP, serverPort;

	@Autowired
	private CloudManager cloudManager;

	private static final String JOB_ID = "jobid";

	private String curlHeaderTemplate;
	private String curlFooterTemplate;

	public String buildScript(ComputeTask task, CloudServer server)
	{
		readTemplates();

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

		String prefix = ComputeExecutorPilotDB.weaveFreemarker(curlHeaderTemplate, values);
		String postfix = ComputeExecutorPilotDB.weaveFreemarker(curlFooterTemplate, values);

		//for testing
		sb.append(prefix);

		String script = task.getComputeScript();
		sb.append(script);

		sb.append(postfix);

		return sb.toString();
	}

	private void readTemplates()
	{
		InputStream inStreamHeader = getClass().getClassLoader().getResourceAsStream("templates/cloud/openstack/header.ftl");
		InputStream inStreamFooter = getClass().getClassLoader().getResourceAsStream("templates/cloud/openstack/footer.ftl");

		StringWriter writer = new StringWriter();
		try
		{
			IOUtils.copy(inStreamHeader, writer);
			curlHeaderTemplate = writer.toString();

			writer = new StringWriter();
			IOUtils.copy(inStreamFooter, writer);
			curlFooterTemplate = writer.toString();

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void readServerProperties()
	{
		Properties prop = new Properties();
		InputStream input = null;

		try
		{
			input = new FileInputStream(".openstack.properties");
			prop.load(input);

			serverIP = prop.getProperty(CloudManager.SERVER_IP);
			serverPort = prop.getProperty(CloudManager.SERVER_PORT);

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
