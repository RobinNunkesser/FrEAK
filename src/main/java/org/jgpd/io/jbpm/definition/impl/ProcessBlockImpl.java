package org.jgpd.io.jbpm.definition.impl;

import java.util.*;

import org.jgpd.io.jbpm.definition.*;
import org.jgpd.jgraph.JGpdGraphCell;
import org.jgpd.jgraph.JoinCell;
import org.jgpd.jgraph.SplitCell;
import org.jgraph.graph.GraphModel;

public class ProcessBlockImpl extends DefinitionObjectImpl implements ProcessBlock {
	
	protected Collection nodes = null;
	protected HashMap attributes = null;
	protected ProcessBlockImpl parentBlock = null;
	protected Vector childBlocks = null;

	public ProcessBlockImpl()
	{
		this.attributes = new HashMap();
	}
	
	public void populateProcessBlock( 	Object beginCell,
										Object finishCell,
										GraphModel model,
										Vector concurrentBlocks )
	{
		this.nodes = new Vector();
		this.childBlocks = new Vector();
		Boolean finishCellFound = Boolean.valueOf(false);

		((JGpdGraphCell)beginCell).getCellsInDirection(	this.nodes,
													false, // don't nav splits
													model,
													true,  // sourced cells
													finishCellFound); 
		// Check if the finish cell is in nodes
		if ( !(finishCellFound.booleanValue()) )
		{
			// Finish cell not present, need to iterate back through model
			((JGpdGraphCell)finishCell).getCellsInDirection(	this.nodes,
					false, // don't nav joins
					model,
					false,  // sinked cells
					finishCellFound);
		}
		
		// Check if any of the split or joins ( if any ) in nodes
		// match entries in concurrentBlocks
		
		Iterator vecIter = concurrentBlocks.iterator();
		while (vecIter.hasNext())
		{
			ConcurrentBlockImpl block = (ConcurrentBlockImpl)vecIter.next();
			SplitCell split = block.getSplit();
			if ( nodes.contains(split))
			{
				// Concurrent block join must also be in node list then
				JoinCell join = block.getJoin();
				if ( nodes.contains(join))
				{
					// This concurrent block is a child of this process
					// block.  Also remove the split and join from the list
					// of nodes as we don't really store them.
					childBlocks.add(block);
					nodes.remove(split);
					nodes.remove(join);
				}
			}
		}
	}
	
	public String writeXML(String indent)
	{
		String xml = "";
		// Iterate through collections writing XML strings
		
		Iterator iter = attributes.values().iterator();
		while (iter.hasNext())
		{
			AttributeImpl attribute = (AttributeImpl) iter.next();
			
			if (attribute.getName() != null )
			{
				xml = indent + "<attribute name=\" ";
				xml += attribute.getName();
				xml += " \" type=\" ";
				
				// FIXME TODO db -where is the type held?
				
				if (attribute.getInitialValue() != null)
				{
					// attribute has initial value
					xml += "initial-value=\"";
					xml += attribute.getInitialValue() + "\" ";
				}
				
				xml += "\" />";
			}
		}
		
		// Write nodes ( decisions and activity states )
		xml += indent + "<!-- =========== -->\n";
		xml += indent + "<!-- == NODES == -->\n";
		xml += indent + "<!-- =========== -->\n\n";
		
		iter = nodes.iterator();
		while (iter.hasNext())
		{
			JGpdGraphCell cell = (JGpdGraphCell) iter.next();
			xml += ((NodeImpl)(cell).getUserObject()).writeXML(indent);
		}
		// Write concurrent blocks
		
		iter = childBlocks.iterator();
		while (iter.hasNext()) {
			ConcurrentBlockImpl processBlock = (ConcurrentBlockImpl) iter.next();
			xml += processBlock.writeXML( indent );
		}
		return xml;
	}
	
	
	public Collection getNodes() { return this.nodes; }
	public void setNodes(Collection processElements) {  this.nodes = processElements; }
	
	public HashMap getAttributes() { return attributes; }
	public void setAttributes(HashMap attributes) { this.attributes = attributes; }
	
	public ProcessBlockImpl getParentBlock() { return this.parentBlock; }
	public void setParentBlock(ProcessBlockImpl parentBlock) { this.parentBlock = parentBlock; }
	
	public Vector getChildBlocks() { return this.childBlocks; }
	public void setChildBlocks(Vector childBlocks) { this.childBlocks = childBlocks; }
	}
