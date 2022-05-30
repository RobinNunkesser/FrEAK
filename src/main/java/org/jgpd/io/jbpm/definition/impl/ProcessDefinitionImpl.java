package org.jgpd.io.jbpm.definition.impl;

import java.util.*;

import org.jgpd.io.JGpdModelNode;
import org.jgpd.io.jbpm.UI.ProcessPropertiesDialog;
import org.jgpd.io.jbpm.definition.*;
import org.jgpd.jgraph.EndCell;
import org.jgpd.jgraph.JGpdGraphCell;
import org.jgpd.jgraph.JoinCell;
import org.jgpd.jgraph.SplitCell;
import org.jgpd.jgraph.StartCell;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphModel;
import org.jgraph.pad.GPGraph;

public class ProcessDefinitionImpl extends ProcessBlockImpl implements ProcessDefinition
{
	public static ProcessPropertiesDialog dialog = new ProcessPropertiesDialog();
	protected String validationOutput = null;
	protected Vector startCells = null;
	protected Vector endCells = null;
	protected Vector joinCells = null;
	protected Vector splitCells = null;
	protected Vector concurrentPairs = null;
	protected int splitJoinIndices[]  = null;
	
	private String responsibleUserName = null;
	private Integer version = null;
	private StartStateImpl startState = null;
	private EndStateImpl endState = null;
	private byte[] image = null;
	private String imageMimeType = null;
	private Integer imageHeight = null;
	private Integer imageWidth = null;
	
	public ProcessDefinitionImpl()
	{
		super();
	}
	
	public String writeXML(GPGraph graph)
	{
		// Reset validation output string
		validationOutput = new String("jBpm Validation Output\n\n");
		
		GraphModel graphModel = graph.getModel();

		populateCells(graphModel);
		if (validateCells() == false )
		{
			return validationOutput;
		}
		
		Vector concurrentBlocks = new Vector();

		if (createConcurrencyModel(graphModel,concurrentBlocks) == false)
		{
			return validationOutput;
		}
		
		populateProcessBlock( 	(startCells.get(0)),
								(endCells.get(0)),
								graphModel,
								concurrentBlocks );
								
		// Fill in concurrent blocks in same way
		
		Iterator iter = concurrentBlocks.iterator();
		while (iter.hasNext())
		{
			ConcurrentBlock block = (ConcurrentBlock)iter.next();
			block.populateProcessBlock ( block.getSplit(),
										 block.getJoin(),
										 graphModel,
										 concurrentBlocks );
		}
		
		String xml = "<?xml version=\"1.0\"?>\n\n";
		String indent = "  ";
		
		// Header comments
		
		xml += "<!DOCTYPE process-definition PUBLIC\n";
		xml += indent + "\"jbpm/processdefinition_1_0\"\n";
		xml += indent + "\"http://jbpm.org/dtd/processdefinition_1_0.dtd\">\n";
		
		xml += "<process-definition>\n\n";
		
		xml += indent + "<!-- =================================== -->\n";
		xml += indent + "<!-- == PROCESS DEFINITION PROPERTIES == -->\n";
		xml += indent + "<!-- =================================== -->\n";
		
		xml += indent + "<responsible>" + this.responsibleUserName + "</responsible>\n";
		xml += indent + "<name>" + this.name + "</name>\n";
		xml += indent + "<description>" + this.description + "</description>\n";
		
		xml += "\n";
		xml += indent + "<!-- =================================== -->\n";
		xml += indent + "<!-- ======= START & END STATES ======== -->\n";
		xml += indent + "<!-- =================================== -->\n";
		
		if ( startCells.get(0) != null )
		{
			xml += ((NodeImpl)((JGpdGraphCell)(startCells.get(0))).getUserObject()).writeXML(indent);
		}
		else
		{
			// FIXME TODO db error condition, validation didn't work
		}
		
		if ( endCells.get(0) != null )
		{
			xml += ((NodeImpl)((JGpdGraphCell)(endCells.get(0))).getUserObject()).writeXML(indent);
		}
		else
		{
			// FIXME TODO db error condition, validation didn't work
		}
		
		// Write attributes
		xml += super.writeXML(indent);
		
		xml += "</process-definition>";
		return xml;
	}
	
	public void getUIProperties(GPGraph graph)
	{
		dialog.showPropertyDialog(graph, this);
	}
	
