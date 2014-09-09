package org.molgenis.compute.db.controller;

import java.util.Date;

public class RunModel
{
	private final String name;
	private final boolean running;
    private final boolean submitting;
    private final boolean complete;
	private final boolean owned;
	private final boolean cancelled;
	private final boolean vmrun;
	private final boolean hasFailed;
	private final String backendUrl;
	private final Date creationTime;
	private final String owner;

    public RunModel(String name, boolean running, boolean submitting,
					boolean complete, boolean cancelled, boolean owned,
					boolean vmrun, boolean hasFailed,
					String backendUrl, Date creationTime, String owner)
	{
		this.name = name;
		this.running = running;
        this.submitting = submitting;
        this.complete = complete;
		this.owned = owned;
		this.vmrun = vmrun;
		this.hasFailed = hasFailed;
		this.backendUrl = backendUrl;
		this.creationTime = creationTime;
		this.owner = owner;
		this.cancelled = cancelled;
	}

	public String getName()
	{
		return name;
	}

	public boolean isRunning()
	{
		return running;
	}


    public boolean isSubmitting()
    {
        return submitting;
    }

    public boolean isComplete()
    {
        return complete;
    }

	public boolean isOwned()
	{
		return owned;
	}

	public boolean isHasFailed()
	{
		return hasFailed;
	}
	public boolean isVmrun()
	{
		return vmrun;
	}

	public String getBackendUrl()
	{
		return backendUrl;
	}

	public Date getCreationTime()
	{
		return creationTime;
	}

	public String getOwner()
	{
		return owner;
	}

	public boolean isCancelled()
	{
		return cancelled;
	}
}