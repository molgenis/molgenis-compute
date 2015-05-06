package org.molgenis.compute.sysexecutor;

public interface SystemCommandExecutor
{
	public void setWorkingDirectory(String workingDirectory);
	
	public void setEnvironmentVar(String name, String value);
	 
	public String getCommandOutput();
	 
	public String getCommandError();
	 
	public int runCommand(String commandLine) throws Exception;
}
