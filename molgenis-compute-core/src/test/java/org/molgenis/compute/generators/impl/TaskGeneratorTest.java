package org.molgenis.compute.generators.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.molgenis.compute.model.Context;
import org.molgenis.compute.model.Parameters;
import org.molgenis.compute.model.Protocol;
import org.molgenis.compute.model.Step;
import org.molgenis.compute.model.StringStore;
import org.molgenis.compute.model.impl.DataEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TaskGeneratorTest {
  private Context context;
  private ScriptGenerator scriptGenerator;
  private StringStore stringStore;
  private TaskGenerator taskGenerator;

  @BeforeMethod
  public void setUpBeforeMethod() {
    context = mock(Context.class);
    when(context.getParameters()).thenReturn(mock(Parameters.class));
    scriptGenerator = mock(ScriptGenerator.class);
    stringStore = mock(StringStore.class);
    taskGenerator = new TaskGenerator(context, scriptGenerator, stringStore);
  }

  @Test
  public void testGetProtocolParam() {
    Protocol protocol =
        when(mock(Protocol.class).getName()).thenReturn("protocols/protocol.sh").getMock();
    when(protocol.getType()).thenReturn("sh");
    assertEquals(taskGenerator.getProtocolParam(protocol), "protocols_protocol");
  }

  @Test
  public void testSetProtocolWalltime() {
    String walltime = "01:23:45";
    Protocol protocol = when(mock(Protocol.class).getWalltime()).thenReturn(walltime).getMock();
    Step step = when(mock(Step.class).getProtocol()).thenReturn(protocol).getMock();
    DataEntity localParamater = mock(DataEntity.class);
    DataEntity defaultResourcesMap = mock(DataEntity.class);
    taskGenerator.setProtocolWalltime(step, localParamater, defaultResourcesMap);
    verify(localParamater).set("walltime", walltime);
  }

  @Test
  public void testSetProtocolWalltimeNotDefinedGlobal() {
    String walltime = "01:23:45";
    Protocol protocol = mock(Protocol.class);
    when(protocol.getName()).thenReturn("MyProtocol");
    when(protocol.getType()).thenReturn("sh");
    Step step = when(mock(Step.class).getProtocol()).thenReturn(protocol).getMock();
    DataEntity localParamater = mock(DataEntity.class);
    DataEntity defaultResourcesMap = mock(DataEntity.class);
    when(defaultResourcesMap.getString("user_walltime")).thenReturn(walltime);
    taskGenerator.setProtocolWalltime(step, localParamater, defaultResourcesMap);
    verify(localParamater).set("walltime", walltime);
  }

  @Test
  public void testSetProtocolWalltimeNotDefinedGlobalProtocol() {
    String walltime = "01:23:45";
    String protocolName = "step0";
    Protocol protocol = when(mock(Protocol.class).getName()).thenReturn(protocolName).getMock();
    when(protocol.getType()).thenReturn("sh");
    Step step = when(mock(Step.class).getProtocol()).thenReturn(protocol).getMock();
    DataEntity localParamater = mock(DataEntity.class);
    DataEntity defaultResourcesMap = mock(DataEntity.class);
    when(defaultResourcesMap.getString("user_walltime_step0")).thenReturn(walltime);
    taskGenerator.setProtocolWalltime(step, localParamater, defaultResourcesMap);
    verify(localParamater).set("walltime", walltime);
  }

  @Test
  public void testSetProtocolWalltimeNotDefinedDefault() {
    String walltime = "01:23:45";
    Protocol protocol = mock(Protocol.class);
    when(protocol.getName()).thenReturn("MyProtocol");
    when(protocol.getType()).thenReturn("sh");
    when(protocol.getDefaultWalltime()).thenReturn(walltime).getMock();
    Step step = when(mock(Step.class).getProtocol()).thenReturn(protocol).getMock();
    DataEntity localParamater = mock(DataEntity.class);
    DataEntity defaultResourcesMap = mock(DataEntity.class);
    taskGenerator.setProtocolWalltime(step, localParamater, defaultResourcesMap);
    verify(localParamater).set("walltime", walltime);
  }

  @Test
  public void testSetProtocolMemory() {
    String memory = "4gb";
    Protocol protocol = mock(Protocol.class);
    when(protocol.getName()).thenReturn("MyProtocol");
    when(protocol.getType()).thenReturn("sh");
    when(protocol.getMemory()).thenReturn(memory).getMock();
    Step step = when(mock(Step.class).getProtocol()).thenReturn(protocol).getMock();
    DataEntity localParamater = mock(DataEntity.class);
    DataEntity defaultResourcesMap = mock(DataEntity.class);
    taskGenerator.setProtocolMemory(step, localParamater, defaultResourcesMap);
    verify(localParamater).set("mem", memory);
  }

  @Test
  public void testSetProtocolNodes() {
    String nodes = "4";
    Protocol protocol = mock(Protocol.class);
    when(protocol.getName()).thenReturn("MyProtocol");
    when(protocol.getType()).thenReturn("sh");
    when(protocol.getNodes()).thenReturn(nodes).getMock();
    Step step = when(mock(Step.class).getProtocol()).thenReturn(protocol).getMock();
    DataEntity localParamater = mock(DataEntity.class);
    DataEntity defaultResourcesMap = mock(DataEntity.class);
    taskGenerator.setProtocolNodes(step, localParamater, defaultResourcesMap);
    verify(localParamater).set("nodes", nodes);
  }

  @Test
  public void testSetProtocolQueue() {
    String queue = "short";
    Protocol protocol = mock(Protocol.class);
    when(protocol.getName()).thenReturn("MyProtocol");
    when(protocol.getType()).thenReturn("sh");
    when(protocol.getQueue()).thenReturn(queue);
    Step step = when(mock(Step.class).getProtocol()).thenReturn(protocol).getMock();
    DataEntity localParamater = mock(DataEntity.class);
    DataEntity defaultResourcesMap = mock(DataEntity.class);
    taskGenerator.setProtocolQue(step, localParamater, defaultResourcesMap);
    verify(localParamater).set("queue", queue);
  }

  @Test
  public void testSetProtocolPpn() {
    String ppn = "4";
    Protocol protocol = mock(Protocol.class);
    when(protocol.getName()).thenReturn("MyProtocol");
    when(protocol.getType()).thenReturn("sh");
    when(protocol.getPpn()).thenReturn(ppn);
    Step step = when(mock(Step.class).getProtocol()).thenReturn(protocol).getMock();
    DataEntity localParamater = mock(DataEntity.class);
    DataEntity defaultResourcesMap = mock(DataEntity.class);
    taskGenerator.setProtocolPpn(step, localParamater, defaultResourcesMap);
    verify(localParamater).set("ppn", ppn);
  }
}
