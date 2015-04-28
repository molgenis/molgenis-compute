package org.molgenis.compute5;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.cli.HelpFormatter;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.molgenis.compute5.generators.BackendGenerator;
import org.molgenis.compute5.generators.CreateWorkflowGenerator;
import org.molgenis.compute5.generators.EnvironmentGenerator;
import org.molgenis.compute5.generators.TaskGenerator;
import org.molgenis.compute5.model.Compute;
import org.molgenis.compute5.model.Parameters;
import org.molgenis.compute5.model.ParametersFolder;
import org.molgenis.compute5.model.Task;
import org.molgenis.compute5.model.Workflow;
import org.molgenis.compute5.parsers.ParametersCsvParser;
import org.molgenis.compute5.parsers.WorkflowCsvParser;
import org.molgenis.compute5.sysexecutor.SysCommandExecutor;

/**
 * Commandline program for compute5. Usage: -w workflow.csv -p parameters.csv
 * [-p moreParameters.csv]
 * 
 * NB parameters will be 'natural joined' when overlapping columns.
 */
public class ComputeCommandLine
{
	private static final Logger LOG = Logger.getLogger(ComputeCommandLine.class);
	private CommandLineRunContainer commandLineRunContainer = null;
	
	public static void main(String[] args) throws Exception
	{
		BasicConfigurator.configure();

		LOG.info("### MOLGENIS COMPUTE ###");
		String version = ComputeCommandLine.class.getPackage().getImplementationVersion();
		if (null == version) version = "development";
		LOG.info("Version: " + version);

		// disable freemarker logging
		freemarker.log.Logger.selectLoggerLibrary(freemarker.log.Logger.LIBRARY_NONE);

		// parse options
		ComputeProperties computeProperties = new ComputeProperties(args);

		new ComputeCommandLine().execute(computeProperties);

	}

	public CommandLineRunContainer execute(ComputeProperties computeProperties) throws Exception
	{
		Compute compute = new Compute(computeProperties);

		if(computeProperties.showHelp)
		{
			new HelpFormatter().printHelp("sh molgenis-compute.sh -p parameters.csv", computeProperties.getOptions());
			return commandLineRunContainer;
		}

		if (computeProperties.create)
		{
			new CreateWorkflowGenerator(computeProperties.createDirName);
			return commandLineRunContainer;
		}
		else if (computeProperties.clear)
		{
			File file = new File(Parameters.PROPERTIES);

			if(file.delete())
			{
				LOG.info(file.getName() + " is cleared");
			}
			else
			{
				LOG.info("Fail to clear " + file.getName());
			}
			return commandLineRunContainer;
		}
		else if (computeProperties.generate)
		{
			String toPrint = computeProperties.workFlow;
			LOG.info("Using workflow:         " + new File(toPrint).getAbsolutePath());

			if (defaultsExists(computeProperties))
			{
				toPrint = computeProperties.defaults;
				LOG.info("Using defaults:         "
						+ new File(toPrint).getAbsolutePath());
			}

			for(int i = 0; i < computeProperties.parameters.length; i++)
			{
				toPrint = computeProperties.parameters[i];
				LOG.info("Using parameters:       " + new File(toPrint).getAbsolutePath());
			}
			LOG.info("Using run (output) dir: " + new File(computeProperties.runDir).getAbsolutePath());
			LOG.info("Using backend:          " + computeProperties.backend);
			LOG.info("Using runID:            " + computeProperties.runId + "\n\n");

			generate(compute, computeProperties);

			if (computeProperties.list)
			{
				// list *.sh files in rundir
				File[] scripts = new File(computeProperties.runDir).listFiles(
						new FilenameFilter()
				{
					public boolean accept(File dir, String filename)
					{
						return filename.endsWith(".sh");
					}
				});

				LOG.info("Generated jobs that are ready to run:");
				if (null == scripts) System.out.println("None. Remark: the run (output) directory '"
						+ computeProperties.runDir + "' does not exist.");
				else if (0 == scripts.length) System.out.println("None.");
				else for (File script : scripts)
					{
						System.out.println("- " + script.getName());
					}
			}
		}

		if (computeProperties.execute)
		{
			String runDir = computeProperties.runDir;
			SysCommandExecutor exe =  new SysCommandExecutor();
			exe.runCommand("sh " + runDir + "/submit.sh");

			String err = exe.getCommandError();
			String out = exe.getCommandOutput();

			System.out.println("\nScripts are executed/submitted on " + computeProperties.backend);

			System.out.println(out);
			System.out.println(err);
		}
		
		return commandLineRunContainer;
	}

