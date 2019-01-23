package org.molgenis.compute.parsers.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.molgenis.compute.ComputeProperties;
import org.molgenis.compute.model.Input;
import org.molgenis.compute.model.Output;
import org.molgenis.compute.model.Parameters;
import org.molgenis.compute.model.Protocol;
import org.molgenis.compute.parsers.ProtocolParser;
import org.molgenis.compute.urlreader.impl.UrlReaderImpl;

public class ProtocolParserImpl implements ProtocolParser {
  private UrlReaderImpl urlReaderImpl = new UrlReaderImpl();

  @Override
  public Protocol parse(
      File workflowDirectory, String protocolPath, ComputeProperties computeProperties)
      throws IOException {
    try {
      File templateFile = null;
      // first test path within workflowDir

      if (computeProperties.isWebWorkflow) {
        templateFile =
            urlReaderImpl.createFileFromGithub(computeProperties.webWorkflowLocation, protocolPath);
      } else {
        templateFile = new File(workflowDirectory.getAbsolutePath() + "/" + protocolPath);
        if (!templateFile.exists()) {
          // what is going on here?
          templateFile = new File(protocolPath);
          if (!templateFile.exists())
            throw new IOException("protocol '" + protocolPath + "' cannot be found");
        }
      }
      String fileExtension = FilenameUtils.getExtension(protocolPath);

      // start reading
      try (BufferedReader reader = new BufferedReader(new FileReader(templateFile))) {
        return createProtocol(protocolPath, fileExtension, reader);
      }

    } catch (Exception e) {
      throw new IOException("Parsing of protocol " + protocolPath + " failed: " + e.getMessage());
    }
  }

  /** package-private for testability. */
  Protocol createProtocol(String protocolName, String protocolType, BufferedReader reader)
      throws IOException {
    Protocol protocol = new Protocol(protocolName);
    protocol.setType(protocolType);

    String description = "";
    String template = "";

    // Then read the non-# as template

    // need to harvest all lines that start with #
    // need to harvest all other lines
    String line;
    while ((line = reader.readLine()) != null) {
      // Always add line to protocol
      template += line + "\n";

      if (line.startsWith("#")) {
        // remove #, trim spaces, then split on " "
        line = line.substring(1).trim();

        // make a list out of line with spaces and commas
        List<String> values = Arrays.asList(line.replaceAll("^[,\\s]+", "").split("[,\\s]+"));

        if (values.size() > 0) {
          if (values.get(0).equals("MOLGENIS")) {
            for (int i = 1; i < values.size(); i++) {
              if (values.get(i).startsWith(Parameters.QUEUE))
                protocol.setQueue(values.get(i).substring(Parameters.QUEUE.length() + 1));

              if (values.get(i).startsWith(Parameters.WALLTIME))
                protocol.setWalltime(values.get(i).substring(Parameters.WALLTIME.length() + 1));

              if (values.get(i).startsWith(Parameters.NODES))
                protocol.setNodes(values.get(i).substring(Parameters.NODES.length() + 1));

              if (values.get(i).startsWith(Parameters.PPN))
                protocol.setPpn(values.get(i).substring(Parameters.PPN.length() + 1));

              if (values.get(i).startsWith(Parameters.MEMORY))
                protocol.setMemory(values.get(i).substring(Parameters.MEMORY.length() + 1));
            }
          }
          // description?
          else if (values.get(0).equals("description") && values.size() > 1) {
            // add all elements
            for (int i = 1; i < values.size(); i++) {
              description += values.get(i) + " ";
            }
            description += "\n";
          }

          // input, syntax = "#input inputVarName1, inputVarName2"
          else if (values.get(0).equals(Parameters.STRING)
              || values.get(0).equals(Parameters.LIST_INPUT)) {

            boolean combinedListsNotation = false;
            if (values.get(0).equals(Parameters.LIST_INPUT) && values.size() > 2) {
              // see folding tests
              combinedListsNotation = true;
            }

            // assume name column
            if (values.size() < 2)
              throw new IOException("param requires 'name', e.g. '#string input1'");

            for (int i = 1; i < values.size(); i++) {
              Input input = new Input(values.get(i));
              input.setType(values.get(0));
              input.setCombinedListsNotation(combinedListsNotation);
              protocol.addInput(input);
            }
          }

          // output, syntax = "#output outputVarName1, outputVarName2"
          else if (values.get(0).equals("output")) {
            if (values.size() < 2)
              throw new IOException("output requires 'name', e.g. '#output myOutputVariable'");

            for (int i = 1; i < values.size(); i++) {
              Output output = new Output(values.get(i));
              output.setValue(Parameters.NOTAVAILABLE);
              protocol.addOutput(output);
            }
          }
        }
      }
    }
    protocol.setDescription(description);
    protocol.setTemplate(template);

    return protocol;
  }
}
