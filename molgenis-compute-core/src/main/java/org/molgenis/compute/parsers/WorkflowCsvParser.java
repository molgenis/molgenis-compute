package org.molgenis.compute.parsers;

import java.io.IOException;

import org.molgenis.compute.ComputeProperties;
import org.molgenis.compute.model.impl.WorkflowImpl;

/**
 * This class reads and parses CSV files submitted as a 'workflow' into the compute {@link WorkflowImpl} object
 */
public interface WorkflowCsvParser
{
	/**
	 * Parse a csv into a {@link WorkflowImpl} class
	 * 
	 * @param workflowPath
	 * @param computeProperties
	 * @return A {@link WorkflowImpl} containing the contents of the parsed CSV
	 * @throws IOException
	 */
	public WorkflowImpl parse(String workflowPath, ComputeProperties computeProperties) throws IOException;
}
