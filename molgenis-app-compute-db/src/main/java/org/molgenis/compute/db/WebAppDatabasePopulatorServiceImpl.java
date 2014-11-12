package org.molgenis.compute.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.molgenis.compute.db.controller.HomeController;
import org.molgenis.compute.runtime.ComputeBackend;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.db.WebAppDatabasePopulatorService;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.MolgenisSecurityWebAppDatabasePopulatorService;
import org.molgenis.security.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WebAppDatabasePopulatorServiceImpl implements WebAppDatabasePopulatorService
{
	private static final Logger logger = Logger.getLogger(WebAppDatabasePopulatorServiceImpl.class);

	private static final String USERNAME_ADMIN = "admin";
	private static final String USERNAME_USER = "user";

	private static final String NAME = "name";
	private static final String BACKEND_URL = "backendUrl";
	private static final String HOST_TYPE = "hostType";
	private static final String COMMAND = "command";
	private static final String SCHEDULER = "scheduler";
	private static final String ROOT_DIR = "rootDir";

	private static final String SERVERS_DIR = ".servers";

	private final DataService dataService;
	private final MolgenisSecurityWebAppDatabasePopulatorService molgenisSecurityWebAppDatabasePopulatorService;

	@Value("${api.user.name:api}")
	private String apiUserName; // specify in molgenis-server.properties

	@Value("${api.user.password:api}")
	private String apiUserPassword; // specify in molgenis-server.properties

	@Value("${admin.password:@null}")
	private String adminPassword;
	@Value("${admin.email:molgenis+admin@gmail.com}")
	private String adminEmail;
	@Value("${user.password:@null}")
	private String userPassword;
	@Value("${user.email:molgenis+user@gmail.com}")
	private String userEmail;
	@Value("${anonymous.email:molgenis+anonymous@gmail.com}")
	private String anonymousEmail;

	@Autowired
	public WebAppDatabasePopulatorServiceImpl(DataService dataService,
			MolgenisSecurityWebAppDatabasePopulatorService molgenisSecurityWebAppDatabasePopulatorService)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		this.dataService = dataService;

		if (molgenisSecurityWebAppDatabasePopulatorService == null) throw new IllegalArgumentException(
				"MolgenisSecurityWebAppDatabasePopulator is null");
		this.molgenisSecurityWebAppDatabasePopulatorService = molgenisSecurityWebAppDatabasePopulatorService;
	}

	private void insertBackends()
	{
		final File folder = new File(SERVERS_DIR);

		for (final File fileEntry : folder.listFiles())
		{
			if (!fileEntry.isDirectory())
			{
				logger.info("Inserting backend [" + fileEntry.getName() + "]");
				ComputeBackend backend = readUserProperties(SERVERS_DIR + System.getProperty("file.separator")
						+ fileEntry.getName());
				if (backend != null) dataService.add(ComputeBackend.ENTITY_NAME, backend);
			}
		}
	}

	private ComputeBackend readUserProperties(String name)
	{
		Properties prop = new Properties();
		InputStream input = null;

		ComputeBackend backend = new ComputeBackend();

		try
		{
			input = new FileInputStream(name);

			// load a properties file
			prop.load(input);

			backend.setName(prop.getProperty(NAME));
			backend.setBackendUrl(prop.getProperty(BACKEND_URL));
			backend.setHostType(prop.getProperty(HOST_TYPE));
			backend.setCommand(prop.getProperty(COMMAND));
			backend.setScheduler(prop.getProperty(SCHEDULER));
			backend.setRootDir(prop.getProperty(ROOT_DIR));

			return backend;

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
		return null;
	}

	@Override
	@Transactional
	@RunAsSystem
	public void populateDatabase()
	{
		molgenisSecurityWebAppDatabasePopulatorService.populateDatabase(this.dataService, HomeController.ID);

		// add api user
		MolgenisUser userApi = new MolgenisUser();
		userApi.setUsername(apiUserName);
		userApi.setPassword(apiUserPassword);
		userApi.setEmail("molgenis@gmail.com");
		userApi.setFirstName("api");
		userApi.setLastName("api");
		userApi.setActive(true);
		userApi.setSuperuser(true);
		dataService.add(MolgenisUser.ENTITY_NAME, userApi);
		logger.info("Added api user");

		insertBackends();
	}

	@Override
	@Transactional
	@RunAsSystem
	public boolean isDatabasePopulated()
	{
		return dataService.count(MolgenisUser.ENTITY_NAME, new QueryImpl()) > 0;
	}

}