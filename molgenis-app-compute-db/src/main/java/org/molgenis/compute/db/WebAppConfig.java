package org.molgenis.compute.db;

import org.molgenis.DatabaseConfig;
import org.molgenis.compute.db.cloudexecutor.CloudCurlBuilder;
import org.molgenis.compute.db.cloudexecutor.CloudExecutor;
import org.molgenis.compute.db.cloudexecutor.CloudManager;
import org.molgenis.compute.db.cloudexecutor.ServerStarter;
import org.molgenis.compute.db.clusterexecutor.ClusterCurlBuilder;
import org.molgenis.compute.db.clusterexecutor.ClusterExecutor;
import org.molgenis.compute.db.clusterexecutor.ClusterManager;
import org.molgenis.compute.db.executor.ComputeExecutor;
import org.molgenis.compute.db.executor.PilotManager;
import org.molgenis.compute.db.executor.Scheduler;
import org.molgenis.compute.db.pilot.ScriptBuilder;
import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.config.EmbeddedElasticSearchConfig;
import org.molgenis.omx.OmxConfig;
import org.molgenis.omx.config.DataExplorerConfig;
import org.molgenis.ui.MolgenisWebAppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableTransactionManagement
@EnableWebMvc
@EnableScheduling
@ComponentScan("org.molgenis")
@Import(
{ WebAppSecurityConfig.class, DatabaseConfig.class, OmxConfig.class, EmbeddedElasticSearchConfig.class,
		DataExplorerConfig.class })
public class WebAppConfig extends MolgenisWebAppConfig
{
	@Autowired
	private DataService dataService;

	@Autowired
	private ComputeExecutor computeExecutor;

	@Value("${api.user.name:api}")
	private String apiUserName; // specify in molgenis-server.properties

	@Value("${api.user.password:api}")
	private String apiUserPassword; // specify in molgenis-server.properties

	@Bean
	public ScriptBuilder scriptBuilder()
	{
		return new ScriptBuilder(apiUserName, apiUserPassword);
	}

	@Scheduled(fixedDelay = 5000)
	public void expired()
	{
		pilotManager().checkExperiredPilots();
	}

	@Bean
	public PilotManager pilotManager()
	{
		return new PilotManager();
	}

	@Bean
	public Scheduler scheduler()
	{
		return new Scheduler(dataService, taskScheduler(), computeExecutor);
	}

	@Bean
	public CloudManager cloudManager()
	{
		return new CloudManager(taskScheduler());
	}

	@Bean
	public CloudExecutor cloudExecutor()
	{
		return new CloudExecutor();
	}

	@Bean
	public ClusterManager clusterManager()
	{
		return new ClusterManager();
	}

	@Bean
	public ClusterExecutor clusterExecutor()
	{
		return new ClusterExecutor();
	}

	@Bean
	public CloudCurlBuilder cloudCurlBuilder()
	{
		return new CloudCurlBuilder();
	}

	@Bean
	public ClusterCurlBuilder clusterCurlBuilder()
	{
		return new ClusterCurlBuilder();
	}

	@Bean
	public ServerStarter serverStarter()
	{
		return new ServerStarter();
	}

	@Bean(destroyMethod = "shutdown")
	public TaskScheduler taskScheduler()
	{
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(10);

		return scheduler;
	}

}