package org.molgenis.compute.model.impl;

/**
 * This class registers batches in a String[]
 */
public class Batch
{
	public int number = -1;
	public String[] batches;

	public int filledSize = 0;

	public Batch(int num, int size)
	{
		this.number = num;
		batches = new String[size];
	}

	/**
	 * Add a value to the String[] of batches based on the filledSize??
	 * 
	 * @param value
	 */
	public void addValue(String value)
	{
		batches[filledSize] = value;
		filledSize++;
	}
}