package org.molgenis.compute5.parsers.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.molgenis.compute5.ComputeProperties;
import org.molgenis.compute5.generators.TupleUtils;
import org.molgenis.compute5.model.Parameters;
import org.molgenis.compute5.parsers.ParametersCsvParser;
import org.molgenis.compute5.urlreader.UrlReader;
import org.molgenis.data.Entity;
import org.molgenis.data.csv.CsvRepository;
import org.molgenis.data.support.MapEntity;

public class ParametersCsvParserImpl implements ParametersCsvParser
{
	private static final Logger LOG = Logger.getLogger(ParametersCsvParserImpl.class);

	private String runID;
	private ComputeProperties properties;
	private HashMap<String, String> parametersToOverwrite;

	private UrlReader urlReader = new UrlReader();

	public Parameters parse(List<File> files, ComputeProperties computeProperties) throws IOException
	{
		properties = computeProperties;
		Parameters targets = null;
		Set<String> uniqueFiles = new HashSet<String>();

		if (!properties.isWebWorkflow)
		{
			for (File file : files)
			{
				uniqueFiles.add(file.getAbsolutePath().toString());
			}

			targets = parseParamFiles(null, uniqueFiles);
		}
		else
		{
			for (File f : files)
			{
				uniqueFiles.add(f.toString());
			}
			targets = parseParamFiles(null, uniqueFiles);
		}

		// solve the templates
		TupleUtils tupleUtils = new TupleUtils();
		tupleUtils.setRunID(runID);
		if (parametersToOverwrite != null) tupleUtils.setParametersToOverwrite(parametersToOverwrite);
		tupleUtils.solve(targets.getValues());

		// mark all columns as 'user_*'
		int count = 0;
		List<MapEntity> userTargets = new ArrayList<MapEntity>();
		for (MapEntity v : targets.getValues())
		{
			MapEntity t = new MapEntity();
			for (String col : v.getAttributeNames())
			{
				t.set(Parameters.USER_PREFIX + col, v.get(col));
			}
			t.set(Parameters.ID_COLUMN, count++);
			userTargets.add(t);
		}

		targets = new Parameters();
		targets.setValues(userTargets);

		return targets;
	}

	@SuppressWarnings("unchecked")
	public Parameters parseParamFiles(Parameters targets, Set<String> paramFileSet) throws IOException
	{
		// ensure targets are initialized
		if (targets == null)
		{
			targets = new Parameters();
		}

		// if no files to parse, then we're done
		if (paramFileSet.isEmpty())
		{
			LOG.warn("No parameter files found, continuing without one...");
			return targets;
		}

		LOG.info("Start of parseParamFiles " + paramFileSet.toString());

		// get a file to parse
		String fileName = paramFileSet.iterator().next();

		File file = null;
		if (!properties.isWebWorkflow)
		{
			file = new File(fileName);
		}
		else
		{
			file = urlReader.createFileFromGithub(properties.webWorkflowLocation, fileName);
		}

		// remove file from the set we have to parse
		paramFileSet.remove(fileName);

		// initialize set of files we have parsed
		Set<String> paramFileSetDone = new HashSet<String>();

		// if targets exist then get parsed file set
		if (0 < targets.getValues().size()) paramFileSetDone = (Set<String>) targets.getValues().get(0)
				.get(Parameters.PARAMETER_COLUMN);

		// if we have already parsed this file then skip file f
		if (paramFileSetDone.contains(fileName))
		{
			return parseParamFiles(targets, paramFileSet);
		}
		else
		{
			// add parsed file to the list of parsed files and ensure we'll not
			// do this file again
			paramFileSetDone.add(fileName);

			// get file as list of tuples
			List<Entity> tupleList = asTuples(file);

			// If path to workflow is relative then prepend its parent's path
			tupleList = updatePath(tupleList, Parameters.WORKFLOW, file);

			// get other param files we have to parse, and validate that all
			// values in 'parameters' column equal. If file path is relative
			// then prepend its parent's path (f)
			HashSet<String> newParameterFileSet = getParamFiles(tupleList, file);

			// Remove all files that are already done
			newParameterFileSet.removeAll(paramFileSetDone);

			// merge new paramFileSet with current one
			paramFileSet.addAll(newParameterFileSet);

			// expand tupleLst on col's with lists/iterators (except
			// 'parameters')
			tupleList = expand(tupleList);

			// join on overlapping col's (except 'parameters')
			targets = join(targets, tupleList);

			// update targets with 'parsed file'
			targets = addParsedFile(targets, paramFileSetDone);

			// parse rest of param files
			return parseParamFiles(targets, paramFileSet);
		}
	}

	public void setRunID(String runID)
	{
		this.runID = runID;
	}

	public void setParametersToOverwrite(HashMap<String, String> parametersToOverwrite)
	{
		this.parametersToOverwrite = parametersToOverwrite;
	}

