package org.molgenis.compute5.sysexecutor;

import java.io.InputStream;

/**
 * This class allows for the asynchronous reading of an input stream
 */
public interface AsyncStreamReader
{
	/**
	 * Returns the buffer used to instantiate this {@link AsyncStreamReader}
	 * 
	 * @return A string representation of a {@link StringBuffer}
	 */
	public String getBuffer();

	/**
	 * Reads the {@link InputStream} used to instantiate this {@link AsyncStreamReader} and writes every line as an info
	 * log
	 */
	public void run();

	/**
	 * When called stops the reading if supplied {@link InputStream}
	 */
	public void stopReading();
}
