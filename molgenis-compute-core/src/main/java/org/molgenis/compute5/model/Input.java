package org.molgenis.compute5.model;

import org.molgenis.compute5.Validator;

/** Input for a protocol.*/
public class Input
{
	public static final String TYPE_STRING = "string";
	public static final String TYPE_LIST = "list";

	//unique name within a protocol
	private String name;
	//description of this parameter
	private String description;
	private String type;

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
		//Validate that 'name' does only contain a-zA-Z0-9
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

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
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
