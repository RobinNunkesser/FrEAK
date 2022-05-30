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

/**
 * ScheduleStatusData is sent by RunControl to its StateListener to inform about the current state of the Schedule. All information may be a bit dated, because the Schedule runs concurrently.
 * @author  Stefan
 */
public class ScheduleStatusData {
	public GenerationIndex lastPointInCurrentBatch;

	/**
	 * The currentTimeIndex of the running Schedule
	 */
	public GenerationIndex currentTimeIndex;
	/**
	 * The last TimeIndex available as seek target in the current replay.
	 */
	public GenerationIndex lastReplayPoint;
	/**
	 * The last TimeIndex in the current run available as seek target in the
	 * current replay.
	 */
	public GenerationIndex lastReplayPointInCurrentRun;
	/**
	 * The time in seconds used on simulating the current Schedule so far. 
	 */
	public double accumulatedRunTime;
}
