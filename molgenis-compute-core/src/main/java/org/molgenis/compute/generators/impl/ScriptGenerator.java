package org.molgenis.compute.generators.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.molgenis.compute.ComputeProperties;
import org.molgenis.compute.model.Script;
import org.molgenis.compute.model.Step;
import org.molgenis.compute.model.Task;
import org.molgenis.compute.model.TaskInfo;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class ScriptGenerator
{
	private static final Logger LOG = Logger.getLogger(ScriptUtils.class);

	private ComputeProperties computeProperties;
	private String outputDirectory;
	private Script script;

	public ScriptGenerator(ComputeProperties computeProperties) throws IOException
	{
		this.computeProperties = computeProperties;
		outputDirectory = computeProperties.backend;

		// Create a script object with the header, footer, and submit templates
		script = setScriptTemplates();
	}

	/**
	 * Generates every script for one {@link Step} Appends all the created scripts from one {@link Step} to the main
	 * submit.sh
	 * 
	 * @param tasks
	 * @throws IOException
	 */
	public List<TaskInfo> generateTaskScripts(Iterable<Task> tasks) throws IOException
	{
		List<TaskInfo> taskInfos = new ArrayList<TaskInfo>();
		for (Task task : tasks)
		{
			generateTaskScript(task);
			taskInfos.add(new TaskInfo(task.getName(), task.getPreviousTasks()));
		}
		
		return taskInfos;
	}

	/**
	 * Generates a single task script
	 *
	 * @param task
	 * @param absolutePath
	 * @throws IOException
	 */
	private void generateTaskScript(Task task) throws IOException
	{
		try
		{
			File taskOutputFile = new File(computeProperties.runDir + File.separator + task.getName() + ".sh");
			FileWriter fileWriter = new FileWriter(taskOutputFile);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

			script.getHeaderTemplate().process(task.getParameters(), bufferedWriter);
			bufferedWriter.write("\n" + task.getScript() + "\n");
			script.getFooterTemplate().process(task.getParameters(), bufferedWriter);

			bufferedWriter.close();
			fileWriter.close();

			LOG.info("Generated " + taskOutputFile.getName());
		}
		catch (TemplateException e)
		{
			LOG.error(task.getName() + " Failed to generate! Reason:\n");
			throw new IOException(this.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}

	/**
	 * Adds the template for the header and footer to script object
	 * 
	 * @return
	 * @throws IOException
	 */
	private Script setScriptTemplates() throws IOException
	{
		String headerTemplate = ScriptUtils.generateHeaderTemplate(computeProperties, outputDirectory);
		String footerTemplate = ScriptUtils.generateFooterTemplate(computeProperties, outputDirectory);

		Configuration configuration = new Configuration();

		return new Script(new Template("header", new StringReader(headerTemplate), configuration),
				new Template("footer", new StringReader(footerTemplate), configuration));
	}

	public void generateSubmitScript(List<TaskInfo> taskInfos) throws IOException
	{
		String submitTemplate = ScriptUtils.generateSubmitTemplate(computeProperties, outputDirectory);
		script.setSubmitTemplate(new Template("submit", new StringReader(submitTemplate), new Configuration()));

		try
		{
			File submitFile = new File(computeProperties.runDir + File.separator + "submit.sh");
			FileWriter fileWriter = new FileWriter(submitFile);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

			Map<String, Object> templateModel = new HashMap<String, Object>();
			templateModel.put("tasks", taskInfos);

			script.getSubmitTemplate().process(templateModel, bufferedWriter);
			bufferedWriter.close();

			LOG.info("Generated " + submitFile.getName());
		}
		catch (TemplateException e)
		{
			LOG.error("submit.sh Failed to generate! Reason:\n");
			throw new IOException(this.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}
}
