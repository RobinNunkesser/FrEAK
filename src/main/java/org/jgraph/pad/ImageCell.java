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

package org.jgraph.pad;

import org.jgpd.jgraph.JGpdGraphCell;

/**
 * @author bensond
 *
 * An image cell is a cell that can have no fill, a colour fill, the static
 * image type of a sub-class or a custom image fill. The image provided needs
 * to be on the correct shape with appropriate transparency to not overlap
 * the draw edge of the shape
 */
public class ImageCell extends JGpdGraphCell {

	public ImageCell()
	{
		this(null);
	}

	public ImageCell(Object externalModelNode)
	{
		super(externalModelNode);
	}

}