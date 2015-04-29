package org.molgenis.compute5.parsers.impl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.molgenis.compute5.ComputeProperties;
import org.molgenis.compute5.model.Input;
import org.molgenis.compute5.model.Parameters;
import org.molgenis.compute5.model.Protocol;
import org.molgenis.compute5.model.Step;
import org.molgenis.compute5.model.Workflow;
import org.molgenis.compute5.parsers.WorkflowCsvParser;
import org.molgenis.compute5.urlreader.UrlReader;

import au.com.bytecode.opencsv.CSVReader;

public class WorkflowCsvParserImpl implements WorkflowCsvParser
{
	private Vector<String> stepNames = new Vector<String>();
	private ProtocolParserImpl parser = new ProtocolParserImpl();
	private UrlReader urlReader = new UrlReader();
	private ProtocolAnalyser protocolAnalyser = new ProtocolAnalyser();
	public static final String WORKFLOW_COMMENT_SIGN = "#";
	private Map<String, String> resultParsing = null;

	public Workflow parse(String workflowPath, ComputeProperties computeProperties) throws IOException
	{
		CSVReader reader = null;
		try
		{
			if (computeProperties.isWebWorkflow)
			{
				File workflowFile = urlReader.createFileFromGithub(computeProperties.webWorkflowLocation, workflowPath);
				reader = new CSVReader(new FileReader(workflowFile), ',');

			}
			else reader = new CSVReader(new FileReader(workflowPath), ',');

			Workflow workFlow = new Workflow();

			List<String[]> listLines = reader.readAll();
			boolean isFirstRow = true;

			// current format name, protocol, dependencies
			for (String[] row : listLines)
			{
				if (isFirstRow)
				{
					if (row[0].equalsIgnoreCase(Parameters.STEP_HEADING_IN_WORKFLOW)
							&& row[1].equalsIgnoreCase(Parameters.PROTOCOL_HEADING_IN_WORKFLOW)
							&& row[2].equalsIgnoreCase(Parameters.PARAMETER_MAPPING_HEADING_IN_WORKFLOW)) isFirstRow = false;
					else throw new IOException("Error in the header of workflow file");
				}
				else
				{

					if ((row[0] == null) || (row[0].length() == 0)) throw new IOException("required column '"
							+ Parameters.STEP_HEADING_IN_WORKFLOW + "' is missing in row " + row);
					if ((row[1] == null) || (row[1].length() == 0)) throw new IOException("required column '"
							+ Parameters.PROTOCOL_HEADING_IN_WORKFLOW + "' is missing in row " + row);

					String stepName = row[0];

					if (stepName.startsWith(WORKFLOW_COMMENT_SIGN)) continue;

					Step step = new Step(stepName);
					stepNames.add(stepName);
					File workflowDir = new File(workflowPath).getParentFile();
					String fileName = row[1];

					Protocol protocol = parser.parse(workflowDir, fileName, computeProperties);
					protocolAnalyser.analyseProtocolVariables(protocol);

					step.setProtocol(protocol);
					String strParameters = row[2];
					if (strParameters != null)
					{
						HashSet<String> dependencies = parseParametersDependencies(strParameters);
						if (dependencies.size() > 0) step.setPreviousSteps(dependencies);

						if (resultParsing.size() > 0) step.setParametersMapping(resultParsing);
					}

					Set<Input> inputs = protocol.getInputs();
					for (Input input : inputs)
					{
						step.addParameter(input.getName());
					}

					workFlow.addStep(step);
				}

			}
			return workFlow;
		}
		catch (IOException e)
		{
			throw new IOException("Parsing of workflow failed: " + e.getMessage()
					+ ".\nThe workflow csv requires columns " + Parameters.STEP_HEADING_IN_WORKFLOW + ","
					+ Parameters.PROTOCOL_HEADING_IN_WORKFLOW + "," + Parameters.PARAMETER_MAPPING_HEADING_IN_WORKFLOW
					+ ".");
		}
		finally
		{
			reader.close();
		}
	}

	private HashSet<String> parseParametersDependencies(String string) throws IOException
	{
		HashSet<String> dependencies = new HashSet<String>();
		// split per ; and then key/value pairs are split by "="
		resultParsing = new LinkedHashMap<String, String>();

		String[] pairs = string.split(";");

		for (String pair : pairs)
		{
			String[] expr = pair.split("=");
			if (expr.length > 1)
			{
				resultParsing.put(expr[0], expr[1]);
				// here find dependencies from parameters names
				expr[1] = expr[1].replace(Parameters.STEP_PARAM_SEP_PROTOCOL, Parameters.TRIPLE_UNDERSCORE);
				String[] subExpr = expr[1].split(Parameters.TRIPLE_UNDERSCORE);
				if (subExpr.length > 1) if (stepNames.contains(subExpr[0])) dependencies.add(subExpr[0]);

			}
			else
			{
				if (stepNames.contains(expr[0])) dependencies.add(expr[0]);
			}
		}

		return dependencies;
	}
}
