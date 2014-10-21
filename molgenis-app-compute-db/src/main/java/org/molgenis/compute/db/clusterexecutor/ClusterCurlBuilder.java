package org.molgenis.compute.db.clusterexecutor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.molgenis.compute.db.cloudexecutor.CloudCurlBuilder;
import org.molgenis.compute.db.cloudexecutor.CloudManager;
import org.molgenis.compute.db.cloudexecutor.CloudServer;
import org.molgenis.compute.db.executor.ComputeExecutorPilotDB;
import org.molgenis.compute.runtime.ComputeTask;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by hvbyelas on 5/19/14.
 */
public class ClusterCurlBuilder
{

	private static final String JOB_ID = "jobid";
	private static final String JOB_NAME = "jobname";

	private String curlHeaderTemplate;
	private String curlFooterTemplate;

	private Hashtable<String, String> values = null;

	public String buildScript(ComputeTask task)
	{
		readTemplates();

		StringBuilder sb = new StringBuilder();

		values = new Hashtable<String, String>();
		//
		readServerProperties();

		values.put(JOB_ID, task.getId() + "");
		String backend = task.getComputeRun().getComputeBackend().getName();
		values.put("backend", backend);
		values.put(JOB_NAME, task.getName());


		String prefix = ComputeExecutorPilotDB.weaveFreemarker(curlHeaderTemplate, values);
		String postfix = ComputeExecutorPilotDB.weaveFreemarker(curlFooterTemplate, values);

		String script = task.getComputeScript();

		String lookup = task.getName() + ".sh.started";

		int index = script.indexOf(lookup);
		String top = script.substring(0, index + lookup.length() + 2);
		String bottom = script.substring(index + lookup.length() + 3);

		sb.append(top);
		sb.append(prefix);
		sb.append(bottom);
		sb.append(postfix);

		return sb.toString();
	}

	private void readTemplates()
	{
		InputStream inStreamHeader = getClass().getClassLoader().getResourceAsStream("templates/cluster/header.ftl");
		InputStream inStreamFooter = getClass().getClassLoader().getResourceAsStream("templates/cluster/footer.ftl");

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
			input = new FileInputStream(".cluster.properties");
			prop.load(input);

			values.put("IP", prop.getProperty(CloudManager.SERVER_IP));
			values.put("PORT", prop.getProperty(CloudManager.SERVER_PORT));

			values.put(CloudManager.API_USER, prop.getProperty(CloudManager.API_USER));
			values.put(CloudManager.API_PASS, prop.getProperty(CloudManager.API_PASS));
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
