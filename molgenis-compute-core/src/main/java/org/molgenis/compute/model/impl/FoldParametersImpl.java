package org.molgenis.compute.model.impl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.molgenis.compute.ComputeProperties;
import org.molgenis.compute.model.FoldParameters;
import org.molgenis.compute.urlreader.impl.UrlReaderImpl;

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Reads parameters from files and filters the combinations.
 */
public class FoldParametersImpl implements FoldParameters
{
	// Lists the parameter combinations found in each file
	// Each element in the list maps parameter name to a list of values
	private final List<Map<String, List<String>>> parameterCombinationsInFiles;

	/**
	 * Creates a new instance of {@link FoldParametersImpl}.
	 * 
	 * @param parameterFiles
	 *            files to load the parameters from
	 * @param computeProperties
	 *            {@link ComputeProperties} to find out if the files need to be retrieved from the file system or from
	 *            the web
	 */
	public FoldParametersImpl(List<File> parameterFiles, ComputeProperties computeProperties)
	{
		parameterCombinationsInFiles = new ArrayList<Map<String, List<String>>>();
		for (File parameterFile : parameterFiles)
		{
			tryReadParameterCombinationsFromFile(parameterFile, computeProperties);
		}
	}

	/**
	 * Constructor for tests
	 * 
	 * @param parameterCombinations
	 *            {@link List} containing parameters per file
	 */
	public FoldParametersImpl(List<Map<String, List<String>>> parameterCombinations)
	{
		this.parameterCombinationsInFiles = parameterCombinations;
	}

