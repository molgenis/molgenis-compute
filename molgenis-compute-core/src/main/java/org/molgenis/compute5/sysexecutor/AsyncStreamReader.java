package org.molgenis.compute5.sysexecutor;

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
	 * 
	 */
	public void run();

	/**
	 * 
	 */
	public void stopReading();
}
