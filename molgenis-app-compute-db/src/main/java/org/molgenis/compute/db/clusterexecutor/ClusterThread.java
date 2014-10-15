package org.molgenis.compute.db.clusterexecutor;

import org.apache.log4j.Logger;
import org.molgenis.compute.db.cloudexecutor.CloudExecutor;
import org.molgenis.compute.runtime.ComputeRun;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Created with IntelliJ IDEA.
 * User: hvbyelas
 * Date: 5/7/14
 * Time: 4:08 PM
 * To change this template use File | Settings | File Templates.
 */

public class ClusterThread implements Runnable
{
	private static final Logger LOG = Logger.getLogger(ClusterThread.class);
	public static final String SUBMIT = "submit";
	public static final String CANCEL = "cancel";
	private final String username;
	private final String password;
	private final String operation;

	private ComputeRun run = null;
	private ClusterExecutor executor = null;

	public ClusterThread(ClusterExecutor clusterExecutor,
						 ComputeRun run, String username, String password, String operation, SecurityContext ctx)
	{
		this.executor = clusterExecutor;
		this.run = run;
		this.username = username;
		this.password = password;
		this.operation = operation;
		SecurityContextHolder.setContext(ctx);
	}


	@Override
	public void run()
	{
		if(operation.equalsIgnoreCase(SUBMIT))
			executor.submitRun(run, username, password);
		else if(operation.equalsIgnoreCase(CANCEL))
			executor.cancelRun(run, username, password);

	}
}
