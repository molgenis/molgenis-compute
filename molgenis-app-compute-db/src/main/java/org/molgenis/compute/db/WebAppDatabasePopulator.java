package org.molgenis.compute.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.molgenis.MolgenisDatabasePopulator;
import org.molgenis.compute.runtime.ComputeBackend;
import org.molgenis.compute.runtime.ComputeParameterValue;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.compute.runtime.ComputeTaskHistory;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.security.Login;
import org.molgenis.framework.server.MolgenisPermissionService;
import org.molgenis.framework.server.MolgenisPermissionService.Permission;
import org.molgenis.omx.auth.MolgenisGroup;
import org.molgenis.omx.auth.MolgenisPermission;
import org.molgenis.omx.auth.MolgenisRole;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.auth.OmxPermissionService;
import org.molgenis.omx.auth.util.PasswordHasher;
import org.molgenis.omx.controller.DataSetsIndexerController;
import org.molgenis.omx.core.MolgenisEntity;
import org.molgenis.omx.core.RuntimeProperty;
import org.molgenis.ui.MolgenisMenuController.VoidPluginController;
import org.molgenis.compute.db.controller.HomeController;
import org.molgenis.compute.db.controller.PilotDashboardController;
import org.molgenis.util.Entity;
import org.springframework.beans.factory.annotation.Value;

public class WebAppDatabasePopulator extends MolgenisDatabasePopulator
{
	private static final Logger logger = Logger.getLogger(WebAppDatabasePopulator.class);

	@Value("${api.user.name:api}")
	private String apiUserName; // specify in molgenis-server.properties

	@Value("${api.user.password:api}")
	private String apiUserPassword; // specify in molgenis-server.properties

	@Value("${admin.password:@null}")
	private String adminPassword;
	
