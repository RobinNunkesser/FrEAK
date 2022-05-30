/*
 *
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

package org.jgpd.io.jbpm.definition.impl;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class Parameter
{
	private String name = new String("Set Parameter Name");
	private String value = new String("Set Parameter Value");

	public Parameter( String nom, String val )
	{
		name = nom;
		value = val;
	}

	public Parameter( Node node, String val )
	{
		setValue(val);

		// Get attributes
		NamedNodeMap attrMap = node.getAttributes();
		Node nameNode = attrMap.getNamedItem("name");
		String name = nameNode.getNodeValue();
		setName(name);
	}

	public String writeXML(String indent)
	{
		String xml = indent + "<parameter " +
			"name=\"" +
			getName() +
			"\">";

		xml += getValue();

		xml += "</parameter>\n";

		return xml;
	}

	/**
	 * @return
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return
	 */
	public String getValue()
	{
		return value;
	}

	/**
	 * @param value
	 */
	public void setValue(String value)
	{
		this.value = value;
	}

}