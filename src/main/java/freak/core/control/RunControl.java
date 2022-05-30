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

import java.util.*;

import freak.Freak;

/**
 * @author  Stefan
 */
public class RunControl {
	private StateListener stateListener;
	private LinkedList requestQueue = new LinkedList();
	
	private Schedule activeSchedule;
	private Replay replay;
	
	private volatile FreakSimulationThread simulationThread;
	private Actions.Action lastProcessed;
	private boolean programTerminationRequest;
	
	private GenerationIndex runTarget;
	private GenerationIndex seekTarget;
	private boolean inSeekSequence;
	private double speedLimit = Double.POSITIVE_INFINITY;
	
	
	public RunControl(StateListener gui) {
		simulationThread= new FreakSimulationThread();
		this.stateListener = gui;
		simulationThread.start();
	}

	private class FreakSimulationThread extends Thread {
		public FreakSimulationThread() {
			super("FreakSimulationThread");
			setPriority(MIN_PRIORITY);
		}
		
		public void run() {
			mainLoop();
			stateListener.terminated(lastProcessed);
		}
	}
	
// PROCESSING LOOP *************************************************************

	private void mainLoop() {
		waitForRequests();
		Thread thisThread = Thread.currentThread();
		while(simulationThread == thisThread) {
			processRequests();
			if (programTerminationRequest) {
				simulationThread=null;
				return;
			}
			
			updateTargetStatus();
			if (allTargetsReached()) {
				if (!inSeekSequence) sendSimulationCompleted();
				waitForRequests();
				continue;
			}
			try {
				waitForSpeedLimit();
			} catch (RequestRecievedException exc) {
				continue;
			}
			try {
				step();
			} catch (Exception exc) {
				sendErrorMessage(exc);
				fallback(exc);
			}
		}
	}
	
	private void processRequests() {
		Actions.Action request = readRequest();
		while (request != null) {
			request.process(this);
			if (programTerminationRequest) {
				break;
			} else {
				lastProcessed = request;
				request = readRequest();
			}
		}
	}
	
	private Actions.Action readRequest() {
		synchronized (requestQueue) {
			if (!requestQueue.isEmpty()) {
				return (Actions.Action) requestQueue.removeFirst();
			} else {
				return null;
			}
		}
	}
	
	private void updateTargetStatus() {
		// clear seek status, if seeking complete
		if (seekTarget != null) {
			if (activeSchedule.getNextStepTimeIndex().isAfter(seekTarget)) {
				seekTarget = null;	
			}
		}
		
		// clear run status, if running complete
		if (runTarget != null) {
			if (activeSchedule.getNextStepTimeIndex().isAfter(runTarget)) {
				runTarget = null;
			}
		}
	}
	
	private boolean allTargetsReached() {
		return (seekTarget == null && (runTarget == null || inSeekSequence));
	}
	
	private void sendSimulationCompleted() {
		stateListener.simulationCompleted(lastProcessed);
	}
	
	private void waitForRequests() {
		if (activeSchedule != null) activeSchedule.timeController.stopCounting();
		
		synchronized (requestQueue) {
			while (requestQueue.isEmpty()) {
				try {
					requestQueue.wait();
				} catch (InterruptedException exc) {
					throw new RuntimeException(exc);
				}
			}
		}
		
		if (activeSchedule != null) activeSchedule.timeController.startCounting();
	}

	private void waitForSpeedLimit() throws RequestRecievedException {
		if (seekTarget != null) return;
		
		synchronized (requestQueue) {
			if (!requestQueue.isEmpty()) throw new RequestRecievedException();
			
			activeSchedule.timeController.setSpeedLimit(speedLimit);
			activeSchedule.timeController.nextGeneration(requestQueue);
			
			if (!requestQueue.isEmpty()) throw new RequestRecievedException();
		}
	}

	private void step() throws SimulationException {
		if (activeSchedule.getCurrentTimeIndex().isBefore(replay.getLastPoint())) {
			// replaying
			if (replay.isEditPoint(activeSchedule.getNextStepTimeIndex())) {
				activeSchedule = replay.getCheckPointFor(activeSchedule.getNextStepTimeIndex());
				activeSchedule.timeController.startCounting();
				try {
					waitForSpeedLimit();
				} catch (RequestRecievedException exc) {
				}
			} else {
				activeSchedule.step();
			}
		} else {
			// running
			activeSchedule.step();
			replay.addPoint(activeSchedule, false);
		}
		sendAsynchroneousFeedback();
	}

	void terminate() {		
		programTerminationRequest = true;
	}
	
	private void sendAsynchroneousFeedback() {
		stateListener.asynchroneousFeedback(activeSchedule, replay);
	}
	
	private void sendErrorMessage(Exception exc) {
		stateListener.simulationException(exc);
	}
	
	private void fallback(Exception reason) {
		runTarget = null;
		seekTarget = null;
		replay = null;
		activeSchedule = null;
		sendAsynchroneousFeedback();
	}
	
// Asynchroneous requests ******************************************************

	public void request(Actions.Action request) {
		synchronized (requestQueue) {
			requestQueue.add(request);
			requestQueue.notify();
		}
	}

	void start() {
		runTarget = GenerationIndex.LAST;
		seekTarget = null;
	}
	
	void suspend() {
		runTarget = null;
		seekTarget = null;
	}
	
	void setSpeedLimitAsynchroneous(double generationsPerSecond) {
		speedLimit = generationsPerSecond;
	}
	
