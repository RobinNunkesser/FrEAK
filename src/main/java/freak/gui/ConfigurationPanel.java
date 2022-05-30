/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.gui;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.TargetError;
import freak.core.control.BatchList;
import freak.core.control.EventController;
import freak.core.graph.Operator;
import freak.core.graph.Port;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.Module;
import freak.core.modulesupport.PropertyManager;
import freak.core.modulesupport.inspector.Inspector;
import freak.core.modulesupport.inspector.InspectorVetoException;
import freak.gui.scheduleeditor.Util;
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * @author  Oliver, Matthias
 */
public class ConfigurationPanel extends javax.swing.JPanel {

	private Module activeModule;
	private EventController eventController;
	private JPanel propertyPanel;
	private EventTable eventTable;
	private BatchList batchList;

	/**
	 * Creates new ConfigurationPanel
	 * @param m the <code>Module</code> that is to be configured
	 * @param eventController the <code>EventController</code> of the current <code>Schedule</code>
	 * @param eventSources possible eventsources the module can register itself at
	 */
	public ConfigurationPanel(Module m, EventController eventController, Object[] eventSources, BatchList batchList) {
		initComponents();
		this.batchList = batchList;
		
		propertyGenerationCode.getDocument().addDocumentListener(new DocumentListener()
			{
				public void changedUpdate(DocumentEvent arg0) {
					update(arg0.getDocument());				
				}
			
				public void insertUpdate(DocumentEvent arg0) {
					update(arg0.getDocument());
				}
				
				public void removeUpdate(DocumentEvent arg0) {
					update(arg0.getDocument());
				}
				
				private void update(Document d) {
					Interpreter i = new Interpreter();
					i.setStrictJava(true);
					try {
			
						i.set("batch", 0);

						try {
							i.eval(d.getText(0,d.getLength()));
						} catch (BadLocationException e1) {
						}
						
						((javax.swing.border.TitledBorder)scrollErrorMsg.getBorder()).setTitle("Code compilation successful.");
						((javax.swing.border.TitledBorder)scrollErrorMsg.getBorder()).setTitleColor(Color.gray);
						taErrorMsg.setText("");
						panelCodeEditing.updateUI();

					} catch (EvalError e) {
						if (getErrorLine(e) < 0) {
							((javax.swing.border.TitledBorder)scrollErrorMsg.getBorder()).setTitle("Runtime error in unknown line");
						} else {
							((javax.swing.border.TitledBorder)scrollErrorMsg.getBorder()).setTitle("Error in line: "+getErrorLine(e));
						}
						((javax.swing.border.TitledBorder)scrollErrorMsg.getBorder()).setTitleColor(Color.red);
						taErrorMsg.setText(getErrorMessage(e));
						panelCodeEditing.updateUI();
					}
				}
			});

		if (m != null) {
			this.eventController = eventController;
			this.activeModule = m;

			displayModuleInfo();

			if (activeModule instanceof Operator && hasAnyPortWithDescription((Operator)activeModule))
				displayPortInfo();
			else
				scrollPorts.setVisible(false);

			// -- show property panel if module is configurable
			// -- a StandardPropertyPanel is always instant
			if (activeModule instanceof Configurable) {
				propertyPanel = ((Configurable)activeModule).getInspector();
				scrollProperties.setViewportView(propertyPanel);
			} else
				scrollProperties.setVisible(false);

			// -- show event table if module has non-static events
			if (eventController.hasCustomizableEvents(activeModule)) {
				eventTable = new EventTable(activeModule, eventController, eventSources);
				scrollEvents.setViewportView(eventTable);
			} else {
				scrollEvents.setVisible(false);
			}
			if (activeModule instanceof Configurable) {
				propertyGenerationCode.setText(batchList.getConfigurationCode((Configurable)activeModule));
			}
		} else {
			panelModule.setVisible(false);
			scrollPorts.setVisible(false);
			scrollProperties.setVisible(false);
			scrollEvents.setVisible(false);
		}

	}
	
