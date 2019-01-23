package org.molgenis.compute.parsers.impl;

import static org.testng.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import org.molgenis.compute.model.Input;
import org.molgenis.compute.model.Output;
import org.molgenis.compute.model.Protocol;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ProtocolParserImplTest {
  private ProtocolParserImpl protocolParser;

  @BeforeMethod
  public void setUpBeforeMethod() {
    protocolParser = new ProtocolParserImpl();
  }

  @Test
  public void testCreateProtocol() throws IOException {
    String protocolName = "step0";
    String protocolType = "ftl";
    String template =
        "#MOLGENIS queue=short_queue mem=4gb walltime=05:59:00 nodes=1 ppn=1\n"
            + "#description protocol description\n"
            + "#string inString\n"
            + "#list inList\n"
            + "#output out\n"
            + "\n"
            + "for item in \"${inList[@]}\"\n"
            + "do\n"
            + "    echo ${item}\n"
            + "done"
            + "out=${inString}_hasBeenInStep1";

    Input expectedStringInput = new Input("inString");
    expectedStringInput.setType("string");
    Input expectedListInput = new Input("inList");
    expectedListInput.setType("list");
    Output expectedOutput = new Output("out");
    expectedOutput.setValue("notavailable");

    Protocol expectedProtocol = new Protocol("step0");
    expectedProtocol.setType("ftl");
    expectedProtocol.setDescription("protocol description \n");
    expectedProtocol.setTemplate(
        "#MOLGENIS queue=short_queue mem=4gb walltime=05:59:00 nodes=1 ppn=1\n"
            + "#description protocol description\n"
            + "#string inString\n"
            + "#list inList\n"
            + "#output out\n"
            + "\n"
            + "for item in \"${inList[@]}\"\n"
            + "do\n"
            + "    echo ${item}\n"
            + "done"
            + "out=${inString}_hasBeenInStep1\n");
    expectedProtocol.setMemory("4gb");
    expectedProtocol.setNodes("1");
    expectedProtocol.setPpn("1");
    expectedProtocol.setQueue("short_queue");
    expectedProtocol.setWalltime("05:59:00");
    expectedProtocol.addInput(expectedStringInput);
    expectedProtocol.addInput(expectedListInput);
    expectedProtocol.addOutput(expectedOutput);

    try (BufferedReader bufferedReader = new BufferedReader(new StringReader(template))) {
      Protocol protocol = protocolParser.createProtocol(protocolName, protocolType, bufferedReader);
      assertEquals(protocol, expectedProtocol);
    }
  }

  @Test
  public void testCreateProtocolWithoutHeader() throws IOException {
    String protocolName = "step0";
    String protocolType = "ftl";
    String template = "";

    Protocol expectedProtocol = new Protocol(protocolName);
    expectedProtocol.setType("ftl");
    expectedProtocol.setDescription("");
    expectedProtocol.setTemplate("");

    try (BufferedReader bufferedReader = new BufferedReader(new StringReader(template))) {
      Protocol protocol = protocolParser.createProtocol(protocolName, protocolType, bufferedReader);
      assertEquals(protocol, expectedProtocol);
    }
  }
}
