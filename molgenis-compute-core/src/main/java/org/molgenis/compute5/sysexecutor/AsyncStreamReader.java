package org.molgenis.compute5.sysexecutor;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.molgenis.compute5.generators.TaskGenerator;

class AsyncStreamReader extends Thread
{
	private static final Logger LOG = Logger.getLogger(TaskGenerator.class);

	private StringBuffer buffer = null;
	private InputStream inputStream = null;
	private boolean stop = false;
	private String newLine = null;

	public AsyncStreamReader(InputStream inputStream, StringBuffer buffer)
	{
		this.inputStream = inputStream;
		this.buffer = buffer;
		this.newLine = System.getProperty("line.separator");
	}

	public String getBuffer()
	{
		return buffer.toString();
	}

	public void run()
	{
		try
		{
			readCommandOutput();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public void stopReading()
	{
		stop = true;
	}

	private void readCommandOutput() throws IOException
	{
		BufferedReader bufOut = new BufferedReader(new InputStreamReader(inputStream));
		String line = null;
		while ((stop == false) && ((line = bufOut.readLine()) != null))
		{
			buffer.append(line + newLine);
			LOG.debug(line);
		}
		bufOut.close();
	}
}
