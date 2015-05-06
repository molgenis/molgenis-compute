package org.molgenis.compute.model;

import java.util.List;
import java.util.Map;

/**
 * Loads parameter combinations from files and filters them.
 */
public interface FoldParameters
{
	/**
	 * This method checks if there are multiple parameter files
	 * 
	 * @return boolean indicating if there are multiple parameter files
	 */
	public boolean isMultiParameterFiles();

	/**
	 * Method that counts how often a parameter is found in the parameter files
	 * 
	 * @param name
	 *            name of the parameter
	 * @return number of files this parameter was found in. this value is 0 or 1
	 */
	public int numberOfFilesContainingParameter(String name);

	/**
	 * <p>
	 * Filters parameter values.
	 * </p>
	 * 
	 * <ul>
	 * <li>First this method determines which file contained the parameter with parameterName. There is only one such
	 * file.</li>
	 * <li>This parameter file contains entries that are a combination of parameter values. This method looks at each of
	 * those combinations. For each combination it determines if all of the parameter values specified in the filters
	 * are the same as those in the combination. This way it filters the combinations, discarding all combinations that
	 * do not match one or more filters.</li>
	 * <li>Then it returns all values for the parameterName in the parameter combinations that matched the filters.</li>
	 * </ul>
	 * 
	 * So, for instance, if the parameter p1 is present in a file that contains the following combinations of parameter
	 * values for parameters <code>project</code>, <code>dir</code> and <code>sample</code>:
	 * 
	 * <table>
	 * <tr>
	 * <th>project</th>
	 * <th>dir</th>
	 * <th>sample</th>
	 * </tr>
	 * <tr>
	 * <td>project1</td>
	 * <td>dir1</td>
	 * <td>sample1</td>
	 * </tr>
	 * <tr>
	 * <td>project1</td>
	 * <td>dir2</td>
	 * <td>sample2</td>
	 * </tr>
	 * <tr>
	 * <td>project1</td>
	 * <td>dir2</td>
	 * <td>sample3</td>
	 * </tr>
	 * <tr>
	 * <td>project2</td>
	 * <td>dir2</td>
	 * <td>sample4</td>
	 * </tr>
	 * </table>
	 * 
	 * and the filters specify desired values <code>dir = dir2</code> and <code>project = project2</code>, then the rows
	 * that match the filters are rows 2 and 3.
	 * 
	 * If the requested parameterName is sample, the result will be <code>["sample2", "sample3"]</code>
	 * 
	 * @param parameterName
	 *            the name of the parameter to give values for
	 * @param filters
	 *            {@link Map} mapping parameter names to desired parameter values
	 * @return {@link List} of filtered parameter values for parameter with name parameterName
	 */
	public List<String> getFilteredParameterValues(String parameterName, Map<String, String> filters);

	/**
	 * Lists the content of all parameter files.
	 * 
	 * @return a {@link List}, containing for each parameter file a {@link Map} that maps parameter name to a
	 *         {@link List} of parameter values
	 */
	public List<Map<String, List<String>>> getParameters();
}
