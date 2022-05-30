/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.core.event;

/**
 * A BatchEvent is sent by the Schedule every time a batch is started.
 * @author  Dirk, Stefan
 */
public class BatchEvent extends Event {
	private BatchEventSource source;
	private int batchIndex;

	public BatchEvent(BatchEventSource source, int batchIndex) {
		this.source = source;
		this.batchIndex = batchIndex;
	}

	/**
	 * @return  the source
	 * @uml.property  name="source"
	 */
	public BatchEventSource getSource() {
		return source;
	}

	/**
	 * @return  the batchIndex
	 * @uml.property  name="batchIndex"
	 */
	public int getBatchIndex() {
		return batchIndex;
	}
}
