package org.molgenis.compute.model;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.molgenis.compute.ComputeProperties;

/**
 * This class performs complicated folding and can set and get parameters, tell you if there are multiple parameter
 * files or can count for you how often a parameter is found in the list of existing parameters
 */
public interface FoldParameters
{
	/**
	 * This method creates {@link Parameters} objects by parsing parameter files or a github URL
	 * 
	 * @param fromFiles
	 * @param computeProperties
	 */
	public void setFromFiles(List<File> parameterFiles, ComputeProperties computeProperties);

	/**
	 * This method checks if there are multiple parameters
	 * 
	 * @return If there are multiple parameters
	 */
	public boolean isMultiParametersFiles();

	/**
	 * Method that counts how often a parameter is found
	 * 
	 * @param name
	 * @return
	 */
	public int howManyTimesParameterIsFound(String name);

	/**
	 * TODO What does this method do exactly?
	 * 
	 * @param name
	 * @param foreach
	 * @return
	 */
	public List<String> folding(String name, Hashtable<String, String> foreach);

	/**
	 * Method that returns a map of parameters
	 * 
	 * @return A map of parameters
	 */
	public List<HashMap<String, List<String>>> getParameters();
}
