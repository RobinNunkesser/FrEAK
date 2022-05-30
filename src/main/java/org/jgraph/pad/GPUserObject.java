/*
 * @(#)TranslatorConstants.java	1.2 02.02.2003
 *
 * Copyright (C) 2003 sven.luzar
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
package org.jgraph.pad;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

import org.jgraph.graph.DefaultGraphCell;

/**
 * @author Gaudenz Alder
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
/**
 *
 * @author gaudenz alder
 * @version 1.0
 *
 */
public class GPUserObject
	implements Serializable, DefaultGraphCell.ValueChangeHandler {

	/* defaultKey of the property used to return a value from the toString method
	 */
	public static String keyValue = "value";

	/* defaultKey of the property used to return a value from the toString method
	 */
	public static String keyURI = "url";

	/* Map that holds the attributes (key value pairs)
	 */
	protected Map properties;

	public GPUserObject() {
		// empty constructor required by XMLDecoder
		this(null, null);
	}

	/**
	 *
	 */
	public GPUserObject(String label, Map properties) {
		if (properties == null)
			properties = new Hashtable();
		this.properties = new Hashtable(properties);
		if (label != null)
			valueChanged(label);
	}

	public GPUserObject(String label) {
		this(label, null);
	}

	public GPUserObject(Map properties) {
		this(null, properties);
	}

	/* (non-Javadoc)
	 * @see org.jgraph.graph.DefaultGraphCell.ValueChangeHandler#valueChanged(java.lang.Object)
	 */
	public Object valueChanged(Object newValue) {
		if (newValue instanceof String)
			return putProperty(keyValue, newValue);
		return this;
	}

	/* (non-Javadoc)
	 * @see org.jgraph.graph.DefaultGraphCell.ValueChangeHandler#clone()
	 */
	public Object clone() {
		return new GPUserObject(properties);
	}

	public Object getProperty(Object key) {
		return properties.get(key);
	}

	public Object putProperty(Object key, Object value) {
		if (value != null)
			return properties.put(key, value);
		return properties.remove(key);
	}

	/**
	 * @return
	 */
	public Map getProperties() {
		return properties;
	}

	/**
	 * @param map
	 */
	public void setProperties(Map map) {
		properties = map;
	}

	public String toString() {
		Object label = properties.get(keyValue);
		if (label != null)
			return label.toString();
		return super.toString();
	}

}
