/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.control;

import java.io.*;
import java.util.*;

import bsh.EvalError;
import bsh.Interpreter;

import freak.Freak;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.Configuration;
import freak.core.modulesupport.PropertyManager;

/**
 * A BatchList contains an ordered List of Batches. Each Schedule has exactly one
 * BatchList. The BatchLists' essential function is to enable the Schedule to
 * get the active Batch for a run by using the run numbers stored in each
 * Batch.
 * 
 * @author Stefan, Matthias
 */
public class BatchList implements Serializable {
	
	private List batches = new ArrayList();
	
	/**
	 * A Map where the key corresponds to a module and the value to a string.
	 * The string is some kind of code which will be interpreted to generate
	 * the configuration for each batch.
	 * 
	 * The configuration for a module which may be stored in the batch is
	 * overridden if there is code in the configurationCodes map for the module. 
	 */
	private Map configurationCodes = new HashMap();
	
	protected BatchList() {
	}

	/**
	 * @see java.util.List#add
	 */
	public void add(Batch batch, boolean applyConfigurationCode) {
		batches.add(batch);
		if (applyConfigurationCode) {
			applyConfigurationCodes(batch);
		}
	}

	/**
	 * @see java.util.List#set
	 */
	public void set(int index, Batch batch) {
		batches.set(index, batch);
	}

	/**
	 * @see java.util.List#remove
	 */
	public void removeBatch(int index) {
		batches.remove(index);
	}

	/**
	 * @see java.util.List#get
	 */
	public Batch get(int index) {
		Batch batch = (Batch)batches.get(index);
		return batch;
	}
	
	// TODO matthias JavaDocs
	private void applyConfigurationCodes(Batch batch) {
		for (Iterator iter = configurationCodes.keySet().iterator(); iter.hasNext();) {
			applyConfigurationCode(batch, (Configurable)iter.next());
		}
	}
	
	//	TODO matthias JavaDocs
	private void applyConfigurationCode(Batch batch, Configurable module) {
		// backup old configuration
		Configuration old_config = module.getConfiguration();
		
		// set configuration within current batch if available
		Configuration config = null;
		try {
			config = batch.getConfiguration(module);

			if (config != null) {
				module.setConfiguration(config);
			}
		} catch (NoSuchElementException e) {
		}
		
		String code = (String)configurationCodes.get(module);
		Interpreter i = new Interpreter();
		try {
			i.set("batch", batches.indexOf(batch));
			 
			String[] pNames = PropertyManager.getListOfPropertyNames(module);
            for (int j = 0; j < pNames.length; j++) {
            	try {
					i.set(pNames[j], PropertyManager.getProperty(module, pNames[j]));
				} catch (UnsupportedOperationException e) {
				}
			}

			i.eval(code);
			
			for (int j = 0; j < pNames.length; j++) {
				if (i.get(pNames[j]) != null) {
					PropertyManager.setProperty(module, pNames[j], i.get(pNames[j]));
				}
			}
			
	        batch.putConfiguration(module, module.getConfiguration());
		} catch (EvalError e) {
			Freak.debug(e.toString(), 3);

			//configurationCodes.remove(module);
		} catch (RuntimeException e) {
			if (e.getCause() instanceof IllegalArgumentException) {
				// probably the wrong property type was used; do nothing
				// TODO Dirk, Matthias: Warning for the user?
			} else throw e;
		}
		module.setConfiguration(old_config);
	}

	/**
	 * @see java.util.List#clear
	 */
	public void clear() {
		batches.clear();
	}

	/**
	 * @see java.util.List#iterator
	 */
	public Iterator iterator() {
		return Collections.unmodifiableList(batches).iterator();
	}

	/**
	 * @see java.util.List#size
	 */
	public int size() {
		return batches.size();
	}
	
	// TODO matthias JavaDocs
	public void setConfigurationCode(Configurable module, String code) {
		if (code.trim().equals("")) {
			configurationCodes.remove(module);
		} else {
			configurationCodes.put(module, code);
		}
		for (Iterator iter = batches.iterator(); iter.hasNext();) {
			Batch batch = (Batch) iter.next();
			applyConfigurationCode(batch, module);
		}
	}
	
	public String getConfigurationCode(Configurable module) {
		if (configurationCodes.get(module) != null) {
			return (String)configurationCodes.get(module);
		}
		
		
		String code = "// variable batch contains the current batch number (starting from 0)\n\n";
		
		String[] pNames = PropertyManager.getListOfPropertyNames(module);

		int pCount = pNames.length;

		for (int i = 0; i < pCount; i++) {
			try {
				Class c = PropertyManager.getProperty(module, pNames[i]).getClass();
				int arrayCount = 0;
				while (c.isArray()) {
					c = c.getComponentType();
					arrayCount++;
				}
				code += c.getName();
				while (0 < arrayCount--) {
					code += "[]";
				}
				code +=" "+pNames[i]+";\n";
			} catch (UnsupportedOperationException e) {
			}
		}
		
		code += "\n\n";
		
		for (int i = 0; i < pCount; i++) {
			try {
				//code += "// "+PropertyManager.getProperty(module, pNames[i]).getClass().getName()+"\n";
				
				String value = "";
				Object property = PropertyManager.getProperty(module, pNames[i]);
				if (property instanceof Number || property instanceof Boolean) {
					value = property.toString();
				} else if (property instanceof String) {
					value = "\""+property+"\"";
				}
				value = value.trim();
				if (value.equals("")) {
/*					code += "//"+pNames[i]+" = "+PropertyManager.getProperty(module, pNames[i]).getClass().getName()+" x;" +
					"// "+PropertyManager.getProperty(module, pNames[i])+"\n\n";*/
					code += "//"+pNames[i]+" = "+PropertyManager.getProperty(module, pNames[i])+"\n";
				} else {
					value.replaceAll("\n"," ");
					code += "// "+pNames[i]+" = "+value+";\n";
				}
			} catch (UnsupportedOperationException e) {
			}
		}
				
		return code;
	}

	/**
	 * Returns the sum of the number of runs in all contained Batches.
	 */
	public int getTotalRuns() {
		int result = 0;
		for (Iterator i = batches.iterator(); i.hasNext();) {
			Batch batch = (Batch)i.next();
			result += batch.getRuns();
		}
		return result;
	}
}
