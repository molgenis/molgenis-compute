package org.molgenis.compute5.urlreader;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import java.io.*;
import java.net.URL;

/**
 * Class that handles the reading of URL's and writes URL content to files
 */
public class UrlReader
{
	private static final Logger LOG = Logger.getLogger(UrlReader.class);

	/**
	 * Reads output from a github URL as text and writes it to a file
	 * 
	 * @param root
	 * @param filename
	 * 
	 * @return The location of the temporary file that was created with the contents of the submitted github filename
	 */
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