	/**
	 * Expand tupleLst
	 * 
	 * @param tupleLst
	 */
	private static List<Entity> expand(List<Entity> tupleLst)
	{
		// all expanded tuples
		List<Entity> resultLst = new ArrayList<Entity>();

		for (Entity t : tupleLst)
		{
			// expanded tuples for this tuple
			List<MapEntity> expandedTupleLst = new ArrayList<MapEntity>();
			expandedTupleLst.add(new MapEntity(t));

			for (String col : t.getAttributeNames())
			{
				if (col.equals(Parameters.PARAMETER_COLUMN)) continue;

				List<String> values = asList(t, col);

				// expand each of the tuples in expandedTupleLst with values in
				// this column
				List<MapEntity> expandedTupleLstTmp = new ArrayList<MapEntity>();
				for (MapEntity wt : expandedTupleLst)
				{
					for (String v : values)
					{
						// expanded wt
						MapEntity ewt = new MapEntity(wt);
						ewt.set(col, v);
						expandedTupleLstTmp.add(ewt);
					}
				}

				expandedTupleLst.clear();
				expandedTupleLst.addAll(expandedTupleLstTmp);
			}

			resultLst.addAll(expandedTupleLst);
		}

		return resultLst;
	}

	/**
	 * Converts an Entity to a list of strings
	 * 
	 * @param t
	 * @param col
	 * @return A list of Entity columns
	 */
	private static List<String> asList(Entity t, String col)
	{
		String s = t.getString(col);

		// deal with 'empty' values
		if (null == s) return new ArrayList<String>(Arrays.asList(""));

		Pattern pattern = Pattern.compile("([+-]?[0-9]+)\\.\\.([+-]?[0-9]+)");
		Matcher matcher = pattern.matcher(s);

		// first try as sequence, eg 3..5 (meaning 3, 4, 5)
		if (matcher.find())
		{
			List<String> seq = new ArrayList<String>();
			int first = Integer.parseInt(matcher.group(1));
			int second = Integer.parseInt(matcher.group(2));
			int from = Math.min(first, second);
			int to = Math.max(first, second);

			for (Integer i = from; i <= to; i++)
				seq.add(i.toString());

			return seq;
		}
		else
		{
			// no sequence, then return as list (values will be converted to
			// list with only that value)
			return t.getList(col);
		}
	}

	/**
	 * Update targets with actual parsed files
	 * 
	 * @param targets
	 * @param paramFileSetDone
	 */
	private static Parameters addParsedFile(Parameters targets, Set<String> paramFileSetDone)
	{
		for (MapEntity t : targets.getValues())
		{
			t.set(Parameters.PARAMETER_COLUMN, paramFileSetDone);
		}

		return targets;
	}

	/**
	 * Merge tupleLst with targets based on overlapping columns (except 'parameters')
	 * 
	 * @param targets
	 * @param right
	 */
	private Parameters join(Parameters targets, List<Entity> right)
	{
		// joined tuples that we want to return
		List<MapEntity> joined = new ArrayList<MapEntity>();

		// current tuples
		List<MapEntity> left = targets.getValues();

		if (0 == right.size())
		{
			// nothing to join
			return targets;
		}
		else if (0 == left.size())
		{
			// nothing to join, convert 'right' into targets
			for (Entity t : right)
			{
				MapEntity newValue = new MapEntity(t);
				joined.add(newValue);
			}
		}
		else
		{
			// determine intersection of col names (except param column):
			// joinFields
			Set<String> joinFields = new HashSet<String>();
			for (String s : left.get(0).getAttributeNames())
				joinFields.add(s);

			Set<String> rightFields = new HashSet<String>();
			for (String s : right.get(0).getAttributeNames())
				rightFields.add(s);

			joinFields.remove(Parameters.PARAMETER_COLUMN);
			joinFields.retainAll(rightFields);

			for (Entity l : left)
			{
				for (Entity r : right)
				{
					// determine whether tuples match and thus should be joinded
					boolean match = true;
					Iterator<String> it = joinFields.iterator();
					while (it.hasNext())
					{
						String field = it.next();
						if (!l.getString(field).equals(r.getString(field))) match = false;
					}

					// if joinFields match, then join into new tuple and add
					// that to 'joined'
					if (match)
					{
						MapEntity t = new MapEntity();
						t.set(r);
						t.set(l);
						joined.add(t);
					}
				}
			}

		}

		targets = new Parameters();
		targets.setValues(joined);

		return targets;
	}