	/**
	 * Tries to read the property combinations from a parameter file.
	 * 
	 * @param parameterFile
	 *            the file to read from
	 * @param computeProperties
	 *            {@link ComputeProperties} to find out if the files need to be retrieved from the file system or from
	 *            the web
	 */
	private void tryReadParameterCombinationsFromFile(File parameterFile, ComputeProperties computeProperties)
	{
		CSVReader reader = null;
		try
		{
			reader = new CSVReader(new FileReader(retrieveFile(computeProperties, parameterFile)));
			Map<String, List<String>> parameterCombinationsInFile = readParameterCombinations(reader);
			parameterCombinationsInFiles.add(parameterCombinationsInFile);
		}
		catch (ParameterOccursInMultipleFilesException e)
		{
			e.setFileName(parameterFile.getName());
			throw e;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (reader != null)
				{
					reader.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	private File retrieveFile(ComputeProperties computeProperties, File parameterFile)
	{
		File result = parameterFile;
		if (computeProperties.isWebWorkflow)
		{
			String fileName = parameterFile.getName();
			File githubWorkflowFile = new UrlReaderImpl().createFileFromGithub(computeProperties.webWorkflowLocation,
					fileName);
			result = githubWorkflowFile;
		}
		return result;
	}

	/**
	 * Reads parameter combinations from a {@link CSVReader}.
	 * 
	 * @param reader
	 *            {@link CSVReader} for the file
	 * @return {@link Map} mapping parameter name to a list of parameter values
	 * @throws IOException
	 *             if reading from file fails
	 */
	private Map<String, List<String>> readParameterCombinations(CSVReader reader) throws IOException
	{
		List<String[]> allLines = reader.readAll();
		if (isPropertyFileFormat(allLines))
		{
			return parsePropertiesFormat(allLines);
		}
		else
		{
			return parseTableFormat(allLines);
		}
	}

	/**
	 * Determines the format of the parameter combination file.
	 * 
	 * @param allLines
	 *            the lines in
	 * @return boolean indicating if the file is in property file format
	 */
	private boolean isPropertyFileFormat(List<String[]> allLines)
	{
		String[] header = allLines.get(0);
		return header[0].contains("=");
	}

	/**
	 * Parses a file containing parameter combinations written in the table format.
	 * 
	 * Example:
	 * 
	 * <pre>
	 * project, dir, sample
	 * project1, dir1, sample1
	 * project1, dir2, sample2
	 * project1, dir2, sample3
	 * project2, dir2, sample4
	 * </pre>
	 * 
	 * @param allLines
	 *            the file lines
	 * @return Map mapping paramName to {@link List}s of paramValues for that parameter
	 */
	private Map<String, List<String>> parseTableFormat(List<String[]> allLines)
	{
		Map<String, List<String>> result = new LinkedHashMap<>();
		String[] header = allLines.get(0);
		for (int columnIndex = 0; columnIndex < header.length; columnIndex++)
		{
			String columnName = header[columnIndex];
			List<String> columnValues = new ArrayList<String>();
			for (int lineIndex = 1; lineIndex < allLines.size(); lineIndex++)
			{
				String str = allLines.get(lineIndex)[columnIndex];
				columnValues.add(str);
			}
			assertParameterIsNotYetRead(columnName);
			result.put(columnName, columnValues);
		}
		return result;
	}

	/**
	 * Parses a file containing parameter combinations written in the property format.
	 * 
	 * Example
	 * 
	 * <pre>
	 * project=project1,project1,project1,project2
	 * dir=dir1,dir2,dir2,dir2
	 * sample=sample1,sample2,sample3,sample4
	 * </pre>
	 * 
	 * @param allLines
	 *            file lines
	 * @return Map mapping parameter name to a {@link List} containing the values
	 */
	private Map<String, List<String>> parsePropertiesFormat(List<String[]> allLines)
	{
		Map<String, List<String>> result = new LinkedHashMap<>();
		// properties file
		for (String[] line : allLines)
		{
			if (line.length > 0)
			{
				List<String> values = new ArrayList<String>();
				int eq = line[0].indexOf("=");
				String name = line[0].substring(0, eq);
				String value = line[0].substring(eq + 1);

				values.add(value);

				for (int i = 1; i < line.length; i++)
					values.add(line[i].trim());

				assertParameterIsNotYetRead(name);
				result.put(name, values);
			}
		}
		return result;
	}

	@Override
	public boolean isMultiParameterFiles()
	{
		return parameterCombinationsInFiles.size() > 1;
	}

	@Override
	public int numberOfFilesContainingParameter(final String name)
	{
		return Collections2.filter(parameterCombinationsInFiles, new Predicate<Map<String, List<String>>>()
		{
			@Override
			public boolean apply(Map<String, List<String>> parametersFile)
			{
				return parametersFile.containsKey(name);
			}
		}).size();
	}

	@Override
	public List<String> getFilteredParameterValues(String parameterName, Map<String, String> filters)
	{
		for (Map<String, List<String>> parametersInFile : parameterCombinationsInFiles)
		{
			if (parametersInFile.containsKey(parameterName))
			{
				// this is the parameter file that we are looking for
				return getFilteredParameterValues(parameterName, filters, parametersInFile);
			}
		}
		return new ArrayList<String>();
	}

	/**
	 * Filters parameter value combinations.
	 * 
	 * @param parameterName
	 *            name of the parameter to give the filtered values for
	 * @param filters
	 *            the filters to apply, as a {@link Map} that maps parameter name to desired parameter value
	 * @param parametersInFile
	 *            all parameters in the file, as a {@link Map} that maps the parameter name to a list of parameter
	 *            values
	 * @return {@link List} of filtered values for parameter with parameterName
	 */
	private List<String> getFilteredParameterValues(String parameterName, Map<String, String> filters,
			Map<String, List<String>> parametersInFile)
	{
		List<String> result = Lists.newArrayList();
		List<String> resultCandidates = parametersInFile.get(parameterName);
		filters = relevantFilters(filters, parametersInFile.keySet());

		for (int parameterValueIndex = 0; parameterValueIndex < resultCandidates.size(); parameterValueIndex++)
		{
			Map<String, String> parameterCombination = getParameterCombination(parametersInFile, parameterValueIndex);
			if (allFiltersMatch(filters, parameterCombination))
			{
				result.add(resultCandidates.get(parameterValueIndex));
			}
		}
		return result;
	}

	/**
	 * Gets the parameter combination for all parameters in a file, for a certain index.
	 * 
	 * @param parametersInFile
	 *            the parameter combinations in the file
	 * @param index
	 *            the index of the parameter combination in the file
	 * @return {@link Map} mapping parameter name to parameter value
	 */
	private static Map<String, String> getParameterCombination(Map<String, List<String>> parametersInFile, int index)
	{
		Map<String, String> result = new LinkedHashMap<>();
		for (Entry<String, List<String>> param : parametersInFile.entrySet())
		{
			result.put(param.getKey(), param.getValue().get(index));
		}
		return result;
	}

	/**
	 * Determines if a certain parameter combination matches a set of filters.
	 * 
	 * @param filters
	 *            the filters that should match, as a {@link Map} mapping parameter name to desired parameter value
	 * @param parameterCombination
	 *            the parameter combination to check
	 * @return boolean indicating if the parameter combination matches the filters or not
	 */
	private boolean allFiltersMatch(Map<String, String> filters, Map<String, String> parameterCombination)
	{
		return Maps.difference(parameterCombination, filters).entriesDiffering().isEmpty();
	}

	/**
	 * Determines which of a certain set of filters is relevant for a certain set of parameter names.
	 * 
	 * @param filters
	 *            the full list of filters, as a {@link Map} that maps parameter name to filtered parameter value
	 * @param relevantNames
	 *            the set of relevant parameter names
	 * @return only those filters that filter one of the relevant parameters, as a {@link Map} that maps parameter name
	 *         to filtered parameter value
	 */
	private Map<String, String> relevantFilters(Map<String, String> filters, final Set<String> relevantNames)
	{
		return Maps.filterKeys(filters, new Predicate<String>()
		{
			@Override
			public boolean apply(String key)
			{
				return relevantNames.contains(key);
			}

		});
	}

	@Override
	public List<Map<String, List<String>>> getParameters()
	{
		return parameterCombinationsInFiles;
	}

	/**
	 * Asserts that a particular parameter has not yet been read into {@link #parameterCombinationsInFiles}.
	 * 
	 * @param parameterName
	 *            name of the parameter
	 */
	private void assertParameterIsNotYetRead(String parameterName)
	{
		if (numberOfFilesContainingParameter(parameterName) > 0)
		{
			throw new ParameterOccursInMultipleFilesException(parameterName);
		}
	}
}
