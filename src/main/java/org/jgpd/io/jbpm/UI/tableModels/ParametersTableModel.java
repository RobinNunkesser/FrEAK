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

import java.util.Iterator;

import javax.swing.JTable;

import org.jgpd.io.jbpm.definition.impl.Parameter;
import org.jgpd.io.jbpm.definition.impl.ParametersImpl;

public class ParametersTableModel extends JbpmTableModel
{
	public ParametersTableModel( Object cell )
	{
		super(cell);

		setColumnIdentifiers(new Object[]{"Name", "Value"});
		// No entries allowed, set any default entry string
		setDefaultCellEntries(new Object[]{"Enter name here", "Enter value here"});
		setAddDeletingAllowed(true);
		setColumnEditable(new boolean[]{true,true});
		setMaxNumDynamicRows(50000);
		setNumStaticRows(0);

		ParametersImpl params = (ParametersImpl)userObject;

		Iterator iter = params.iterator();
		while (iter.hasNext())
		{
			Parameter param = (Parameter) iter.next();
			addRow( new Object[] {
					param.getName(),
					param.getValue()});
		}

	}

	public Object getValueAt(int nRow, int nCol) {
		if (nRow < 0 || nRow>=getRowCount()) return "";

		if ( nCol == 0)
		{
			switch (nRow)
			{
			case 0:
				return "Name";
			case 1:
				return "Value";
			}
		}
		else if ( nCol == 1 )
		{
			ParametersImpl params = (ParametersImpl)userObject;
			if ( nRow > params.size() )
			{
				return "";
			}
			
			Parameter param = (Parameter)params.get(nRow);
			
			switch (nRow)
			{
			case 0:
				return param.getName();
			case 1:
				return param.getValue();
			}
		}
		return "";
	}

	public void setValueAt(Object value, int nRow, int nCol)
	{
		if (nRow < 0 || nRow>=getRowCount() || value == null) return;
		if (nCol != 1) return;
		
		ParametersImpl params = (ParametersImpl)userObject;
		if ( nRow > params.size() ) return;
			
		Parameter param = (Parameter)params.get(nRow);
		switch (nRow)
		{
		case 0:
		param.setName(value.toString());
			break;

		case 1:
		param.setValue(value.toString());
			break;
			
		}
	}

	public void configureTable(JTable table)
	{
		super.configureTable(table);
	}
}
