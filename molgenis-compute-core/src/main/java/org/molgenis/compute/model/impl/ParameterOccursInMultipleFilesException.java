package org.molgenis.compute.model.impl;

public class ParameterOccursInMultipleFilesException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	private final String name;

	private String fileName;

	public ParameterOccursInMultipleFilesException(String name)
	{
		this.name = name;
	}

	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	@Override
	public String getMessage()
	{
		return "Parameter occurs in multiple files. [ " + name + " ] is present in [ " + fileName
				+ "] and one another parameters file";

	}

}
