package org.molgenis.compute5.generators;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

import org.molgenis.compute5.model.Parameters;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;


public class TupleUtils
{
	/**
	 * Collapse tuples on targets
	 * 
	 * @param parameters
	 * @param targets
	 * @return
	 */
	private String runID = null;
	private HashMap<String, String> parametersToOverwrite = null;

	public static List<MapEntity> collapse(List<? extends Entity> parameters, List<String> targets)
	{
		Map<String, MapEntity> result = new LinkedHashMap<String, MapEntity>();

		for (Entity row : parameters)
		{
			// generate key
			String key = "";
			for (String target : targets)
				key += row.getString(target) + "_";

			// if first, create tuple, create lists for non-targets
			if (result.get(key) == null)
			{
				MapEntity collapsedRow = new MapEntity();
				for (String col : row.getAttributeNames())
				{
					if (targets.contains(col))
					{
						collapsedRow.set(col, row.get(col));
					}
					else
					{
						List<Object> list = new ArrayList<Object>();
						list.add(row.get(col));
						collapsedRow.set(col, list);
					}
				}
				result.put(key, collapsedRow);
			}
			else
			{
				for (String col : row.getAttributeNames())
				{
					if (!targets.contains(col))
					{
						@SuppressWarnings("unchecked")
						List<String> list = (List<String>) result.get(key).get(col);
						list.add(row.getString(col));
						result.get(key).set(col, list);
					}
				}
			}
		}

		return new ArrayList<MapEntity>(result.values());
	}

	/**
	 * Tuples can have values that are freemarker templates, e.g. ${other
	 * column}. This method will solve that
	 * 
	 * @throws IOException
	 * @throws TemplateException
	 */
	public void solve(List<MapEntity> values) throws IOException
	{
		// Freemarker configuration
		Configuration conf = new Configuration();

		boolean done = false;
		replaceParameters(values);

		while (!done)
		{
			boolean updated = false;

			String original, value;
			Template template;
			StringWriter out;
			String unsolved = "";

			for (MapEntity t : values)
			{
				for (String col : t.getAttributeNames())
				{
					original = t.getString(col);

					if (original.contains("${"))
					{
						// check for self reference (??)
						if (original.contains("${" + col + "}"))
							throw new IOException("could not solve " + col + "='"
								+ original + "' because template references to self");

						template = new Template(col, new StringReader(original), conf);
						out = new StringWriter();
						try
						{
							Map<String, Object> map = toMap(t);
							//I do not know, how to fix it differently
							map.put("runid", runID);
							template.process(map, out);
							value = out.toString();
							if (!value.equals(original))
							{
								updated = true;
								t.set(col, value);
							}
						}
						catch (Exception e)
						{
							unsolved += "could not solve " + col + "='" + original + "': " + e.getMessage() + "\n";
						}
					}
				}
			}

			if (!updated)
			{
				if (unsolved.length() > 0)
				{
					throw new IOException(unsolved);
				}
				done = true;
			}
		}
	}

	private void replaceParameters(List<MapEntity> map)
	{
		if(parametersToOverwrite != null)
		{
			for (Map.Entry<String, String> entry : parametersToOverwrite.entrySet())
			{
				String key = entry.getKey();
				String value = entry.getValue();
				for(MapEntity tuple: map)
				{
					tuple.set(key, value);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	/** Convert a tuple into a map. Columns with a '_' in them will be nested submaps. */
	public static Map<String, Object> toMap(Entity t)
	{
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		for (String key : t.getAttributeNames())
		{
				result.put(key, t.get(key));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	/** 
	 * Uncollapse a tuple using an idColumn
	 *  
	 * @param values
	 * @param idColumn
	 * @return
	 */
	public static <E extends Entity> List<E> uncollapse(List<E> values, String idColumn)
	{
		List<E> result = new ArrayList<E>();

		for (E original : values)
		{
			if (!(original.get(idColumn) instanceof List))
			{
				return values;
			}
			else
			{
				for (int i = 0; i < original.getList(idColumn).size(); i++)
				{
					MapEntity copy = new MapEntity();
					for (String col : original.getAttributeNames())
					{
						if (original.get(col) instanceof List)
						{
							copy.set(col, original.getList(col).get(i));
						}
						else
						{
							copy.set(col, original.get(col));
						}
					}
					result.add((E) copy);
				}
			}
		}

		return result;
	}

	public void setRunID(String runID)
	{
		this.runID = runID;
	}

	public void setParametersToOverwrite(HashMap parametersToOverwrite)
	{
		this.parametersToOverwrite = parametersToOverwrite;
	}
}
