/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.core.modulesupport.inspector;

import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.InvalidPropertyException;
import freak.core.modulesupport.Module;
import java.awt.LayoutManager;
import javax.swing.JPanel;

/**
 * A class for inspectors that are used to edit properties of modules.
 * @author  Dirk
 */
public class Inspector extends JPanel {

	private Module module; 

	public Inspector(LayoutManager layout, boolean isDoubleBuffered, Module module) {
		super(layout, isDoubleBuffered);
		this.module = module;
	}

	public Inspector(LayoutManager layout, Module module) {
		super(layout);
		this.module = module;
	}

	public Inspector(boolean isDoubleBuffered, Module module) {
		super(isDoubleBuffered);
		this.module = module;
	}

	public Inspector(Module module) {
		super();
		this.module = module;
	}
	
	/**
	 * This method is called by the GUI at the time the Inspector is being 
	 * closed.
	 *  
	 * @throws InspectorVetoException if the inspector vetoes being closed by the GUI. 
	 */
	public void finishEditing() throws InspectorVetoException {
		try {
			if (module instanceof Configurable) ((Configurable)module).testProperties();
		} catch (InvalidPropertyException e) {
			throw new InspectorVetoException(e.getMessage());
		}
	}

}
