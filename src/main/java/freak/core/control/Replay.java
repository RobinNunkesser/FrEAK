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

import freak.core.util.*;
import java.io.*;
import java.util.*;

/**
 * A Replay contains a set of Points, that were encountered during simulation  of a Schedule, sorted by TimeIndex. These points may additionally store a copy of the Schedula at this Point, making it a CheckPoint. CheckPoints may additionally be EditPoints, Points where the Schedule has changed. Although replay stores all points conceptually it does not actually store all points because there can be huge amounts of them. But as the Schedule never jumps over a generation, it is sufficient to store the last point of each run.
 * @author  Stefan
 */
public class Replay implements Serializable {
	private static double defaultCheckPointInterval = 1.2;
	private static long defaultMaxMemoryUsage = 2000000; //last is not included
	private static long defaultSingleScheduleMemoryThreshhold = 200000;

	private double checkPointInterval = defaultCheckPointInterval;
	private int runEndCheckPointInterval = 1;
	private long maxMemoryUsage = defaultMaxMemoryUsage;
	private long singleScheduleMemoryThreshold = defaultSingleScheduleMemoryThreshhold;
	private boolean alsoStripRunEndPoints = false;

	private int estimatedMemoryRequirement;
	private double lastCheckPointProcessingTime;

	private GenerationIndex lastPoint;
	private SortedMap lastPointInRuns = new TreeMap();
	private SortedMap lastPointInBatches = new TreeMap();
	
	private SortedMap checkPoints = new TreeMap();
	private SortedSet editPoints = new TreeSet();
	

	/**
	 * Returns true if the timeIndex is a point of this replay.
	 */
	public boolean containsPoint(GenerationIndex timeIndex) {
		GenerationIndex runEnd = getLastPointInRun(timeIndex.toRunIndex());
		if (runEnd == null)
			return false;
		return runEnd.generation >= timeIndex.generation;
	}

	/**
	 * Returns a copy of the Schedule on the CheckPoint that was recorded last
	 * before or exactly on the time index.
	 */
	Schedule getCheckPointFor(GenerationIndex timeIndex) {
		try {
			SortedMap head = upTo(checkPoints, timeIndex);
			Object key = head.lastKey();
			CheckPoint point = (CheckPoint)head.get(key);
			Schedule copy = (Schedule)StreamCopy.copy(point.schedule);
			return copy;
		} catch (NotSerializableException exc) {
			throw new RuntimeException(exc);
		}
	}

	boolean isEditPoint(GenerationIndex timeIndex) {
		return editPoints.contains(timeIndex);
	}

	public GenerationIndex getLastPointInBatch(int batch) {
		return (GenerationIndex) lastPointInBatches.get(new Integer(batch));
	}

	/**
	 * Returns the last Point that was recorded in this run during the
	 * Simulation.
	 * 
	 * @return may be null
	 */
	public GenerationIndex getLastPointInRun(RunIndex runIndex) {
		return (GenerationIndex)lastPointInRuns.get(runIndex);
	}

	/**
	 * Returns the end of this Replay.
	 * @return  may be null
	 * @uml.property  name="lastPoint"
	 */
	public GenerationIndex getLastPoint() {
		return lastPoint;
	}

	/**
	 * Adds a Point to this Replay. The Replay will decide, if the point should
	 * become a checkpoint.
	 * 
	 * @param activeSchedule the original Schedule as it is on the Point 
	 * @param isEditPoint marks a CheckPoint as EditPoint
	 */
	void addPoint(Schedule activeSchedule, boolean isEditPoint) {
		activeSchedule.timeController.timeOutStart();

		GenerationIndex timeIndex = activeSchedule.getCurrentTimeIndex();
		lastPoint = timeIndex;
		lastPointInRuns.put(timeIndex.toRunIndex(), timeIndex);
		lastPointInBatches.put(new Integer(timeIndex.batch), timeIndex);	
		double currentTime = activeSchedule.timeController.getProcessingTime();

		if (isEditPoint) {
			// always store a copy on edit points
			createCheckPoint(timeIndex, activeSchedule);
			editPoints.add(timeIndex);
		} /* else {
			if (activeSchedule.isCurrentRunFinished() && timeIndex.run % runEndCheckPointInterval == 0) {
				trimMemory();

				//check again
				if (estimatedMemoryRequirement <= maxMemoryUsage && timeIndex.run % runEndCheckPointInterval == 0) {
					createCheckPoint(timeIndex, activeSchedule);
				}
			} else if (currentTime - lastCheckPointProcessingTime >= checkPointInterval) {
				// copys for checkpoints only after some time
				trimMemory();

				// check again, some points may have been removed
				if (estimatedMemoryRequirement <= maxMemoryUsage && currentTime - lastCheckPointProcessingTime >= checkPointInterval) {
					createCheckPoint(timeIndex, activeSchedule);
				}
			}
		} */

		activeSchedule.timeController.timeOutStop();
	}

