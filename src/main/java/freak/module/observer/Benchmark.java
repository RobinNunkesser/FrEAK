/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.observer;

import freak.core.control.*;
import freak.core.event.*;
import freak.core.observer.*;

/**
 * @author Michael
 */
public class Benchmark extends AbstractObserver implements GenerationEventListener {

	public Benchmark(Schedule schedule) {
		super(schedule);
		setMeasure(Observer.GENERATIONS);
	}

	public String getName() {
		return "Benchmark";
	}

	public String getDescription() {
		return "Computes the number of generations per second.";
	}

	public Class getOutputDataType() {
		return String.class;
	}

	public void createEvents() {
		schedule.getEventController().addEvent(this, GenerationEvent.class, schedule);
	}

	private long first = 0;
	private long gens = 0;

	public void generation(GenerationEvent evt) {
		long now = System.currentTimeMillis();
		if (first == 0) {
			first = now;
		} else {
			gens++;
			long t = now - first;
			if (t >= 1000) {
				double gps = 1000 * gens / (double)t;
				String s;
				if (gps > 10) {
					s = Math.round(gps) + " generations per second";
				} else {
					s = Math.rint(gps * 100) / 100 + " generations per second";
				}
				updateViews(s);
				first = now;
				gens = 0;
			}
		}
	}

}