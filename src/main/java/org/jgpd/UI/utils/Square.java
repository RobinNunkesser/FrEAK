/*
 *
 *
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

package org.jgpd.UI.utils;

import java.awt.Rectangle;



public class Square extends Rectangle
{

    public Rectangle limitToSquare()
    {
    	// Limit the larger of the two dimensions to the smaller
    	// to return a square with dimensions equal to the smaller of the
    	// thisangle dimensions.
    	if ( this.width > this.height )
    	{
			// Wide and short
			// move the x co-ord right by half of the difference between
			// the width of the thisangle and the width of the sqaure
			// ( think about it )
			this.x = this.x + ( ( this.width - this.height ) / 2 );
    		this.width = this.height;
    		
    	}
    	else
    	{
    		// Thin and tall
			// move the y co-ord down by half of the difference between
			// the height of the thisangle and the height of the sqaure
			this.y = this.y + ( ( this.height - this.width ) / 2 );
			this.height = this.width;
    	}

    return this;
    }
}
