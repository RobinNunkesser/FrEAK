package org.jgpd.io.jbpm.definition;

public interface ProcessDefinition extends ProcessBlock {
  String getResponsibleUserName();
  Integer getVersion();
  StartState getStartState();
  EndState getEndState();
  byte[] getImage();
  String getImageMimeType();
  Integer getImageHeight();
  Integer getImageWidth();
}
