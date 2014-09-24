package org.molgenis.compute.db.cloudexecutor;

import org.apache.log4j.Logger;
import org.molgenis.compute.runtime.ComputeRun;

/**
 * Created with IntelliJ IDEA.
 * User: hvbyelas
 * Date: 5/7/14
 * Time: 4:08 PM
 * To change this template use File | Settings | File Templates.
 */

public class StopperThread implements Runnable
{
	private static final Logger LOG = Logger.getLogger(StopperThread.class);

	private String runName = null;
	private ServerStarter starter = null;

	public StopperThread(ServerStarter starter, String runName)
	{
		this.starter = starter;
		this.runName = runName;
	}


	@Override
	public void run()
	{
		starter.stopServers(runName);
	}
}
