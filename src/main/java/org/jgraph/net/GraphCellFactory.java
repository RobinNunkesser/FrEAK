/*
 * @(#)Graphpad.java	1.2 11/11/02
 *
 * Copyright (C) 2001 Gaudenz Alder
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package org.jgraph.net;

import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultPort;
import org.jgraph.pad.EllipseCell;
import org.jgpd.jgraph.*;
import org.jgraph.pad.GPUserObject;
import org.jgraph.pad.TextCell;

public class GraphCellFactory {

	// Must return an object!
	public static DefaultGraphCell createCell(String type, Object userObject) {
		String label = (userObject != null) ? userObject.toString() : "";
		GPUserObject wrapper = new GPUserObject(label);
		if (type.equals("edge"))
			return new DefaultEdge(wrapper);

		else if (type.equals("ellipse"))
			return new EllipseCell(wrapper);

		else if (type.equals("activity"))
			return new ActivityCell(wrapper);

		else if (type.equals("decision"))
			return new DecisionCell(wrapper);

		else if (type.equals("start"))
			return new StartCell(wrapper);

		else if (type.equals("end"))
			return new EndCell(wrapper);

		else if (type.equals("split"))
			return new SplitCell(wrapper);

		else if (type.equals("join"))
			return new JoinCell(wrapper);

		else if (type.equals("text"))
			return new TextCell(wrapper);

		else if (type.equals("port"))
			return new DefaultPort(wrapper);

		// INV: type.equals("rect")
		return new DefaultGraphCell(wrapper);
	}

	public static String getType(Object cell) {
		if (cell instanceof DefaultPort)
			return "port";
		else if (cell instanceof TextCell)
			return "text";
		else if (cell instanceof SplitCell)
			return "split";
		else if (cell instanceof JoinCell)
			return "join";
		else if (cell instanceof EllipseCell)
			return "ellipse";
	    else if (cell instanceof ActivityCell)
	        return "activity";
	    else if (cell instanceof DecisionCell)
	        return "decision";
	    else if (cell instanceof StartCell)
	        return "start";
	    else if (cell instanceof EndCell)
	        return "end";
		else if (cell instanceof DefaultEdge)
			return "edge";
		return "rect";
	}

}
