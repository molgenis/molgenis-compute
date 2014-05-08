package org.molgenis.compute.db.cloudexecutor;

import org.apache.log4j.Logger;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.data.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

/**
 * Created with IntelliJ IDEA.
 * User: hvbyelas
 * Date: 5/6/14
 * Time: 3:24 PM
 * To change this template use File | Settings | File Templates.
 */


public class CloudManager
{
	private static final Logger LOG = Logger.getLogger(CloudManager.class);

	private static final String AUTH = "auth";
	private static final String COMPUTE = "compute";
	public static final String TENANT = "tenant";
	private static final String IMAGE = "image";

	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String KEYPASS = "keypass";

	public static final String IP_POOL = "ippool";

	private String KEYSTONE_USERNAME;
	private String KEYSTONE_PASSWORD;
	private String SSHPASS;

	private String KEYSTONE_AUTH;
	private String KEYSTONE_COMPUTE;
	private String KEYSTONE_TENANT;
	private String KEYSTONE_IMAGE;
	private String KEYSTONE_IP_POOL;

	public static final String SERVER_ACTIVE_STATUS = "ACTIVE";

	@Autowired
	private DataService dataService;

	@Autowired
	CloudExecutor cloudExecutor;

	private final TaskScheduler taskScheduler;// = new ThreadPoolTaskScheduler();
	private final Map<Integer, ScheduledFuture<?>> scheduledJobs = new HashMap<Integer, ScheduledFuture<?>>();
	private List<CloudServer> servers = new ArrayList<CloudServer>();

	public CloudManager(TaskScheduler taskScheduler)
	{
		readUserProperties();
		this.taskScheduler = taskScheduler;
	}

	public void executeRun(ComputeRun run, String username, String password)
	{
		KEYSTONE_USERNAME = username;
		KEYSTONE_PASSWORD = password;

		String backendName = run.getComputeBackend().getName();

		//for testing now
		startServers(backendName, 3);

		CloudThread executor = new CloudThread(run, cloudExecutor);
		ScheduledFuture<?> future = taskScheduler.scheduleWithFixedDelay(executor, run.getPollDelay());

		scheduledJobs.put(run.getId(), future);
	}

	public void stopExecutingRun(ComputeRun run)
	{
		ScheduledFuture<?> future = scheduledJobs.get(run.getId());
		if(future != null)
		{
			future.cancel(false);
			scheduledJobs.remove(run.getId());
		}
	}

	public void startServers(String backendName, int numberOfServers)
	{

	}

	private void stopServer(String serverID)
	{

	}

	public CloudServer getAvailServer()
	{
		return null;
	}

	public void setRunCompleted(Integer runID)
	{
		//evaluate here if servers are still needed
	}

	private void readUserProperties()
	{
		Properties prop = new Properties();
		InputStream input = null;

		try
		{
			input = new FileInputStream(".openstack.properties");

			// load a properties file
			prop.load(input);

			// get the property value and print it out

			SSHPASS = prop.getProperty(KEYPASS);
			KEYSTONE_AUTH = prop.getProperty(AUTH);;
			KEYSTONE_COMPUTE = prop.getProperty(COMPUTE);
			KEYSTONE_TENANT = prop.getProperty(TENANT);
			KEYSTONE_IMAGE = prop.getProperty(IMAGE);
			KEYSTONE_IP_POOL = prop.getProperty(IP_POOL);

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
