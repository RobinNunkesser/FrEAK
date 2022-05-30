/*
 *
 * Copyright (C) 2003-2004 David Benson
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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.jgraph.JGraph;
import org.jgraph.graph.CellMapper;
import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.VertexRenderer;
import org.jgraph.graph.VertexView;
import org.jgraph.pad.resources.ImageLoader;

public class SplitView extends VertexView
{
	public static int defaultWidth = 50;
	public static int defaultHeight = 50;
	protected static Image defaultImage = null;
	
	public static SplitRenderer renderer = new SplitRenderer();

	public SplitView(Object cell, JGraph graph, CellMapper cm)
	{
		super(cell, graph, cm);

		if ( defaultImage == null )
		{
			Icon icon = ImageLoader.getImageIcon("split_image.gif");
			if (icon instanceof ImageIcon)
			{
				defaultImage = ((ImageIcon) icon).getImage();
			}
		}
	}


	public CellViewRenderer getRenderer()
	{
		return renderer;
	}


	public static class SplitRenderer extends VertexRenderer {

		public void paint(Graphics g)
		{
			if ( defaultImage != null )
			{
				Dimension d = getSize();
				g.drawImage(defaultImage, 0, 0, d.width - 1, d.height - 1, graph);
				super.paint(g);
			}
		}
	}
}