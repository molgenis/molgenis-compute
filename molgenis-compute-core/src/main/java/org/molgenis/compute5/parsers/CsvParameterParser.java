package org.molgenis.compute5.parsers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.molgenis.compute5.ComputeProperties;
import org.molgenis.compute5.model.Parameters;

/**
 * Parser for csv file parameters. Includes the solving of templated values.
 */
public interface CsvParameterParser
{
	/**
	 * Parse CSV files into {@link Parameters}
	 * 
	 * @param files
	 * @param computeProperties
	 * @return A list of {@link Parameters}
	 * @throws IOException
	 */
	public Parameters parse(List<File> files, ComputeProperties computeProperties) throws IOException;

	/**
	 * Parse paramFileSet into Parameters targets.
	 * 
	 * @param targets
	 *            contains Parameters after parsing paramFileSet
	 * @param paramFileSet
	 *            Set of parameter files to parse
	 * @return
	 * @throws IOException
	 */
	public Parameters parseParamFiles(Parameters targets, Set<String> paramFileSet) throws IOException;

	/**
	 * Set the run ID
	 * 
	 * @param runID
	 */
	public void setRunID(String runID);

	/**
	 * Overwrite existing parameters with the supplied parameters
	 * 
	 * @param parametersToOverwrite
	 */
	public void setParametersToOverwrite(HashMap<String, String> parametersToOverwrite);
}
