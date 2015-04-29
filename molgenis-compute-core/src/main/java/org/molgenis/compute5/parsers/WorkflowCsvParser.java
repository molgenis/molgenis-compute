package org.molgenis.compute5.parsers;

import java.io.IOException;

import org.molgenis.compute5.ComputeProperties;
import org.molgenis.compute5.model.Workflow;

/**
 * This class reads and parses CSV files submitted as a 'workflow' into the compute {@link Workflow} object
 */
public interface WorkflowCsvParser
{
	/**
	 * Parse a csv into a {@link Workflow} class
	 * 
	 * @param workflowPath
	 * @param computeProperties
	 * @return A {@link Workflow} containing the contents of the parsed CSV
	 * @throws IOException
	 */
	public Workflow parse(String workflowPath, ComputeProperties computeProperties) throws IOException;
}
