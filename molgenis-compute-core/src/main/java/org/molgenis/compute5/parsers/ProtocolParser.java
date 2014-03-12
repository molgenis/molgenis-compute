package org.molgenis.compute5.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.molgenis.compute5.ComputeProperties;
import org.molgenis.compute5.model.Input;
import org.molgenis.compute5.model.Output;
import org.molgenis.compute5.model.Parameters;
import org.molgenis.compute5.model.Protocol;
import org.molgenis.compute5.urlreader.UrlReader;

public class ProtocolParser
{
	/**
	 * 
	 *
	 * @param workflowDir
	 *            , used as primary search path. If missing it uses runtime path/absolute path
	 * @param protocolPath
	 * @param computeProperties
	 * @return
	 * @throws IOException
	 */
	private UrlReader urlReader = new UrlReader();

	public Protocol parse(File workflowDir, String protocolPath, ComputeProperties computeProperties) throws IOException
	{
		try
		{
			File templateFile = null;
			// first test path within workflowDir

			if(computeProperties.isWebWorkflow)
			{
				templateFile = urlReader.createFileFromGithub(computeProperties.webWorkflowLocation,
						protocolPath);
			}
			else
			{
				templateFile = new File(workflowDir.getAbsolutePath() + "/" + protocolPath);
				if (!templateFile.exists())
				{
					//what is going on here?
					templateFile = new File(protocolPath);
					if (!templateFile.exists()) throw new IOException("protocol '" + protocolPath + "' cannot be found");
				}
			}

			// start reading
			Protocol protocol = new Protocol(protocolPath);
			String ext = FilenameUtils.getExtension(protocolPath);

			protocol.setType(ext);

			String description = "";
			String template = "";

			BufferedReader reader = new BufferedReader(new FileReader(templateFile));
			try
			{

				// Then read the non-# as template

				// need to harvest all lines that start with #
				// need to harvest all other lines
				String line;
				while ((line = reader.readLine()) != null)
				{
					// Always add line to protocol
					template += line + "\n";

					if (line.startsWith("#"))
					{
						// remove #, trim spaces, then split on " "
						line = line.substring(1).trim();
//						List<String> els = new ArrayList<String>();
//						Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(line);
//						while (m.find())
//							els.add(m.group(1));

						//make a list out of line with spaces and commas
						String[] values = line.replaceAll("^[,\\s]+", "").split("[,\\s]+");
						List<String> els = Arrays.asList(values);

						if (els.size() > 0)
						{
							if (els.get(0).equals("MOLGENIS"))
							{
								for (int i = 1; i < els.size(); i++)
								{
									if (els.get(i).startsWith(Parameters.QUEUE)) protocol.setQueue(els.get(i).substring(
											Parameters.QUEUE.length() + 1));

									if (els.get(i).startsWith(Parameters.WALLTIME)) protocol.setWalltime(els.get(i).substring(
											Parameters.WALLTIME.length() + 1));

									if (els.get(i).startsWith(Parameters.NODES)) protocol.setNodes(els.get(i).substring(
											Parameters.NODES.length() + 1));

									if (els.get(i).startsWith(Parameters.PPN)) protocol.setPpn(els.get(i).substring(
											Parameters.PPN.length() + 1));

									if (els.get(i).startsWith(Parameters.MEMORY)) protocol.setMemory(els.get(i).substring(
											Parameters.MEMORY.length() + 1));

								}
							}
							// description?
							else if (els.get(0).equals("description") && els.size() > 1)
							{
								// add all elements
								for (int i = 1; i < els.size(); i++)
								{
									description += els.get(i) + " ";
								}
								description += "\n";
							}

							// input, syntax = "#input inputVarName1, inputVarName2"
							else if (els.get(0).equals(Parameters.STRING)
									|| els.get(0).equals(Parameters.LIST_INPUT))
							{
								boolean allUniqueInputsCombination = false;
								if(els.get(0).equals(Parameters.LIST_INPUT) &&
										els.size() > 2)
								{
									//see folding tests
									allUniqueInputsCombination = true;
								}

								// assume name column
								if (els.size() < 2)
									throw new IOException(
										"param requires 'name', e.g. '#string input1'");

								for(int i = 1; i < els.size(); i++)
								{
									Input input = new Input(els.get(i));
									input.setType(els.get(0));
									input.foldingTypeUniqueCombination(allUniqueInputsCombination);
									protocol.addInput(input);
								}
							}

							// output, syntax = "#output outputVarName1, outputVarName2"
							else if (els.get(0).equals("output"))
							{
								if (els.size() < 2) throw new IOException(
										"output requires 'name', e.g. '#output myOutputVariable'");

								for(int i = 1; i < els.size(); i++)
								{
									Output output = new Output(els.get(i));
									output.setValue(Parameters.NOTAVAILABLE);
									protocol.addOutput(output);
								}

							}
						}
					}
				}
			}
			finally
			{
				reader.close();
			}
			protocol.setDescription(description);
			protocol.setTemplate(template);
			return protocol;
		}
		catch (Exception e)
		{
			throw new IOException("Parsing of protocol " + protocolPath + " failed: " + e.getMessage());
		}

	}
}