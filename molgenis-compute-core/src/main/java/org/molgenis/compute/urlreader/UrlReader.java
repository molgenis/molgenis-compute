package org.molgenis.compute.urlreader;

import java.io.File;

import org.molgenis.compute.generators.impl.BackendGeneratorImpl;
import org.molgenis.compute.model.impl.FoldParametersImpl;
import org.molgenis.compute.parsers.impl.ProtocolParserImpl;

/**
 * Class that handles the reading of URL's and writes URL content to files
 * 
 * Called by:
 * 
 * {@link BackendGeneratorImpl} constructor
 * 
 * {@link ProtocolParserImpl} parse(File, String, ComputeProperties) method
 * 
 * {@link WorkflowCsvParserImpl} parse(String, ComputeProperties) method
 * 
 * {@link CsvParameterParserImpl} parseParamFiles(Parameters, Set<String>) method
 * 
 * {@link FoldParametersImpl} setFromFiles(List<File>, ComputeProperties) method
 */
public interface UrlReader
{
	/**
	 * Reads output from a github URL as text and writes it to a file
	 * 
	 * @param root
	 * @param filename
	 * 
	 * @return The location of the temporary file that was created with the contents of the submitted github filename
	 */
	public File createFileFromGithub(String root, String filename);
}
