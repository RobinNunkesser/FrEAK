/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.core.modulesupport;

import java.io.*;
import java.util.*;
import org.jdom.*;

/**
 * Objects of this class store configuration data. <br> The class provides functionality for extracting and setting properties of objects.<br> Additionally, there is a method for converting a Configuration into a String for displaying purposes.<br> Additionally, you can store and retrieve additional information in a Configuration.
 * @author  Kai, Michael
 */
public class Configuration implements Serializable {

	/**
	 * Objects of this class contain the value of a property
	 * together with its ShortDescription.
	 */
	protected static class PropertyWrapper implements Serializable {
		public Object value;
		public String shortDescription;
	}

	/**
	 * A HashMap with (poperty name, value) entries.
	 */
	protected HashMap simpleConfigs;

	/**
	 * Additional Information
	 */
	protected Object additional;

	/**
	 * String representation if the additional information
	 */
	protected String descriptionOfAdditional;

	/**
	 * Standard constructor which initializes the HashMap.
	 */
	public Configuration() {
		simpleConfigs = new HashMap();
		descriptionOfAdditional = "";

	}

	/**
	 * Sets the additional information in the Configuration.
	 * @param o the information.
	 */
	public void setAdditional(Object o, String descr) {
		additional = o;
		descriptionOfAdditional = descr;

	}

	/**
	 * Returns the additional information stored in the Configuration. 
	 * @return  the information stored.
	 * @uml.property  name="additional"
	 */

	public Object getAdditional() {
		return (additional);
	}

	/**
	 * Extracts all properties with their values from an object and
	 * creates a Configuration object containing these values.
	 *
	 * @param obj the object of which to extract the configuration
	 * @param putTo the configuration object to write the extracted configuration to
	 * @return putTo
	 */
	public static Configuration getConfigurationFor(Object obj, Configuration putTo) {

		// extract names of all properties in obj
		String[] propNames = PropertyManager.getListOfPropertyNames(obj);

		// put all (property name, value) pairs in simpleConfigs map
		for (int i = 0; i < propNames.length; i++) {
			PropertyWrapper wrp = new PropertyWrapper();
			try {
				wrp.value = PropertyManager.getProperty(obj, propNames[i]);
				wrp.shortDescription = PropertyManager.getShortDescriptionFor(obj, propNames[i]);

				putTo.simpleConfigs.put(propNames[i], wrp);
			} catch (UnsupportedOperationException e) {
				String[] pNamesNew = new String[propNames.length - 1];
				if (i > 0)
					System.arraycopy(propNames, 0, pNamesNew, 0, i);
				System.arraycopy(propNames, i + 1, pNamesNew, i, propNames.length - i - 1);
				propNames = pNamesNew;
				i--;
			}
		}
		return putTo;
	}

	/**
	 * Extracts all properties with their values from an object and
	 * creates a Configuration object containing these values.
	 *
	 * @param obj the object of which to extract the configuration
	 * @return the configuration
	 */
	public static Configuration getConfigurationFor(Object obj) {
		return getConfigurationFor(obj, new Configuration());
	}

	/**
	 * Sets the properties of an object to the values stored in the given
	 * Configuration object.
	 * 
	 * @param obj the object of which the properties should be set.
	 * @param conf the configuration object of which to extract the properties.
	 */
	public static void setConfigurationFor(Object obj, Configuration conf) {

		// -- can't set no configuration
		if (conf == null)
			return;

		// construct String array of property names in the simpleConfigs map

		Object[] tmp = conf.simpleConfigs.keySet().toArray();

		int i = 0;
		while ((i < tmp.length) && (tmp[i] != null))
			i++;

		int size = i;

		String[] names = new String[size];

		for (i = 0; i < size; i++)
			names[i] = (String) (tmp[i]);

		// set the properties in the object
		for (i = 0; i < size; i++) {
			PropertyManager.setProperty(
				obj,
				names[i],
				((PropertyWrapper) conf.simpleConfigs.get(names[i])).value);

		}

	}

	/**
	 * Returns a String describing the properties in thee configuration.
	 * 
	 * @return the configuration
	 */
	public String getDescription() {

		// get list of all properties in the map
		Object[] tmp = simpleConfigs.keySet().toArray();

		// determine number of non-null Elements in Array
		int i = 0;
		while ((i < tmp.length) && (tmp[i] != null))
			i++;

		int size = i;

		String[] pNames = new String[size];

		for (i = 0; i < size; i++)
			pNames[i] = (String) (tmp[i]);

		// concatenate descriptions of properties
		StringBuffer out = new StringBuffer();
		Object val;
		String name;

		for (i = 0; i < pNames.length; i++) {
			val = ((PropertyWrapper) simpleConfigs.get(pNames[i])).value;
			name =
				(
					(PropertyWrapper) simpleConfigs.get(
						pNames[i])).shortDescription;

			out.append(name + "=" + (val == null ? "null" : val.toString()));
			if (i != pNames.length - 1)
				out.append(",");
		}

		// append Description of additional data
		out.append(descriptionOfAdditional);

		return (out.toString());

	}

	/**
	 * @author  nunkesser
	 */
	public static class ConfigurationPersistenceHandler
		implements ClassPersistenceHandler {
		private PersistenceManager pm = null;

		public ConfigurationPersistenceHandler(PersistenceManager pm) {
			this.pm = pm;
		}

		public Element toXML(Object o) throws XMLizeException {
			Configuration config = (Configuration) o;
			Element result = new Element("Configuration");
			result.addContent(pm.toXML(config.simpleConfigs));
			result.addContent(pm.toXML(config.additional));
			result.setAttribute(
				"descriptionOfAdditional",
				config.descriptionOfAdditional);
			return result;
		}

		public Object fromXML(Element e) throws XMLizeException {
			Configuration result = new Configuration();
			result.simpleConfigs =
				(HashMap) pm.fromXML((Element) e.getChildren().get(0));
			result.additional = e.getChildren().get(1);
			result.descriptionOfAdditional =
				e.getAttributeValue("descriptionOfAdditional");
			return result;
		}

		public Class handles() {
			return Configuration.class;
		}
	}

	/**
	 * @author  nunkesser
	 */
	public static class PropertyWrapperPersistenceHandler
		implements ClassPersistenceHandler {
		private PersistenceManager pm = null;

		public PropertyWrapperPersistenceHandler(PersistenceManager pm) {
			this.pm = pm;
		}

		public Element toXML(Object o) throws XMLizeException {
			PropertyWrapper pw = (PropertyWrapper) o;
			Element result = new Element("Property");
			result.addContent(pm.toXML(pw.value));
			result.setAttribute("description", pw.shortDescription);
			return result;
		}

		public Object fromXML(Element e) throws XMLizeException {
			PropertyWrapper result = new PropertyWrapper();
			result.value = pm.fromXML((Element) e.getChildren().get(0));
			result.shortDescription = e.getAttribute("description").getValue();
			return result;
		}

		public Class handles() {
			return PropertyWrapper.class;
		}
	}

}
