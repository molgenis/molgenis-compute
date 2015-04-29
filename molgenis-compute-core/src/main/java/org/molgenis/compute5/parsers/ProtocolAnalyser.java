package org.molgenis.compute5.parsers;

import org.molgenis.compute5.model.Protocol;

/**
 * This class analyzes protocol variables to see if they can be broken
 */
public interface ProtocolAnalyser
{
	/**
	 * Goes through the protocol template to see if the protocol has the correct format
	 * 
	 * @param protocol
	 */
	public void analyseProtocolVariables(Protocol protocol);
}
