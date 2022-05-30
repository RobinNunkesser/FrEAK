/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.view.swingsupport;

import java.io.*;

/**
 * The superinteface of all freak components to act as a model for swing
 * components. As swing components and their natural models cannot be updated
 * from the freak simulation thread, FreakSwingModels provide a replacement for
 * swing models that can be updated any time. They will store all necessary
 * data, and hide changes from swing, until an effective update can take place.
 * This update will be triggered in regular intervalls by the UpdateManager.
 * UpdateManager uses the flush method of FreakSwingModels to do this.
 * 
 * FreakSwingModels are also used to store the data of a view in a freak file or
 * in checkpoints for replaying, because swing components cannot be serialized
 * independently from the whole component tree.
 * 
 * All FreakSwingModels will have a setView method, that will assign them as a
 * model to an appropriate swing component. This method must be called from the
 * event dispatching thread.
 * 
 * FreakSwingModels must be synchronized externally, if their data is changed.
 * After a data change, adding the FreakSwingModel to the UpdateManagers dirty
 * list will cause a deferred update of the associated swing component.
 * 
 * 
 * The tutorial on views provides examples and a more detailed explanation.
 * 
 * @author Stefan
 */
public interface FreakSwingModel extends Serializable {
	/**
	 * Updates the associated swing component with the stored data of this
	 * FreakSwingModel. This method must be called from the event dispatching
	 * thread.
	 */
	public void flush();
}
