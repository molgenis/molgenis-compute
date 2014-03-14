package org.molgenis.compute5.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.molgenis.compute5.ComputeProperties;
import org.molgenis.compute5.model.*;
import org.molgenis.compute5.urlreader.UrlReader;
import org.molgenis.data.Entity;
import org.molgenis.data.csv.CsvRepository;
import org.molgenis.data.support.MapEntity;

/** Parser for the workflow csv */
public class WorkflowCsvParser
{
	private Vector<String> stepNames = new Vector();
	private ProtocolParser parser = new ProtocolParser();
	private UrlReader urlReader = new UrlReader();
	private ProtocolAnalyser protocolAnalyser = new ProtocolAnalyser();
	public static final String WORKFLOW_COMMENT_SIGN = "#";

	public Workflow parse(String workflowPath, ComputeProperties computeProperties) throws IOException
	{
		try
		{
			CsvRepository reader = null;
			if(computeProperties.isWebWorkflow)
			{
				File workflowFile = urlReader.createFileFromGithub(computeProperties.webWorkflowLocation,
						workflowPath);
//				reader = new CsvRepository(new BufferedReader(new FileReader(workflowFile)), ",", null);
				reader = new CsvRepository(workflowFile, ',', null);

			}
			else
				reader = new CsvRepository(new File(workflowPath), ',', null);
//				reader = new CsvRepository(new BufferedReader(new FileReader(workflowPath)), ",", null);

			Workflow wf = new Workflow();

			for (Entity row : reader)
			{
				// check value
				if (row.get(Parameters.STEP_HEADING_IN_WORKFLOW) == null)
					throw new IOException("required column '" + Parameters.STEP_HEADING_IN_WORKFLOW +
							"' is missing in row " + row);
				if (row.get(Parameters.PROTOCOL_HEADING_IN_WORKFLOW) == null)
					throw new IOException("required column '" + Parameters.PROTOCOL_HEADING_IN_WORKFLOW +
							"' is missing in row " + row);

				String stepName = row.getString(Parameters.STEP_HEADING_IN_WORKFLOW);

				if(stepName.startsWith(WORKFLOW_COMMENT_SIGN))
					continue;

				Step step = new Step(stepName);
				stepNames.add(stepName);
				File workflowDir = new File(workflowPath).getParentFile();
				String fileName = row.getString(Parameters.PROTOCOL_HEADING_IN_WORKFLOW);

				Protocol protocol = parser.parse(workflowDir, fileName, computeProperties);
				protocolAnalyser.analysesProtocolVariables(protocol);

				step.setProtocol(protocol);
				String strParameters = row.getString(Parameters.PARAMETER_MAPPING_HEADING_IN_WORKFLOW);
				if(strParameters!=null)
				{
					HashSet<String> dependencies = parseParametersDependencies(strParameters);
					if(dependencies.size() > 0)
						step.setPreviousSteps(dependencies);

					if(resultParsing.size() > 0)
						step.setParametersMapping(resultParsing);
				}

				Set<Input> inputs = protocol.getInputs();
				for(Input input : inputs)
				{
					step.addParameter(input.getName());
				}

				wf.addStep(step);
			}

			return wf;
		}
		catch (IOException e)
		{
			throw new IOException("Parsing of workflow failed: " + e.getMessage()
					+ ".\nThe workflow csv requires columns " + Parameters.STEP_HEADING_IN_WORKFLOW + "," + Parameters.PROTOCOL_HEADING_IN_WORKFLOW + "," + Parameters.PARAMETER_MAPPING_HEADING_IN_WORKFLOW + ".");
		}
	}

	private Map<String, String> resultParsing = null;

	private HashSet<String> parseParametersDependencies(String string) throws IOException
	{
		HashSet<String> dependencies = new HashSet();
		// split per ; and then key/value pairs are split by "="
		resultParsing = new LinkedHashMap<String, String>();

		String[] pairs = string.split(";");

		for (String pair : pairs)
		{
			String[] expr = pair.split("=");
			if (expr.length > 1)
			{
				resultParsing.put(expr[0], expr[1]);
				//here find dependencies from parameters names
				expr[1] = expr[1].replace(Parameters.STEP_PARAM_SEP_PROTOCOL, Parameters.TRIPLE_UNDERSCORE);
				String [] subExpr = expr[1].split(Parameters.TRIPLE_UNDERSCORE);
				if(subExpr.length > 1)
					if(stepNames.contains(subExpr[0]))
						dependencies.add(subExpr[0]);

			}
			else
			{
				if(stepNames.contains(expr[0]))
					dependencies.add(expr[0]);
			}
		}

		return dependencies;
	}
}
