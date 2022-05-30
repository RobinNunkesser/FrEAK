/*
 *
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

package org.jgpd.io.jbpm.definition.impl;

import org.jgpd.io.jbpm.definition.Action;
import org.jgpd.io.jbpm.definition.EventType;
import org.jgpd.io.utils.FilePackage;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ActionImpl implements Action {

	protected FilePackage handler = new FilePackage(null ,null);
	protected FilePackage exception = new FilePackage(null ,null);
	protected ParametersImpl parameters = new ParametersImpl(); // parameter elements 0-n
	private EventType eventType = EventType.PROCESS_END;

	public ActionImpl()
	{

	}

	public ActionImpl(Node node)
	{
		String nodeValue = null;
		// Get attributes
		NamedNodeMap attrMap = node.getAttributes();
		Node nameNode = attrMap.getNamedItem("event");
		if (nameNode != null )
		{
			String name = nameNode.getNodeValue();
//			setDescription(name);
		}

		nameNode = attrMap.getNamedItem("handler");
		if (nameNode != null )
		{
			String rt = nameNode.getNodeValue();
//			setRootDir(new File(rt));
		}

		nameNode = attrMap.getNamedItem("on-exception");
		if (nameNode != null )
		{
			String tar = nameNode.getNodeValue();
//			setTarget(new File(tar));
		}


		// Get sub-elements
		for (int i = 0; i < node.getChildNodes().getLength(); i++)
		{
			Node child = node.getChildNodes().item(i);
			String nextChildString = child.getNodeName().toLowerCase();
			if (child.getNodeName().toLowerCase().equals("parameter"))
			{
				Node next_child = child.getFirstChild();
				try {
					nodeValue = next_child.getNodeValue();
				} catch (Exception e) {
					// FIXME - do something
				}

				if ( nodeValue != null )
				{
					Parameter para = new Parameter(child, nodeValue);
					parameters.add(para);
				}
				else
				{
					// FIXME, no value to parameter - error
				}
			}
		}
	}

	public EventType getEventType() { return this.eventType; }
	public void setEventType(EventType eventType) { this.eventType = eventType; }

	/**
	 * @return
	 */
	public FilePackage getHandler() {
		return handler;
	}

	/**
	 * @param handler
	 */
	public void setHandler(FilePackage handler) {
		this.handler = handler;
	}

	/**
	 * @return
	 */
	public FilePackage getException() {
		return exception;
	}

	/**
	 * @param exception
	 */
	public void setException(FilePackage exception) {
		this.exception = exception;
	}

	public String toString()
	{
		return handler.toString();
	}
}
