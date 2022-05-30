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

/** 
 * Uniquely indentifies a run in a schedule.
 * 
 * @author Stefan
 */
public class RunIndex implements Comparable, Serializable {
	public final int batch;
	public final int run;

	public RunIndex(int batch, int run) {
		this.batch = batch;
		this.run = run;
	}

	public RunIndex nextRun() {
		if (batch == 0)
			return new RunIndex(1, 1);
		if (run == Integer.MAX_VALUE)
			return new RunIndex(batch + 1, 1);
		return new RunIndex(batch, run + 1);
	}

	public RunIndex nextBatchStart() {
		if (batch == 0)
			return new RunIndex(1, 1);
		return new RunIndex(batch + 1, 1);
	}

	public int compareTo(Object other) {
		RunIndex t = (RunIndex)other;

		if (batch != t.batch) {
			return batch - t.batch;
		}
		return run - t.run;
	}

	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (!(other instanceof RunIndex))
			return false;

		return compareTo(other) == 0;
	}

	public int hashCode() {
		return batch + run;
	}

	public String toString() {
		return "Batch: " + batch + ", Run: " + run;
	}
}