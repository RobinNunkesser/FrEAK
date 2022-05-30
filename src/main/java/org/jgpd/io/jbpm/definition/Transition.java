package org.jgpd.io.jbpm.definition;

public interface Transition extends DefinitionObject {
  Node getFrom();
  Node getTo();
}
