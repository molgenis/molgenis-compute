package org.molgenis.compute.db.api;

public class RunStatusResponse extends ApiResponse
{
	private RunStatus runStatus;

	public RunStatusResponse()
	{
	}

	public RunStatus getRunStatus()
	{
		return runStatus;
	}

	public void setRunStatus(RunStatus runStatus)
	{
		this.runStatus = runStatus;
	}

}
