package org.molgenis.compute5.urlreader.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.molgenis.compute5.urlreader.UrlReader;

public class UrlReaderImpl implements UrlReader
{
	private static final Logger LOG = Logger.getLogger(UrlReaderImpl.class);

	@Override
	public File createFileFromGithub(String root, String filename)
	{
		if (filename.startsWith("./"))
		{
			filename = filename.substring(2);
		}

		String fullName = root + "/" + filename;
		String name = FilenameUtils.getBaseName(filename);
		String ext = FilenameUtils.getExtension(filename);
		String text = readGithubFile(fullName);

		File temporaryFile = null;
		BufferedWriter bufferedWriter = null;

		try
		{
			// Create temp file.
			temporaryFile = File.createTempFile(name, "." + ext);

			// Delete temp file when program exits.
			temporaryFile.deleteOnExit();

			// Write to temp file
			bufferedWriter = new BufferedWriter(new FileWriter(temporaryFile));
			bufferedWriter.write(text);
		}
		catch (IOException e)
		{
			LOG.error("Error handling file, message is: " + e);
		}
		finally
		{
			try
			{
				bufferedWriter.close();
			}
			catch (IOException e)
			{
				LOG.error("Error closing the file, message is: " + e);
			}
		}

		return temporaryFile;
	}

	/**
	 * This helper function reads the URL and translates it into a String variable
	 * 
	 * @param fullName
	 * @return The String object representation of the URL contents
	 */
	private String readGithubFile(String fullName)
	{
		String text = "";
		BufferedReader bufferedReader = null;
		try
		{
			bufferedReader = new BufferedReader(new InputStreamReader(new URL(fullName).openStream()));
			if (bufferedReader != null)
			{
				String inputLine;
				while ((inputLine = bufferedReader.readLine()) != null)
				{
					text = text + inputLine + "\n";
				}
			}
		}
		catch (IOException e)
		{
			LOG.error("Error handling file, message is: " + e);
		}
		finally
		{
			try
			{
				bufferedReader.close();
			}
			catch (IOException e)
			{
				LOG.error("Error closing the file, message is: " + e);
			}
		}

		return text;
	}
}
