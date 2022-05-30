package org.jgpd.io.jbpm.definition.impl;

import org.jgpd.io.jbpm.definition.*;
import org.jgpd.jgraph.JoinCell;
import org.jgpd.jgraph.SplitCell;
;

public class ConcurrentBlockImpl extends ProcessBlockImpl implements ConcurrentBlock {
	
	public ConcurrentBlockImpl()
	{
		super();
	}
	
	public SplitCell getSplit() { return this.fork; }
	public void setSplit(SplitCell fork) { this.fork = fork; }
	
	public JoinCell getJoin() { return this.join; }
	public void setJoin(JoinCell join) { this.join = join; }
	
	public String writeXML(String indent)
	{
		String xml = "";
		
		xml += indent + "<concurrent-block>\n";
		
		xml += ((ForkImpl)(fork.getUserObject())).writeXML(indent + "  ");
		xml += ((JoinImpl)(join.getUserObject())).writeXML(indent + "  ");
		
		xml += super.writeXML(indent + "  ");
		
		xml += indent + "</concurrent-block>\n\n";
		
		return xml;
	}

	private JoinCell join = null;
	private SplitCell fork = null;
}
