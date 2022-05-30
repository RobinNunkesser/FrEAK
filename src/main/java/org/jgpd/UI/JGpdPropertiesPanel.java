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

package org.jgpd.UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.border.MatteBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.jgpd.UI.tableModels.JGpdTableModel;
import org.jgpd.io.ModelExportInterface;
import org.jgraph.GPGraphpad;
import org.jgraph.pad.GPDocument;
import org.jgraph.pad.GPGraph;

public class JGpdPropertiesPanel
extends JGpdPanel
implements TreeSelectionListener {
	protected JTable table;
	protected JGpdTableModel tableModel;
	private JGpdTableModel nullModel = new JGpdTableModel();
    protected Map properties;
    
	/** JGpdPropertiesPanel provides the means to alter model specific values
	 * for various secondary models.
	 * Must use the {@link #createPropPanel} method to create a new instance.
	 */
	protected JGpdPropertiesPanel(GPGraph g, GPDocument d, GPGraphpad gp)
    {
        super(g,d,gp);

        ModelExportInterface exportModel = document.getExportModel();
        if ( exportModel == null )
        {
        	// FIXME log this error
        	return;
        }
        
//        setLayout(new BorderLayout());

        tableModel = new JGpdTableModel();
        
        table = new JTable(tableModel);
        
//      Properties pane added at bottom of main display
        JPanel tablePanel = makeTablePanel();
        tablePanel.setPreferredSize( new Dimension(200,100) );
        
        setLayout(new BorderLayout());
        add(BorderLayout.CENTER, tablePanel);
        
        tableModel.setAddDeletingAllowed(true);
        
        
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS)); // V1.3- java calls it Y_AXIS
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
// FIXME implement close button that drop the panel down		JButton closeButton = new JButton("Close"); //Translator.getString("Close") FIXME
		JButton addButton = new JButton("Add"); //Translator.getString("Add") FIXME
		JButton deleteButton = new JButton("Delete"); //Translator.getString("Delete") FIXME
		buttonPanel.add(addButton);
		buttonPanel.add(deleteButton);
// FIXME as above		buttonPanel.add(closeButton);
		add(BorderLayout.EAST, buttonPanel);

		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] selectedRows = table.getSelectedRows();
				int numSelectedRows = table.getSelectedRowCount();
				// Remove the rows in order from highest to lowest
				// As the removal of higher numbered rows doesn't
				// effect the position of lower numbers
				int lowestRowRemoved = 20000000; // stupidly high number to start with
				int highestRowSoFar;
				for (int i=0; i < numSelectedRows; i++)
				{
					highestRowSoFar = -1; // needs resetting for each i loop
					for (int j=0; j < numSelectedRows; j++)
					{
						if ( (selectedRows[j] > highestRowSoFar) &&
						     (selectedRows[j] < lowestRowRemoved) )
						{
							// So this is the highest numbered row we've
							// found in the j loop but it isn't one of
							// the rows we've already removed
							highestRowSoFar = selectedRows[j];
						}
					}
					// Remove the last row
					((DefaultTableModel)(table.getModel()))
					    .removeRow(highestRowSoFar);
					// Store this of the lowest row removed so far
					lowestRowRemoved = highestRowSoFar;
				}
			}
		});
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				// FIXME, any way to do this without creating
				// the unused Object[]
				((DefaultTableModel)(table.getModel()))
				  .addRow(new Object[] { "" });
			}
		});
	}

	protected JPanel makeTablePanel()
    {
		JPanel panel = new JPanel(false);
		JScrollPane scrollPane = new JScrollPane(table);
		panel.setLayout(new GridLayout(1, 1));
		panel.add(scrollPane);
		return panel;
	}

    /** JGpdPropertiesPanel factory that returns instance with small inset as a buffer.*/
	public static JPanel createPropPanel(GPGraph g,
										 GPDocument d,
										 GPGraphpad gp)
	{
		JPanel panelWithInternalOffset = new JPanel();
		panelWithInternalOffset.setLayout(new BorderLayout());
		panelWithInternalOffset.setBorder(new MatteBorder(2,2,2,2,Color.WHITE));
		panelWithInternalOffset.add(new JGpdPropertiesPanel(g,d,gp), BorderLayout.CENTER);
		return panelWithInternalOffset;
	}

	//
	// GraphModelListener
	//

	/**
	 * Update the panel when the selection changes
	 */
	public void valueChanged(TreeSelectionEvent e)
    {
		if (listenedComponent != null)
        {
			JTree tree = (JTree)listenedComponent;
        	// Obtain reference to the selected object
			TreePath path = tree.getSelectionPath();
			if (path == null) 
			{
				disablePropPanel();
				return;
			}
			DefaultMutableTreeNode treeNode =
						(DefaultMutableTreeNode)path.getLastPathComponent();

			Object currentUserObject = treeNode.getUserObject();
            
			// Send object hashCode to model interface to ask
			// for panel details if this panel is visible
			
			// FIXME todo if panel !visibile return
			if (currentUserObject != null)
			{
				if ( currentUserObject.equals(lastCellSelected) )
				{
					// if you select the same cell twice
					// this function still fires twice
					// ignore subsequent fires
				}
				else
				{	
					lastCellSelected = currentUserObject;
					ModelExportInterface exportModel = document.getExportModel();
					if ( exportModel != null )
					{
						JGpdTableModel tableModel =
									exportModel.createTableModel( currentUserObject );
						if ( tableModel == null )
						{
							// This type of object does populate the properties panel
							disablePropPanel();
						}
						else
						{
							addTable(tableModel);
						}
					}
					else
					{
						disablePropPanel();
//						FIXME todo log error of no export model available
					}
				}
			}
			else
			{
				disablePropPanel();
				// FIXME todo log error of cell can't be obtained
			}		
		}
	}

	protected void addTable(JGpdTableModel tableModel)
	{
		table.setModel(tableModel);
		
		tableModel.configureTable(table);
		
	}
	
	protected void disablePropPanel()
	{
		table.setModel(nullModel);
		
		// null the lastCellSelected attribute. Otherwise you
		// can select a cell, click on empty space then select
		// the cell again and the valueChanged function will
		// ignore it thinking it is a repeat click
		lastCellSelected = null;
		currentUserObject= null;
	}
	
	public JComponent getMainComponent()
	{
		return table;
	}
}