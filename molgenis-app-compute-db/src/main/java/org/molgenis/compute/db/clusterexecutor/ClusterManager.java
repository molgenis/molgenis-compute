package org.molgenis.compute.db.clusterexecutor;

import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Hashtable;

/**
 * Created by hvbyelas on 10/15/14.
 */
public class ClusterManager
{
	@Autowired
	private ClusterExecutor clusterExecutor;

	private Hashtable<String, Pair<String, String>> runsToUsers = new Hashtable<String, Pair<String, String>>();

	public void executeRun(ComputeRun run, String username, String password, SecurityContext ctx)
	{
		runsToUsers.put(run.getName(), new Pair(username, password));

		SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
		(new Thread(new ClusterThread(clusterExecutor, run, username, password, ClusterThread.SUBMIT, ctx))).start();
	}

	public void cancelRunJobs(ComputeRun run)
	{
		Pair<String, String> userPass = runsToUsers.get(run.getName());
		(new Thread(new ClusterThread(clusterExecutor, run, userPass.getA(),
									userPass.getB(), ClusterThread.CANCEL, null))).start();
	}
}
