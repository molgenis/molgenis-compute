package org.molgenis.compute.generators.impl;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.compute.model.Parameters;
import org.molgenis.compute.model.StringStore;
import org.molgenis.compute.model.Task;
import org.molgenis.compute.model.impl.DataEntity;

import com.google.common.collect.Iterables;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Collapse tuples on targets
 * 
 * @param parameters
 * @param targets
 * @return
 */
public class TupleUtils
{
	private String runID = null;
	private HashMap<String, String> parametersToOverwrite = null;
	private final StringStore stringStore;

	public TupleUtils(StringStore stringStore)
	{
		this.stringStore = stringStore;
	}

	/**
	 * 
	 * @param localParameters
	 * @param targets
	 * @return A list of {@link DataEntity}
	 */
	public static List<DataEntity> collapse(List<DataEntity> localParameters, List<String> targets)
	{
		Map<String, DataEntity> result = new LinkedHashMap<String, DataEntity>();
		for (DataEntity parameter : localParameters)
		{
			// generate key
			String key = generateKeyFromTargets(targets, parameter);

			// Create tuple if the key is not present, create lists for non-targets
			if (result.get(key) == null)
			{
				DataEntity collapsedRow = new DataEntity();
				for (String attribute : parameter.getAttributeNames())
				{
					if (targets.contains(attribute))
					{
						collapsedRow.set(attribute, parameter.get(attribute));
					}
					else
					{
						List<Object> list = new ArrayList<Object>();
						list.add(parameter.get(attribute));
						collapsedRow.set(attribute, list);
					}
				}
				result.put(key, collapsedRow);
			}
			else
			{
				for (String attribute : parameter.getAttributeNames())
				{
					if (!targets.contains(attribute))
					{
						@SuppressWarnings("unchecked")
						List<String> list = (List<String>) result.get(key).get(attribute);
						list.add(parameter.getString(attribute));
						result.get(key).set(attribute, list);
					}
				}
			}
		}

		return new ArrayList<DataEntity>(result.values());
	}

	/**
	 * Generates a key based on the values of the parameter map
	 * 
	 * @param targets
	 * @param parameter
	 * @return The generated key
	 */
	private static String generateKeyFromTargets(List<String> targets, DataEntity parameter)
	{
		String key = "";
		for (String target : targets)
		{
			key += parameter.getString(target) + "_";
		}
		return key;
	}

	/**
	 * Tuples can have values that are freemarker templates, e.g. ${other column}. This method will solve that
	 * 
	 * @throws IOException
	 * @throws TemplateException
	 */
	public void solve(List<DataEntity> parameterValues) throws IOException
	{
		// Freemarker configuration
		Configuration freeMarkerConfiguration = new Configuration();
		Template template;

		replaceParameters(parameterValues);

		// For every Parameter value
		for (DataEntity parameterValue : parameterValues)
		{
			// For every attribute within this parameterValue map
			for (String attribute : parameterValue.getAttributeNames())
			{
				// Store the original
				String original = parameterValue.getString(attribute);

				// If the original contains freemarker syntax
				if (original.contains("${"))
				{
					// Check for self reference (??)
					if (original.contains("${" + attribute + "}"))
					{
						throw new IOException("could not solve " + attribute + "='" + original
								+ "' because template references to self");
					}

					// Create a new template for every attribute. Very expensive!!!
					// TODO can we reuse the same template?
					try
					{
						template = freeMarkerConfiguration.getTemplate(attribute);
					}
					catch (IOException e)
					{
						template = new Template(attribute, new StringReader(original), freeMarkerConfiguration);
					}

					StringWriter writer = new StringWriter();
					try
					{
						Map<String, Object> map = toMap(parameterValue);

						// ??
						map.put("runid", runID);

						// Reads the created template, and writes it to a String object.
						template.process(map, writer);
						String value = writer.toString();

						// If the generated template is not the same as it was originally
						if (!value.equals(original))
						{
							parameterValue.set(attribute, stringStore.intern(value));
						}
					}
					catch (Exception e)
					{
						throw new IOException(
								"could not solve " + attribute + "='" + original + "': " + e.getMessage() + "\n");
					}
				}
			}
		}
	}

	/**
	 * Replaces parameters
	 * 
	 * @param map
	 */
	private void replaceParameters(List<DataEntity> map)
	{
		if (parametersToOverwrite != null)
		{
			for (Map.Entry<String, String> entry : parametersToOverwrite.entrySet())
			{
				String key = entry.getKey();
				String value = entry.getValue();
				for (DataEntity tuple : map)
				{
					tuple.set(key, stringStore.intern(value));
				}
			}
		}
	}

	/**
	 * Convert a tuple into a map. Columns with a '_' in them will be nested submaps.
	 * 
	 * @param target
	 * @return A {@link Map} of String Object key value pairs
	 */
	public static Map<String, Object> toMap(DataEntity target)
	{
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		for (String attribute : target.getAttributeNames())
		{
			result.put(attribute, target.get(attribute));
		}
		return result;
	}

	/**
	 * Uncollapse a tuple using an idColumn
	 * 
	 * @param collapsedEntities
	 * @param idColumn
	 * @return
	 */
	public static List<DataEntity> uncollapse(List<DataEntity> collapsedEntities)
	{
		List<DataEntity> result = new ArrayList<DataEntity>();

		for (DataEntity collapsedEntity : collapsedEntities)
		{
			for (int i = 0; i < collapsedEntity.getList(Parameters.ID_COLUMN).size(); i++)
			{
				DataEntity copy = new DataEntity();
				for (String attribute : Iterables.filter(collapsedEntity.getAttributeNames(),
						TupleUtils::attributesToPreserve))
				{

					if (collapsedEntity.get(attribute) instanceof List)
					{
						copy.set(attribute, collapsedEntity.getList(attribute).get(i));
					}
					else
					{
						copy.set(attribute, collapsedEntity.get(attribute));
					}
				}
				result.add(copy);
			}

		}

		return result;
	}

	private static boolean attributesToPreserve(String attributeName)
	{
		return attributeName.startsWith(Parameters.USER_PREFIX) || attributeName.endsWith(Task.TASKID_COLUMN)
				|| attributeName.endsWith(Task.TASKID_INDEX_COLUMN);
	}

	public void setRunID(String runID)
	{
		this.runID = runID;
	}

	public void setParametersToOverwrite(HashMap<String, String> parametersToOverwrite)
	{
		this.parametersToOverwrite = parametersToOverwrite;
	}
}