	/**
	 * Parse file as list of Tuples and validate that no parameters contain the 'step_param' separator
	 * 
	 * @param file
	 * @return A list of entities
	 * @throws IOException
	 */
	private static List<Entity> asTuples(File file) throws IOException
	{
		List<Entity> tupleList = new ArrayList<Entity>();

		if (file.toString().endsWith(".properties"))
		{
			Properties properties = new Properties();
			FileInputStream fileInputStream = new FileInputStream(file);
			try
			{
				properties.load(fileInputStream);
			}
			catch (Exception e)
			{
				LOG.error("Error loading the file input stream, message is: " + e);
			}
			finally
			{
				fileInputStream.close();
			}

			// set this.variables
			Entity keyValueEntity = new MapEntity();
			Iterator<Object> keySetIterator = properties.keySet().iterator();
			while (keySetIterator.hasNext())
			{
				String key = keySetIterator.next().toString();
				String value = properties.getProperty(key);
				keyValueEntity.set(key, value);
			}

			tupleList.add(keyValueEntity);
		}
		else
		{
			// assume we want to parse csv
			if (!file.toString().endsWith(".csv"))
			{
				// assume we want to append '.csv'
				LOG.warn("File '" + file.toString() + "' does not end with *.properties or *.csv.");
				if (file.exists() && file.isFile())
				{
					LOG.info("\tThe file exists. We'll assume it is in the CSV-format and start parsing it...");
				}
				else
				{
					LOG.info("\tWe couldn't find the file. We'll append the extension '.csv' and try again with: "
							+ file.toString() + ".csv");

					file = new File(file.toString() + ".csv");
				}
			}

			for (Entity entity : new CsvRepository(file, null))
			{
				tupleList.add(entity);
			}
		}

		return tupleList;
	}

	/**
	 * Validate that all values (set of files) in 'parameters' column are equal and return them as a set. If a file does
	 * not have an absolute path, then use the path of its parent as a starting point.
	 * 
	 * @param tupleList
	 * @param file
	 * @return set of files (in AbsoluteFile notation) to be included
	 * @throws IOException
	 */
	private static HashSet<String> getParamFiles(List<Entity> tupleList, File file) throws IOException
	{
		boolean noParamColumnFoundYet = true;

		// use this string to validate that all values in parameter column are
		// equal
		String paramFilesString = null;

		// transform list into file set
		HashSet<String> fileSet = new HashSet<String>();

		for (Entity entity : tupleList)
		{
			for (String columnName : entity.getAttributeNames())
			{
				if (columnName.equals(Parameters.PARAMETER_COLUMN))
				{
					if (noParamColumnFoundYet)
					{
						// first row, param column found
						noParamColumnFoundYet = false;

						// should be equal for all following tuples:
						paramFilesString = entity.getString(columnName);

						// iterate through list and add absolute paths to
						// return-set
						for (String value : entity.getList(columnName))
						{
							// if file has no absolute path, then use the path
							// of its parent as path
							if (value.charAt(0) == '/')
							{
								fileSet.add(value);
							}
							else
							{
								fileSet.add(file.getParent() + File.separator + value);
							}
						}
					}
					else
					{
						if (!entity.getString(columnName).equals(paramFilesString)) throw new IOException(
								"Values in '"
										+ Parameters.PARAMETER_COLUMN
										+ "' column are not equal in file '"
										+ file.toString()
										+ "', please fix:\n'"
										+ entity.getString(columnName)
										+ "' is different from '"
										+ paramFilesString
										+ "'.\n"
										+ "You could put all values 'comma-separated' in each cell and repeat that on each line in your file, e.g.:\n"
										+ "\"" + entity.getString(columnName) + "," + paramFilesString + "\"");
					}
				}
			}
			// if first row did not contain parameter column, then next rows
			// won't either, so return empty set
			if (noParamColumnFoundYet) return fileSet;

		}

		return fileSet;
	}

	/**
	 * If the path to a 'column' (eg workflow) file is relative, then prepend parent's path
	 * 
	 * @param tupleList
	 * @return A list of entities
	 */
	private static List<Entity> updatePath(List<Entity> tupleList, String columnName, File file)
	{
		List<Entity> updatedTupleList = new ArrayList<Entity>();

		for (Entity entity : tupleList)
		{
			Entity tuple = new MapEntity(entity);

			for (String colName : entity.getAttributeNames())
			{
				if (colName.equals(columnName))
				{
					List<String> fileLocationList = new ArrayList<String>();
					// iterate through list and add absolute paths to
					// return-set
					for (String value : entity.getList(colName))
					{
						// if file has no absolute path, then use the path
						// of its parent (file f) as path
						if (value.charAt(0) == '/')
						{
							fileLocationList.add(value);
						}
						else
						{
							fileLocationList.add(file.getParent() + File.separator + value);
						}
					}

					// put updated paths back in tuple
					if (fileLocationList.size() == 1)
					{
						tuple.set(colName, fileLocationList.get(0));
					}
					else
					{
						tuple.set(colName, fileLocationList);
					}
				}
			}

			updatedTupleList.add(tuple);
		}

		return updatedTupleList;
	}
}