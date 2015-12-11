package org.molgenis.compute.parsers.impl;

import org.apache.log4j.Logger;
import org.molgenis.compute.model.Input;
import org.molgenis.compute.model.Output;
import org.molgenis.compute.model.Protocol;
import org.molgenis.compute.parsers.ProtocolAnalyser;

public class ProtocolAnalyserImpl implements ProtocolAnalyser
{
	private static final Logger LOG = Logger.getLogger(ProtocolAnalyserImpl.class);

	private static final String strStart = "${";
	private static final String strEnd = "}";

	@Override
	public void analyseProtocolVariables(Protocol protocol)
	{
		String template = protocol.getTemplate();
		int position = 0;

		while (true)
		{
			int iStart = template.indexOf(strStart, position);
			if (iStart == -1) return;
			int iEnd = template.indexOf(strEnd, iStart);
			if (iEnd == -1)
			{
				LOG.warn("Protocol [" + protocol + "] can be broken");
				return;
			}
			position = iEnd + 1;

			String variable = template.substring(iStart + strStart.length(), iEnd);
			analyseVariable(variable, protocol);
		}
	}

	/**
	 * Method checks to see if one of the output or input names equals the submitted variable name. Throws a warning if
	 * it does not.
	 * 
	 * @param variable
	 * @param protocol
	 */
	private void analyseVariable(String variable, Protocol protocol)
	{
		int end = variable.indexOf("[");
		if (end > -1) variable = variable.substring(0, end);

		boolean warn = true;

		for (Input input : protocol.getInputs())
		{
			String name = input.getName();
			if (name.equals(variable))
			{
				warn = false;
				break;
			}
		}

		if (warn) for (Output output : protocol.getOutputs())
		{
			String name = output.getName();
			if (name.equals(variable))
			{
				warn = false;
				break;
			}
		}

		if (warn)
		{
			LOG.warn("Variable [" + variable + "] in Protocol [" + protocol.getName() + "] perhaps is not getting mapped");
		}
	}
}
