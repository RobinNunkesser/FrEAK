/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.core.util;

import javax.swing.*;

/**
 * @author Stefan
 */
public class SwingUtilitiesExtra {
	/**
	 * @author  nunkesser
	 */
	private static class FlaggedRunnable implements Runnable {
		private Runnable target;
		private boolean done;

		public FlaggedRunnable(Runnable target) {
			this.target = target;
		}

		public synchronized void run() {
			target.run();
			done = true;
			notify();
		}

		/**
		 * @return  the done
		 * @uml.property  name="done"
		 */
		public synchronized boolean isDone() {
			return done;
		}
	}

	public static void uninterruptibleInvokeAndWait(Runnable r) {
		FlaggedRunnable task = new FlaggedRunnable(r);
		SwingUtilities.invokeLater(task);

		boolean interrupted = false;
		while (!task.isDone()) {
			try {
				synchronized (task) {
					task.wait();
				}
			} catch (InterruptedException exc) {
				interrupted = true;
			}
		}

		if (interrupted)
			Thread.currentThread().interrupt();
	}
}
