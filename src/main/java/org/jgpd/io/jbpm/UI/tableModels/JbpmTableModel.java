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

package org.jgpd.io.jbpm.UI.tableModels;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.jgpd.UI.tableModels.JGpdTableModel;

class JbpmTableModel extends JGpdTableModel
{
	protected Object userObject;
	
	JbpmTableModel(Object cell)
	{
		super();
		userObject = cell;
	}
	
	public void configureTable(JTable table)
	{
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}


}