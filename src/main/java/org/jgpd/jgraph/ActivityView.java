/*
 *
 *
 * Copyright (C) 2003 sven.luzar
 * Copyright (C) 2003 David Benson
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

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import org.jgraph.JGraph;
import org.jgraph.graph.CellMapper;
import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.VertexRenderer;
import org.jgraph.graph.VertexView;

public class ActivityView extends VertexView
{
	public static int default_width = 100;
	public static int default_height = 60;

	public static ActivityRenderer renderer = new ActivityRenderer();

	public ActivityView(Object cell, JGraph graph, CellMapper cm) {
		super(cell, graph, cm);
	}

	/**
	 * Returns the intersection of the bounding rectangle and the
	 * straight line between the source and the specified point p.
	 * The specified point is expected not to intersect the bounds.
	 */
	public static int getArcSize(int width, int height) {
        int arcSize;

        // The arc width of a activity rectangle is 1/5th of the larger
        // of the two of the dimensions passed in, but at most 1/2
        // of the smaller of the two. 1/5 because it looks nice and 1/2
        // so the arc can complete in the given dimension

        if ( width <= height ) {
			arcSize = height / 5;
        	if (arcSize > (width / 2) ) {
				arcSize = width/2;
        	}
        }else{
			arcSize = width / 5;
        	if (arcSize > (height /2) ) {
        		arcSize = height/2;
        	}
        }

		return arcSize;
	}


	public CellViewRenderer getRenderer() {
		return renderer;
	}

	public static class ActivityRenderer extends VertexRenderer {

		public void paint(Graphics g) {
			int b = borderWidth;
			Graphics2D g2 = (Graphics2D) g;
			Dimension d = getSize();
			boolean tmp = selected;
			int roundRectArc = ActivityView.getArcSize(d.width -b, d.height - b);
			if (super.isOpaque()) {
				g.setColor(super.getBackground());
				g.fillRoundRect(b - 1,
				                b - 1,
				                d.width - b,
				                d.height - b,
				                roundRectArc,
				                roundRectArc );
			}
			try {
				setBorder(null);
				setOpaque(false);
				selected = false;
				super.paint(g);
			} finally {
				selected = tmp;
			}
			if (bordercolor != null) {
				g.setColor(bordercolor);
				g2.setStroke(new BasicStroke(b));
				g.drawRoundRect(b - 1,
				                b - 1,
				                d.width - b,
				                d.height - b,
				                roundRectArc,
				                roundRectArc );
			}
			if (selected) {
				g2.setStroke(GraphConstants.SELECTION_STROKE);
				g.setColor(graph.getHighlightColor());
				g.drawRoundRect(b - 1,
								b - 1,
								d.width - b,
								d.height - b,
				                roundRectArc,
				                roundRectArc );
			}
		}
	}

}