package org.molgenis.compute;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.molgenis.compute.db.api.ComputeDbApiClient;
import org.molgenis.compute.db.api.ComputeDbApiConnection;
import org.molgenis.compute.db.api.CreateRunRequest;
import org.molgenis.compute.db.api.HttpClientComputeDbApiConnection;
import org.molgenis.compute.db.api.StartRunRequest;
import org.molgenis.compute.generators.impl.EnvironmentGenerator;
import org.molgenis.compute.generators.impl.ScriptGenerator;
import org.molgenis.compute.generators.impl.TaskGenerator;
import org.molgenis.compute.generators.impl.WorkflowGenerator;
import org.molgenis.compute.model.Context;
import org.molgenis.compute.model.Input;
import org.molgenis.compute.model.Parameters;
import org.molgenis.compute.model.Protocol;
import org.molgenis.compute.model.Step;
import org.molgenis.compute.model.Task;
import org.molgenis.compute.model.TaskInfo;
import org.molgenis.compute.model.Workflow;
import org.molgenis.compute.model.impl.FoldParametersImpl;
import org.molgenis.compute.model.impl.WorkflowImpl;
import org.molgenis.compute.parsers.impl.CsvParameterParserImpl;
import org.molgenis.compute.parsers.impl.WorkflowCsvParserImpl;
import org.molgenis.compute.sysexecutor.impl.SystemCommandExecutorImpl;