	void startSeekSequence() {
		inSeekSequence = true;
	}
		
	void endSeekSequence() {
		inSeekSequence = false;
	}

	void seek(GenerationIndex seekTarget) {	
		this.seekTarget = seekTarget;
		loadBestSchedule(seekTarget);
	}

	public void seekToStart() {		
		seek(GenerationIndex.START);
	}

	public void seekToLastBatch() {
		GenerationIndex from = (seekTarget != null) ? seekTarget : activeSchedule.getCurrentTimeIndex();
		
		if (from.batch <= 1) {
			seek(GenerationIndex.START);
		} else {
			seek(new GenerationIndex(from.batch - 1, from.run, from.generation));
		}
	}
	
	public void seekToNextBatch() {
		GenerationIndex from = (seekTarget != null) ? seekTarget : activeSchedule.getCurrentTimeIndex();
		
		if (from.batch == 0) {
			seek(GenerationIndex.FIRST);
		} else {
			if (from.batch < activeSchedule.getBatchList().size()) {
				seek(new GenerationIndex(from.batch + 1, from.run, from.generation));
			}
		}
	}
	
	public void seekToLastRun() {
		GenerationIndex from = (seekTarget != null) ? seekTarget : activeSchedule.getCurrentTimeIndex();
		
		if (from.run > 1) {
			seek(new GenerationIndex(from.batch, from.run - 1, from.generation));
		}
	}
	
	public void seekToNextRun() {
		GenerationIndex from = (seekTarget != null) ? seekTarget : activeSchedule.getCurrentTimeIndex();
		
		if (from.batch != 0) {
			if (from.run < activeSchedule.getBatchList().get(from.batch - 1).getRuns()) {
				seek(new GenerationIndex(from.batch, from.run + 1, from.generation));
			}
		}
	}
	
	public void seekToLastGeneration() {
		GenerationIndex from = (seekTarget != null) ? seekTarget : activeSchedule.getCurrentTimeIndex();
		
		if (from.generation > 1) {
			seek(new GenerationIndex(from.batch, from.run, from.generation - 1));
		}
	}
	
	public void seekToNextGeneration() {
		GenerationIndex from = (seekTarget != null) ? seekTarget : activeSchedule.getCurrentTimeIndex();
		
		if (from.batch != 0) {
			seek(new GenerationIndex(from.batch, from.run, from.generation + 1));
		}
	}
	
	public void stepBack() {
		GenerationIndex now = activeSchedule.getCurrentTimeIndex();
		if (now.equals(GenerationIndex.START)) return;
		
		if (now.generation != 1) {
			seek(new GenerationIndex(now.batch, now.run, 1));
		} else {
			if (now.run > 1) {
				seek(new GenerationIndex(now.batch, now.run - 1, 1));
			} else {
				seek(replay.getLastPointInBatch(now.batch - 1).runStart());
			}
		}
	}
	
	void stepForward() {
		GenerationIndex now = activeSchedule.getCurrentTimeIndex();
		GenerationIndex target;
		
		if (activeSchedule.isLastRunInBatch()) {
			if (activeSchedule.isLastBatch()) {
				seek(now.nextBatchStart());
				return;
			} else {
				target = now.nextBatchStart(); 		
			}
		} else {
			target = now.nextRunStart();
		}
		
		if (replay.containsPoint(target)) {
			seek(target);
		} else {
			activeSchedule.skip();
			replay.removeAllSince(activeSchedule.getCurrentTimeIndex());
			replay.addPoint(activeSchedule, true);
			seekTarget = target;
		
			sendAsynchroneousFeedback();
		}
	}

	public void seekToReplayEnd() {		
		seek(replay.getLastPoint());
	}
	
	private void loadBestSchedule(GenerationIndex seekTarget) {
		Schedule stored = replay.getCheckPointFor(seekTarget);
	
		boolean currentAfterTarget = activeSchedule.getCurrentTimeIndex().isAfter(seekTarget);
		boolean currentBeforeStored = activeSchedule.getCurrentTimeIndex().isBefore(stored.getCurrentTimeIndex());
		if (currentBeforeStored || currentAfterTarget) {
			activeSchedule = stored;
			sendAsynchroneousFeedback();
		}
	}

// Synchroneous Requests *******************************************************

	public void setNewSchedule(Schedule newSchedule) {
		activeSchedule = newSchedule;
		replay = new Replay();
		replay.addPoint(activeSchedule, true);
		sendSynchroneousFeedback();
	}
	
	
	public void fromFile(FreakFile file) {
		replay = file.replay;
		activeSchedule = file.activeSchedule;
		sendSynchroneousFeedback();
	}
	
	public void scheduleEdited(Schedule editedSchedule) {
		activeSchedule = editedSchedule;
		replay.removeAllSince(activeSchedule.getCurrentTimeIndex());
		replay.addPoint(activeSchedule, true);
		sendSynchroneousFeedback();
	}

	/**
	 * @param speedLimit  the speedLimit to set
	 * @uml.property  name="speedLimit"
	 */
	public void setSpeedLimit(double speedLimit) {
		this.speedLimit = speedLimit;
		sendSynchroneousFeedback();
	}

	public Schedule getSchedule() {
		return activeSchedule;
	}
	
	public FreakFile toFile() {
		return new FreakFile(replay, activeSchedule);
	}
	
	private void sendSynchroneousFeedback() {
		stateListener.synchroneousFeedback(activeSchedule, replay);
	}
}
