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

import org.jgpd.io.jbpm.definition.impl.DecisionImpl;

public class DecisionTableModel extends JbpmTableModel
{
	public DecisionTableModel( Object cell )
	{
		super(cell);

		setColumnIdentifiers(new Object[]{"Property",
													  "Value"});
		// No entries allowed, set any default entry string
		setDefaultCellEntries(new Object[]{"", ""});
		setAddDeletingAllowed(false);
		setColumnEditable(new boolean[]{false,true});
		setMaxNumDynamicRows(0);
		insertRow( 0, new Object[] { "Name",
									((DecisionImpl)userObject).getName(), });
		setNumStaticRows(1);
	}

	public Object getValueAt(int nRow, int nCol) {
		if (nRow < 0 || nRow>=getRowCount()) return "";

		if ( nCol == 0)
		{
			switch (nRow)
			{
			case 0:
				return "Event";
			}
		}
		else if ( nCol == 1 )
		{
			switch (nRow)
			{
			case 0:
				return ((DecisionImpl)userObject).getName();
			}
		}
		return "";
	}

	public void setValueAt(Object value, int nRow, int nCol)
	{
		if (nRow < 0 || nRow>=getRowCount() || value == null) return;
		if (nCol != 1) return;
		
		switch (nRow)
		{
		case 0:
			((DecisionImpl)userObject).setName(value.toString());
			break;
		}
	}

	public void configureTable(JTable table)
	{
		super.configureTable(table);
	}	
}