	private static boolean defaultsExists(ComputeProperties computeProperties) throws IOException
	{

		// if exist include defaults.csv in parameterFiles
		if (null == computeProperties.defaults)
			return false;
		else
			if(!computeProperties.isWebWorkflow)
				return new File(computeProperties.defaults).exists();
			else
				return true;
	}

	private void generate(Compute compute, ComputeProperties computeProperties) throws Exception
	{
		// create a list of parameter files
		List<File> parameterFiles = new ArrayList<File>();

		for (String f : computeProperties.parameters)
			parameterFiles.add(new File(f));
		if (defaultsExists(computeProperties))
				parameterFiles.add(new File(computeProperties.defaults));

		// parse param files
		ParametersCsvParser parser = new ParametersCsvParser();
		//set runID here, which will be passed to TupleUtils to solve method
		parser.setRunID(computeProperties.runId);

		if(computeProperties.hasParametersToOverwrite())
			parser.setParametersToOverwrite(computeProperties.getParametersToOverwrite());

		Parameters parameters = parser.parse(parameterFiles, computeProperties);
		ParametersFolder parametersContainer = new ParametersFolder();
		parametersContainer.setFromFiles(parameterFiles, computeProperties);
		compute.setParametersContainer(parametersContainer);
		compute.setParameters(parameters);

        if(computeProperties.batchOption != null)
            compute.createBatchAnalyser(computeProperties.batchVariable, computeProperties.batchSize);

		LOG.info("Starting script generation...");
		// create outputdir
		File dir = new File(computeProperties.runDir);
		computeProperties.runDir = dir.getCanonicalPath();
		dir.mkdirs();

		//uncomment when fixed
		// document inputs
//		new DocTotalParametersCsvGenerator().generate(new File(computeProperties.runDir + "/doc/inputs.csv"),
//				compute.getParameters());

		// parse workflow
		Workflow workflow = new WorkflowCsvParser().parse(computeProperties.workFlow, computeProperties);
		compute.setWorkflow(workflow);

		// create environment.txt with user parameters that are used in at least
		// one of the steps
		HashMap<String, String> userEnvironment = new EnvironmentGenerator().generate(compute, computeProperties.runDir);
		compute.setMapUserEnvironment(userEnvironment);

		TaskGenerator taskGenerator = new TaskGenerator();

		// analyse lists in workflow protocols
		// we need to know if list input are coming from the same or different parameter files
		// to combine lists or leave them separated
		if(parametersContainer.getParameters().size() >= 2) taskGenerator.determineCombineLists(workflow);
		
		// generate the tasks
		List<Task> tasks = taskGenerator.generate(compute);
		compute.setTasks(tasks);

		commandLineRunContainer = new BackendGenerator(computeProperties).generate(compute, dir);

//TODO:	FIX	generate documentation
//		new DocTotalParametersCsvGenerator().generate(new File(computeProperties.runDir + "/doc/outputs.csv"),
//				compute.getParameters());
//		new DocWorkflowDiagramGenerator().generate(new File(computeProperties.runDir + "/doc"), compute.getWorkflow());
//		new DocTasksDiagramGenerator().generate(new File(computeProperties.runDir + "/doc"), compute.getTasks());

		LOG.info("Generation complete.");
	}

}