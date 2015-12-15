package org.molgenis.compute.generators.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.apache.log4j.Logger;
import org.molgenis.compute.model.Context;
import org.molgenis.compute.model.Parameters;
import org.molgenis.compute.model.StringStore;
import org.molgenis.compute.model.Task;
import org.molgenis.compute.model.Workflow;
import org.molgenis.compute.model.impl.DataEntity;

/**
 * Writes the environment to file.
 */
public class EnvironmentGenerator
{
	private static final Logger LOG = Logger.getLogger(EnvironmentGenerator.class);

	public static final String GLOBAL_PREFIX = "global_";

	private BufferedWriter envWriter;
	private Workflow workflow = null;

	private StringStore stringStore;

	public EnvironmentGenerator(StringStore stringStore)
	{
		this.stringStore = stringStore;
	}

	public void writeEnvironmentToFile(Context context) throws Exception
	{
		envWriter.write("#\n## User parameters\n#\n");
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
					envWriter.write(GLOBAL_PREFIX + assignment.toString() + '\n');
				}
			}
		}
	}

	public void generate(Context context, String workDir) throws Exception
	{
		Parameters.ENVIRONMENT_FULLPATH = workDir + File.separator + Parameters.ENVIRONMENT;

		File envFile = new File(Parameters.ENVIRONMENT_FULLPATH);
		envFile.delete();

		// create new environment file
		envFile.createNewFile();

		envWriter = new BufferedWriter(new FileWriter(envFile, true));
		writeEnvironmentToFile(context);
		envWriter.close();
	}
}
