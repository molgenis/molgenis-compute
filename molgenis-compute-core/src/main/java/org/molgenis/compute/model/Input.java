package org.molgenis.compute.model;

import org.molgenis.compute.Validator;

/**
 * Input for a protocol
 */
public class Input
{
	public static final String TYPE_STRING = "string";
	public static final String TYPE_LIST = "list";
	public enum Type {STRING, LIST}
	
	// unique name within a protocol
	private String name;
	// description of this parameter
	private String description;
	private Type type;

	private boolean combineLists = true;
	private boolean isKnownRunTime = false;

	private boolean combinedListsNotation = false;

	public Input(String name)
	{
		this.setName(name);
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		// Validate that 'name' does only contain a-zA-Z0-9
		Validator.validateParameterName(name);
		this.name = name;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public Type getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = Type.valueOf(type.toUpperCase());
	}

	public boolean isKnownRunTime()
	{
		return isKnownRunTime;
	}

	public void setKnownRunTime(boolean knownRunTime)
	{
		isKnownRunTime = knownRunTime;
	}

	public void setCombineLists(boolean combineLists)
	{
		this.combineLists = combineLists;
	}

	public boolean isCombineLists()
	{
		return combineLists;
	}

	public boolean isCombinedListsNotation()
	{
		return combinedListsNotation;
	}

	public void setCombinedListsNotation(boolean combinedListsNotation)
	{
		this.combinedListsNotation = combinedListsNotation;
	}
}
