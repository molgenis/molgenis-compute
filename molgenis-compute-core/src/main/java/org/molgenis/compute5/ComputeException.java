package org.molgenis.compute5;

public class ComputeException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public ComputeException(String result)
	{
		super(result);
	}

}
