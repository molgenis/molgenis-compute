package org.molgenis.compute.db.api;

public class RunStatusRequest
{
	private final String runName;

	public RunStatusRequest(String runName)
	{
		this.runName = runName;
	}

	public String getRunName()
	{
		return runName;
	}

}
