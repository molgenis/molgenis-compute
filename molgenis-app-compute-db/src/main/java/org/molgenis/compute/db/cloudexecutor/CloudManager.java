package org.molgenis.compute.db.cloudexecutor;

import com.woorea.openstack.keystone.model.Access;
import com.woorea.openstack.keystone.model.authentication.UsernamePassword;
import com.woorea.openstack.nova.Nova;
import com.woorea.openstack.nova.api.ServersResource;
import com.woorea.openstack.nova.model.*;
import org.apache.log4j.Logger;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.data.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import com.woorea.openstack.keystone.Keystone;

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
	private static final String FLAVOR = "flavor";
	private static final String VOLUME = "volume";

	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String KEYPASS = "keypass";

	public static final String IP_POOL = "ippool";

	private static final String SERVER_NAME = "MolgenisServer";
	private static final String DEVICE_NAME = "/dev/vdb";
	private static final String MOUNT_COMMAND = "mount /dev/vdb/ /storage";

	private String KEYSTONE_USERNAME;
	private String KEYSTONE_PASSWORD;
	private String SSHPASS;

	private String KEYSTONE_AUTH;
	private String KEYSTONE_COMPUTE;
	private String KEYSTONE_TENANT;
	private String KEYSTONE_FLAVOR;
	private String KEYSTONE_IMAGE;
	private String KEYSTONE_IP_POOL;
	private String KEYSTONE_VOLUME;

	public static final String SERVER_STATUS_ACTIVE = "ACTIVE";
	public static final String VOLUME_STATUS_AVAILABLE = "available";
	public static final String VOLUME_STATUS_IN_USE = "in-use";


	private Nova novaClient = null;

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

		//TODO: move to separated thread
		//now one for testing
		startServers(backendName, 1);

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
		//now, we have only one cloud, so we do not really need backendName

		for(int i = 0; i < numberOfServers; i++)
		{
			CloudServer cloudServer = new CloudServer();
			try
			{
				boolean isSuccess = launchNewServer(cloudServer);
				if(isSuccess)
					servers.add(cloudServer);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
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
			KEYSTONE_FLAVOR = prop.getProperty(FLAVOR);
			KEYSTONE_IP_POOL = prop.getProperty(IP_POOL);
			KEYSTONE_VOLUME = prop.getProperty(VOLUME);

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

	private boolean launchNewServer(CloudServer cloudServer) throws InterruptedException
	{
		LOG.info("Start launching server");

		readUserProperties();

		System.out.println("Getting token");
		Keystone keystone = new Keystone(KEYSTONE_AUTH);
		Access access = keystone.tokens()
				.authenticate(new UsernamePassword(KEYSTONE_USERNAME, KEYSTONE_PASSWORD))
				.withTenantName(KEYSTONE_TENANT)
				.execute();
		LOG.info("...done");

		keystone.token(access.getToken().getId());

		novaClient = new Nova(KEYSTONE_COMPUTE);
		novaClient.token(access.getToken().getId());

		LOG.info("Selecting flavour (start-up disk size)");
		Flavor templateFlavor = null;
		Flavors flavors = novaClient.flavors().list(true).execute();
		for(Flavor flavor : flavors)
		{
			if(flavor.getName().equalsIgnoreCase(KEYSTONE_FLAVOR))
				templateFlavor = flavor;
		}


		LOG.info("Booting instance");
		ServerForCreate serverForCreate = new ServerForCreate();
		serverForCreate.setName(SERVER_NAME);

		serverForCreate.setImageRef(KEYSTONE_IMAGE);
		serverForCreate.setFlavorRef(templateFlavor.getId());
		Server server = novaClient.servers().boot(serverForCreate).execute();

		String id = server.getId();
		cloudServer.setId(id);

		Servers servers = novaClient.servers().list(false).execute();

		boolean notStarted = true;
		while(notStarted)
		{
			for(Server s : servers)
			{
				if(s.getId().equalsIgnoreCase(id))
				{
					Server sss = novaClient.servers().show(s.getId()).execute();
					if(sss.getStatus().equalsIgnoreCase(SERVER_STATUS_ACTIVE))
						notStarted = false;
					LOG.info("... starting server");
				}
			}
			Thread.sleep(1000);
		}

		LOG.info("server is started");

		LOG.info("Take allocated floating IP");
		FloatingIp floatingIp = null;

		FloatingIps ips = novaClient.floatingIps().list().execute();

		for(FloatingIp ip : ips)
		{
			if(ip.getInstanceId() == null && ip.getPool().equalsIgnoreCase(KEYSTONE_IP_POOL))
			{
				floatingIp = ip;
				break;
			}
		}

		if(floatingIp != null)
		{
			ServersResource.AssociateFloatingIp associateFloatingIp =
					novaClient.servers().associateFloatingIp(server.getId(), floatingIp.getIp());
			associateFloatingIp.execute();
		}
		else
		{
			LOG.error("No IP is available");
			return false;
		}
		ips = novaClient.floatingIps().list().execute();

		boolean doNotHaveExternalIP = true;
		while(doNotHaveExternalIP)
		{
			for(FloatingIp ip : ips)
			{
				if(ip.getIp().equalsIgnoreCase(floatingIp.getIp()) &&
						ip.getInstanceId().equalsIgnoreCase(id))
				{
					doNotHaveExternalIP = false;
					cloudServer.setExternalIP(floatingIp.getIp());
				}
				Thread.sleep(1000);
			}
			System.out.println("... assigning external IP");
		}

		LOG.info("external IP is assigned");

		LOG.info("Attaching volume...");

		Volumes volumes = novaClient.volumes().list(true).execute();

		String volumeID = null;

		for(Volume volume : volumes)
		{
			System.out.println("Name: " + volume.getName());
			if(volume.getName().contains(KEYSTONE_VOLUME) && volume.getStatus().equalsIgnoreCase(VOLUME_STATUS_AVAILABLE))
			{
				volumeID = volume.getId();
			}
		}

		if(volumeID == null)
		{
			LOG.error("No volume is available");
			return false;
		}

		ServersResource.AttachVolume attachVolume = novaClient.servers().attachVolume(cloudServer.getId(), volumeID, DEVICE_NAME);
		attachVolume.execute();

		boolean notAttached = true;
		while(notAttached)
		{
			volumes = novaClient.volumes().list(false).execute();
			for(Volume v : volumes)
			{
				if(v.getId().equalsIgnoreCase(volumeID))
				{
					if(v.getStatus().equalsIgnoreCase(VOLUME_STATUS_IN_USE))
						notAttached = false;
					System.out.println("... attaching volume");
					Thread.sleep(1000);
				}
			}
		}

		LOG.info("... volume is attached");

		LOG.info("Mounting volume...");
		boolean notMounted = true;
		while(notMounted)
			notMounted = !RemoteExecutor.executeCommandRemote(cloudServer.getExternalIP(), SSHPASS, MOUNT_COMMAND);

		return true;
	}

}
