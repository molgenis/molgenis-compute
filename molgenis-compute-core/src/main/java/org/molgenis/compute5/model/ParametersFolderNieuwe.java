package org.molgenis.compute5.model;

import au.com.bytecode.opencsv.CSVReader;
import org.molgenis.compute5.ComputeProperties;
import org.molgenis.compute5.urlreader.UrlReader;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: hvbyelas
 * Date: 3/12/14
 * Time: 4:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class ParametersFolderNieuwe
{
	//map parameter name and values
	List<HashMap> parameters = new ArrayList<HashMap>();
	private UrlReader urlReader = new UrlReader();


	public void setFromFiles(List<File> fromFiles, ComputeProperties computeProperties)
	{
		for (File file : fromFiles)
		{
			try
			{
				CSVReader reader = null;


				if(!computeProperties.isWebWorkflow)
					reader = new CSVReader(new FileReader(file));
				else
				{
					String fString = file.getName();
					File f = urlReader.createFileFromGithub(computeProperties.webWorkflowLocation, fString);
					reader = new CSVReader(new FileReader(f));
				}


				HashMap<String, List<String>> onefileParameters = new HashMap<String, List<String>>();

				List<String[]> allLines = reader.readAll();

				String[] header = allLines.get(0);

				//lets skip java properties format files
				//they do not contain lists for time being; so they will not been used in "new" folding
				if (header[0].contains("="))
					break;

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
					onefileParameters.put(head, values);
				}

				parameters.add(onefileParameters);

			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public boolean isMultiParametersFiles()
	{
		if (parameters.size() > 1)
			return true;
		return false;
	}

	public int isParameterFindTimes(String name)
	{
		int i = 0;
		for (HashMap<String, List<String>> parametersFile : parameters)
		{
			boolean hasID = parametersFile.containsKey(name);
			if (hasID)
				i++;
		}
		return i;
	}

	public List<String> foldingNieuwe(String name, Hashtable<String, String> foreach)
	{
		List<String> values = new ArrayList<String>();
		for (HashMap<String, List<String>> parametersFile : parameters)
		{
			boolean hasID = parametersFile.containsKey(name);
			if (hasID)
			{
				//this is the parameter file that we are looking for
				Hashtable<String, String> neededForeach = new Hashtable<String, String>();

				Enumeration keys = foreach.keys();
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
					//now, we have values on which we will fold
					//we can create table
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


					Enumeration neededKeys = neededForeach.keys();
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

					//now we start all the folding magic
					for (int i = 1; i <= parametersFile.get(name).size(); i++)
					{
						boolean stillGood = true;
						Enumeration k = neededForeach.keys();
						for (int j = 1; j <= neededForeach.size(); j++)
						{
							String key = (String) k.nextElement();

							String v1 = foreach.get(key);
							String v2 = table[j][i];
							if (!v1.equalsIgnoreCase(v2))
							{
								stillGood = false;
								//values.add(table[0][i]);
							}
						}

						if(stillGood)
							values.add(table[0][i]);
					}
				}
				else
				{
					//we are lucky this time
					//we can take all parameters
					values = parametersFile.get(name);
				}
			}
		}
		return values;
	}
}
