package org.molgenis.compute5.urlreader;

import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.net.URL;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: hvbyelas
 * Date: 11/6/13
 * Time: 3:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class UrlReader
{
	private String readFileGithub(String fullName)
	{
		String text = "";

		URL protocol = null;
		InputStream stream = null;
		BufferedReader in = null;
		try
		{
			protocol = new URL(fullName);
			stream = protocol.openStream();
			InputStreamReader reader = new InputStreamReader(stream);

			in = new BufferedReader(reader);
		}
		catch (IOException e)
		{
			System.out.println("problem with remote file: " + fullName);
//			e.printStackTrace();
		}

		if (in != null)
		{
			String inputLine;
			try
			{
				while ((inputLine = in.readLine()) != null)
					text = text + "\n" + inputLine;
				in.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return text;
	}

	public File createFileFromGithub(String root, String filename)
	{
		File temp = null;
		if(filename.startsWith("./"))
			filename = filename.substring(2);
		String fullName = root + "/" + filename;

		String text = readFileGithub(fullName);

		String name = FilenameUtils.getBaseName(filename);
		String ext = FilenameUtils.getExtension(filename);


		try {
			// Create temp file.
			temp = File.createTempFile(name, "." + ext);

			// Delete temp file when program exits.
			temp.deleteOnExit();

			// Write to temp file
			BufferedWriter out = new BufferedWriter(new FileWriter(temp));
			out.write(text);
			out.close();
		} catch (IOException e)
		{
		}
		return temp;
	}

}
