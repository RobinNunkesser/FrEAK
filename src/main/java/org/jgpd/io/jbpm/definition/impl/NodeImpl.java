package org.jgpd.io.jbpm.definition.impl;

import java.util.*;

import org.jgpd.io.JGpdModelNode;
import org.jgpd.io.jbpm.definition.*;

public class NodeImpl extends DefinitionObjectImpl implements Node {
	
	public NodeImpl()
	{
		this.arrivingTransitions = new HashSet();
		this.leavingTransitions = new HashSet();
	}
	
	public String writeXML(String indent)
	{
		String xml = "";
		
		Iterator iter = leavingTransitions.iterator();
		while ( iter.hasNext() )
		{
			TransitionImpl transition = (TransitionImpl)(iter.next());
			xml += transition.writeXML(indent);
		}
		
		return xml;
	}
	
	public void resetTransitions()
	{
		this.leavingTransitions = new HashSet();
	}

	public ProcessBlock getProcessBlock() { return this.processBlock; }
	public void setProcessBlock(ProcessBlock processBlock) { this.processBlock = processBlock; }
	
	public void addArrivingTransition(TransitionImpl trans)
	{
		if (this.arrivingTransitions.contains(trans))
		{
			// FIXME TODO db, error condition. the collection should not
			// already have the transition
		}
		else
		{
			this.arrivingTransitions.add(trans);
		}
	}
	
	public Collection getLeavingTransitions() { return this.leavingTransitions; }
	public void setLeavingTransitions(Collection leavingTransitions) { this.leavingTransitions = leavingTransitions; }
	
	public void addLeavingTransition(TransitionImpl trans)
	{
		if (this.leavingTransitions.contains(trans))
		{
			// FIXME TODO db, error condition. the collection should not
			// already have the transition
		}
		else
		{
			this.leavingTransitions.add(trans);
		}
	}

	/**
	 * The contract to this function says the transition is connected
	 * both ends ( sourceCell != null )
	 */
	public void addSourcedTransition(JGpdModelNode sourceCell,
									 String        transitionName)
	{
		TransitionImpl trans = new TransitionImpl();
		addLeavingTransition(trans);
		trans.setFrom(this);
		trans.setTo((NodeImpl)sourceCell);
		if ( transitionName != null )
		{
			trans.setName(transitionName);
		}
	}
	
	public String toString()
	{
		return getName();
	}
	
	protected ProcessBlock processBlock = null;
	protected Collection arrivingTransitions = null;
	protected Collection leavingTransitions = null;
}
