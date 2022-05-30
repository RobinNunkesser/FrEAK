/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.module.view;

import freak.core.control.Schedule;
import freak.core.event.BatchEvent;
import freak.core.event.RunEvent;
import freak.core.event.RunEventListener;
import freak.core.observer.Observer;
import freak.core.view.HistoryView;
import freak.core.view.ObserverUnknownException;
import freak.core.view.swingsupport.FreakLargeTableModel;
import freak.core.view.swingsupport.FreakScrollBarAdjustmentListener;
import freak.core.view.swingsupport.FreakTableModel;
import freak.core.view.swingsupport.UpdateManager;

import java.awt.BorderLayout;
import java.lang.reflect.Array;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * A view that displays the number values for the last generations in  tabular form.
 * @author  Dirk, Stefan
 */
public class NumberTable extends HistoryView implements RunEventListener {
	private int maxNumberOfColumns;

	private FreakTableModel tableModel;

	private String measure;

	/**
	 * Constructs a new <code>NumberTable</code>.
	 * 
	 * @param schedule a link back to the current schedule.
	 */
	public NumberTable(Schedule schedule) {
		super(schedule);

		setPropertyNumberOfDisplayedValues(new Integer(0));
	}

	public String getName() {
		return "Number Table";
	}

	public String getDescription() {
		return "Displays the numbers in a table.";
	}

	public Class[] getInputDataTypes() {
		return new Class[] { Number.class, byte[].class, short[].class, int[].class, long[].class, float[].class, double[].class };
	}

	public JPanel createPanel() {
		//create the main table and connect to data
		JTable table = new JTable() {
			public TableCellRenderer getCellRenderer(int row, int column) {
					//render numbers right alinged
	Object data = getModel().getValueAt(row, column);
				return getDefaultRenderer(data.getClass());
			}
		};
		tableModel.setView(table);

		//create containers	
		JPanel panel = new JPanel();
		JScrollPane scrollPane = new JScrollPane(table);
		panel.setLayout(new BorderLayout());
		panel.add(scrollPane, BorderLayout.CENTER);

		//scrollbar should stick at bottom
		JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
		scrollBar.addAdjustmentListener(new FreakScrollBarAdjustmentListener(scrollBar, true));

		//finally return panel
		return panel;
	}

	public void update(Object o) {
		super.update(o);

		try {
			synchronized (tableModel) {
				double[] numbers = null;

				// extract doubles from the received object
				if (o instanceof Number) {
					numbers = new double[] {((Number)o).doubleValue()};
				} else {
					// extract doubles from an array of primitive data types
					if (o.getClass().isArray()) {
						int length = Array.getLength(o);
						numbers = new double[length];
						for (int i = 0; i < length; i++) {
							numbers[i] = Array.getDouble(o, i);
						}
					}
				}

				// the rows of the table model are saved in the history
				Object[] row = createRow(numbers, getCurrentValue());

				updateTableModel(row);
			} // release lock

			UpdateManager.markDirty(tableModel);
		} catch (ObserverUnknownException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates a new row for the <code>TableModel</code>. The first column
	 * shows the specified number of the row, the following rows show the 
	 * specified numbers.
	 * 
	 * @param numbers the data for all but the first column.
	 * @param rowNumber the number of the row.
	 * @return an object array containing data for a row of the <code>TableModel</code>.
	 */
	protected Object[] createRow(double[] numbers, int rowNumber) {
		if (numbers.length + 1 > maxNumberOfColumns) {
			maxNumberOfColumns = numbers.length + 1;
		}

		Object[] result = new Object[numbers.length + 1];
		result[0] = new Integer(rowNumber);
		for (int i = 0; i < numbers.length; i++) {
			result[i + 1] = new Double(numbers[i]);
		}
		return result;
	}

	/**
	 * Updates the table model and adds a new row to it.
	 * If necessary, the first rows are removed to match the number of 
	 * displayed generations.
	 * 
	 * @param row a new row that is to be added.
	 */
	protected void updateTableModel(Object[] row) {
		// create new columns if necessary
		if (tableModel.getColumnCount() < maxNumberOfColumns) {
			String[] columnNames = new String[maxNumberOfColumns];
			columnNames[0] = measure;
			for (int i = 1; i < columnNames.length; i++) {
				columnNames[i] = String.valueOf(i);
			}
			tableModel.setColumnNames(columnNames);
		}

		// remove rows if necessary
		if (getPropertyNumberOfDisplayedValues().intValue() > 0) {
			while (tableModel.getRowCount() >= getPropertyNumberOfDisplayedValues().intValue()) {
				tableModel.removeFirstRow();
			}
		}

		// add new row
		tableModel.addRow(row);
	}

	public void runStarted(RunEvent evt) {
		super.runStarted(evt);
		
		if (getObserver().getMeasure() == Observer.GENERATIONS) {
			synchronized (tableModel) {
				tableModel.clear();

				maxNumberOfColumns = 1;
				String[] columnNames = new String[1];
				columnNames[0] = measure;
				tableModel.setColumnNames(columnNames);
			}

			UpdateManager.markDirty(tableModel);
		}
	}
	
	public void createEvents() {
		schedule.getEventController().addEvent(this, RunEvent.class, schedule);
	}

	public void setObserver(Observer observer) {
		super.setObserver(observer);

		if (getObserver() != null) {
			switch(getObserver().getMeasure()) {
				case Observer.GENERATIONS : measure = "Generation"; break;
				case Observer.RUNS : measure = "Run"; break;
				case Observer.BATCHES : measure = "Batch"; break;
			}
		}
		tableModel = new FreakLargeTableModel(new String[] { measure });
	}

	public void batchStarted(BatchEvent evt) {
		super.batchStarted(evt);
		
		if (getObserver().getMeasure() == Observer.RUNS) {
			synchronized (tableModel) {
				tableModel.clear();

				maxNumberOfColumns = 1;
				String[] columnNames = new String[1];
				columnNames[0] = measure;
				tableModel.setColumnNames(columnNames);
			}

			UpdateManager.markDirty(tableModel);
		}
	}

}
