package org.molgenis.compute.generators.impl;

import java.io.*;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.molgenis.compute.CommandLineRunContainer;
import org.molgenis.compute.ComputeProperties;
import org.molgenis.compute.GeneratedScript;
import org.molgenis.compute.generators.BackendGenerator;
import org.molgenis.compute.model.Context;
import org.molgenis.compute.model.Parameters;
import org.molgenis.compute.model.Task;
import org.molgenis.compute.urlreader.impl.UrlReaderImpl;

import com.google.common.collect.Iterables;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class BackendGeneratorImpl implements BackendGenerator
{
	private ComputeProperties computeProperties = null;
	private static final Logger LOG = Logger.getLogger(BackendGeneratorImpl.class);

	private static final String BATCH = "batch";
	private static final String SUBMIT = "submit.sh";

	private String headerTemplate = "";
	private String footerTemplate = "";
	private String submitTemplate = "";

	private UrlReaderImpl urlReaderImpl = new UrlReaderImpl();

	private Configuration configuration = new Configuration();
	private CommandLineRunContainer commandlineRunContainer = new CommandLineRunContainer();

	Template submit = null;
	Template header = null;
	Template footer = null;

	/**
	 * Instantiates a new backend generator
	 * 
	 * @param computeProperties
	 * @throws IOException
	 */
	public BackendGeneratorImpl(ComputeProperties computeProperties) throws IOException
	{
		this.computeProperties = computeProperties;

		String directory = computeProperties.backend;

		if (!computeProperties.database.equalsIgnoreCase(Parameters.BACKEND_TYPE_GRID)
				|| !computeProperties.database.equalsIgnoreCase(Parameters.BACKEND_TYPE_CLOUD))
		{
			this.setHeaderTemplate(readInClasspath(
					"templates" + File.separator + directory + File.separator + "header.ftl", directory));
			this.setFooterTemplate(readInClasspath(
					"templates" + File.separator + directory + File.separator + "footer.ftl", directory));
			this.setSubmitTemplate(readInClasspath(
					"templates" + File.separator + directory + File.separator + "submit.ftl", directory));
		}

		if (computeProperties.customHeader != null)
		{
			File customHeaderFile = null;
			if (computeProperties.isWebWorkflow)
			{
				customHeaderFile = urlReaderImpl.createFileFromGithub(computeProperties.webWorkflowLocation,
						computeProperties.customHeader);
				if (customHeaderFile != null) this.setHeaderTemplate(FileUtils.readFileToString(customHeaderFile));
				else System.out.println(">> Custom header not found (" + customHeaderFile + ")");
			}
			else
			{
				customHeaderFile = new File(computeProperties.customHeader);
				if (customHeaderFile.exists())
				{
					System.out.println(">> Custom header: " + customHeaderFile);
					this.setHeaderTemplate(FileUtils.readFileToString(customHeaderFile));
				}
				else
				{
					System.out.println(">> Custom header not found (" + customHeaderFile + ")");
				}
			}
		}

		if (computeProperties.customFooter != null)
		{
			File customFooterFile = null;
			if (computeProperties.isWebWorkflow)
			{
				customFooterFile = urlReaderImpl.createFileFromGithub(computeProperties.webWorkflowLocation,
						computeProperties.customFooter);
				if (customFooterFile != null) this.setFooterTemplate(FileUtils.readFileToString(customFooterFile));
				else System.out.println(">> Custom footer not found (" + customFooterFile + ")");
			}
			else
			{
				customFooterFile = new File(computeProperties.customFooter);
				if (customFooterFile.exists())
				{
					System.out.println(">> Custom footer: " + customFooterFile);
					this.setFooterTemplate(FileUtils.readFileToString(customFooterFile));
				}
				else System.out.println(">> Custom footer not found (" + customFooterFile + ")");
			}
		}

		if (computeProperties.customSubmit != null)
		{
			File customSubmitFile = null;
			if (computeProperties.isWebWorkflow)
			{
				customSubmitFile = urlReaderImpl.createFileFromGithub(computeProperties.webWorkflowLocation,
						computeProperties.customSubmit);
				if (customSubmitFile != null) this.setSubmitTemplate(FileUtils.readFileToString(customSubmitFile));
				else System.out.println(">> Custom submit script not found (" + customSubmitFile + ")");
			}
			else
			{
				customSubmitFile = new File(computeProperties.customSubmit);
				if (customSubmitFile.exists())
				{
					System.out.println(">> Custom submit script: " + customSubmitFile);
					this.setSubmitTemplate(FileUtils.readFileToString(customSubmitFile));
				}
				else System.out.println(">> Custom submit script not found (" + customSubmitFile + ")");
			}
		}

	}

	/**
	 * Generates submit scripts and jobs
	 * 
	 * @param context
	 * @param targetDirectory
	 * 
	 * @return {@link CommandLineRunContainer}
	 */
	public CommandLineRunContainer generate(Context context, File targetDirectory) throws IOException
	{
		Iterable<Task> tasks = context.getTasks();

		// get templates for header and footer
		submit = new Template("submit", new StringReader(this.getSubmitTemplate()), configuration);
		header = new Template("header", new StringReader(this.getHeaderTemplate()), configuration);
		footer = new Template("footer", new StringReader(this.getFooterTemplate()), configuration);

		if (computeProperties.batchOption == null)
		{
			generateSubmit(SUBMIT, tasks, targetDirectory.getAbsolutePath());
			generateJobs(tasks, targetDirectory.getAbsolutePath());
		}
		else
		{
			for (int i = 0; i < context.getBatchesSize(); i++)
			{
				List<Task> batchTasks = new ArrayList<Task>();

				for (Task t : tasks)
				{
					if (t.getBatchNumber() == i)
					{
						batchTasks.add(t);
					}
				}

				String dir = targetDirectory.getAbsolutePath() + File.separator + BATCH + i;

				generateSubmit(SUBMIT, batchTasks, dir);
				generateJobs(batchTasks, dir);
			}
		}

		return commandlineRunContainer;
	}

	/**
	 * Reads the class path and returns a string with its contents
	 * 
	 * @param file
	 * @param backend
	 * @throws IOException
	 */
	private String readInClasspath(String file, String backend) throws IOException
	{
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(file);

		if (inputStream == null)
		{
			LOG.error("Specified [" + backend + "] is unknown or unavailable");
			throw new IOException(
					"Specified [" + backend + "] is unknown or unavailable. Create the following file: " + file);
		}
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

		StringBuilder result = new StringBuilder();
		try
		{
			String inputLine;
			while ((inputLine = bufferedReader.readLine()) != null)
			{
				result.append(inputLine + "\n");
			}
		}
		finally
		{
			bufferedReader.close();
		}
		return result.toString();
	}

	/**
	 * generates the tasks scripts
	 * 
	 * @param tasks
	 * @param absolutePath
	 * @throws IOException
	 */
	private void generateJobs(Iterable<Task> tasks, String absolutePath) throws IOException
	{
		for (Task task : tasks)
		{
			try
			{
				GeneratedScript generatedScript = new GeneratedScript();
				File outFile = new File(absolutePath + File.separator + task.getName() + ".sh");
				Writer writer = new StringWriter();

				header.process(task.getParameters(), writer);
				writer.write("\n" + task.getScript() + "\n");
				footer.process(task.getParameters(), writer);
				String strScript = writer.toString();
				FileUtils.writeStringToFile(outFile, strScript);
				writer.close();

				generatedScript.setName(task.getName());
				generatedScript.setStepName(task.getStepName());
				generatedScript.setScript(strScript);

				commandlineRunContainer.addTask(generatedScript);

				System.out.println("Generated " + outFile);
			}
			catch (TemplateException e)
			{
				throw new IOException("Backend generation of task '" + task.getName() + "' failed for "
						+ this.getClass().getSimpleName() + ": " + e.getMessage());
			}
		}
	}

	/**
	 * generates the submit scripts
	 * 
	 * @param submitScript
	 * @param tasks
	 * @param targetDir
	 * @throws IOException
	 */
	private void generateSubmit(String submitScript, Iterable<Task> tasks, String targetDir) throws IOException
	{
		try
		{
			File outFile = new File(targetDir + File.separator + submitScript);
			Writer writer = new StringWriter();

			Map<String, Object> taskMap = new HashMap<String, Object>();
			taskMap.put("tasks", tasks.iterator());
			taskMap.put("tasksIterable", tasks);

			submit.process(taskMap, writer);
			String strSubmit = writer.toString();
			FileUtils.writeStringToFile(outFile, strSubmit);
			writer.close();

			commandlineRunContainer.setSumbitScript(strSubmit);

			System.out.println("Generated " + outFile);
		}
		catch (TemplateException e)
		{
			throw new IOException("Backend generation failed for " + this.getClass().getSimpleName() + "\n\nError is:\n"
					+ e.toString());
		}
	}

	public String getHeaderTemplate()
	{
		return headerTemplate;
	}

	public void setHeaderTemplate(String headerTemplate)
	{
		this.headerTemplate = headerTemplate;
	}

	public String getFooterTemplate()
	{
		return footerTemplate;
	}

	public void setFooterTemplate(String footerTemplate)
	{
		this.footerTemplate = footerTemplate;
	}

	public String getSubmitTemplate()
	{
		return submitTemplate;
	}

	public void setSubmitTemplate(String submitTemplate)
	{
		this.submitTemplate = submitTemplate;
	}
}