	/**
	 * Removes all points after the time index from this replay.
	 */
	void removeAllSince(GenerationIndex timeIndex) {
		RunIndex runIndex = timeIndex.toRunIndex();
		int batch = timeIndex.batch;
		
		checkPoints = new TreeMap(upTo(checkPoints, timeIndex));
		editPoints = new TreeSet(upTo(editPoints, timeIndex));
		
		lastPointInBatches = new TreeMap(upTo(lastPointInBatches, batch));
		lastPointInBatches.put(new Integer(batch), timeIndex);
		
		lastPointInRuns = new TreeMap(upTo(lastPointInRuns, runIndex));
		lastPointInRuns.put(runIndex, timeIndex);
		
		lastPoint = timeIndex;
		
		recreateProcessingInformation();
	}

	private void trimMemory() {
		// trim necessary?
		while (estimatedMemoryRequirement > maxMemoryUsage) {
			try {
				//remove every second checkpoint, excluding runEndPoints and editPoints
				removeEverySecondCheckpoint(alsoStripRunEndPoints);
				if (alsoStripRunEndPoints) {
					runEndCheckPointInterval *= 2;
				} else {
					checkPointInterval *= 2;
				}
				// update estimateMemoryRequirement and lastTime
				recreateProcessingInformation();
			} catch (NoSuchElementException exc) {
				// all checkpoints removed, but still not enough memory
				if (!alsoStripRunEndPoints) {
					// try to go on with all but edit points
					alsoStripRunEndPoints = true;
					checkPointInterval = Double.POSITIVE_INFINITY;
				} else {
					// all but edit points removed, nothing more can be removed
					runEndCheckPointInterval = Integer.MAX_VALUE;
					break;
				}
			}
		}
	}

	private void removeEverySecondCheckpoint(boolean includeRunEndPoints) {
		Iterator i = checkPoints.keySet().iterator();
		int n = 1;
		boolean anyRemoved = false;

		while (i.hasNext()) {
			Object timeIndex = i.next();

			if (n % 2 == 0) { //second point is the first to be removed
				boolean editPoint = editPoints.contains(timeIndex);
				boolean runEndPoint = ((CheckPoint)checkPoints.get(timeIndex)).schedule.isCurrentRunFinished();

				if (!editPoint && (!runEndPoint || includeRunEndPoints)) {
					i.remove();
					anyRemoved = true;
				} else {
					// if a point must be preserved, always remove the next
					n = 1;
				}
			}
			n++;
		}
		if (!anyRemoved)
			throw new NoSuchElementException();
	}

	private void createCheckPoint(GenerationIndex timeIndex, Schedule schedule) {
		try {
			byte[] streamedSchedule = StreamCopy.serialize(schedule);
			Schedule copy = (Schedule)StreamCopy.read(streamedSchedule);

			int streamLength = streamedSchedule.length;
			estimatedMemoryRequirement += streamLength;

			copy.timeController.stopCounting();
			CheckPoint point = new CheckPoint(copy, streamedSchedule.length);
			checkPoints.put(timeIndex, point);
			lastCheckPointProcessingTime = schedule.timeController.getProcessingTime();

			if (streamLength > singleScheduleMemoryThreshold) {
				checkPointInterval = Double.POSITIVE_INFINITY;
				runEndCheckPointInterval = Integer.MAX_VALUE;
			}
		} catch (NotSerializableException exc) {
			RuntimeException r = new RuntimeException("Some part of Schedule, probably a Module, is not Serializable.");
			r.initCause(exc);
			throw r;
		}
	}

	private void recreateProcessingInformation() {
		Iterator i = checkPoints.values().iterator();
		CheckPoint point = null;
		estimatedMemoryRequirement = 0;
		while (i.hasNext()) {
			point = (CheckPoint)i.next();
			estimatedMemoryRequirement += point.memoryRequirement;
		}
		lastCheckPointProcessingTime = (point == null) ? 0 : point.schedule.timeController.getProcessingTime();
	}

	private static SortedSet upTo(SortedSet set, GenerationIndex generationIndex) {
		return set.headSet(generationIndex.nextGeneration());
	}

	private static SortedMap upTo(SortedMap map, GenerationIndex generationIndex) {
		return map.headMap(generationIndex.nextGeneration());
	}

	private static SortedMap upTo(SortedMap map, RunIndex runIndex) {
		return map.headMap(runIndex.nextRun());
	}
	
	private static SortedMap upTo(SortedMap map, int batch) {
		return map.headMap(new Integer(batch + 1));
	}

	private static class CheckPoint implements Serializable {
		Schedule schedule;
		int memoryRequirement;

		public CheckPoint(Schedule schedule, int memoryRequirement) {
			this.schedule = schedule;
			this.memoryRequirement = memoryRequirement;
		}
	}
}
