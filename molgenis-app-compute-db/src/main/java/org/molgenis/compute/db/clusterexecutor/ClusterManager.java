package org.molgenis.compute.db.clusterexecutor;

import org.molgenis.compute.db.cloudexecutor.CloudManager;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Properties;

/**
 * Created by hvbyelas on 10/15/14.
 */
public class ClusterManager
{
	@Autowired
	private ClusterExecutor clusterExecutor;

	private Hashtable<String, Pair<String, String>> runsToUsers = new Hashtable<String, Pair<String, String>>();
	private String defUser;
	private String defPass;

	public void executeRun(ComputeRun run, String username, String password, SecurityContext ctx)
	{
		runsToUsers.put(run.getName(), new Pair(username, password));

		SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
		(new Thread(new ClusterThread(clusterExecutor, run, username, password, ClusterThread.SUBMIT, ctx))).start();
	}

	public void cancelRunJobs(ComputeRun run, SecurityContext ctx)
	{
		Pair<String, String> userPass = runsToUsers.get(run.getName());
		if(userPass != null)
			(new Thread(new ClusterThread(clusterExecutor, run, userPass.getA(),
									userPass.getB(), ClusterThread.CANCEL, ctx))).start();
		else
		{
			//read default from parameters file
			readUserProperties();
			(new Thread(new ClusterThread(clusterExecutor, run, defUser,
					defPass, ClusterThread.CANCEL, ctx))).start();
		}
	}

	private void readUserProperties()
	{
		Properties prop = new Properties();
		InputStream input = null;

		try
		{
			input = new FileInputStream(".cluster.properties");

			// load a properties file
			prop.load(input);

			defUser = prop.getProperty(CloudManager.KEYPASS);
			defPass = prop.getProperty(CloudManager.SERVER_USERNAME);
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
	}

}