/**
 * Commandline program for compute5. Usage: -w workflow.csv -p parameters.csv [-p moreParameters.csv]
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
		Context context = new Context(computeProperties);

		String userName = null;
		String pass = null;
		ComputeDbApiConnection dbApiConnection = null;
		ComputeDbApiClient dbApiClient = null;

		// check if we are working with database; database default is database=none
		if (!computeProperties.database.equalsIgnoreCase(Parameters.DATABASE_DEFAULT))
		{
			userName = computeProperties.molgenisuser;
			pass = computeProperties.molgenispass;

			dbApiConnection = new HttpClientComputeDbApiConnection(computeProperties.database, computeProperties.port,
					"/api/v1", userName, pass);
			dbApiClient = new ComputeDbApiClient(dbApiConnection);
		}

		if (computeProperties.showHelp)
		{
			new HelpFormatter().printHelp("sh molgenis-compute.sh -p parameters.csv", computeProperties.getOptions());
			return commandLineRunContainer;
		}

		if (computeProperties.create)
		{
			new WorkflowGenerator(computeProperties.createDirName);
			return commandLineRunContainer;
		}
		else if (computeProperties.clear)
		{
			File file = new File(Parameters.PROPERTIES);

			if (file.delete())
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
				LOG.info("Using defaults:         " + new File(toPrint).getAbsolutePath());
			}

			for (int i = 0; i < computeProperties.getParameterFiles().length; i++)
			{
				toPrint = computeProperties.getParameterFiles()[i];
				LOG.info("Using parameters:       " + new File(toPrint).getAbsolutePath());
			}

			LOG.info("Using run (output) dir: " + new File(computeProperties.runDir).getAbsolutePath());
			LOG.info("Using backend:          " + computeProperties.backend);
			LOG.info("Using runID:            " + computeProperties.runId + "\n\n");

			generate(context, computeProperties);

			if (Parameters.DATABASE_DEFAULT.equals(computeProperties.database))
			{ // if database none (= off), then do following
				if (computeProperties.list)
				{
					// list *.sh files in rundir
					File[] scripts = new File(computeProperties.runDir).listFiles(new FilenameFilter()
					{
						@Override
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

				Iterable<Task> tasks = context.getTasks();
				String submitScript = "none";
				if (backendName.equalsIgnoreCase(Parameters.SCHEDULER_PBS)
						|| backendName.equalsIgnoreCase(Parameters.SCHEDULER_SLURM))
				{
					for (Task task : tasks)
					{
						String name = task.getName();
						String wrappedScript = FileUtils
								.readFileToString(new File(computeProperties.runDir + "/" + name + ".sh"));
						task.setScript(wrappedScript);
					}
					submitScript = FileUtils.readFileToString(new File(computeProperties.runDir + "/submit.sh"));
				}

				String environment = context.getUserEnvironment();

				CreateRunRequest createRunRequest = new CreateRunRequest(runName, backendUrl, pollInterval, tasks,
						environment, userName, submitScript);

				dbApiClient.createRun(createRunRequest);

				System.out.println("\n Run " + computeProperties.runId + " is inserted into database on "
						+ computeProperties.database);
			}
		}

		if (computeProperties.execute)
		{
			if (computeProperties.database.equalsIgnoreCase(Parameters.DATABASE_DEFAULT))
			{
				String runDir = computeProperties.runDir;
				SystemCommandExecutorImpl exe = new SystemCommandExecutorImpl();
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

				if ((backendPass == null) || (backendUserName == null))
				{
					LOG.info("\nPlease specify username and password for computational back-end");
					LOG.info("Use --backenduser[-bu] and --backendpassword[-bp] for this");
					return commandLineRunContainer;
				}

				StartRunRequest startRunRequest = new StartRunRequest(computeProperties.runId, backendUserName,
						backendPass);
				dbApiClient.start(startRunRequest);
				LOG.info("\n" + computeProperties.runId + "is submitted for execution " + computeProperties.backend
						+ " by user " + backendUserName);
			}
		}
		return commandLineRunContainer;
	}

	/**
	 * Helper function to check if the defaults.csv exists
	 * 
	 * @param computeProperties
	 * @throws IOException
	 */
	private static boolean defaultsExists(ComputeProperties computeProperties) throws IOException
	{
		// if exist include defaults.csv in parameterFiles
		if (null == computeProperties.defaults) return false;
		else if (!computeProperties.isWebWorkflow) return new File(computeProperties.defaults).exists();
		else return true;
	}

	/**
	 * Generate {@link Parameters} and {@link Task}s. After generate is complete, bash scripts will be available to
	 * submit to the cluster
	 * 
	 * @param context
	 * @param computeProperties
	 * @throws Exception
	 */
	private void generate(Context context, ComputeProperties computeProperties) throws Exception
	{
		// Create a list of parameter files
		List<File> parameterFiles = new ArrayList<File>();

		// Add files from the computeProperties parameter String array to the list of parameterFiles
		for (String parameterFile : computeProperties.getParameterFiles())
		{
			parameterFiles.add(new File(parameterFile));
		}

		// Add default property file if it exists
		if (defaultsExists(computeProperties))
		{
			parameterFiles.add(new File(computeProperties.defaults));
		}

		// Parse all parameters
		CsvParameterParserImpl parser = new CsvParameterParserImpl();

		// This run ID will be passed to the TupleUtils "to solve" method
		parser.setRunID(computeProperties.runId);

		if (computeProperties.hasParametersToOverwrite())
		{
			parser.setParametersToOverwrite(computeProperties.getParametersToOverwrite());
		}

		// Fold parameters
		FoldParametersImpl foldParameters = new FoldParametersImpl(parameterFiles, computeProperties);

		// TODO set number of files, only used to check size
		context.setFoldParameters(foldParameters);
		context.setParameters(parser.parse(parameterFiles, computeProperties));

		// Batch the compute job if there is a batch option available
		if (computeProperties.batchOption != null)
		{
			context.createBatchAnalyser(computeProperties.batchVariable, computeProperties.batchSize);
		}

		LOG.info("### Starting script generation ###");

		File outputDirectory = new File(computeProperties.runDir);
		computeProperties.runDir = outputDirectory.getCanonicalPath();
		outputDirectory.mkdirs();

		// Parse workflow
		WorkflowImpl workflowImpl = new WorkflowCsvParserImpl().parse(computeProperties.workFlow, computeProperties);
		context.setWorkflow(workflowImpl);

		// Create environment.txt with user parameters that are used in at least one of the steps
		HashMap<String, String> userEnvironment = new EnvironmentGenerator().generate(context,
				computeProperties.runDir);
		context.setMapUserEnvironment(userEnvironment);

		// Create a ScriptGenerator object. This object creates the header, footer, and submit template on
		// initialization. The object can then be used to create scripts for every generated task.
		ScriptGenerator scriptGenerator = new ScriptGenerator(computeProperties);

		// Create a TaskGenerator object with the current context object
		TaskGenerator taskGenerator = new TaskGenerator(context, scriptGenerator);

		// Analyze lists in workflow protocols.
		// We need to know if list inputs are coming from the same or from a different parameter file to combine lists
		// or leave them separated
		if (foldParameters.getParameters().size() >= 2)
		{
			determineCombineLists(workflowImpl);
		}

		// Generate tasks, store task names and previous steps in a list of TaskInfo objects
		List<TaskInfo> taskInfos = taskGenerator.generate();

		// Generate submit script with the TaskInfo objects
		scriptGenerator.generateSubmitScript(taskInfos);

		LOG.info("### Task generation has been completed. ###");
	}

	/**
	 * Analyze lists in workflow protocols and determine whether these lists should be combined or not
	 * 
	 * @param workflow
	 */
	private void determineCombineLists(Workflow workflow)
	{
		for (Step step : workflow.getSteps())
		{
			Protocol protocol = step.getProtocol();

			// calculate how many separated lists we have
			int size = 0;
			for (Input input : protocol.getInputs())
			{
				if (input.getType().equalsIgnoreCase(Input.TYPE_LIST))
				{
					size++;
				}
			}

			if (size > 1) for (Input input : protocol.getInputs())
			{
				if (input.getType().equalsIgnoreCase(Input.TYPE_LIST) && !input.isCombinedListsNotation())
				{
					input.setCombineLists(false);
				}
			}
		}
	}
}