package org.jgpd.io.jbpm.definition;

public interface ProcessState extends State {
  ProcessDefinition getSubProcess();
  String getActorExpression();
}
