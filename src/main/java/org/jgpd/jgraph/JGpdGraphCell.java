/*
 *
 * Copyright (C) 2004 David Benson
 *
 * JGpd is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * JGpd is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JGpd; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package org.jgpd.jgraph;

import java.awt.Rectangle;
import java.util.*;

import javax.swing.tree.MutableTreeNode;

import org.jgpd.io.JGpdModelNode;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;

/**
 * The JGpd implementation for the GraphCell interface.
 *
 * @version 1.0 1/1/02
 * @author Gaudenz Alder
 */

public class JGpdGraphCell extends DefaultGraphCell
{
	/**
	 * Creates an empty cell.
	 */
	public JGpdGraphCell() {
		this(null);
	}

	/**
	 * Creates a graph cell and initializes it with the specified user object.
	 *
	 * @param userObject an Object provided by the user that constitutes
	 *                   the cell's data
	 */
	public JGpdGraphCell(Object userObject) {
		this(userObject, null);
	}

	/**
	 * Constructs a cell that holds a reference to the specified user object
	 * and contains the specified array of children and sets default values
	 * for the bounds attribute.
	 *
	 * @param userObject reference to the user object
	 * @param children array of children
	 */
	public JGpdGraphCell(Object userObject, MutableTreeNode[] children) {
		super(userObject, true);
	}

	/**
	 * Creates a graph cell and initializes it with the specified user object.
	 * The GraphCell allows children only if specified.
	 *
	 * @param userObject an Object provided by the user that constitutes
	 *                   the cell's data
	 */
	public JGpdGraphCell(Object userObject, boolean allowsChildren) {
		super(userObject, allowsChildren);
	}

	/**
	 * Apply <code>change</code> to the cell and sync userObject.
	 */
	public Map changeAttributes(Map change)
	{
		Map undo = GraphConstants.applyMap(change, attributes);
		Object newValue = GraphConstants.getValue(attributes);
		// Check for inconsistencies
		if (userObject != null && newValue == null)
		{	
			GraphConstants.setValue(attributes, userObject);
		}
		else if (userObject instanceof ValueChangeHandler)
		{
			Object oldValue =
				((ValueChangeHandler) userObject).valueChanged(newValue);
			if (oldValue == null
					|| !newValue.toString().equals(oldValue.toString()))
			{
				GraphConstants.setValue(undo, oldValue);
			}
			GraphConstants.setValue(attributes, userObject);
		}
		// Ensure a user-entered String doesn't overwrite
		// a node object
		else if ( userObject instanceof JGpdModelNode &&
				  newValue instanceof String )
		{
			((JGpdModelNode)userObject).setDisplayName((String)newValue);
		}
		else
		{	
			userObject = newValue;
		}
		// Ensure non-null bounds
		Rectangle bounds = GraphConstants.getBounds(attributes);
		if (bounds == null)
		{	
			GraphConstants.setBounds(attributes, defaultBounds);
		}
		return undo;
	}

	/**
	 * Create a clone of the cell. The cloning of the
	 * user object is deferred to the cloneUserObject()
	 * method.
	 *
	 * @return Object  a clone of this object.
	 */
	public Object clone() {
		JGpdGraphCell c = (JGpdGraphCell) super.clone();
		return c;
	}

	/**
	 * Create a clone of the user object. This is provided for
	 * subclassers who need special cloning. This implementation
	 * simply returns a reference to the original user object.
	 *
	 * @return Object  a clone of this cells user object.
	 */
	protected Object cloneUserObject() {
		if (userObject instanceof ValueChangeHandler)
			return ((ValueChangeHandler) userObject).clone();
		return userObject;
	}
	
	public void getCellsInDirection(Collection cells,
									boolean navigateSplits,
									GraphModel model,
									boolean sourceDirection,
									Boolean finishCellFound)
	{
		// FIXME, need a recursion list to allow for loopbacks
		
		Object[] thisCell = new Object[1];
		thisCell[0] = this;
		
		Set edges = DefaultGraphModel.getEdges(model, thisCell);

		Object nextVertex = null;
		Iterator iter = edges.iterator();
		while ( iter.hasNext() )
		{
			// Recurse into function collecting nodes
			Object edge = iter.next();
			
			if ( sourceDirection == true )
			{
				// i.e. we're traversing forward across transitions
				nextVertex = DefaultGraphModel.getTargetVertex(model, edge);
			}
			else
			{
				// i.e. we're traversing backwards across transitions
				nextVertex = DefaultGraphModel.getSourceVertex(model, edge);
			}
			// No vertex connected to this edge?  Try next iteration
			// Also ignore if the edge we've picked is sinked at the source
			// cell ( we only want to travel outwards )
			if ( (nextVertex != null) && (nextVertex != this))
			{
				if ( navigateSplits == true )
				{
					// We're talking all sourced nodes
					cells.add(nextVertex);
					((JGpdGraphCell)nextVertex).
						getCellsInDirection(cells,
											navigateSplits,
											model,
											sourceDirection,
											finishCellFound );
				}
				else
				{
					if ( nextVertex instanceof SplitCell )
					{
						if (sourceDirection)
						{
							// Have to nested ifs as we don't want _any_ splits in the
							// else clause
							cells.add(nextVertex);
						}
					}
					else if ( nextVertex instanceof JoinCell )
					{
						if ( !(sourceDirection) )
						{
							// Have to nested ifs as we don't want _any_ join in the
							// else clause
							cells.add(nextVertex);
						}
						else
						{
							finishCellFound = Boolean.valueOf(true);
						}
					}
					else
					{
						// Don't want split or joins in here
						if ( !(cells.contains(nextVertex)))
						{
							cells.add(nextVertex);
							((JGpdGraphCell)nextVertex).
									getCellsInDirection(cells,
														navigateSplits,
														model,
														sourceDirection,
														finishCellFound );
						}
						else
						{
							// FIXME, I shouldn't have to rely on the .contains()
						}
					}
				}
			}
		}
	}
}
