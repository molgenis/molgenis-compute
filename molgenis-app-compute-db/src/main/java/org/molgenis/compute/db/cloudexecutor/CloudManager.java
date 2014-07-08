package org.molgenis.compute.db.cloudexecutor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;
import org.molgenis.compute.runtime.ComputeRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;

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

	public static final String KEYPASS = "keypass";
//
	public static final String API_USER = "apiuser";
	public static final String API_PASS = "apipass";
	public static final String SERVER_USERNAME = "serverusername";

	private String KEYSTONE_USERNAME;
	private String KEYSTONE_PASSWORD;
	private String SSHPASS;

	private String COMPUTE_API_USER;
	private String COMPUTE_API_PASS;
	private String COMPUTE_SERVER_USERNAME;

	@Autowired
	private CloudExecutor cloudExecutor;

	@Autowired
	private ServerStarter serverStarter;

	private final TaskScheduler taskScheduler;
	private final Map<Integer, ScheduledFuture<?>> scheduledJobs = new HashMap<Integer, ScheduledFuture<?>>();
	private List<CloudServer> servers = new ArrayList<CloudServer>();
	private String backendName;

	public CloudManager(TaskScheduler taskScheduler)
	{
		readUserProperties();
		this.taskScheduler = taskScheduler;
	}

	public String getSshPass()
	{
		return SSHPASS;
	}

	public void executeRun(ComputeRun run, String username, String password)
	{
		KEYSTONE_USERNAME = username;
		KEYSTONE_PASSWORD = password;

		backendName = run.getComputeBackend().getName();

		(new Thread(new StarterThread(serverStarter))).start();

		CloudThread executor = new CloudThread(run, cloudExecutor);

		//now for testing to run it once
		//remove 000
		ScheduledFuture<?> future = taskScheduler.scheduleWithFixedDelay(executor, 50000);

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

	public void stopAllServers()
	{

	}

	private void stopServer(String serverID)
	{

	}

	public CloudServer getAvailServer()
	{
		for(CloudServer server : servers)
		{
			if(!server.isInUse())
			{
				server.setInUse(true);
				return server;
			}
		}
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

			SSHPASS = prop.getProperty(KEYPASS);
			COMPUTE_API_USER = prop.getProperty(API_USER);
			COMPUTE_API_PASS = prop.getProperty(API_PASS);
			COMPUTE_SERVER_USERNAME = prop.getProperty(SERVER_USERNAME);
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

	public String getServerUsername()
	{
		return COMPUTE_SERVER_USERNAME;
	}

	public String getApiUser()
	{
		return COMPUTE_API_USER;
	}

	public String getApiPass()
	{
		return COMPUTE_API_PASS;
	}

	public List<CloudServer> getCloudServers()
	{
		return servers;
	}

	public int getTotalNumberServers()
	{
		return servers.size();
	}

	public void addNewServer(CloudServer cloudServer)
	{
		servers.add(cloudServer);
	}

	public String getBackendName()
	{
		return backendName;
	}

	public String getKeyStoneUser()
	{
		return KEYSTONE_USERNAME;
	}

	public String getKeyStonePass()
	{
		return KEYSTONE_PASSWORD;
	}
}
