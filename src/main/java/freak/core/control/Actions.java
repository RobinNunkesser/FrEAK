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
 * All asynchronous commands to run control.
 * 
 * @author Stefan Tannenbaum
 */
public abstract class Actions {
	public static abstract class Action {
		abstract void process(RunControl target);
	}
	
	public static class SuspendAction extends Action {
		void process(RunControl target) {
			target.suspend();
		}
	}
	
	public static class StartAction extends Action {
		void process(RunControl target) {
			target.start();
		}
	}
	
	public static class SetSpeedAction extends Action{
		public final double generationsPerSecond;
	
		public SetSpeedAction(double generationsPerSecond) {
			this.generationsPerSecond = generationsPerSecond;
		}
		
		void process(RunControl target) {
			target.setSpeedLimitAsynchroneous(generationsPerSecond);
		}
	}
	
	public static class StartSeekSequenceAction extends Action {
		void process(RunControl target) {
			target.startSeekSequence();
		}
	}
	
	public static class EndSeekSequenceAction extends Action {
		void process(RunControl target) {
			target.endSeekSequence();
		}
	}
	
	public static abstract class SeekAction extends Action {
	}
	
	public static class SeekToLastBatchAction extends SeekAction {
		void process(RunControl target) {
			target.seekToLastBatch();
		}
	}
	
	public static class SeekToNextBatchAction extends SeekAction {
		void process(RunControl target) {
			target.seekToNextBatch();
		}
	}
	
	public static class SeekToLastRunAction extends SeekAction {
		void process(RunControl target) {
			target.seekToLastRun();
		}
	}
	
	public static class SeekToNextRunAction extends SeekAction {
		void process(RunControl target) {
			target.seekToNextRun();
		}
	}
	
	public static class SeekToLastGenerationAction extends SeekAction {
		void process(RunControl target) {
			target.seekToLastGeneration();
		}
	}
	
	public static class SeekToNextGenerationAction extends SeekAction {
		void process(RunControl target) {
			target.seekToNextGeneration();
		}
	}
	
	public static class stepBackAction extends SeekAction {
		void process(RunControl target) {
			target.stepBack();
		}
	}	
	
	public static class StepForwardAction extends SeekAction {
		void process(RunControl target) {
			target.stepForward();
		}
	}
	
	public static class SeekToReplayEndAction extends SeekAction {
		void process(RunControl target) {
			target.seekToReplayEnd();
		}
	}
	
	public static class SeekToStartAction extends SeekAction {
		void process(RunControl target) {
			target.seekToStart();
		}
	}
	
	/**
	 * @author  nunkesser
	 */
	public static class SeekToTargetAction extends SeekAction {
		public final GenerationIndex seekTarget;
	
		public SeekToTargetAction(GenerationIndex seekTarget) {
			this.seekTarget = seekTarget;
		}
		
		void process(RunControl target) {
			target.seek(seekTarget);
		}
	}
	
	public static class TerminateAction extends Action {
		void process(RunControl target) {
			target.terminate();
		}
	}
}
