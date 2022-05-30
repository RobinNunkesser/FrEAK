/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.gui.scheduleeditor;

import freak.core.control.*;
import freak.core.modulesupport.*;
import java.util.*;
import javax.swing.table.*;

/**
 * @author  Oliver, Dirk
 */
public class BatchTableModel extends AbstractTableModel {

	final String[] columnNames = { "Runs", "Search Space", "Fitness", "Population", "Status" };
	private Schedule schedule;
	private BatchList batchList;

	public BatchTableModel(Schedule schedule) {
		super();
		this.schedule = schedule;
		batchList = schedule.getBatchList();
	}

	private String getBatchConfigStringOf(Module module, Batch batch) {
		String returnString;
		if (module == null) {
			returnString = "<no object>";
		} else if (module instanceof Configurable) {
			try {
				Configuration conf = batch.getConfiguration((Configurable)module);
				returnString = conf.getDescription();
			} catch (NoSuchElementException e) {
				returnString = "<no config>";
			}
		} else {
			returnString = "<not configurable>";
		}
		return returnString;
	}

	private String getSearchSpaceConfig(Batch batch) {
		return getBatchConfigStringOf(schedule.getPhenotypeSearchSpace(), batch);
	}

	private String getFitnessConfig(Batch batch) {
		return getBatchConfigStringOf(schedule.getRealFitnessFunction(), batch);
	}

	private String getInitializationConfig(Batch batch) {
		return getBatchConfigStringOf(schedule.getInitialization(), batch);
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return batchList.size();
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return columnNames.length;
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		Batch batch = batchList.get(rowIndex);

		switch (columnIndex) {
			case 0 :
				return new Integer(batch.getRuns());
			case 1 :
				return getSearchSpaceConfig(batch);
			case 2 :
				return getFitnessConfig(batch);
			case 3 :
				return getInitializationConfig(batch);
		}

		if (batch.isFinished()) return "finished";
		if (batch.isStarted()) return "running";
		return "coming up";
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnClass(int)
	 */
	public Class getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}
}
