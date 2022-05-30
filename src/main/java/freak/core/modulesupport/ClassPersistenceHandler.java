/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.modulesupport;

import org.jdom.*;

/**
 * @author Matthias
 */
public interface ClassPersistenceHandler {

	public Element toXML(Object o) throws Exception;

	public Object fromXML(Element e) throws Exception;

	public Class handles();

}