	protected void populateCells(GraphModel model)
	{
		startCells = new Vector();
		endCells = new Vector();
		splitCells = new Vector();
		joinCells = new Vector();
		
		int nodes = model.getRootCount();
		for ( int i = 0; i <nodes; i++ )
		{
			boolean resetTransitions = false;
			
			Object cell = model.getRootAt(i);
			Object node = ((DefaultGraphCell)cell).getUserObject();
			
			if (node instanceof NodeImpl)
			{
				// Setup transition details
				((NodeImpl)node).resetTransitions();
				Object[] thisCell = new Object[1];
				thisCell[0] = cell;
				
				Set edges = DefaultGraphModel.getEdges(model, thisCell);

				Object nextVertex = null;
				Iterator iter = edges.iterator();
				while ( iter.hasNext() )
				{
					// Recurse into function collecting nodes
					Object edge = iter.next();
					
					nextVertex = DefaultGraphModel.getTargetVertex(model, edge);

					// No vertex connected to this edge?  Try next iteration
					// Also ignore if the edge we've picked is sinked at the source
					// cell ( we only want to travel outwards )
					if ( (nextVertex != null) && (nextVertex != cell))
					{
						((NodeImpl)node).addSourcedTransition((JGpdModelNode)((JGpdGraphCell)nextVertex).getUserObject(),
								(((DefaultGraphCell)edge).getUserObject()).toString());
					}
				}
			}
			
			if (cell instanceof StartCell)
			{
				startCells.add(cell);
			}
			else if (cell instanceof EndCell)
			{
				endCells.add(cell);
			}
			else if (cell instanceof SplitCell)
			{
				splitCells.add(cell);
			}
			else if (cell instanceof JoinCell)
			{
				joinCells.add(cell);
			}
		}
	}
	
	protected boolean validateCells()
	{
		boolean modelValid = true;
		// Right number of start and end cells?
		if ( startCells.size() == 0 )
		{
			validationOutput += "No start cell, please create one with a single transition leaving it.\n";
			modelValid = false;
		}
		if ( startCells.size() > 1 )
		{
			validationOutput += (new Integer(startCells.size())) + " start cells defined - only 1 allowed\n";
			modelValid = false;
		}
		if ( endCells.size() == 0 )
		{
			validationOutput += "No end cell, please create one with a single transition entering it.\n";
			modelValid = false;
		}
		if ( endCells.size() > 1 )
		{
			validationOutput += (new Integer(endCells.size())) + " end cells defined - only 1 allowed\n";
			modelValid = false;
		}
		
		// In jBpm must be same number of split and join cells
		if ( splitCells.size() > joinCells.size() )
		{
			validationOutput += "There are " +
				(new Integer( splitCells.size() - joinCells.size())) +
				" more splits than joins. There should be an equal number of both.\n";
			modelValid = false;
		}
		if ( joinCells.size() > splitCells.size() )
		{
			validationOutput += "There are " +
				(new Integer( joinCells.size() - splitCells.size())) +
				" more joins than splits. There should be an equal number of both.\n";
			modelValid = false;
		}
		
		return modelValid;
	}

	protected boolean createConcurrencyModel(GraphModel model,
											 Vector concurrentBlocks)
	{
		int numPairs = splitCells.size();
		splitJoinIndices = new int[numPairs];

		// Take each split in turn and find the paired join assuming
		// this is a valid jBpm model
		for (int i = 0; i < numPairs; i++)
		{
			// Initialise the pairing index to -1 so we know if a pair isn't found
			splitJoinIndices[i] = -1;

			// Maintain list of nodes we visit to avoid recursion problems 
			Vector recursionList = new Vector();
			Object joinCell = findPairedJoinSourcedFromCell( model, splitCells.get(i), recursionList, 0 );
			
			if ( joinCell == null )
			{
				validationOutput += "Concurrent block(s) invalid - please check model against jBpm specifications";
				return false;
			}
			
			// get the index of the join and assign it to the split
			splitJoinIndices[i] = joinCells.indexOf(joinCell);
			
		}
		
		// Check none of the entries are -1 ( not set or not found in joinCells vector )
		// and check there are no duplicates
		for (int i = 0; i< numPairs; i++)
		{
			for ( int j = i; j < numPairs; j++)
			{
				if ( i != j )
				{
					if (splitJoinIndices[i] == splitJoinIndices[j])
					{
						validationOutput += "Concurrent block(s) invalid - please check model against jBpm specifications";
						return false;
					}
				}
			}
		}
		
		// Fill collection of concurrent blocks
		for ( int i = 0; i < numPairs; i++ )
		{
			ConcurrentBlockImpl conBlock = new ConcurrentBlockImpl();
			conBlock.setSplit( (SplitCell)splitCells.get(i));
			conBlock.setJoin( (JoinCell)joinCells.get(splitJoinIndices[i]));
			concurrentBlocks.add(conBlock);
		}

		return true;
	}

