package org.molgenis.compute.generators.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.molgenis.compute.model.Context;
import org.molgenis.compute.model.Parameters;
import org.molgenis.compute.model.StringStore;
import org.molgenis.compute.model.Task;
import org.molgenis.compute.model.Workflow;
import org.molgenis.compute.model.impl.DataEntity;

import com.gs.collections.impl.map.mutable.UnifiedMap;

/**
 * Returns initial environment with all user params that are used somewhere in this workflow
 * 
 * @param compute
 * @return
 */

public class EnvironmentGenerator
{
	private static final Logger LOG = Logger.getLogger(EnvironmentGenerator.class);

	public static final String GLOBAL_PREFIX = "global_";

	private BufferedWriter envWriter;
	private Map<String, String> environment = new UnifiedMap<String, String>();
	private Workflow workflow = null;

	private StringStore stringStore;

	public EnvironmentGenerator(StringStore stringStore)
	{
		this.stringStore = stringStore;
	}

	public void writeEnvironmentToFile(Context context) throws Exception
	{
		writeLineToFile("#");
		writeLineToFile("## User parameters");
		writeLineToFile("#");
		workflow = context.getWorkflow();

		// get all parameters from parameters.csv
		for (String globalParameterName : workflow.getUserInputParams())
		{
			String prefixedParameterName = Parameters.USER_PREFIX + globalParameterName;
			for (DataEntity dataEntity : context.getParameters().getValues())
			{
				String value = dataEntity.getString(prefixedParameterName);
				Integer index = dataEntity.getInt(Parameters.USER_PREFIX + Task.TASKID_COLUMN);

				if (value == null)
				{
					dataEntity.set(stringStore.intern(Parameters.USER_PREFIX + globalParameterName),
							stringStore.intern(workflow.findMatchingOutput(globalParameterName, dataEntity)));
					LOG.warn("Variable [" + index + "] has run time value");
				}
				else
				{
					StringBuilder assignment = new StringBuilder();
					assignment.append(globalParameterName).append("[").append(index).append("]=\"").append(value)
							.append("\"");
					writeLineToFile(assignment.toString());
					environment.put(stringStore.intern(globalParameterName + "[" + index + "]"),
							stringStore.intern(value));
				}
			}
		}
	}

	public Map<String, String> generate(Context context, String workDir) throws Exception
	{
		Parameters.ENVIRONMENT_FULLPATH = workDir + File.separator + Parameters.ENVIRONMENT;

		File envFile = new File(Parameters.ENVIRONMENT_FULLPATH);
		envFile.delete();

		// create new environment file
		envFile.createNewFile();

		envWriter = new BufferedWriter(new FileWriter(envFile, true));
		writeEnvironmentToFile(context);
		envWriter.close();

		return environment;
	}

	private void writeLineToFile(String line) throws IOException
	{
		if (line.startsWith("#"))
		{
			envWriter.write(line);
			envWriter.newLine();
		}
		else
		{
			// TODO Remove this global prefix
			envWriter.write(GLOBAL_PREFIX);
			envWriter.write(line);
			envWriter.newLine();
		}
	}
}
