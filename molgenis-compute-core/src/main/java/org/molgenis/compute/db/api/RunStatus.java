package org.molgenis.compute.db.api;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class RunStatus
{
	private final int generated;
	private final int ready;
	private final int running;
	private final int failed;
	private final int done;
	private final int cancelled;
	private final int submitted;
	private final int started;
	private final int jobsubmitted;

	private final boolean isComplete;

	public RunStatus(int generated, int ready, int running, int failed, int done, int cancelled, int jobsubmitted,
			int submitted, int started, boolean isComplete)
	{
		this.generated = generated;
		this.ready = ready;
		this.running = running;
		this.done = done;
		this.failed = failed;
		this.cancelled = cancelled;
		this.jobsubmitted = jobsubmitted;
		this.submitted = submitted;
		this.started = started;
		this.isComplete = isComplete;
	}

	public int getGenerated()
	{
		return generated;
	}

	public int getReady()
	{
		return ready;
	}

	public int getRunning()
	{
		return running;
	}

	public int getDone()
	{
		return done;
	}

	public int getFailed()
	{
		return failed;
	}

	public int getCancelled()
	{
		return cancelled;
	}

	public int getSubmitted()
	{
		return submitted;
	}

	public int getJobsubmitted()
	{
		return jobsubmitted;
	}

	public int getStarted()
	{
		return started;
	}

	public boolean isComplete()
	{
		return isComplete;
	}

	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this);
	}

}
