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
 * SpeedLimit is able to slow down a Schedule to a specified number of
 * Generations per second in average, by using Thread.sleep. It can cope with
 * variable processing times between Generations and imprecise sleep times.
 * 
 * The class is in fact much more general, and could slow down any periodic
 * process.
 * 
 * @author Stefan
 */
class TimeController implements Serializable {
	private double accumulatedRunTime;
	private double accumulatedProcessingTime;

	private boolean running;
	private boolean inTimeOut;
	private long periodStartTime;
	private long timeOutStart;
	private long periodWaitTime;

	private double generationsPerSecond;
	private double aheadOfTime = 0;
	private long lastGenerationStart = 0;

	double getRuntime() {
		return accumulatedRunTime + getPeriodRunTime();
	}

	double getProcessingTime() {
		return accumulatedProcessingTime + getPeriodProcessingTime();
	}

	double getPeriodRunTime() {
		if (running) {
			return (System.currentTimeMillis() - periodStartTime) / 1000.0;
		} else {
			return 0;
		}
	}

	double getPeriodProcessingTime() {
		if (running) {
			return (System.currentTimeMillis() - periodStartTime - periodWaitTime) / 1000.0;
		} else {
			return 0;
		}
	}

	void startCounting() {
		periodStartTime = System.currentTimeMillis();
		periodWaitTime = 0;

		lastGenerationStart = 0;
		aheadOfTime = 0;
		running = true;
	}

	void stopCounting() {
		if (!running)
			return;
		if (inTimeOut)
			timeOutStop();

		long now = System.currentTimeMillis();
		double runTime = (now - periodStartTime) / 1000.0;
		double processingTime = (now - periodStartTime - periodWaitTime) / 1000.0;

		accumulatedRunTime += Math.max(0, runTime);
		accumulatedProcessingTime += Math.max(0, processingTime);

		running = false;
	}

	/**
	 * The time will not be counted as processing time, until timeOutStop is
	 * called. 
	 */
	void timeOutStart() {
		if (!running)
			return;

		timeOutStart = System.currentTimeMillis();
		inTimeOut = true;
	}

	void timeOutStop() {
		if (!running)
			return;

		long timeOut = System.currentTimeMillis() - timeOutStart;
		periodWaitTime += timeOut;
		inTimeOut = false;
	}

	void setSpeedLimit(double generationsPerSecond) {
		if (generationsPerSecond != this.generationsPerSecond) {
			aheadOfTime = 0;
			this.generationsPerSecond = generationsPerSecond;
		}
	}

	/**
	 * This method should be called on each generation. It throttles the
	 * Schedule to the desired speed by returning delayed. The delay is not
	 * applied on each invocation, but managed by a credit system.
	 */
	void nextGeneration(Object waitOn) {
		if (lastGenerationStart == 0) {
			//no delay before first generation
			lastGenerationStart = System.currentTimeMillis();
		} else {
			//find duration
			long entryTime = System.currentTimeMillis();
			double duration = (entryTime - lastGenerationStart) / 1000.0;
			lastGenerationStart = entryTime;

			//recalculate difference from desired time
			aheadOfTime += 1 / generationsPerSecond - duration;

			//take action
			if (aheadOfTime > 0.05) {
				//if more than 0.05 seconds ahead wait a little (this accounts to next generations time)
				long pause = (long) (aheadOfTime * 1000);
				try {
					waitOn.wait(pause);
				} catch (InterruptedException exc) {
					Thread.currentThread().interrupt();
				}
				long reallyWaited = System.currentTimeMillis() - entryTime;
				periodWaitTime += reallyWaited;
			} else if (aheadOfTime < -0.3) {
				//if more than 0.3 seconds delayed reduce credit
				aheadOfTime = -0.3;
			}
		}
	}
}
