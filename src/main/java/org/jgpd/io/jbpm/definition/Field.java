package org.jgpd.io.jbpm.definition;

import java.io.*;

public interface Field extends Serializable {
  String getName();
  String getDescription();
  Attribute getAttribute();
  State getState();
  FieldAccess getAccess();
}
