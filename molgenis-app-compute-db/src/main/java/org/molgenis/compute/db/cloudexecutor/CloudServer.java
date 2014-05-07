package org.molgenis.compute.db.cloudexecutor;

import org.molgenis.compute5.db.api.Backend;

/**
 * Created with IntelliJ IDEA.
 * User: hvbyelas
 * Date: 5/7/14
 * Time: 1:58 PM
 * To change this template use File | Settings | File Templates.
 */

public class CloudServer
{

	private Backend backend = null;

	private String id;
	private String externalIP;
	private String hostName;
	private String currentJobID;
	private boolean isInUse = false;

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

	public String getExternalIP()
	{
		return externalIP;
	}

	public void setExternalIP(String externalIP)
	{
		this.externalIP = externalIP;
	}

	public String getHostName()
	{
		return hostName;
	}

	public void setHostName(String hostName)
	{
		this.hostName = hostName;
	}

	public String getCurrentJobID()
	{
		return currentJobID;
	}

	public void setCurrentJobID(String currentJobID)
	{
		this.currentJobID = currentJobID;
	}
}
