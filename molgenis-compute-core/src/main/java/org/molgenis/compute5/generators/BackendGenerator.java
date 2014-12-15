package org.molgenis.compute5.generators;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.poi.util.IOUtils;
import org.molgenis.compute5.CommandLineRunContainer;
import org.molgenis.compute5.ComputeProperties;
import org.molgenis.compute5.GeneratedScript;
import org.molgenis.compute5.model.Parameters;
import org.molgenis.compute5.model.Task;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.molgenis.compute5.urlreader.UrlReader;

/** Parameters of the backend, either PBS, SGE, GRID, etc */
public class BackendGenerator
{

	private static final Logger LOG = Logger.getLogger(BackendGenerator.class);


	private String headerTemplate = "";
	private String footerTemplate = "";
	private String submitTemplate = "";

	private UrlReader urlReader = new UrlReader();

	private String readInJar(String file) throws IOException
	{
		URL header = this.getClass().getResource(file);
		if (header == null) throw new IOException("file " + file + " is missing for backend "
				+ this.getClass().getSimpleName());

		BufferedReader stream = new BufferedReader(new InputStreamReader(header.openStream()));

		StringBuilder result = new StringBuilder();
		try
		{

			String inputLine;

			while ((inputLine = stream.readLine()) != null)
				result.append(inputLine + "\n");
		}
		finally
		{
			stream.close();
		}
		return result.toString();
	}


	private String readInClasspath(String file, String backend) throws IOException
	{
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(file);

		if(in == null)
		{
			LOG.error("Specified [" + backend + "] is unknown or unavailable");
			throw new IOException("Specified [" + backend + "] is unknown or unavailable. Create a file " + file);
		}
		BufferedReader stream = new BufferedReader(new InputStreamReader(in));

		StringBuilder result = new StringBuilder();
		try
		{
			String inputLine;

			while ((inputLine = stream.readLine()) != null)
				result.append(inputLine + "\n");
		}
		finally
		{
			stream.close();
		}
		return result.toString();
	}

	public CommandLineRunContainer generate(List<Task> tasks, File targetDir) throws IOException
	{
		Configuration conf = new Configuration();
		CommandLineRunContainer container = new CommandLineRunContainer();

		// get templates for header and footer
		Template header = new Template("header", new StringReader(this.getHeaderTemplate()), conf);
		Template footer = new Template("footer", new StringReader(this.getFooterTemplate()), conf);
		Template submit = new Template("submit", new StringReader(this.getSubmitTemplate()), conf);

		// generate the submit script
		try
		{
			File outFile = new File(targetDir.getAbsolutePath() + File.separator +"submit.sh");
			Writer out = new StringWriter();

			Map<String, Object> taskMap = new HashMap<String, Object>();
			taskMap.put("tasks", tasks);

			submit.process(taskMap, out);
			String strSubmit = out.toString();
			FileUtils.writeStringToFile(outFile, strSubmit);
			out.close();

			container.setSumbitScript(strSubmit);

			System.out.println("Generated " + outFile);
		}
		catch (TemplateException e)
		{
			throw new IOException("Backend generation failed for " + this.getClass().getSimpleName() + "\n\nError is:\n" + e.toString());
		}

		// generate the tasks scripts
		for (Task task : tasks)
		{
			try
			{
				GeneratedScript generatedScript = new GeneratedScript();
				File outFile = new File(targetDir.getAbsolutePath() + File.separator + task.getName() + ".sh");
				Writer out = new StringWriter();

				header.process(task.getParameters(), out);
				out.write("\n" + task.getScript() + "\n");
				footer.process(task.getParameters(), out);
				String strScript = out.toString();
				FileUtils.writeStringToFile(outFile, strScript);
				out.close();

				generatedScript.setName(task.getName());
				generatedScript.setStepName(task.getStepName());
				generatedScript.setScript(strScript);

				container.addTask(generatedScript);

				System.out.println("Generated " + outFile);
			}
			catch (TemplateException e)
			{
				throw new IOException("Backend generation of task '" + task.getName() + "' failed for "
						+ this.getClass().getSimpleName() + ": " + e.getMessage());
			}
		}
		return container;
	}

	public String getHeaderTemplate()
	{
		return headerTemplate;
	}

	public void setHeaderTemplate(String headerTemplate)
	{
		this.headerTemplate = headerTemplate;
	}

	public void appendCustomHeader(String customHeader)
	{
		this.headerTemplate += "\n" + customHeader;
	}
	
	public String getFooterTemplate()
	{
		return footerTemplate;
	}

	public void setFooterTemplate(String footerTemplate)
	{
		this.footerTemplate = footerTemplate;
	}
	
	public void appendCustomFooter(String customFooter)
	{
		this.footerTemplate += "\n" + customFooter;
	}

	public String getSubmitTemplate()
	{
		return submitTemplate;
	}

	public void setSubmitTemplate(String submitTemplate)
	{
		this.submitTemplate = submitTemplate;
	}

	public BackendGenerator(ComputeProperties cp) throws IOException
	{
		String dir = cp.backend;

		if(!cp.database.equalsIgnoreCase(Parameters.BACKEND_TYPE_GRID) ||
				!cp.database.equalsIgnoreCase(Parameters.BACKEND_TYPE_CLOUD))
		{
			this.setHeaderTemplate(readInClasspath("templates" + File.separator + dir + File.separator + "header.ftl", dir));
			this.setFooterTemplate(readInClasspath("templates" + File.separator + dir + File.separator + "footer.ftl", dir));
			this.setSubmitTemplate(readInClasspath("templates" + File.separator + dir + File.separator + "submit.ftl", dir));
		}

		if (cp.customHeader != null)
		{
			File h = null;
			if(cp.isWebWorkflow)
			{
				h = urlReader.createFileFromGithub(cp.webWorkflowLocation, cp.customHeader);
				if(h != null)
					this.appendCustomHeader(FileUtils.readFileToString(h));
				else
					System.out.println(">> Custom header not found (" + h + ")");
			}
			else
			{
				h = new File(cp.customHeader);
				if (h.exists())
				{
					System.out.println(">> Custom header: " + h);
					this.appendCustomHeader(FileUtils.readFileToString(h));
				}
				else
					System.out.println(">> Custom header not found (" + h + ")");
			}
		}

		if(cp.customFooter != null)
		{
			File f = null;
			if(cp.isWebWorkflow)
			{
				f = urlReader.createFileFromGithub(cp.webWorkflowLocation, cp.customFooter);
				if(f != null)
					this.appendCustomFooter(FileUtils.readFileToString(f));
				else
					System.out.println(">> Custom footer not found (" + f + ")");
			}
			else
			{
				f = new File(cp.customFooter);
				if (f.exists())
				{
					System.out.println(">> Custom footer: " + f);
					this.appendCustomFooter(FileUtils.readFileToString(f));
				}
				else
					System.out.println(">> Custom footer not found (" + f + ")");
			}
		}

		if(cp.customSubmit != null)
		{
			File s = null;
			if(cp.isWebWorkflow)
			{
				s = urlReader.createFileFromGithub(cp.webWorkflowLocation, cp.customSubmit);
				if(s != null)
					this.setSubmitTemplate(FileUtils.readFileToString(s));
				else
					System.out.println(">> Custom footer not found (" + s + ")");
			}
			else
			{
				s = new File(cp.customSubmit);
				if (s.exists())
				{
					System.out.println(">> Custom submit script: " + s);
					this.setSubmitTemplate(FileUtils.readFileToString(s));
				}
				else
					System.out.println(">> Custom submit script not found (" + s + ")");
			}
		}

	}
}
