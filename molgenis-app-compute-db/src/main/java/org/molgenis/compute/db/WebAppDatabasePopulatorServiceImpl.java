package org.molgenis.compute.db;

import org.apache.log4j.Logger;
import org.molgenis.compute.runtime.ComputeBackend;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.db.WebAppDatabasePopulatorService;
import org.molgenis.omx.auth.*;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.account.AccountService;
import org.molgenis.security.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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
	public WebAppDatabasePopulatorServiceImpl(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		this.dataService = dataService;
	}

	private void insertBackends()
	{
		final File folder = new File(SERVERS_DIR);

		for (final File fileEntry : folder.listFiles())
		{
			if (!fileEntry.isDirectory())
			{
				logger.info("Inserting backend [" + fileEntry.getName() + "]");
				ComputeBackend backend = readUserProperties(SERVERS_DIR + System.getProperty("file.separator") + fileEntry.getName());
				if(backend != null)
					dataService.add(ComputeBackend.ENTITY_NAME, backend);
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
		if (adminPassword == null) throw new RuntimeException(
				"please configure the admin.password property in your molgenis-server.properties");
		if (userPassword == null) throw new RuntimeException(
				"please configure the user.password property in your molgenis-server.properties");

		String firstName = "John";
		String lastName = "Doe";

		// add api user
		MolgenisUser userApi = new MolgenisUser();
		userApi.setUsername(apiUserName);
		userApi.setPassword(new BCryptPasswordEncoder().encode(apiUserPassword));
		userApi.setEmail("molgenis@gmail.com");
		userApi.setFirstName("api");
		userApi.setLastName("api");
		userApi.setActive(true);
		userApi.setSuperuser(true);
		dataService.add(MolgenisUser.ENTITY_NAME, userApi);
		logger.info("Added api user");

		MolgenisUser userAdmin = new MolgenisUser();
		userAdmin.setUsername(USERNAME_ADMIN);
		userAdmin.setPassword(new BCryptPasswordEncoder().encode(adminPassword));
		userAdmin.setEmail(adminEmail);
		userAdmin.setFirstName(firstName);
		userAdmin.setLastName(lastName);
		userAdmin.setActive(true);
		userAdmin.setSuperuser(true);
		userAdmin.setFirstName(USERNAME_ADMIN);
		userAdmin.setLastName(USERNAME_ADMIN);
		dataService.add(MolgenisUser.ENTITY_NAME, userAdmin);

		UserAuthority suAuthority = new UserAuthority();
		suAuthority.setMolgenisUser(userAdmin);
		suAuthority.setRole("ROLE_SU");
		dataService.add(UserAuthority.ENTITY_NAME, suAuthority);

		MolgenisUser anonymousUser = new MolgenisUser();
		anonymousUser.setUsername(SecurityUtils.ANONYMOUS_USERNAME);
		anonymousUser.setPassword(new BCryptPasswordEncoder().encode(SecurityUtils.ANONYMOUS_USERNAME));
		anonymousUser.setEmail(anonymousEmail);
		anonymousUser.setActive(true);
		anonymousUser.setSuperuser(false);
		dataService.add(MolgenisUser.ENTITY_NAME, anonymousUser);

		UserAuthority anonymousAuthority = new UserAuthority();
		anonymousAuthority.setMolgenisUser(anonymousUser);
		anonymousAuthority.setRole(SecurityUtils.AUTHORITY_ANONYMOUS);
		dataService.add(UserAuthority.ENTITY_NAME, anonymousAuthority);

		MolgenisUser userUser = new MolgenisUser();
		userUser.setUsername(USERNAME_USER);
		userUser.setPassword(new BCryptPasswordEncoder().encode(userPassword));
		userUser.setEmail(userEmail);
		userUser.setFirstName(firstName);
		userUser.setLastName(lastName);
		userUser.setActive(true);
		userUser.setSuperuser(false);
		userUser.setFirstName(USERNAME_USER);
		userUser.setLastName(USERNAME_USER);
		dataService.add(MolgenisUser.ENTITY_NAME, userUser);

		MolgenisGroup usersGroup = new MolgenisGroup();
		usersGroup.setName(AccountService.ALL_USER_GROUP);
		dataService.add(MolgenisGroup.ENTITY_NAME, usersGroup);
		usersGroup.setName(AccountService.ALL_USER_GROUP);

		MolgenisGroupMember molgenisGroupMember1 = new MolgenisGroupMember();
		molgenisGroupMember1.setMolgenisGroup(usersGroup);
		molgenisGroupMember1.setMolgenisUser(userUser);
		dataService.add(MolgenisGroupMember.ENTITY_NAME, molgenisGroupMember1);

		for (String entityName : dataService.getEntityNames())
		{
			GroupAuthority entityAuthority = new GroupAuthority();
			entityAuthority.setMolgenisGroup(usersGroup);
			entityAuthority.setRole(SecurityUtils.AUTHORITY_ENTITY_READ_PREFIX + entityName.toUpperCase());
			dataService.add(GroupAuthority.ENTITY_NAME, entityAuthority);
		}

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