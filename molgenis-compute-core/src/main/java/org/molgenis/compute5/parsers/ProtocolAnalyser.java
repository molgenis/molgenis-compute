package org.molgenis.compute5.parsers;

import org.apache.log4j.Logger;
import org.molgenis.compute5.model.Input;
import org.molgenis.compute5.model.Output;
import org.molgenis.compute5.model.Protocol;

/**
 * Created with IntelliJ IDEA.
 * User: hvbyelas
 * Date: 11/8/13
 * Time: 9:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class ProtocolAnalyser
{
	private static final String strStart = "${";
	private static final String strEnd = "}";

	private Protocol protocol = null;

	private static final Logger LOG = Logger.getLogger(ProtocolAnalyser.class);

	public void analysesProtocolVariables(Protocol p)
	{
		this.protocol = p;
		String template = protocol.getTemplate();
		int position = 0;

		while (true)
		{
			int iStart = template.indexOf(strStart, position);
			if(iStart == -1)
				return;
			int iEnd = template.indexOf(strEnd, iStart);
			if(iEnd == -1)
			{
				LOG.warn("Protocol [" + protocol + "] can be broken");
				return;
			}
			position = iEnd + 1;

			String variable = template.substring(iStart + strStart.length(), iEnd);
			analyseVariable(variable);
		}
	}

	private void analyseVariable(String variable)
	{
		int end = variable.indexOf("[");
		if(end > -1)
			variable = variable.substring(0, end);

		boolean warn = true;

		for(Input input : protocol.getInputs())
		{
			String name = input.getName();
			if(name.equals(variable))
			{
				warn = false;
				break;
			}
		}

		if(warn)
			for(Output output : protocol.getOutputs())
			{
				String name = output.getName();
				if(name.equals(variable))
				{
					warn = false;
					break;
				}
			}

		if(warn)
		{
			LOG.warn("Variable [" + variable + "] in Protocol [" + protocol.getName() + "] can be unmapped");
		}
	}
}
