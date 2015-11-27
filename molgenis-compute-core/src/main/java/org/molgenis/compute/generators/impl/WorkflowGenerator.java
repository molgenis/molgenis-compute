package org.molgenis.compute.generators.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

public class WorkflowGenerator
{
	private static final Logger LOG = Logger.getLogger(WorkflowGenerator.class);
	public static final String WORKFlOW_NAME = "workflows/myworkflow";

	public WorkflowGenerator(String createWorkflowDirectory)
	{
		File target = new File(createWorkflowDirectory);
		File file = new File(Thread.currentThread().getContextClassLoader().getResource(WORKFlOW_NAME).getFile());

		try
		{
			copyFolder(file, target.getAbsoluteFile());
			LOG.info("... Basic workflow structure is created");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	public static void copyFolder(File source, File destination) throws IOException
	{
		if (source.isDirectory())
		{
			if (!destination.exists())
			{
				destination.mkdirs();
			}

			String files[] = source.list();

			for (String file : files)
			{
				File sourceFile = new File(source, file);
				File destinationFile = new File(destination, file);

				copyFolder(sourceFile, destinationFile);
			}
		}
		else
		{
			InputStream inputStream = new FileInputStream(source);
			OutputStream outputStream = new FileOutputStream(destination);

			byte[] buffer = new byte[1024];

			int length;

			while ((length = inputStream.read(buffer)) > 0)
			{
				outputStream.write(buffer, 0, length);
			}

			inputStream.close();
			outputStream.close();
		}
	}
}