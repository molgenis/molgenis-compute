package org.molgenis.compute.db.cloudexecutor;

import com.woorea.openstack.keystone.Keystone;
import com.woorea.openstack.keystone.model.Access;
import com.woorea.openstack.keystone.model.authentication.UsernamePassword;
import com.woorea.openstack.nova.Nova;
import com.woorea.openstack.nova.api.ServersResource;
import com.woorea.openstack.nova.model.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

	public static final String IP_POOL = "ippool";

	public static final String SERVER_USERNAME = "serverusername";

	private String SSHPASS;

	private String KEYSTONE_AUTH;
	private String KEYSTONE_COMPUTE;
	private String KEYSTONE_TENANT;
	private String KEYSTONE_FLAVOR;
	private String KEYSTONE_IMAGE;
	private String KEYSTONE_IP_POOL;
	private String KEYSTONE_VOLUME;
	private String COMPUTE_API_USER;
	private String COMPUTE_API_PASS;
	private String COMPUTE_SERVER_USERNAME;

	private static final String SERVER_NAME = "MolgenisServer";

	public static final String SERVER_STATUS_ACTIVE = "ACTIVE";
	public static final String VOLUME_STATUS_AVAILABLE = "available";
	public static final String VOLUME_STATUS_IN_USE = "in-use";

	private static final String DEVICE_NAME = "/dev/vdb";
	private static final String MOUNT_COMMAND = "mount /dev/vdb/ /storage";

	private Nova novaClient = null;

	@Autowired
	private CloudManager cloudManager;

	public void startServers()
	{
		String backendName = cloudManager.getBackendName();
		int numberToStart = cloudManager.getNumberToStart();
		startServers(backendName, numberToStart);
	}

	public void startServers(String backendName, int numberOfServers)
	{
		//now, we have only one cloud, so we do not really need backendName
		readUserProperties();

		for(int i = 0; i < numberOfServers; i++)
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
			KEYSTONE_IP_POOL = prop.getProperty(IP_POOL);
			KEYSTONE_VOLUME = prop.getProperty(VOLUME);
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
			notMounted = !RemoteExecutor.executeCommandRemote(cloudServer.getExternalIP(), SSHPASS, COMPUTE_SERVER_USERNAME, MOUNT_COMMAND);

		return true;
	}

}
