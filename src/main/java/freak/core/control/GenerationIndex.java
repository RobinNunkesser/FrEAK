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

import java.io.*;

/**
 * TimeIndex represents a measure of progress during the execution of a Schedule.
 * @author  Stefan
 */
public class GenerationIndex implements Comparable, Serializable {
	public static final GenerationIndex START = new GenerationIndex(0, 0, 0);
	public static final GenerationIndex FIRST = new GenerationIndex(1, 1, 1);
	public static final GenerationIndex	LAST = new GenerationIndex(Integer.MAX_VALUE - 1, Integer.MAX_VALUE, Integer.MAX_VALUE);
	public static final GenerationIndex END = new GenerationIndex(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

	public final int batch;
	public final int run;
	public final int generation;

	public GenerationIndex(int batch, int run, int generation) {
		this.batch = batch;
		this.run = run;
		this.generation = generation;
	}

	public RunIndex toRunIndex() {
		return new RunIndex(batch, run);
	}



	public GenerationIndex nextGeneration() {
		if (batch == 0)
			return new GenerationIndex(1, 1, 1);
		if (run == Integer.MAX_VALUE)
			return new GenerationIndex(batch + 1, 1, 1);
		if (generation == Integer.MAX_VALUE)
			return new GenerationIndex(batch, run + 1, 1);
		return new GenerationIndex(batch, run, generation + 1);
	}

	public GenerationIndex nextRunStart() {
		if (batch == 0)
			return new GenerationIndex(1, 1, 1);
		if (run == Integer.MAX_VALUE)
			return new GenerationIndex(batch + 1, 1, 1);
		return new GenerationIndex(batch, run + 1, 1);
	}

	public GenerationIndex nextBatchStart() {
		if (batch == 0)
			return new GenerationIndex(1, 1, 1);
		return new GenerationIndex(batch + 1, 1, 1);
	}
	
	public GenerationIndex runStart() {
		if (batch == 0) return START;
		return new GenerationIndex(batch, run, 1);
	}
	
	public GenerationIndex batchStart() {
		if (batch == 0) return START;
		return new GenerationIndex(batch, 1, 1);
	}
	
	

	public boolean isBefore(Object other) {
		return compareTo(other) < 0;
	}
	
	public boolean isAfter(Object other) {
		return compareTo(other) > 0;
	}

	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (!(other instanceof GenerationIndex))
			return false;

		return compareTo(other) == 0;
	}

	public int compareTo(Object other) throws ClassCastException, NullPointerException {
		GenerationIndex t = (GenerationIndex)other;

		if (batch != t.batch) {
			return batch - t.batch;
		}
		if (run != t.run) {
			return run - t.run;
		}
		return generation - t.generation;
	}

	public int hashCode() {
		return batch + run + generation;
	}

	public String toString() {
		return "Batch: " + batch + ", Run: " + run + ", Generation: " + generation;
	}
}