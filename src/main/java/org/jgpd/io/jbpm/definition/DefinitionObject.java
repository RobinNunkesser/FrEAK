package org.jgpd.io.jbpm.definition;

import java.io.Serializable;

public interface DefinitionObject extends Serializable {
  String getName();
  boolean hasName();
  String getDescription();
}
