package org.molgenis.compute.db.clusterexecutor;

import org.apache.log4j.Logger;
import org.molgenis.compute.db.cloudexecutor.CloudExecutor;
import org.molgenis.compute.runtime.ComputeRun;

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

	private ComputeRun run = null;
	private ClusterExecutor executor = null;

	public ClusterThread(ComputeRun run, ClusterExecutor executor)
	{
		this.run = run;
		this.executor = executor;
	}


	@Override
	public void run()
	{
		executor.submitRun(run);
	}
}
