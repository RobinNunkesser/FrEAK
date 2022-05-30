package org.jgpd.io.jbpm.definition.impl;

import org.jgpd.io.jbpm.definition.Attribute;
import org.jgpd.io.jbpm.definition.ProcessBlock;
import org.w3c.dom.Node;

public class AttributeImpl extends DefinitionObjectImpl implements Attribute {

  public AttributeImpl() {}

  public AttributeImpl(Node node)
  {
  	// FIXME finish me
  }

  public void validate() {
//    super.validate();
  }

  public String getDisplayedNodeType()
  {
  	return nodeType;
  }
  
  
  public ProcessBlock getScope() { return this.scope; }
  public void setScope(ProcessBlock scope) { this.scope = scope; }

  public String getInitialValue() { return initialValue; }
  public void setInitialValue(String initialValue) { this.initialValue = initialValue; }

  private ProcessBlock scope = null;
  private String initialValue = null;
  public static final String nodeType = new String("attribute");
}
