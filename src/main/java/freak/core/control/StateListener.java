/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.control;

/**
 * @author Stefan
 */
public interface StateListener {
	public void asynchroneousFeedback(Schedule schedule, Replay replay);
	public void synchroneousFeedback(Schedule activeSchedule, Replay replay);
	
	public void simulationCompleted(Actions.Action lastProcessed);
	public void simulationException(Exception exc);
	public void terminated(Actions.Action lastProcessedBeforeTermination);
}
