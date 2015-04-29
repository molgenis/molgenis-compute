package org.molgenis.compute5.model;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.molgenis.compute5.ComputeProperties;

/**
 * 
 */
public interface FoldParameters
{
	/**
	 * 
	 * @param fromFiles
	 * @param computeProperties
	 */
	public void setFromFiles(List<File> fromFiles, ComputeProperties computeProperties);
	
	/**
	 * 
	 * @return
	 */
	public boolean isMultiParametersFiles();
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	public int isParameterFindTimes(String name);
	
	/**
	 * TODO What does this method do exactly?
	 * 
	 * @param name
	 * @param foreach
	 * @return
	 */
	public List<String> folding(String name, Hashtable<String, String> foreach);
	
	
	/**
	 * 
	 * @return
	 */
	public List<HashMap<String,List<String>>> getParameters();
}
