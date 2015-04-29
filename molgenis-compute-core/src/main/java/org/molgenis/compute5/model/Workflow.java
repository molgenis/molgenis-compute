package org.molgenis.compute5.model;

import java.util.List;
import java.util.Set;

/**
 * This class allows to get {@link Step}s, get user parameters, add {@link Step}s, retrieve a {@link Step} based on the
 * name of the previous {@link Step}, or to check if a {@link Parameters} has a {@link Step} prefix
 */
public interface Workflow
{
	/**
	 * This method returns a {@link Set} of user defined parameters based on the parameter mapping that is present for
	 * every step
	 * 
	 * @return A {@link Set} of user parameters
	 */
	public Set<String> getUserParameters();

	/**
	 * This method gets the list of {@link Step}s
	 * 
	 * @return
	 */
	public List<Step> getSteps();

	/**
	 * This method adds a {@link Step} to the globally set list of {@link Step}s
	 * 
	 * @param step
	 */
	public void addStep(Step step);

	/**
	 * This method returns a {@link Step} object based on a provided step name
	 * 
	 * @param stepName
	 * @return A step based on the supplied step name
	 */
	public Step getStep(String stepName);

	/**
	 * This method checks to see if a given parameter contains a step prefix
	 * 
	 * @param parameter
	 * @return A boolean that tells you if a parameter has a step prefix
	 */
	public boolean parameterHasStepPrefix(String parameter);

}
