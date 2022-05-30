/*
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

package org.jgpd.io.jbpm.UI;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import org.jgpd.UI.tableModels.JGpdTableModel;
import org.jgpd.io.jbpm.definition.impl.ProcessDefinitionImpl;
import org.jgraph.pad.GPGraph;

public class ProcessPropertiesDialog
{
	/* Map that holds the attributes (key value pairs)
	 */
	protected transient JDialog propertiesDialog;

	protected transient JTable table;

	protected transient JGpdTableModel tableModel;

	public ProcessPropertiesDialog(){}

	public void showPropertyDialog(final GPGraph graph,
									  final ProcessDefinitionImpl process)
	{
		Frame frame = (Frame) SwingUtilities.windowForComponent(graph);
		if (frame != null && propertiesDialog == null)
		{
			propertiesDialog = new JDialog(frame, "", false);
			Container fContentPane = propertiesDialog.getContentPane();
			fContentPane.setLayout(new BorderLayout());
			tableModel = new JGpdTableModel(new Object[] { "Property", "Value" }, 0);

			tableModel.setRowCount(0);
			tableModel.setColumnIdentifiers(new Object[]{"Property",
			"Value"});
			// No entries allowed, set any default entry string
			tableModel.setDefaultCellEntries(new Object[]{"", ""});
			tableModel.setAddDeletingAllowed(false);
			tableModel.setColumnEditable(new boolean[]{false,true});
			tableModel.setMaxNumDynamicRows(0);
			tableModel.setNumStaticRows(3);

			tableModel.insertRow( 0, new Object[] {
					"Name",
					process.getName()});
			tableModel.insertRow( 1, new Object[] {
					"Description",
					process.getDescription()});
			tableModel.insertRow( 2, new Object[] {
					"Responsible",
					process.getResponsibleUserName()});

			tableModel.setNumStaticRows(3);

			table = new JTable(tableModel);
			JScrollPane scrollpane = new JScrollPane(table);

			fContentPane.add(BorderLayout.CENTER, scrollpane);
			JButton okButton = new JButton("OK"); //Translator.getString("OK") FIXME
			JButton cancelButton = new JButton("Cancel"); //Translator.getString("Close") FIXME
			JPanel buttonPanel = new JPanel();
			buttonPanel.add(okButton);
			buttonPanel.add(cancelButton);
			fContentPane.add(BorderLayout.SOUTH, buttonPanel);
			okButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						apply(process, tableModel);
						propertiesDialog.dispose();
					}
				});
			cancelButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						propertiesDialog.dispose();
					}
				});
			propertiesDialog.setSize(new Dimension(300, 300));
			propertiesDialog.setLocationRelativeTo(frame);
		}
		
		propertiesDialog.setTitle("Process properties");
		propertiesDialog.show();
	}

	protected void apply(	ProcessDefinitionImpl process,
							JGpdTableModel model)
	{
		Vector propsVector = model.getDataVector();
		Object name = ((Vector)propsVector.get(0)).get(1);
		Object desc = ((Vector)propsVector.get(1)).get(1);
		Object user = ((Vector)propsVector.get(2)).get(1);
		
		if ( name != null )
		{
			process.setName(name.toString());
		}

		if ( desc != null )
		{
			process.setDescription(desc.toString());
		}
		
		if ( user != null )
		{
			process.setResponsibleUserName(user.toString());
		}
	}
}