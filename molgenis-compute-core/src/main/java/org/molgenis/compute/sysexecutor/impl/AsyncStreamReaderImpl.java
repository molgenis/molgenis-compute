package org.molgenis.compute.sysexecutor.impl;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.molgenis.compute.sysexecutor.AsyncStreamReader;

class AsyncStreamReaderImpl extends Thread implements AsyncStreamReader
{
	private static final Logger LOG = Logger.getLogger(AsyncStreamReaderImpl.class);

	private StringBuffer buffer = null;
	private InputStream inputStream = null;
	private String newLine = System.getProperty("line.separator");
	private boolean stop = false;

	public AsyncStreamReaderImpl(InputStream inputStream, StringBuffer buffer)
	{
		this.inputStream = inputStream;
		this.buffer = buffer;
	}

	@Override
	public String getBuffer()
	{
		return buffer.toString();
	}

	@Override
	public void run()
	{
		BufferedReader bufferedReader = null;
		try
		{
			bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			String line = null;
			while ((stop == false) && ((line = bufferedReader.readLine()) != null))
			{
				buffer.append(line + newLine);
				LOG.info(line);
			}
		}
		catch (Exception e)
		{
			LOG.error("Error while reading the input stream, message is: " + e);
		}
		finally
		{
			try
			{
				bufferedReader.close();
			}
			catch (IOException e)
			{
				LOG.error("Failed to close file: " + e);
			}
		}
	}

	@Override
	public void stopReading()
	{
		stop = true;
	}
}
