package org.molgenis.compute.model.impl;

import au.com.bytecode.opencsv.CSVReader;

import org.molgenis.compute.ComputeProperties;
import org.molgenis.compute.model.FoldParameters;
import org.molgenis.compute.urlreader.impl.UrlReaderImpl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class FoldParametersImpl implements FoldParameters
{
	// map parameter name and values
	List<HashMap<String, List<String>>> parameters = new ArrayList<HashMap<String, List<String>>>();
	private UrlReaderImpl urlReaderImpl = new UrlReaderImpl();

	@Override
	public void setFromFiles(List<File> parameterFiles, ComputeProperties computeProperties)
	{
		for (File parameterFile : parameterFiles)
		{
			CSVReader reader = null;
			try
			{
				if (!computeProperties.isWebWorkflow)
				{
					reader = new CSVReader(new FileReader(parameterFile));
				}
				else
				{
					String fileName = parameterFile.getName();
					File githubWorkflowFile = urlReaderImpl.createFileFromGithub(computeProperties.webWorkflowLocation,
							fileName);
					reader = new CSVReader(new FileReader(githubWorkflowFile));
				}

				HashMap<String, List<String>> onefileParameters = new HashMap<String, List<String>>();

				List<String[]> allLines = reader.readAll();

				String[] header = allLines.get(0);

				if (header[0].contains("="))
				{
					// properties file
					for (String[] array : allLines)
					{
						if (array.length > 0)
						{
							List<String> values = new ArrayList<String>();
							int eq = array[0].indexOf("=");
							String name = array[0].substring(0, eq);
							String value = array[0].substring(eq + 1);

							values.add(value);

							for (int i = 1; i < array.length; i++)
								values.add(array[i].trim());

							checkIfParameterExists(name, parameterFile);
							onefileParameters.put(name, values);
						}
					}
				}
				else
				{
					String[][] table = new String[header.length][allLines.size()];

					for (int j = 0; j < allLines.size(); j++)
					{
						String[] line = allLines.get(j);
						for (int i = 0; i < header.length; i++)
						{
							table[i][j] = line[i];
						}
					}

					for (int j = 0; j < header.length; j++)
					{
						String head = table[j][0];
						List<String> values = new ArrayList<String>();
						for (int i = 1; i < allLines.size(); i++)
						{
							String str = table[j][i];
							values.add(str);
						}

						// first check if this parameters already exist
						checkIfParameterExists(head, parameterFile);

						onefileParameters.put(head, values);
					}

					parameters.add(onefileParameters);
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					reader.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public boolean isMultiParametersFiles()
	{
		if (parameters.size() > 1)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public int howManyTimesParameterIsFound(String name)
	{
		int numberOfTimesParameterIsFound = 0;
		for (HashMap<String, List<String>> parametersFile : parameters)
		{
			if (parametersFile.containsKey(name))
			{
				numberOfTimesParameterIsFound++;
			}
		}
		return numberOfTimesParameterIsFound;
	}

	@Override
	public List<String> folding(String name, Hashtable<String, String> foreach)
	{
		List<String> values = new ArrayList<String>();
		for (HashMap<String, List<String>> parametersFile : parameters)
		{
			boolean hasID = parametersFile.containsKey(name);
			if (hasID)
			{
				// this is the parameter file that we are looking for
				Hashtable<String, String> neededForeach = new Hashtable<String, String>();

				Enumeration<String> keys = foreach.keys();
				while (keys.hasMoreElements())
				{
					String key = (String) keys.nextElement();
					String value = foreach.get(key);

					boolean hasNeededID = parametersFile.containsKey(key);
					if (hasNeededID)
					{
						neededForeach.put(key, value);
					}
				}

				if (neededForeach.size() > 0)
				{
					// now, we have values on which we will fold
					// we can create table
					// + 1 for table header
					int x = parametersFile.get(name).size() + 1;
					// + 1 for name
					int y = neededForeach.size() + 1;
					String[][] table = new String[y][x];

					table[0][0] = name;
					for (int j = 0; j < parametersFile.get(name).size(); j++)
					{
						String value = parametersFile.get(name).get(j);
						table[0][j + 1] = value;
					}

					Enumeration<String> neededKeys = neededForeach.keys();
					for (int i = 1; i <= neededForeach.size(); i++)
					{
						String key = (String) neededKeys.nextElement();
						table[i][0] = key;
						for (int j = 0; j < parametersFile.get(key).size(); j++)
						{
							String value = parametersFile.get(key).get(j);
							table[i][j + 1] = value;
						}

					}

					// now we start all the folding magic
					// This should not be magic!
					for (int i = 1; i <= parametersFile.get(name).size(); i++)
					{
						boolean stillGood = true;
						Enumeration<String> k = neededForeach.keys();
						for (int j = 1; j <= neededForeach.size(); j++)
						{
							String key = (String) k.nextElement();

							String v1 = foreach.get(key);
							String v2 = table[j][i];
							if (!v1.equalsIgnoreCase(v2))
							{
								stillGood = false;
							}
						}

						if (stillGood)
						{
							// if(!values.contains(table[0][i]))
							values.add(table[0][i]);
						}
					}
				}
				else
				{
					// we can take all parameters
					values = parametersFile.get(name);
				}
			}
		}
		return values;
	}

	@Override
	public List<HashMap<String, List<String>>> getParameters()
	{
		return parameters;
	}

	private void checkIfParameterExists(String head, File file)
	{
		for (HashMap<String, List<String>> oneFile : parameters)
		{
			for (String key : oneFile.keySet())
			{
				if (key.equals(head))
				{
					throw new RuntimeException("Parameter is doubled. [ " + head + " ] is present in [ "
							+ file.getName() + "] and one another parameters file");
				}
			}
		}
	}
}
