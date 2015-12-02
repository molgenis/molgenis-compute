package org.molgenis.compute.generators.impl;

import static org.apache.commons.io.FileUtils.readFileToString;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.molgenis.compute.ComputeProperties;
import org.molgenis.compute.urlreader.impl.UrlReaderImpl;

public class ScriptUtils
{
	private static final Logger LOG = Logger.getLogger(ScriptGenerator.class);

	// Used to read web workflows
	private static UrlReaderImpl URL_READER = new UrlReaderImpl();

	/**
	 * Generates the header template from either a custom header template, or the default one
	 * 
	 * @param computeProperties
	 * @param outputDirectory
	 * @return The header template in String form
	 * @throws IOException
	 */
	public static String generateHeaderTemplate(ComputeProperties computeProperties, String outputDirectory)
			throws IOException
	{
		if (computeProperties.customHeader != null)
		{
			File customHeaderFile = null;
			if (computeProperties.isWebWorkflow)
			{
				customHeaderFile = URL_READER.createFileFromGithub(computeProperties.webWorkflowLocation,
						computeProperties.customHeader);
				if (customHeaderFile != null)
				{
					return readFileToString(customHeaderFile);
				}
				else LOG.warn(">> Custom header not found (" + customHeaderFile + ")");
			}
			else
			{
				customHeaderFile = new File(computeProperties.customHeader);
				if (customHeaderFile.exists())
				{
					System.out.println(">> Custom header: " + customHeaderFile);
					return readFileToString(customHeaderFile);
				}
				else
				{
					LOG.warn(">> Custom header not found (" + customHeaderFile + ")");
				}
			}
		}
		else
		{
			return readInClasspath("templates" + File.separator + outputDirectory + File.separator + "header.ftl",
					outputDirectory);
		}

		return "";
	}

	/**
	 * Generates the footer template from either a custom footer template, or the default one
	 * 
	 * @param computeProperties
	 * @param outputDirectory
	 * @return The footer template in String form
	 * @throws IOException
	 */
	public static String generateFooterTemplate(ComputeProperties computeProperties, String outputDirectory)
			throws IOException
	{
		if (computeProperties.customFooter != null)
		{
			File customFooterFile = null;
			if (computeProperties.isWebWorkflow)
			{
				customFooterFile = URL_READER.createFileFromGithub(computeProperties.webWorkflowLocation,
						computeProperties.customFooter);
				if (customFooterFile != null) return readFileToString(customFooterFile);
				else LOG.warn(">> Custom footer not found (" + customFooterFile + ")");
			}
			else
			{
				customFooterFile = new File(computeProperties.customFooter);
				if (customFooterFile.exists())
				{
					System.out.println(">> Custom footer: " + customFooterFile);
					return readFileToString(customFooterFile);
				}
				else LOG.warn(">> Custom footer not found (" + customFooterFile + ")");
			}
		}
		else
		{
			return readInClasspath("templates" + File.separator + outputDirectory + File.separator + "footer.ftl",
					outputDirectory);
		}

		return "";
	}

	/**
	 * Generates the submit template from either a custom submit template, or the default one
	 * 
	 * @param computeProperties
	 * @param outputDirectory
	 * @return The submit template in String form
	 * @throws IOException
	 */
	public static String generateSubmitTemplate(ComputeProperties computeProperties, String outputDirectory)
			throws IOException
	{
		if (computeProperties.customSubmit != null)
		{
			File customSubmitFile = null;
			if (computeProperties.isWebWorkflow)
			{
				customSubmitFile = URL_READER.createFileFromGithub(computeProperties.webWorkflowLocation,
						computeProperties.customSubmit);
				if (customSubmitFile != null) return readFileToString(customSubmitFile);
				else LOG.warn(">> Custom submit script not found (" + customSubmitFile + ")");
			}
			else
			{
				customSubmitFile = new File(computeProperties.customSubmit);
				if (customSubmitFile.exists())
				{
					System.out.println(">> Custom submit script: " + customSubmitFile);
					return readFileToString(customSubmitFile);
				}
				else LOG.warn(">> Custom submit script not found (" + customSubmitFile + ")");
			}
		}
		else
		{
			return readInClasspath("templates" + File.separator + outputDirectory + File.separator + "submit.ftl",
					outputDirectory);
		}

		return "";
	}

	/**
	 * Reads the class path and returns a string with its contents
	 * 
	 * @param file
	 * @param backend
	 * @throws IOException
	 */
	private static String readInClasspath(String file, String backend) throws IOException
	{
		InputStream inputStream = ScriptUtils.class.getClassLoader().getResourceAsStream(file);

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
}
