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

import java.util.Hashtable;
import java.util.Map;

public class GraphCellIdentityMap {

	protected Map ids = new Hashtable(), objects = new Hashtable();
	long nextID = 0;

	protected void put(String ID, Object obj) {
		ids.put(ID, obj);
		objects.put(obj, ID);
		long id = Long.parseLong(ID);
		nextID = Math.max(nextID, id + 1);
	}

	protected String createID() {
		return Long.toString(nextID++);
	}

	public String getID(Object obj) {
		String ID = null;
		if (obj != null) {
			ID = (String) objects.get(obj);
			if (obj != null && ID == null) {
				ID = createID();
				put(ID, obj);
			}
		}
		return ID;
	}

	public Object getObject(String ID, String type) {
		Object obj = ids.get(ID);
		if (obj == null && type != null) {
			obj = GraphCellFactory.createCell(type, null);
			put(ID, obj);
		}
		return obj;
	}

	public Object[] getObjects(String[] IDs) {
		if (IDs != null) {
			Object[] cells = new Object[IDs.length];
			for (int i = 0; i < IDs.length; i++)
				cells[i] = getObject(IDs[i], null);
			return cells;
		}
		return null;
	}

	protected Object removeID(String ID) {
		Object obj = ids.remove(ID);
		if (obj != null)
			objects.remove(obj);
		return obj;
	}

}
