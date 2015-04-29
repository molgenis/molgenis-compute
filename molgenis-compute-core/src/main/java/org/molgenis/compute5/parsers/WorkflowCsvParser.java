package org.molgenis.compute5.parsers;

import java.io.IOException;

import org.molgenis.compute5.ComputeProperties;
import org.molgenis.compute5.model.impl.WorkflowImpl;

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
