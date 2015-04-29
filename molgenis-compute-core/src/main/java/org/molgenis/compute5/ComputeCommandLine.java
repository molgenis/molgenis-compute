package org.molgenis.compute5;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.molgenis.compute5.db.api.*;
import org.molgenis.compute5.generators.*;
import org.molgenis.compute5.model.*;
import org.molgenis.compute5.model.impl.FoldParametersImpl;
import org.molgenis.compute5.parsers.impl.CsvParameterParserImpl;
import org.molgenis.compute5.parsers.impl.WorkflowCsvParserImpl;
import org.molgenis.compute5.sysexecutor.impl.SystemCommandExecutorImpl;

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

		String userName = null;
		String pass = null;
		ComputeDbApiConnection dbApiConnection = null;
		ComputeDbApiClient dbApiClient = null;

		//check if we are working with database; database default is database=none
		if(!computeProperties.database.equalsIgnoreCase(Parameters.DATABASE_DEFAULT))
		{
			userName = computeProperties.molgenisuser;
			pass = computeProperties.molgenispass;

			dbApiConnection = new HttpClientComputeDbApiConnection(computeProperties.database,
					computeProperties.port, "/api/v1", userName, pass);
			dbApiClient = new ComputeDbApiClient(dbApiConnection);
		}

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

			if (Parameters.DATABASE_DEFAULT.equals(computeProperties.database))
			{ // if database none (= off), then do following
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
			else
			{
				String runName = computeProperties.runId;

				String backendUrl = computeProperties.backendUrl;
				String backendName = computeProperties.backend;
				Long pollInterval = Long.parseLong(computeProperties.interval);

				List<Task> tasks = compute.getTasks();
				String submitScript = "none";
				if(backendName.equalsIgnoreCase(Parameters.SCHEDULER_PBS) ||
						backendName.equalsIgnoreCase(Parameters.SCHEDULER_SLURM))
				{
					for(Task task: tasks)
					{
						String name = task.getName();
						String wrappedScript = FileUtils.readFileToString(new File(computeProperties.runDir + "/" + name + ".sh"));
						task.setScript(wrappedScript);
					}
					submitScript =  FileUtils.readFileToString(new File(computeProperties.runDir + "/submit.sh"));
				}

				String environment = compute.getUserEnvironment();

				CreateRunRequest createRunRequest = new CreateRunRequest(runName, backendUrl, pollInterval,
						tasks, environment, userName, submitScript);

				dbApiClient.createRun(createRunRequest);

				System.out.println("\n Run " + computeProperties.runId + " is inserted into database on "
						+ computeProperties.database);
			}
		}

		if (computeProperties.execute)
		{
			if(computeProperties.database.equalsIgnoreCase(Parameters.DATABASE_DEFAULT))
			{
				String runDir = computeProperties.runDir;
				SystemCommandExecutorImpl exe =  new SystemCommandExecutorImpl();
				exe.runCommand("sh " + runDir + "/submit.sh");

				String err = exe.getCommandError();
				String out = exe.getCommandOutput();

				System.out.println("\nScripts are executed/submitted on " + computeProperties.backend);

				System.out.println(out);
				System.out.println(err);

			}
			else
			{
				String backendUserName = computeProperties.backenduser;
				String backendPass = computeProperties.backendpass;

				if((backendPass == null) || (backendUserName == null))
				{
					LOG.info("\nPlease specify username and password for computational back-end");
					LOG.info("Use --backenduser[-bu] and --backendpassword[-bp] for this");
					return commandLineRunContainer;
				}

				StartRunRequest startRunRequest = new StartRunRequest(computeProperties.runId, backendUserName, backendPass);
				dbApiClient.start(startRunRequest);
				LOG.info("\n" + computeProperties.runId + "is submitted for execution "
						+ computeProperties.backend + " by user " + backendUserName);
			}
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
		CsvParameterParserImpl parser = new CsvParameterParserImpl();
		//set runID here, which will be passed to TupleUtils to solve method
		parser.setRunID(computeProperties.runId);

		if(computeProperties.hasParametersToOverwrite())
			parser.setParametersToOverwrite(computeProperties.getParametersToOverwrite());

		Parameters parameters = parser.parse(parameterFiles, computeProperties);
		FoldParametersImpl parametersContainer = new FoldParametersImpl();
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
		Workflow workflow = new WorkflowCsvParserImpl().parse(computeProperties.workFlow, computeProperties);
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