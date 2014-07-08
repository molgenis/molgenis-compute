package org.molgenis.compute.db.cloudexecutor;

import org.molgenis.compute5.db.api.Backend;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
/**
 * Created with IntelliJ IDEA.
 * User: hvbyelas
 * Date: 5/7/14
 * Time: 1:58 PM
 * To change this template use File | Settings | File Templates.
 */

public class CloudServer
{
	private static final Logger LOG = Logger.getLogger(CloudServer.class);


	private Backend backend = null;

	private String id;
	private String floatingIpExtern;
	private String floatingIpTarget;

	private String fixedIpExtern;
	private String fixedIpTarget;

	private String hostName;
	private int currentJobID;
	private boolean isInUse = false;
	private List<String> finishedJobs = new ArrayList<String>();

	public String getFloatingIpTarget()
	{
		return floatingIpTarget;
	}

	public void setFloatingIpTarget(String floatingIpTarget)
	{
		this.floatingIpTarget = floatingIpTarget;
	}

	public String getFixedIpExtern()
	{
		return fixedIpExtern;
	}

	public void setFixedIpExtern(String fixedIpExtern)
	{
		this.fixedIpExtern = fixedIpExtern;
	}

	public String getFixedIpTarget()
	{
		return fixedIpTarget;
	}

	public void setFixedIpTarget(String fixedIpTarget)
	{
		this.fixedIpTarget = fixedIpTarget;
	}

	public void setCurrentJobID(int currentJobID)
	{
		this.currentJobID = currentJobID;
	}

	public List<String> getFinishedJobs()
	{
		return finishedJobs;
	}

	public void setFinishedJobs(List<String> finishedJobs)
	{
		this.finishedJobs = finishedJobs;
	}

	public Backend getBackend()
	{
		return backend;
	}

	public void setBackend(Backend backend)
	{
		this.backend = backend;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getFloatingIpExtern()
	{
		return floatingIpExtern;
	}

	public void setFloatingIpExtern(String floatingIpExtern)
	{
		this.floatingIpExtern = floatingIpExtern;
	}

	public String getHostName()
	{
		return hostName;
	}

	public void setHostName(String hostName)
	{
		this.hostName = hostName;
	}

	public int getCurrentJobID()
	{
		return currentJobID;
	}

	public void setCurrentJobID(Integer currentJobID)
	{
		this.currentJobID = currentJobID;
	}

	public boolean isInUse()
	{
		return isInUse;
	}

	public void setInUse(boolean isInUse)
	{
		this.isInUse = isInUse;
	}

	public void addFinishedJob(String jobID)
	{
		if(currentJobID == Integer.parseInt(jobID))
		{
			finishedJobs.add(currentJobID + "");
			currentJobID = -1;
		}
		else
		{
			LOG.error("Job IDs are different [ " + currentJobID + " & " + jobID + " ] for server [ " + id + " ] " );
		}
	}
}
