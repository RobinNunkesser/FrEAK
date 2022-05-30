package org.jgpd.io.jbpm.definition.impl;

import org.jgpd.io.jbpm.definition.*;

public class ProcessStateImpl extends StateImpl implements ProcessState {

  public ProcessStateImpl() {}

  public ProcessDefinition getSubProcess() { return this.subProcess; }
  public void setSubProcess(ProcessDefinition subProcess) { this.subProcess = subProcess; }

  public String getActorExpression() { return this.actorExpression; }
  public void setActorExpression(String actorExpression) { this.actorExpression = actorExpression; }

  private ProcessDefinition subProcess = null;
  private String actorExpression = null;
}
