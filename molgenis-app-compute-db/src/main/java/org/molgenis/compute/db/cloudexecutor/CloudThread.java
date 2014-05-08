package org.molgenis.compute.db.cloudexecutor;

import org.apache.log4j.Logger;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.runas.RunAsSystem;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hvbyelas
 * Date: 5/7/14
 * Time: 4:08 PM
 * To change this template use File | Settings | File Templates.
 */

public class CloudThread implements Runnable
{
	private static final Logger LOG = Logger.getLogger(CloudThread.class);

	private ComputeRun run = null;
	private CloudExecutor executor = null;

	public CloudThread(ComputeRun run, CloudExecutor cloudExecutor)
	{
		this.run = run;
		this.executor = cloudExecutor;
	}


	@Override
	public void run()
	{
		executor.executeRun(run);
	}
}
