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

	private ComputeRun run = null;
	private ServerStarter starter = null;

	public StopperThread(ServerStarter starter)
	{
		this.starter = starter;
	}


	@Override
	public void run()
	{
		starter.stopServers();
	}
}
