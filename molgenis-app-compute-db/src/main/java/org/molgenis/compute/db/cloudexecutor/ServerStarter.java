package org.molgenis.compute.db.cloudexecutor;

import com.woorea.openstack.keystone.Keystone;
import com.woorea.openstack.keystone.model.Access;
import com.woorea.openstack.keystone.model.authentication.UsernamePassword;
import com.woorea.openstack.nova.Nova;
import com.woorea.openstack.nova.api.ServersResource;
import com.woorea.openstack.nova.model.*;
import org.apache.log4j.Logger;
import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.compute.runtime.ComputeVM;
import org.molgenis.data.DataService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

/**
 * Created by hvbyelas on 5/21/14.
 */
public class ServerStarter
{
	private static final Logger LOG = Logger.getLogger(ServerStarter.class);

	private static final String AUTH = "auth";
	private static final String COMPUTE = "compute";
	public static final String TENANT = "tenant";
	private static final String IMAGE = "image";
	private static final String FLAVOR = "flavor";
	private static final String VOLUME = "volume";

	public static final String KEYPASS = "keypass";

	public static final String API_USER = "apiuser";
	public static final String API_PASS = "apipass";

	public static final String IP_POOL_EXTERN = "ippool_extern";
	public static final String IP_POOL_TARGET = "ippool_target";

	public static final String FIXED_NETWORK_EXTERN_ID = "fixed_network_extern_id";
	public static final String FIXED_NETWORK_TARGET_ID = "fixed_network_target_id";

	public static final String FIXED_NETWORK_EXTERN_IP_PREFIX = "fixed_network_extern_ip_prefix";
	public static final String FIXED_NETWORK_TARGET_IP_PREFIX = "fixed_network_target_ip_prefix";

	public static final String NETWORK_STARTING_NUBMER = "network_start_number";

	public static final String NUMBER_OF_SERVERS = "numberofservers";
	public static final String MOUNT_TARGET_COMMAND = "mount_target";

	public static final String SERVER_USERNAME = "serverusername";

	private String SSHPASS;

	private String KEYSTONE_AUTH;
	private String KEYSTONE_COMPUTE;
	private String KEYSTONE_TENANT;
	private String KEYSTONE_FLAVOR;
	private String KEYSTONE_IMAGE;
	private String KEYSTONE_IP_POOL_EXTERN;
	private String KEYSTONE_IP_POOL_TARGET;
	private String KEYSTONE_FIXED_NETWORK_EXTERN_ID;
	private String KEYSTONE_FIXED_NETWORK_TARGET_ID;

	private String KEYSTONE_FIXED_NETWORK_EXTERN_IP_PREFIX;
	private String KEYSTONE_FIXED_NETWORK_TARGET_IP_PREFIX;

	private String KEYSTONE_VOLUME;
	private String COMPUTE_API_USER;
	private String COMPUTE_API_PASS;
	private String COMPUTE_SERVER_USERNAME;
	private int numberToStart;
	private int KEYSTONE_STARTING_IP;
	private String KEYSTONE_MOUNT_TARGET_COMMAND;

	private static final String SERVER_NAME = "MolgenisServer";

	public static final String SERVER_STATUS_ACTIVE = "ACTIVE";
	public static final String VOLUME_STATUS_AVAILABLE = "available";
	public static final String VOLUME_STATUS_IN_USE = "in-use";

	private static final String DEVICE_NAME = "/dev/vdb";
	private static final String MOUNT_COMMAND = "mount /dev/vdb/ /storage";

	private int keystone_network_current_number;


	private Nova novaClient = null;

	@Autowired
	private CloudManager cloudManager;

	@Autowired
	private DataService dataService;

	public void startServers()
	{
		String backendName = cloudManager.getBackendName();
		startServers(backendName);
	}

