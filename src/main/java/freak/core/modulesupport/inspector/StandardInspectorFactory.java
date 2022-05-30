/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.modulesupport.inspector;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import freak.core.graph.Operator;
import freak.core.modulesupport.Module;
import freak.core.modulesupport.PropertyManager;
import freak.core.parametercontroller.ParameterController;
import freak.gui.CheckBoxDialog;

/**
 * This class is a factory for generating Standard Inspectors
 * for objects' properties. For a property to be accessable,
 * there must be getter and setter methods in the class following the
 * following naming conventions.<br>
 * 
 * For a property with name <code>X</code> of type <code>Type</code> 
 * there must be the methods<br>
 * <code>Type getPropertyX()</code><br>
 * <code>void setPropertyX(Type)</code><br>
 * 
 * in the class.
 *  
 * @author Kai, Dirk, Heiko, Michael
 */
public class StandardInspectorFactory {

	private static boolean parameterControllerDependencyWarningAcknowledged =
		false;

	/**
	 * Returns a Standard inspector for the given object. For Details on
	 * how to make properties accessable see the HOWTO file.
	 * 
	 * @param module the module for which the Inspector should be created.
	 * @return the Inspector.
	 */
	public static CustomizableInspector getStandardInspectorFor(final Module module) {

		// construct JTextArea for descriptions of properties
		JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setToolTipText(
			"Here you can read the description of the selected property.");

		// construct table data
		String[] pNames = PropertyManager.getListOfPropertyNames(module);

		int pCount = pNames.length;

		// sort property names by short descriptions.
		Arrays.sort(pNames, new Comparator() {
			public int compare(Object o1, Object o2) {
				if ((!(o1 instanceof String)) || (!(o2 instanceof String)))
					return 0;
				String descr1 =
					PropertyManager.getShortDescriptionFor(module, (String) o1);
				String descr2 =
					PropertyManager.getShortDescriptionFor(module, (String) o2);
				return descr1.compareTo(descr2);
			}
		});

		String[] pLongDescr = new String[pCount];
		Object[][] tableData = new Object[pCount][2];

		for (int i = 0; i < pCount; i++) {
			try {
				tableData[i][1] = PropertyManager.getProperty(module, pNames[i]);
				tableData[i][0] = PropertyManager.getShortDescriptionFor(module, pNames[i]);
				
				// create long description
				pLongDescr[i] = PropertyManager.getLongDescriptionFor(module, pNames[i]);
			} catch (UnsupportedOperationException e) {
				// TODO ? remove this comment and/or clean this code <matthias>
				// dirty
				pCount--;
				String[] pNamesNew = new String[pCount];
				if (i>0) System.arraycopy(pNames, 0, pNamesNew, 0, i);
				System.arraycopy(pNames, i+1, pNamesNew, i, pNames.length-i-1);
				String[] pLongDescrNew = new String[pCount];
				if (i>0) System.arraycopy(pLongDescr, 0, pLongDescrNew, 0, i);
				Object[][] tableDataNew = new Object[pCount][2];
				if (i>0) System.arraycopy(tableData, 0, tableDataNew, 0, i);
				pNames = pNamesNew;
				tableData = tableDataNew;
				pLongDescr = pLongDescrNew;
				i--;
			}
		}

		// create table model
		final StandardInspectorTableModel tm =
			(new StandardInspectorFactory()).new StandardInspectorTableModel(
				tableData,
				module,
				pNames,
				pLongDescr,
				textArea);

		// create table
		StandardInspectorTable table =
			(new StandardInspectorFactory()).new StandardInspectorTable(
				tm,
				textArea,
				pLongDescr);

		table.setPreferredScrollableViewportSize(table.getPreferredSize());

		// add listener for parameter controller dependency warnings
		tm.addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				for (int i = e.getFirstRow(); i <= e.getLastRow(); i++) {
					checkParameterControllerDependencies(
						module,
						tm.getPropertyName(i));
				}
			}
		});

		CustomizableInspector inspector = new CustomizableInspector(table, module);

		inspector.setLayout(new GridBagLayout());

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.weighty = 0.5;
		gridBagConstraints.fill = GridBagConstraints.BOTH;

		inspector.add(new JScrollPane(table), gridBagConstraints);

		/* Customize the StandardInspector to support objects of 
		 * unknown classes.
		 */
		inspector.customize(
			Color.class,
			new ColorRenderer(),
			new ColorEditor());
		inspector.customize(
			Double[].class,
			new DoubleArrayRenderer(),
			new DoubleArrayEditor());
		inspector.customize(
			StringArrayWrapper.class,
			new StringArrayRenderer(),
			new StringArrayEditor());

		/*
		 * Create description.
		 */
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setMinimumSize(new java.awt.Dimension(10, 50));
		scrollPane.setPreferredSize(new java.awt.Dimension(10, 50));

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.weighty = 0.0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(5, 0, 0, 0);
		inspector.add(scrollPane, gridBagConstraints);

		return inspector;
	}

	/**
	 * Shows a warning if a parameter controller is controlling the 
	 * specified property in <code>obj</code>.
	 *  
	 * @param obj the object and property container.
	 * @param propName the name of the property to check.
	 */
	private static void checkParameterControllerDependencies(
		final Object obj,
		final String propName) {
		if (!parameterControllerDependencyWarningAcknowledged) {
			if (obj instanceof Operator) {
				Operator operator = (Operator) obj;

				List concurrentControllers =
					operator
						.getOperatorGraph()
						.getParameterControllersForProperty(
						operator,
						propName);

				if (concurrentControllers.size() > 0) {
					ParameterController[] controllers =
						new ParameterController[concurrentControllers.size()];
					concurrentControllers.toArray(controllers);

					// sort controllers alphabetically by name
					Arrays.sort(controllers, new Comparator() {
						public int compare(Object o1, Object o2) {
							if ((!(o1 instanceof ParameterController))
								|| (!(o2 instanceof ParameterController)))
								return 0;
							return ((ParameterController) o1)
								.getName()
								.compareTo(
								((ParameterController) o2).getName());
						}
					});

					// create message
					String descr =
						PropertyManager.getShortDescriptionFor(
							operator,
							propName);
					StringBuffer s =
						new StringBuffer(
							"The property "
								+ descr
								+ " is controlled by the following parameter controller"
								+ (controllers.length > 1 ? "s" : "")
								+ ":\n");
					for (int i = 0; i < controllers.length; i++) {
						s.append("\n    " + controllers[i].getName());
					}
					s.append(
						"\n\nNote that your input might be overwritten by the controller"
							+ (controllers.length > 1 ? "s" : "")
							+ ".");

					// show dialog
					CheckBoxDialog dialog =
						new CheckBoxDialog(
							null,
							true,
							s.toString(),
							"Warning",
							"don't show this warning again");
					dialog.setLocationRelativeTo(null);
					dialog.setVisible(true);
					parameterControllerDependencyWarningAcknowledged =
						dialog.isCheckBoxSelected();
				}
			}
		}
	}

	/**
	 * A TableModel for the StandardInspector.
	 *  
	 * @author Kai, Dirk
	 */
	public class StandardInspectorTableModel extends AbstractTableModel {

		/**
		 * The table model's data.
		 */
		protected Object[][] data;

		/**
		 * The names of the properties in the table.
		 */
		protected String[] pNames;

		/**
		 * The object of which the properties are modified.
		 */
		protected Object propContainer;

		/**
		 * This is used for updating the long descriptions when values of
		 * properties change.
		 */
		protected String[] pLongDescr;

		/**
		 * This is used for updating the long descriptions when values of
		 * properties change.
		 */
		protected JTextArea textArea;

		/**
		 * This constructor sets the table's data.
		 * 
		 * @param data the table's data
		 * @param propContainer the object whose properties are displayed and editable.
		 * @param pNames the names of the properties in the table
		 * @param textArea the JTextArea where the descriptions of properties are displayed
		 */
		public StandardInspectorTableModel(
			Object[][] data,
			Object propContainer,
			String[] pNames,
			String[] pLongDescr,
			JTextArea textArea) {
			this.data = data;
			this.propContainer = propContainer;
			this.pNames = pNames;
			this.pLongDescr = pLongDescr;
			this.textArea = textArea;

		}

		/**
		 * Returns true for column 2 which contains the editable cells.
		 */
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return (columnIndex == 1);

		}

		/**
		 * Receives changes in the table.
		 */
		public void setValueAt(Object value, int row, int col) {

			// get property name from pNames array, NOT from the table.
			// The strings in the table are the short descriptions of the properties.
			String propName = pNames[row];

			Class propType =
				PropertyManager.getPropertyType(propContainer, propName);

			// Update property
			// we have to check the Class of value

			try {

				if (PropertyManager.assignableFrom(propType, Float.class)) {
					PropertyManager.setProperty(
						propContainer,
						propName,
						new Float((String) value));

				} else if (
					PropertyManager.assignableFrom(propType, Double.class)) {
					PropertyManager.setProperty(
						propContainer,
						propName,
						new Double((String) value));

				} else if (
					PropertyManager.assignableFrom(propType, Byte.class)) {
					PropertyManager.setProperty(
						propContainer,
						propName,
						new Byte((String) value));

				} else if (
					PropertyManager.assignableFrom(propType, Integer.class)) {
					PropertyManager.setProperty(
						propContainer,
						propName,
						new Integer((String) value));

				} else if (
					PropertyManager.assignableFrom(propType, String.class)) {
					PropertyManager.setProperty(propContainer, propName, value);

				} else if (
					PropertyManager.assignableFrom(propType, Boolean.class)) {
					PropertyManager.setProperty(propContainer, propName, value);

				} else if (
					PropertyManager.assignableFrom(propType, Double.class)) {
					PropertyManager.setProperty(
						propContainer,
						propName,
						new Double((String) value));

				} else if (
					PropertyManager.assignableFrom(propType, Long.class)) {
					PropertyManager.setProperty(
						propContainer,
						propName,
						new Long((String) value));

				} else if (
					PropertyManager.assignableFrom(propType, Short.class)) {
					PropertyManager.setProperty(
						propContainer,
						propName,
						new Short((String) value));

				} else {
					PropertyManager.setProperty(propContainer, propName, value);
				}

			} catch (IllegalArgumentException e) {
				System.out.println("Error converting input: " + e);
				//e.printStackTrace();
			}

			/*
			 * Refetch property value to get the result of the setProperty method 
			 * in the current module. 
			 * So instead of the input value, getProperty(setProperty(value)) 
			 * is used.
			 */
			value = PropertyManager.getProperty(propContainer, propName);

			if (value != data[row][col]) {

				// Update table data and fire events
				updateAllFields(row);
			}
		}

		/**
		 * This method updates all (name,value) pairs in the table.
		 * 
		 * @param updatedRow the row which has changed, needed for
		 * updating the text Area displaying the property's long description
		 */
		protected void updateAllFields(int updatedRow) {

			updateAllFields();

			// update long description of edited row in text Area
			textArea.setText(pLongDescr[updatedRow]);

		}

		/**
		* This method updates all (name,value) pairs in the table.		
		*/
		public void updateAllFields() {
			for (int i = 0; i < pNames.length; i++) {
				// update description
				String sDescr =
					PropertyManager.getShortDescriptionFor(
						propContainer,
						pNames[i]);
				if (!sDescr.equals(data[i][0])) {
					data[i][0] = sDescr;
					fireTableCellUpdated(i, 0);
				}

				// update value
				Object value =
					PropertyManager.getProperty(propContainer, pNames[i]);
				if (!value.equals(data[i][1])) {
					data[i][1] = value;
					fireTableCellUpdated(i, 1);
				}

				// update long description
				pLongDescr[i] =
					PropertyManager.getLongDescriptionFor(
						propContainer,
						pNames[i]);

			}
		}

		/**
		 * Returns the number of rows/properties in the table.
		 */
		public int getRowCount() {
			return data.length;
		}

		/**
		 * Returns the number of columns in the table, always 2.
		 */
		public int getColumnCount() {
			return (2);
		}

		/**
		 * Returns the data at the specified position in the table.
		 */
		public Object getValueAt(int rowIndex, int columnIndex) {
			return data[rowIndex][columnIndex];
		}

		/**
		 * Returns the property name for the specified row. 
		 */
		protected String getPropertyName(int row) {
			return pNames[row];
		}

		/**
		 * Returns the name for the specified column.
		 */
		public String getColumnName(int column) {
			return (column == 0 ? "Property" : "Value");
		}

	}

	/**
	 * When a cell is edited, a listener is used to take care 
	 * that the editor is stopped and the new data is set if the editor
	 * component loses focus. 
	 * 
	 * @author Dirk
	 */
	class StandardInspectorTable extends CustomizableInspectorTable {

		public StandardInspectorTable(
			final TableModel dm,
			JTextArea textArea,
			String[] propDescriptions) {
			super(dm);

			// set up selection model for rows and listener
			// for changing property descriptions in text Area			
			this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			ListSelectionModel rowSM = this.getSelectionModel();
			rowSM.addListSelectionListener(
				new SelectionListener(textArea, propDescriptions));

		}

		/**
		 * This listens for row selection events and changes
		 * the property description in the TextArea.
		 *  
		 * @author Kai
		 */
		class SelectionListener implements ListSelectionListener {

			protected JTextArea textArea;
			protected String[] propDescriptions;

			public SelectionListener(
				JTextArea textArea,
				String[] propDescriptions) {
				this.textArea = textArea;
				this.propDescriptions = propDescriptions;

			}

			public void valueChanged(ListSelectionEvent e) {

				if (e.getValueIsAdjusting())
					return;

				ListSelectionModel lsm = (ListSelectionModel) e.getSource();

				if (!lsm.isSelectionEmpty()) {
					int row = lsm.getMinSelectionIndex();

					textArea.setText(propDescriptions[row]);

				}

			}

		}

	}

}
