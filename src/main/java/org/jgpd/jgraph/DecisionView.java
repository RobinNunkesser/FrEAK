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
import java.awt.Polygon;

import org.jgraph.JGraph;
import org.jgraph.graph.CellMapper;
import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.VertexRenderer;
import org.jgraph.graph.VertexView;

public class DecisionView extends VertexView
{
    public static int default_width = 50;
    public static int default_height = 80;

	public static DecisionRenderer renderer = new DecisionRenderer();

	public DecisionView(Object cell, JGraph graph, CellMapper cm) {
		super(cell, graph, cm);
	}


	public CellViewRenderer getRenderer() {
		return renderer;
	}

	public static class DecisionRenderer extends VertexRenderer {

		public void paint(Graphics g) {
			int b = borderWidth;
			Graphics2D g2 = (Graphics2D) g;
			Dimension d = getSize();

			// Set up x and y arrays for polygon ( which is how we're drawin
			// our diamond shape )
			int [] polyx =  {  0,
							  (d.width - b)/2,
							   d.width - b,
							  (d.width - b)/2 };
			int [] polyy =  { (d.height - b)/2,
							   0,
							  (d.height - b)/2,
							  (d.height - b) };

			boolean tmp = selected;
			if (super.isOpaque()) {
				Polygon poly = new Polygon( polyx, polyy, 4 );
				g.setColor(super.getBackground());
				g2.fillPolygon( poly );
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
				Polygon poly = new Polygon( polyx, polyy, 4 );
				g2.draw(poly);
			}
			if (selected) {
				g2.setStroke(GraphConstants.SELECTION_STROKE);
				g.setColor(graph.getHighlightColor());
				Polygon poly = new Polygon( polyx, polyy, 4 );
				g2.draw(poly);
			}
		}
	}
}