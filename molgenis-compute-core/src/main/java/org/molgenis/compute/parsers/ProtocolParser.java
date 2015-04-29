package org.molgenis.compute.parsers;

import java.io.File;
import java.io.IOException;

import org.molgenis.compute.ComputeProperties;
import org.molgenis.compute.model.Protocol;

/**
 * This class uses the workflow directory to parse protocols
 * 
 * ???
 *
 */
public interface ProtocolParser
{
	/**
	 * Used as primary search path. If missing it uses the runtime path or absolute path
	 *
	 * @param workflowDirectory
	 * @param protocolPath
	 * @param computeProperties
	 * @return
	 * @throws IOException
	 */
	public Protocol parse(File workflowDirectory, String protocolPath, ComputeProperties computeProperties)
			throws IOException;
}
