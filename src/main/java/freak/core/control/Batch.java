/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.core.control;

import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.Configuration;
import freak.core.modulesupport.Module;
import freak.core.util.StreamCopy;
import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A Batch is a set of runs with common configuration of all modules. A Schedule contains a list of Batches that will be run subsequently.
 * @author  Stefan, Matthias, Dirk
 */
public class Batch implements Serializable {
	
	private Map configurationObjects = new HashMap();
	private int runs = 100;
	
	// a flag used to indicate that this batch has been started 
	private boolean started = false;

	// a flag used to indicate that this batch is finished 
	private boolean finished = false;

	/**
	 * Associates a Configuration with a Configurable for this Batch. The
	 * Configurable may get the Batch during simulation via the RunStartedEvent,
	 * and query it for its Configuration.
	 * 
	 * @see freak.core.event.RunEventListener#runStarted
	 */
	public void putConfiguration(Configurable configurableObject, Configuration configuration) {
		if (configuration == null) throw new NullPointerException();

		configurationObjects.put(configurableObject, configuration);
	}

	/**
	 * Returns the Configuration associated with the Configurable.
	 * 
	 * @exception NoSuchElementException if no Configuration is associated with the Configurable.
	 */
	public Configuration getConfiguration(Configurable configurableObject) {
		Configuration result = (Configuration)configurationObjects.get(configurableObject);
		if (result == null) throw new NoSuchElementException();
		return result;
	}

	/**
	 * Returns the number of runs for which this Batch should be active.
	 * @uml.property  name="runs"
	 */
	public int getRuns() {
		return runs;
	}

	/**
	 * Sets the number of runs for which this Batch should be active. The runs will have the same configuration, but may differ in random values.
	 * @param runs  must be > 0
	 * @uml.property  name="runs"
	 */
	public void setRuns(int runs) {
		if (runs < 1)
			throw new IllegalArgumentException();

		this.runs = runs;
	}
	
	
	public void applyAllConfigurations() {
		applyAllConfigurations(configurationObjects);
	}

	private void applyAllConfigurations(Map configurationObjects) {
		Set set = configurationObjects.keySet();
		Module[] modules = (Module[])set.toArray(new Module[set.size()]);
		
		// sort objects by their classes in Freak in the order of the 
		// Schedule Editor
		Arrays.sort(modules, new FreakClassComparator()); 
		
		// apply configurations
		for (int i = 0; i < modules.length; i++) {
			Configurable configurable = (Configurable)modules[i];
			Configuration configuration = (Configuration) configurationObjects.get(configurable);
			
			configurable.setConfiguration(configuration);
		}

	}
	
	/**
	 * @return  <code>true</code> if this batch is finished; <code>false</code> otherwise.
	 * @uml.property  name="finished"
	 */
	public boolean isFinished() {
		return finished;
	}

	/**
	 * Indicates whether this batch is finished or not.
	 * @param b  a boolean value.
	 * @uml.property  name="finished"
	 */
	public void setFinished(boolean b) {
		finished = b;
	}

	/**
	 * @return  <code>true</code> if this batch has been started; <code>false</code> otherwise.
	 * @uml.property  name="started"
	 */
	public boolean isStarted() {
		return started;
	}

	/**
	 * Indicates whether this batch has been started or not.
	 * @param b  a boolean value.
	 * @uml.property  name="started"
	 */
	public void setStarted(boolean b) {
		started = b;
	}
	
	/**
	 * Returns a copy of the current batch. The configurations for the 
	 * configurable Modules are cloned and the status flags are reset to false.
	 * 
	 * @return a copy of the current batch.
	 */
	public Batch copy() {
		Batch result = new Batch();
		
		result.runs = this.runs;
		result.started = false;
		result.finished = false;
		
		Map newMap = new HashMap();
		try {
			for (Iterator iter = this.configurationObjects.keySet().iterator(); iter.hasNext();) {
				Object key = iter.next();
				// create new configuration 
				Object newValue = StreamCopy.copy((Configuration)this.configurationObjects.get(key));
				// since the key is a module inside the schedule, we save
				// the cloned configuration by the same key. 
				newMap.put(key, newValue);
			}
		} catch (NotSerializableException e) {
			throw new RuntimeException(e);
		}
		result.configurationObjects = newMap;
		
		return result;
	}

}
