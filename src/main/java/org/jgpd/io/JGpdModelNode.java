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

package org.jgpd.io;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.jgpd.UI.PropPanelConfig;
import org.jgpd.UI.tableModels.JGpdTableModel;

public class JGpdModelNode implements Cloneable
{
	public JGpdModelNode(){}
	
	public String getDisplayedNodeType()
	{
		return null;
	}
	
	public String getExportModelPrefix()
	{
		return null;
	}
	
	public String readXML()
	{
		return null;
	}
	
	public String writeXML(String indent)
	{
		return null;
	}
	
	public String getDisplayName()
	{
		return null;
	}
	
	public void setDisplayName(String name){}
	
	public PropPanelConfig propPanelModel(JTable[] table, JGpdTableModel[] tableModel)
	{
		return null;
	}
	
	public void applyProperties(DefaultTableModel[] tableModel){}
	
	public void addSourcedTransition(JGpdModelNode sourceCell,
									 String        transitionName){}

}