	public void startServers(String backendName)
	{
		//now, we have only one cloud, so we do not really need backendName
		readUserProperties();

		for(int i = 0; i < numberToStart; i++)
		{
			CloudServer cloudServer = new CloudServer();
			try
			{
				boolean isSuccess = false;
				while(!isSuccess)
				{
					isSuccess = launchNewServer(cloudServer);
					cloudManager.addNewServer(cloudServer);

				}
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
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
			KEYSTONE_VOLUME = prop.getProperty(VOLUME);
			COMPUTE_API_USER = prop.getProperty(API_USER);
			COMPUTE_API_PASS = prop.getProperty(API_PASS);
			COMPUTE_SERVER_USERNAME = prop.getProperty(SERVER_USERNAME);
			numberToStart =  Integer.parseInt(prop.getProperty(NUMBER_OF_SERVERS));
			KEYSTONE_IP_POOL_EXTERN = prop.getProperty(IP_POOL_EXTERN);
			KEYSTONE_IP_POOL_TARGET =prop.getProperty(IP_POOL_TARGET);
			KEYSTONE_FIXED_NETWORK_EXTERN_ID = prop.getProperty(FIXED_NETWORK_EXTERN_ID);
			KEYSTONE_FIXED_NETWORK_TARGET_ID = prop.getProperty(FIXED_NETWORK_TARGET_ID);

			KEYSTONE_FIXED_NETWORK_EXTERN_IP_PREFIX = prop.getProperty(FIXED_NETWORK_EXTERN_IP_PREFIX);
			KEYSTONE_FIXED_NETWORK_TARGET_IP_PREFIX = prop.getProperty(FIXED_NETWORK_TARGET_IP_PREFIX);

			KEYSTONE_MOUNT_TARGET_COMMAND = prop.getProperty(MOUNT_TARGET_COMMAND);

			KEYSTONE_STARTING_IP = Integer.parseInt(prop.getProperty(NETWORK_STARTING_NUBMER));

			keystone_network_current_number = KEYSTONE_STARTING_IP;

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


		System.out.println("Getting token");
		Keystone keystone = new Keystone(KEYSTONE_AUTH);

		Access access = keystone.tokens()
				.authenticate(new UsernamePassword(cloudManager.getKeyStoneUser(), cloudManager.getKeyStonePass()))
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

		String fixed_extern_ip = KEYSTONE_FIXED_NETWORK_EXTERN_IP_PREFIX + keystone_network_current_number;
		String fixed_target_ip = KEYSTONE_FIXED_NETWORK_TARGET_IP_PREFIX + keystone_network_current_number;

		serverForCreate.addNetworks(KEYSTONE_FIXED_NETWORK_EXTERN_ID, fixed_extern_ip);
		serverForCreate.addNetworks(KEYSTONE_FIXED_NETWORK_TARGET_ID, fixed_target_ip);

		keystone_network_current_number++;

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

		LOG.info("Take allocated extern floating IP");
		FloatingIp floatingIp = null;

		FloatingIps ips = novaClient.floatingIps().list().execute();

		for(FloatingIp ip : ips)
		{
			if(ip.getInstanceId() == null && ip.getPool().equalsIgnoreCase(KEYSTONE_IP_POOL_EXTERN))
			{
				floatingIp = ip;
				break;
			}
		}

		if(floatingIp != null)
		{
			ServersResource.AssociateFloatingIp associateFloatingIp =
					novaClient.servers().associateFloatingIp(server.getId(), fixed_extern_ip,
							floatingIp.getIp());
			associateFloatingIp.execute();
		}
		else
		{
			LOG.error("No extern IP is available");
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
					cloudServer.setFixedIpExtern(fixed_extern_ip);
					cloudServer.setFloatingIpExtern(floatingIp.getIp());
				}
				Thread.sleep(1000);
			}
			System.out.println("... assigning extern IP");
		}

		LOG.info("extern IP is assigned");

		LOG.info("Take allocated target floating IP");
		FloatingIp floatingIpTarget = null;

		ips = novaClient.floatingIps().list().execute();

		for(FloatingIp ip : ips)
		{
			if(ip.getInstanceId() == null && ip.getPool().equalsIgnoreCase(KEYSTONE_IP_POOL_TARGET))
			{
				floatingIpTarget = ip;
				break;
			}
		}

		if(floatingIpTarget != null)
		{
			ServersResource.AssociateFloatingIp associateFloatingIp =
					novaClient.servers().associateFloatingIp(server.getId(),
							fixed_target_ip, floatingIpTarget.getIp());
			associateFloatingIp.execute();
		}
		else
		{
			LOG.error("No target IP is available");
			return false;
		}


		ips = novaClient.floatingIps().list().execute();

		boolean hasExternalTarget = true;
		while(hasExternalTarget)
		{
			for(FloatingIp ip : ips)
			{
				if(ip.getIp().equalsIgnoreCase(floatingIpTarget.getIp()) &&
						ip.getInstanceId().equalsIgnoreCase(id))
				{
					hasExternalTarget = false;
					cloudServer.setFloatingIpTarget(fixed_target_ip);
					cloudServer.setFloatingIpTarget(floatingIpTarget.getIp());
				}
				Thread.sleep(1000);
			}
			System.out.println("... waiting for target IP");
		}


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

		LOG.info("Mounting volume >>>");
		boolean notMounted = true;
		while(notMounted)
			notMounted = !RemoteExecutor.executeCommandRemote(cloudServer.getFloatingIpExtern(), SSHPASS, COMPUTE_SERVER_USERNAME, MOUNT_COMMAND);
		LOG.info("... " + KEYSTONE_VOLUME + " is mounted");


		LOG.info("Mounting target >>>");
		notMounted = true;
		while(notMounted)
			notMounted = !RemoteExecutor.executeCommandRemote(cloudServer.getFloatingIpExtern(),
					SSHPASS, COMPUTE_SERVER_USERNAME, KEYSTONE_MOUNT_TARGET_COMMAND);
		LOG.info("... TARGET is mounted");

		ComputeVM computeVM = new ComputeVM();
		computeVM.setServerID(cloudServer.getId());
		computeVM.setFloatingIpTarget(cloudServer.getFloatingIpTarget());
		computeVM.setFloatingIpExtern(cloudServer.getFloatingIpExtern());
		computeVM.setStartTime(new Date());
		dataService.update(ComputeVM.ENTITY_NAME, computeVM);

		return true;
	}

}
