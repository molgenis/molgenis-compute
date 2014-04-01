package org.molgenis.compute5.parsers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.molgenis.compute5.ComputeProperties;
import org.molgenis.compute5.generators.TupleUtils;
import org.molgenis.compute5.model.Parameters;
import org.molgenis.compute5.urlreader.UrlReader;
import org.molgenis.data.Entity;
import org.molgenis.data.csv.CsvRepository;
import org.molgenis.data.support.MapEntity;


/** Parser for parameters csv file(s). Includes the solving of templated values. */
public class ParametersCsvParser
{
	private ComputeProperties properties = null;
	private String runID;
	private HashMap parametersToOverwrite = null;

	private UrlReader urlReader = new UrlReader();

	public Parameters parse(List<File> filesArray, ComputeProperties computeProperties) throws IOException
	{
		properties = computeProperties;
		Parameters targets = null;
		Set<String> fileSet = new HashSet<String>();
		if(!properties.isWebWorkflow)
		{
			for (File f : filesArray)
			{
				fileSet.add(f.getAbsolutePath().toString());
			}

			targets = parseParamFiles(null, fileSet);
		}
		else
		{
			for (File f : filesArray)
			{
				fileSet.add(f.toString());
			}
			targets = parseParamFiles(null, fileSet);
		}

		// solve the templates
		TupleUtils tupleUtils = new TupleUtils();
		tupleUtils.setRunID(runID);
		if(parametersToOverwrite != null)
			tupleUtils.setParametersToOverwrite(parametersToOverwrite);
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

	/**
	 * Parse paramFileSet into Parameters targets.
	 * 
	 * @param targets
	 *            contains Parameters after parsing paramFileSet
	 * @param paramFileSet
	 *            Set of parameter files to parse
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public Parameters parseParamFiles(Parameters targets, Set<String> paramFileSet) throws IOException
	{
		System.out.println(">> Start of parseParamFiles " + paramFileSet.toString());
		// Pre-process input in (1) and (2):
		// (1) ensure targets initialized
		if (targets == null)
		{
			targets = new Parameters();
//			targets.setRunID(runID);
		}
		// if no files to parse, then we're done
		if (paramFileSet.isEmpty())
			return targets;

		// get a file to parse
		String fString = paramFileSet.iterator().next();

		File f = null;
		if(!properties.isWebWorkflow)
			f = new File(fString);
		else
			f = urlReader.createFileFromGithub(properties.webWorkflowLocation, fString);

		// remove file from the set we have to parse
		paramFileSet.remove(fString);

		// initialize set of files we have parsed
		Set<String> paramFileSetDone = new HashSet<String>();

		// if targets exist then get parsed file set
		if (0 < targets.getValues().size()) paramFileSetDone = (Set<String>) targets.getValues().get(0)
				.get(Parameters.PARAMETER_COLUMN);

		// if we have already parsed this file then skip file f
		if (paramFileSetDone.contains(fString))
		{
			return parseParamFiles(targets, paramFileSet);
		}
		else
		{
			// parse file f

			// add parsed file to the list of parsed files and ensure we'll not
			// do this file again
			paramFileSetDone.add(fString);

			// get file f as list of tuples
			List<Entity> tupleLst = asTuples(f);

			// If path to workflow is relative then prepend its parent's path
			// (f).
			tupleLst = updatePath(tupleLst, Parameters.WORKFLOW, f);

			// same for output path
			// tupleLst = updatePath(tupleLst, Parameters.WORKDIR_COLUMN, f);

			// get other param files we have to parse, and validate that all
			// values in 'parameters' column equal. If file path is relative
			// then prepend its parent's path (f)
			HashSet<String> newParamFileSet = getParamFiles(tupleLst, f);

			// Remove all files that are already done
			newParamFileSet.removeAll(paramFileSetDone);

			// merge new paramFileSet with current one
			paramFileSet.addAll(newParamFileSet);

			// expand tupleLst on col's with lists/iterators (except
			// 'parameters')
			tupleLst = expand(tupleLst);

			// join on overlapping col's (except 'parameters')
			targets = join(targets, tupleLst);

			// update targets with 'parsed file'
			targets = addParsedFile(targets, paramFileSetDone);

			// parse rest of param files
			return parseParamFiles(targets, paramFileSet);
		}
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
	 * (1) Parse file f as list of Tuples and (2) validate that no parameters contain the 'step_param' separator
	 * 
	 * @param f
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	private static List<Entity> asTuples(File f) throws IOException
	{
		List<Entity> tLst = new ArrayList<Entity>();

		if (f.toString().endsWith(".properties"))
		{
			Properties p = new Properties();
			FileInputStream fis = new FileInputStream(f);
			try
			{
				p.load(fis);
			}
			finally
			{
				fis.close();
			}
			// set this.variables
			MapEntity t = new MapEntity();
			Iterator<Object> it = p.keySet().iterator();
			while (it.hasNext())
			{
				String key = (String) it.next();
				String value = p.getProperty(key);
				t.set(key, value);
			}

			tLst.add(t);
		}
		else
		{ // assume we want to parse csv
			if (!f.toString().endsWith(".csv"))
			{ // assume we want to append '.csv'
				System.out.println(">> File '" + f.toString() + "' does not end with *.properties or *.csv.");
				if (f.exists() && f.isFile())
				{
					System.out
							.println("\tThe file exists. We'll assume it is in the CSV-format and start parsing it...");
				}
				else
				{
					System.out
							.println("\tWe couldn't find the file. We'll append the extension '.csv' and try again with");
					System.out.println("\t" + f.toString() + ".csv");

					f = new File(f.toString() + ".csv");
				}
			}

			for (Entity t : new CsvRepository(f, null))
			{
				tLst.add(t);
			}
		}

		return tLst;
	}

	/**
	 * (1) Validate that all values (set of files) in 'parameters' column are equal and (2) return them as a set. (3) If
	 * a file does not have an absolute path, then use the path of its parent as a starting point.
	 * 
	 * @param tupleLst
	 * @return set of files (in AbsoluteFile notation) to be included
	 * @throws IOException
	 */
	private static HashSet<String> getParamFiles(List<Entity> tupleLst, File f) throws IOException
	{
		boolean noParamColumnFoundYet = true;

		// use this string to validate that all values in parameter column are
		// equal
		String paramFilesString = null;

		// transform list into file set
		HashSet<String> fileSet = new HashSet<String>();

		for (Entity t : tupleLst)
		{
			for (String colName : t.getAttributeNames())
			{
				if (colName.equals(Parameters.PARAMETER_COLUMN))
				{
					if (noParamColumnFoundYet)
					{
						// first row, param column found
						noParamColumnFoundYet = false;

						// should be equal for all following tuples:
						paramFilesString = t.getString(colName);

						// iterate through list and add absolute paths to
						// return-set
						for (String fString : t.getList(colName))
						{
							// if file has no absolute path, then use the path
							// of its parent (file f) as path
							if (fString.charAt(0) == '/')
							{
								fileSet.add(fString);
							}
							else
							{
								fileSet.add(f.getParent() + File.separator + fString);
							}
						}
					}
					else
					{
						if (!t.getString(colName).equals(paramFilesString)) throw new IOException(
								"Values in '"
										+ Parameters.PARAMETER_COLUMN
										+ "' column are not equal in file '"
										+ f.toString()
										+ "', please fix:\n'"
										+ t.getString(colName)
										+ "' is different from '"
										+ paramFilesString
										+ "'.\n"
										+ "You could put all values 'comma-separated' in each cell and repeat that on each line in your file, e.g.:\n"
										+ "\"" + t.getString(colName) + "," + paramFilesString + "\"");
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
	 * If path to 'column' (eg workflow) file relative, then prepend parent's path (f)
	 * 
	 * @param tupleLst
	 * @return
	 */
	private static List<Entity> updatePath(List<Entity> tupleLst, String column, File f)
	{
		List<Entity> tupleLstUpdated = new ArrayList<Entity>();

		for (Entity t : tupleLst)
		{
			MapEntity wt = new MapEntity(t);

			for (String colName : t.getAttributeNames())
			{
				if (colName.equals(column))
				{
					List<String> wfLst = new ArrayList<String>();
					// iterate through list and add absolute paths to
					// return-set
					for (String fString : t.getList(colName))
					{
						// if file has no absolute path, then use the path
						// of its parent (file f) as path
						if (fString.charAt(0) == '/')
						{
							wfLst.add(fString);
						}
						else
						{
							wfLst.add(f.getParent() + File.separator + fString);
						}
					}

					// put updated paths back in tuple
					if (wfLst.size() == 1)
					{
						wt.set(colName, wfLst.get(0));
					}
					else
					{
						wt.set(colName, wfLst);
					}
				}
			}

			tupleLstUpdated.add(wt);
		}

		return tupleLstUpdated;
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