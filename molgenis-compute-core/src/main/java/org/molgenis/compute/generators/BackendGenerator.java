package org.molgenis.compute.generators;

import java.io.*;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.molgenis.compute.CommandLineRunContainer;
import org.molgenis.compute.ComputeProperties;
import org.molgenis.compute.GeneratedScript;
import org.molgenis.compute.model.Compute;
import org.molgenis.compute.model.Parameters;
import org.molgenis.compute.model.Task;
import org.molgenis.compute.urlreader.impl.UrlReaderImpl;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/** Parameters of the backend, either PBS, SGE, GRID, etc */
public class BackendGenerator
{
    private ComputeProperties cp = null;
	private static final Logger LOG = Logger.getLogger(BackendGenerator.class);

    private static final String BATCH = "batch";
    private static final String SUBMIT = "submit.sh";

	private String headerTemplate = "";
	private String footerTemplate = "";
	private String submitTemplate = "";

	private UrlReaderImpl urlReaderImpl = new UrlReaderImpl();

    private Configuration conf = new Configuration();
    private CommandLineRunContainer container = new CommandLineRunContainer();

    Template submit = null;
    Template header = null;
    Template footer = null;
    
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

	public CommandLineRunContainer generate(Compute compute, File targetDir) throws IOException
	{
        List<Task> tasks = compute.getTasks();

        // get templates for header and footer
        submit = new Template("submit", new StringReader(this.getSubmitTemplate()), conf);
        header = new Template("header", new StringReader(this.getHeaderTemplate()), conf);
        footer = new Template("footer", new StringReader(this.getFooterTemplate()), conf);

        if(cp.batchOption == null)
        {
            generateSubmit(SUBMIT, tasks, targetDir.getAbsolutePath());
            generateJobs(tasks, targetDir.getAbsolutePath());
        }
        else
        {
            for (int i = 0; i < compute.getBatchesSize(); i++)
            {
                List<Task> batchTasks = new ArrayList<Task>();

                for(Task t : tasks)
                {
                    if(t.getBatchNumber() == i)
                    {
                        batchTasks.add(t);
                    }
                }

                String dir = targetDir.getAbsolutePath() + File.separator + BATCH + i;

                generateSubmit(SUBMIT, batchTasks, dir);
                generateJobs(batchTasks, dir);
            }
        }

		return container;
	}

    // generate the tasks scripts
    private void generateJobs(List<Task> tasks, String absolutePath)  throws IOException
    {
        for (Task task : tasks)
        {
            try
            {
                GeneratedScript generatedScript = new GeneratedScript();
                File outFile = new File(absolutePath + File.separator + task.getName() + ".sh");
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
    }

    // generate the submit script
    private void generateSubmit(String s, List<Task> tasks, String targetDir) throws IOException
    {

        try
        {
            File outFile = new File(targetDir + File.separator + s);
            Writer out = new StringWriter();

            Map<String, Object> taskMap = new HashMap<String, Object>();
            taskMap.put("tasks", tasks);

            submit.process(taskMap, out);
            String strSubmit = out.toString();
            FileUtils.writeStringToFile(outFile, strSubmit);
            out.close();

            container.setSumbitScript(strSubmit);

            System.out.println("Generated " + outFile);
        } catch (TemplateException e) {
            throw new IOException("Backend generation failed for " + this.getClass().getSimpleName() + "\n\nError is:\n" + e.toString());
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
        this.cp = cp;

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
				h = urlReaderImpl.createFileFromGithub(cp.webWorkflowLocation, cp.customHeader);
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
				f = urlReaderImpl.createFileFromGithub(cp.webWorkflowLocation, cp.customFooter);
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
				s = urlReaderImpl.createFileFromGithub(cp.webWorkflowLocation, cp.customSubmit);
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
