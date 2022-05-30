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

package org.jgpd.UI.utils;

public class Requirement
{
	public static final int COMPLUSORY = 0;
	public static final int RECOMMENDED = 1;
	public static final int OPTIONAL = 2;
	public static final int NOT_RECOMMENDED = 3;
	public static final int NOT_ALLOWED = 4;
	
	public static final String[] requirementToString = {"Complusory",
			                                            "Recommended",
														"Optional",
														"Not recommended",
														"Not allowed"};
}