	@Override
	protected void initializeApplicationDatabase(Database database) throws Exception
	{
		Login login = database.getLogin();
		database.setLogin(null);
		login.login(database, Login.USER_ADMIN_NAME, adminPassword);

		MolgenisPermissionService permissionService = new OmxPermissionService(database, login);

		database.beginTx();
		try
		{
			Map<String, String> runtimePropertyMap = new HashMap<String, String>();
			
			List<MolgenisGroup> listMolgenisGroup = database.find(MolgenisGroup.class, new QueryRule(MolgenisGroup.NAME,
					Operator.EQUALS, "AllUsers"));
			
			for (Entry<String, String> entry : runtimePropertyMap.entrySet())
			{
				RuntimeProperty runtimeProperty = new RuntimeProperty();
				String app = entry.getKey();
				runtimeProperty.setIdentifier(RuntimeProperty.class.getSimpleName() + '_' + app);
				runtimeProperty.setName(app);
				runtimeProperty.setValue(entry.getValue());
				database.add(runtimeProperty);
			}
			
			MolgenisGroup readUsersGroup = createGroup(database, "readUsers");
			MolgenisGroup readWriteUsersGroup = createGroup(database, "readWriteUsers");
			
			// add api user
			MolgenisUser userApi = new MolgenisUser();
			userApi.setName(apiUserName);
			userApi.setIdentifier(UUID.randomUUID().toString());
			userApi.setPassword(new PasswordHasher().toMD5(apiUserPassword));
			userApi.setEmail("molgenis@gmail.com");
			userApi.setFirstName("api");
			userApi.setLastName("api");
			userApi.setActive(true);
			userApi.setSuperuser(true);
			database.add(userApi);
			logger.info("Added api user");

			MolgenisPermission runPermission = new MolgenisPermission();
			runPermission.setEntity(MolgenisEntity.findByClassName(database, ComputeRun.class.getName()));
			runPermission.setName("ApiUser_ComputeRun_Read_Permission");
			runPermission.setIdentifier(UUID.randomUUID().toString());
			runPermission.setPermission("read");
			runPermission.setRole(userApi);
			database.add(runPermission);

			MolgenisPermission taskPermission = new MolgenisPermission();
			taskPermission.setEntity(MolgenisEntity.findByClassName(database, ComputeTask.class.getName()));
			taskPermission.setName("ApiUser_ComputeTask_Read_Permission");
			taskPermission.setIdentifier(UUID.randomUUID().toString());
			taskPermission.setPermission("read");
			taskPermission.setRole(userApi);
			database.add(taskPermission);

			MolgenisPermission taskWritePermission = new MolgenisPermission();
			taskWritePermission.setEntity(MolgenisEntity.findByClassName(database, ComputeTask.class.getName()));
			taskWritePermission.setName("ApiUser_ComputeTask_Write_Permission");
			taskWritePermission.setIdentifier(UUID.randomUUID().toString());
			taskWritePermission.setPermission("write");
			taskWritePermission.setRole(userApi);
			database.add(taskWritePermission);
			logger.info("Added api user permissions");

			// Create compute user group
			MolgenisGroup userGroup = new MolgenisGroup();
			userGroup.setIdentifier(UUID.randomUUID().toString());
			userGroup.setName("ComputeUser");
			database.add(userGroup);
			logger.info("Added compute user group");

			// Add permissions to user group
			MolgenisPermission runOwnPermission = new MolgenisPermission();
			runOwnPermission.setEntity(MolgenisEntity.findByClassName(database, ComputeRun.class.getName()));
			runOwnPermission.setName("ComputeUser_ComputeTask_Own_Permission");
			runOwnPermission.setIdentifier(UUID.randomUUID().toString());
			runOwnPermission.setPermission("own");
			runOwnPermission.setRole(userGroup);
			database.add(runOwnPermission);

			MolgenisPermission taskOwnPermission = new MolgenisPermission();
			taskOwnPermission.setEntity(MolgenisEntity.findByClassName(database, ComputeTask.class.getName()));
			taskOwnPermission.setName("ComputeUser_ComputeTask_Own_Permission");
			taskOwnPermission.setIdentifier(UUID.randomUUID().toString());
			taskOwnPermission.setPermission("own");
			taskOwnPermission.setRole(userGroup);
			database.add(taskOwnPermission);

			MolgenisPermission taskHistoryOwnPermission = new MolgenisPermission();
			taskHistoryOwnPermission.setEntity(MolgenisEntity.findByClassName(database,
					ComputeTaskHistory.class.getName()));
			taskHistoryOwnPermission.setName("ComputeUser_ComputeTaskHistory_Own_Permission");
			taskHistoryOwnPermission.setIdentifier(UUID.randomUUID().toString());
			taskHistoryOwnPermission.setPermission("own");
			taskHistoryOwnPermission.setRole(userGroup);
			database.add(taskHistoryOwnPermission);

			MolgenisPermission parameterValueOwnPermission = new MolgenisPermission();
			parameterValueOwnPermission.setEntity(MolgenisEntity.findByClassName(database,
					ComputeParameterValue.class.getName()));
			parameterValueOwnPermission.setName("ComputeUser_ComputeParameterValue_Own_Permission");
			parameterValueOwnPermission.setIdentifier(UUID.randomUUID().toString());
			parameterValueOwnPermission.setPermission("own");
			parameterValueOwnPermission.setRole(userGroup);
			database.add(parameterValueOwnPermission);

			MolgenisPermission computeBackendOwnPermission = new MolgenisPermission();
			computeBackendOwnPermission.setEntity(MolgenisEntity.findByClassName(database,
					ComputeBackend.class.getName()));
			computeBackendOwnPermission.setName("ComputeUser_ComputeBackend_Own_Permission");
			computeBackendOwnPermission.setIdentifier(UUID.randomUUID().toString());
			computeBackendOwnPermission.setPermission("own");
			computeBackendOwnPermission.setRole(userGroup);
			database.add(computeBackendOwnPermission);
		
			MolgenisUser anonymousUser = MolgenisUser.findByName(database, Login.USER_ANONYMOUS_NAME);
			
			// Set write permissions that a user can edit own account
			permissionService.setPermissionOnEntity(MolgenisUser.class, anonymousUser.getId(), Permission.READ);
			permissionService.setPermissionOnEntity(MolgenisUser.class, listMolgenisGroup.get(0).getId(), Permission.WRITE);

			setPermissionsForUserGroup(permissionService, database, readUsersGroup, Permission.READ);
			setPermissionsForUserGroup(permissionService, database, readWriteUsersGroup, Permission.WRITE);

			permissionService.setPermissionOnPlugin(VoidPluginController.class, anonymousUser.getId(), Permission.READ);
			permissionService.setPermissionOnPlugin(HomeController.class, anonymousUser.getId(), Permission.READ);
			
			logger.info("Added compute user group permissions");

			insertBackends(database);
			logger.info("Added backends");
			
			setPermissionsForUserGroup(permissionService, database, userGroup, Permission.READ);
			
			database.commitTx();
		}
		catch (Exception e)
		{
			database.rollbackTx();
			throw e;
		}

		database.setLogin(login);
	}

	private void insertBackends(Database database) throws DatabaseException
	{
		ComputeBackend localhost = new ComputeBackend();
		localhost.setName("localhost");
		localhost.setBackendUrl("localhost");
		localhost.setHostType("LOCALHOST");
		localhost.setCommand("sh src/main/shell/local/maverick.sh");
		database.add(localhost);

		ComputeBackend grid = new ComputeBackend();
		grid.setName("ui.grid.sara.nl");
		grid.setBackendUrl("ui.grid.sara.nl");
		grid.setHostType("GRID");
		grid.setCommand("glite-wms-job-submit  -d $USER -o pilot-one $HOME/maverick/maverick${pilotid}.jdl");
		database.add(grid);
	}
	
	private void setPermissionsForUserGroup(MolgenisPermissionService permissionService, Database database,
			MolgenisRole groupName, Permission permission) throws DatabaseException
	{
		// Set entity permissions
		Vector<org.molgenis.model.elements.Entity> entities = database.getMetaData().getEntities(false, false);
		for (org.molgenis.model.elements.Entity e : entities)
		{
			Class<? extends Entity> entityClass = database.getClassForName(e.getName());
			permissionService.setPermissionOnEntity(entityClass, groupName.getId(), permission);

		}

		permissionService.setPermissionOnPlugin(HomeController.class, groupName.getId(), Permission.READ);
		permissionService.setPermissionOnPlugin(DataSetsIndexerController.class, groupName.getId(), 
				Permission.READ);
		permissionService.setPermissionOnPlugin(PilotDashboardController.class, groupName.getId(),
				Permission.READ);
	}
}