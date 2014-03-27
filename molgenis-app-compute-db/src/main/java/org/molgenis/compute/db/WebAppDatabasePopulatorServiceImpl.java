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

@Service
public class WebAppDatabasePopulatorServiceImpl implements WebAppDatabasePopulatorService
{
	private static final Logger logger = Logger.getLogger(WebAppDatabasePopulatorServiceImpl.class);

	private static final String USERNAME_ADMIN = "admin";
	private static final String USERNAME_USER = "user";

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

	@Autowired
	public WebAppDatabasePopulatorServiceImpl(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		this.dataService = dataService;
	}

	private void insertBackends()
	{
		ComputeBackend localhost = new ComputeBackend();
		localhost.setName("localhost");
		localhost.setBackendUrl("localhost");
		localhost.setHostType("LOCALHOST");
		localhost.setCommand("sh src/main/shell/local/maverick.sh");
		dataService.add(ComputeBackend.ENTITY_NAME, localhost);

		ComputeBackend grid = new ComputeBackend();
		grid.setName("ui.grid.sara.nl");
		grid.setBackendUrl("ui.grid.sara.nl");
		grid.setHostType("GRID");
		grid.setCommand("glite-wms-job-submit  -d $USER -o pilot-one $HOME/maverick/maverick${pilotid}.jdl");
		dataService.add(ComputeBackend.ENTITY_NAME, grid);
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