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
 * A handcrafted data structure for large tabes in freak. It provides two
 * intentionally inconsistent views on the same data structure to optimize
 * the footprint.
 * 
 * @author Stefan
 */
public class SlidingWindow implements Serializable {
	private Object[] store = new Object[0];

	private int simViewStart = 0;
	private int simViewEnd = -1;
	private int swingViewStart = 0;
	private int swingViewEnd = -1;

	//TODO Stefan strip on serialization

	public void add(Object data) {
		// if space exhausted, switch to new array
		if (simViewEnd + 1 == store.length) {
			newArray();
		}

		simViewEnd++;
		store[simViewEnd] = data;
	}

	public void removeFirst() {
		simViewStart++;
	}

	public void clear() {
		simViewStart = simViewEnd + 1;
	}

	public Object get(int i) {
		return store[simViewStart + i];
	}

	public int size() {
		return simViewEnd - simViewStart + 1;
	}

	public void synchronizeViews() {
		swingViewStart = simViewStart;
		swingViewEnd = simViewEnd;
	}

	public Object getInSwingView(int i) {
		return store[simViewStart + i];
	}

	public int sizeInSwingView() {
		return swingViewEnd - swingViewStart + 1;
	}

	private void newArray() {
		// TODO Stefan remove obsolete inner values

		// determine required size and obsolete values
		int obsoleteFromStart = swingViewStart;
		int requiredSize = simViewEnd - obsoleteFromStart + 1;

		// copy to new array
		Object[] newStore = new Object[requiredSize * 2 + 1];
		System.arraycopy(store, swingViewStart, newStore, 0, requiredSize);
		store = newStore;

		// adjust view indices
		swingViewStart -= obsoleteFromStart;
		swingViewEnd -= obsoleteFromStart;
		simViewStart -= obsoleteFromStart;
		simViewEnd -= obsoleteFromStart;
	}
}