	/**
	 * Warning, this is a recursive method, handle with care
	 * 
	 * @param model
	 * @param sourceCell
	 * @param recursionList
	 * @param layerLevel is a count of how many nesting levels of concurrent
	 * model we are in. It increments 1 for traversing a split and decrements
	 * 1 for traversing a join. Only if join is found at level 0 does this
	 * constitute a pairing
	 * @return
	 */
	
	// FIXME this could be generalised to find a particular class instead of any just a join
	// also could make the direction ( source/sink ) configurable
	protected Object findPairedJoinSourcedFromCell( GraphModel model,
													Object sourceCell, 
													Vector recursionList,
													int layerLevel)
	{
		//FIXME shouldn't have to create an array of objects when I only have one
		Object[] cells = new Object[1];
		cells[0] = sourceCell;
		
		Set edges = DefaultGraphModel.getEdges(model, cells);

		Object targetVertex = null;
		Object nextVertex = null;
		Iterator iter = edges.iterator();
		while ( iter.hasNext() && targetVertex == null )
		{
			// Read this loop very carefully
			Object edge = iter.next();
			
			nextVertex = DefaultGraphModel.getTargetVertex(model, edge);

			// No vertex connected to this edge?  Try next iteration
			// Also ignore if the edge we've picked is sinked at the source
			// cell ( we only want to travel outwards )
			if ( (nextVertex != null) && (nextVertex != sourceCell))
			{
				if ( nextVertex instanceof JoinCell )
				{
					if ( layerLevel == 0 )
					{
						// Only a pair if we've unwound every split we've
						// traversed with a join
						return nextVertex;
					}
					else
					{
						// Down another level
						--layerLevel;
					}
				}
				if ( nextVertex instanceof SplitCell )
				{
					++layerLevel;
				}
				
				// todo, add this one to the list - recursionList.add(cellID);
				targetVertex = findPairedJoinSourcedFromCell( 	model,
																nextVertex,
																recursionList,
																layerLevel );
				
				// if no result from this path, go down another edge
				if ( targetVertex != null )
				{
					if ( targetVertex instanceof JoinCell )
					{
						return targetVertex;
					}
					else
					{
						// FIXME error, what the hell did we return this object for if it's not what we want?
					}
				}
			}
		}
		
		if ( targetVertex != null )
		{
			// FIXME, shouldn't come here with an object
		}
		
		return null;
	}
	
	public String getResponsibleUserName() { return responsibleUserName; }
	public void setResponsibleUserName( String responsibleUserName ) { this.responsibleUserName = responsibleUserName; }
	
	public Integer getVersion() { return version; }
	public void setVersion ( Integer version ) { this.version = version; }
	
	public StartState getStartState() { return this.startState; }
	public void setStartState(StartState startState) { this.startState = (StartStateImpl) startState; }
	
	public EndState getEndState() { return this.endState; }
	public void setEndState(EndState endState) { this.endState = (EndStateImpl) endState; }
	
	public byte[] getImage() { return image; }
	public void setImage ( byte[] image ) { this.image = image; }
	
	public String getImageMimeType() { return imageMimeType; }
	public void setImageMimeType ( String imageMimeType ) { this.imageMimeType = imageMimeType; }
	
	public Integer getImageHeight() { return imageHeight; }
	public void setImageHeight ( Integer imageHeight ) { this.imageHeight = imageHeight; }
	
	public Integer getImageWidth() { return imageWidth; }
	public void setImageWidth ( Integer imageWidth ) { this.imageWidth = imageWidth; }
	
	public String toString() {
		return "ProcessDefinitionImpl[" + id + "|" + name + "|" + version + "]";
	}
}