	private int getErrorLine(EvalError e) {
		try {
			int line = e.getErrorLineNumber();
			if (line >= 0) {
				return line;
			}
		} catch (Exception exp) {
		}
		
		if (e.getMessage().startsWith("Parse error at line")) {
			try {
				String[] m = e.getMessage().split(" ",9);
				m[6] = m[6].trim();
				int line = Integer.parseInt(m[4].trim().replaceAll(",",""));
				return line;
			} catch (NumberFormatException ex) {
				return -1;
			}
		} else if (e.getMessage().indexOf("'' Token Parsing Error:") > 0) {
			String[] m = e.getMessage().substring(e.getMessage().indexOf("'' Token Parsing Error:")+24).split(" ");
			return Integer.parseInt(m[4].trim().replaceAll(",",""));
		} else {	
			return -1;
		}
	}
	
	private String getErrorMessage(EvalError e) {
		if (e instanceof TargetError) {
		    return ((TargetError)e).getTarget().getLocalizedMessage();
		}
		if (e.getMessage().startsWith("Parse error at line")) {
			String[] m = e.getMessage().split(" ",9);
			m[6] = m[6].trim();
			String err = m[8].trim();

			return err;
		} else {
			String m = e.getMessage().trim();
			
			if (m.indexOf("'' Token Parsing Error:") > 0) {
				return m.substring(m.lastIndexOf("Encountered:"));
			}
			
			String[] searchStrings = {"'' :",  "'' internal Error:"};
			
			int start = 0;
			
			for (int i = 0; i < searchStrings.length; i++) {
				String s = searchStrings[i];
				start = Math.max(m.indexOf(s)+s.length(), start);
			}
			
			
			return m.substring(start).trim();
		}
	}

	private boolean hasAnyPortWithDescription(Operator operator) {
		for (int i = 0; i < operator.getNumberOfInPorts() + operator.getNumberOfOutPorts(); i++) {
			String description = operator.getPortAt(i).getDescription();
			if (description != null && !description.equals(""))
				return true;
		}
		return false;
	}

	private void displayModuleInfo() {
		Util.displayText(tfName, activeModule.getName());
		Util.displayText(taDescription, activeModule.getDescription());
	}

	private void displayPortInfo() {
		Operator g = (Operator)activeModule;

		DefaultTableModel tm = new DefaultTableModel(new Object[] { "Port", "Description" }, 0);
		tablePorts.setModel(tm);

		TableColumn col = tablePorts.getColumnModel().getColumn(0);
		col.setPreferredWidth(col.getMinWidth());

		tablePorts.setEnabled(false);

		for (int i = 0; i < g.getNumberOfInPorts(); i++) {
			Port p = g.getPortAt(i);
			if (p.getDescription() != null && !p.getDescription().equals("")) {
				Object[] row = new Object[] { "In " + new Integer(i + 1), p.getDescription()};
				tm.addRow(row);
			}
		}
		for (int i = g.getNumberOfInPorts(); i < g.getNumberOfInPorts() + g.getNumberOfOutPorts(); i++) {
			Port p = g.getPortAt(i);
			if (p.getDescription() != null && !p.getDescription().equals("")) {
				Object[] row = new Object[] { "Out " + new Integer(i - g.getNumberOfInPorts() + 1), p.getDescription()};
				tm.addRow(row);
			}
		}
	}
	
	/**
	 * This method is called when the ConfigurationPanel is to be closed by the 
	 * GUI.
	 * 
	 * @throws InspectorVetoException if the closing is vetoed by some elements of the panel.
	 */
	public void finishEditing() throws InspectorVetoException {
		if (propertyPanel != null) {
			((Inspector)propertyPanel).finishEditing();
		}
		
		if (activeModule != null) {
			checkCodeEditing();
		}
	}
	
	private void checkCodeEditing() throws InspectorVetoException {
		Interpreter i = new Interpreter();
		i.setStrictJava(true);
		try {
			
			i.set("batch", 0);
			
			// used to check property types
			String[] pNames = PropertyManager.getListOfPropertyNames(activeModule);
			for (int j = 0; j < pNames.length; j++) {
				try {
					i.set(pNames[j], PropertyManager.getProperty(activeModule, pNames[j]));
				} catch (UnsupportedOperationException e) {
				}
			}

			i.eval(propertyGenerationCode.getText());

			// check property types
			for (int j = 0; j < pNames.length; j++) {
				Class typeClass = PropertyManager.getPropertyType(activeModule, pNames[j]);
				if (i.get(pNames[j]) != null) {
					Class inputClass = i.get(pNames[j]).getClass();
					if (!(typeClass.isAssignableFrom(inputClass))) {
						throw new InspectorVetoException("Wrong property type for property " + pNames[j] + ": " + inputClass.getName() + ".\nShould be " + typeClass.getName() + ".");
					}
				}
			}


			if (activeModule instanceof Configurable) {
				batchList.setConfigurationCode((Configurable)activeModule, propertyGenerationCode.getText());
			}			
			
		} catch (EvalError e) {
			throw new InspectorVetoException("Code Error in line: "+getErrorLine(e));
		}
	}
	
