package org.jgpd.io.jbpm.definition;

public interface Attribute extends DefinitionObject {

  ProcessBlock getScope();
  String getInitialValue();
}
