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
import javax.swing.table.TableColumn;

import org.jgpd.UI.utils.FileWizardEditor;
import org.jgpd.io.jbpm.definition.EventType;
import org.jgpd.io.jbpm.definition.impl.ActionImpl;
import org.jgpd.io.utils.FilePackage;

public class ActionTableModel extends JbpmTableModel
{
	public ActionTableModel( Object cell )
	{
		super(cell);
		// Actions tab
		setColumnIdentifiers(new Object[]{"Event","Handler",
				"On_Exception","Parameters"});
		setDefaultCellEntries(new Object[] {EventType.PROCESS_START,
											"Handler not set",
											"Exception not set",
											"Edit Parameters"});
		setAddDeletingAllowed(true);
		setColumnEditable(new boolean[]{true,true,true,true});
		setMaxNumDynamicRows(50000);
		setNumStaticRows(0);
		
		addRow( new Object[] {
				((ActionImpl)userObject).getEventType().toString(),
				((ActionImpl)userObject).getHandler(),
				((ActionImpl)userObject).getException()});
	}
	
	public Object getValueAt(int nRow, int nCol) {
		if (nRow < 0 || nRow>=getRowCount()) return "";

		if ( nCol == 0)
		{
			switch (nRow)
			{
			case 0:
				return "Event";
			case 1:
				return "Handler";
			case 2:
				return "On Exception";
			}
		}
		else if ( nCol == 1 )
		{
			switch (nRow)
			{
			case 0:
				return ((ActionImpl)userObject).getEventType();
			case 1:
				return ((ActionImpl)userObject).getHandler();
			case 2:
				return ((ActionImpl)userObject).getException();
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
			((ActionImpl)userObject).setEventType((EventType)value);
			break;

		case 1:
			((ActionImpl)userObject).setHandler((FilePackage)value);
			break;
			
		case 2:
			((ActionImpl)userObject).setException((FilePackage)value);
			break;
		}
	}

	public void configureTable(JTable table)
	{
		super.configureTable(table);
		
		TableColumn handlerColumn = table.getColumnModel().getColumn(1);
		TableColumn exceptColumn = table.getColumnModel().getColumn(2);
		TableColumn paramColumn = table.getColumnModel().getColumn(3);

//		FileWizardEditor fileWizard =
//			new FileWizardEditor(table);
//		handlerColumn.setCellEditor(fileWizard);
	}
}