	/**
	 * @return the module that is configured in this panel.
	 */
	public Module getConfiguredModule() {
		return activeModule;
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        scrollEvents = new javax.swing.JScrollPane();
        panelModule = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        taDescription = new javax.swing.JTextArea();
        tfName = new javax.swing.JTextField();
        scrollPorts = new javax.swing.JScrollPane();
        tablePorts = new javax.swing.JTable();
        editingType = new javax.swing.JTabbedPane();
        scrollProperties = new javax.swing.JScrollPane();
        panelCodeEditing = new javax.swing.JPanel();
        scrollCode = new javax.swing.JScrollPane();
        propertyGenerationCode = new javax.swing.JTextArea();
        scrollErrorMsg = new javax.swing.JScrollPane();
        taErrorMsg = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        scrollEvents.setBorder(new javax.swing.border.TitledBorder("Events"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        add(scrollEvents, gridBagConstraints);

        panelModule.setLayout(new java.awt.GridBagLayout());

        panelModule.setBorder(new javax.swing.border.TitledBorder("Module-Information"));
        panelModule.setMinimumSize(new java.awt.Dimension(0, 100));
        panelModule.setPreferredSize(new java.awt.Dimension(0, 100));
        jLabel2.setText("Name:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        panelModule.add(jLabel2, gridBagConstraints);

        taDescription.setEditable(false);
        taDescription.setLineWrap(true);
        taDescription.setWrapStyleWord(true);
        jScrollPane1.setViewportView(taDescription);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        panelModule.add(jScrollPane1, gridBagConstraints);

        tfName.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        panelModule.add(tfName, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.1;
        add(panelModule, gridBagConstraints);

        scrollPorts.setBorder(new javax.swing.border.TitledBorder("Port Descriptions"));
        scrollPorts.setMinimumSize(new java.awt.Dimension(0, 80));
        scrollPorts.setPreferredSize(new java.awt.Dimension(0, 80));
        scrollPorts.setViewportView(tablePorts);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.1;
        add(scrollPorts, gridBagConstraints);

        editingType.addTab("Static Editing", scrollProperties);

        panelCodeEditing.setLayout(new java.awt.BorderLayout());

        scrollCode.setBorder(new javax.swing.border.TitledBorder("Property Generation Code"));
        scrollCode.setViewportView(propertyGenerationCode);

        panelCodeEditing.add(scrollCode, java.awt.BorderLayout.CENTER);

        scrollErrorMsg.setBorder(new javax.swing.border.TitledBorder(null, "Error compiling Code", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 12), java.awt.Color.red));
        taErrorMsg.setWrapStyleWord(true);
        taErrorMsg.setLineWrap(true);
        taErrorMsg.setEditable(false);
        taErrorMsg.setRows(4);
        taErrorMsg.setBorder(null);
        scrollErrorMsg.setViewportView(taErrorMsg);

        panelCodeEditing.add(scrollErrorMsg, java.awt.BorderLayout.SOUTH);

        editingType.addTab("Code Editing", panelCodeEditing);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.2;
        add(editingType, gridBagConstraints);

    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane editingType;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel panelCodeEditing;
    private javax.swing.JPanel panelModule;
    private javax.swing.JTextArea propertyGenerationCode;
    private javax.swing.JScrollPane scrollCode;
    private javax.swing.JScrollPane scrollErrorMsg;
    private javax.swing.JScrollPane scrollEvents;
    private javax.swing.JScrollPane scrollPorts;
    private javax.swing.JScrollPane scrollProperties;
    private javax.swing.JTextArea taDescription;
    private javax.swing.JTextArea taErrorMsg;
    private javax.swing.JTable tablePorts;
    private javax.swing.JTextField tfName;
    // End of variables declaration//GEN-END:variables

}
