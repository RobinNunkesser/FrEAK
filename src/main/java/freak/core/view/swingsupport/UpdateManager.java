/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.core.view.swingsupport;

import java.util.*;

import javax.swing.*;

/**
 * This class maintains a list of "dirty" FreakModels that may contain data,
 * that has not yet been written to their swing component. The UpdateManager
 * will cause such data to be written to the swing components in regular
 * intervals by calling flush on all components in the list. The time used in
 * the flush process is not accounted for in the delay, to prevent other threads
 * from starving.
 * 
 * The system is very similar to a swing repaint manager.
 * 
 * @author Stefan
 */
public class UpdateManager {
	private static Set dirtyModels = new HashSet();

	/**
	 * Add a component to the list of dirty FreakModels. The update manager will
	 * call flush on the component in the event dispatching thread after some
	 * time.
	 * 
	 * This method may be called from any thread.
	 */
	public static void markDirty(FreakSwingModel dirtyModel) {
		synchronized (dirtyModels) {
			dirtyModels.add(dirtyModel);
			dirtyModels.notify();
		}
	}

	/**
	 * Immidiately calls flush on all dirty FreakModels. This should always be
	 * done before enabling user actions that may depend on data in the
	 * FreakModels.
	 * 
	 * This method must be called from the event dispatching thread.
	 */
	public static void flushAll() {
		//take a snapshot to keep markDirty available most of the time
		Object[] snapshot;
		synchronized (dirtyModels) {
			snapshot = dirtyModels.toArray();
			dirtyModels.clear();
		}

		//process the snapshot (double flushes cause no harm)
		for (int i = 0; i < snapshot.length; i++) {
			FreakSwingModel model = (FreakSwingModel)snapshot[i];
			synchronized (model) {
				model.flush();
			}
		}
	}

	static {
		new PeriodicUpdate().start();
	}

	private static class PeriodicUpdate extends Thread {
		private static final long delay = 100;
		private static long lastUpdate = 0;
		private static boolean failed;
		
		public PeriodicUpdate() {
			setDaemon(true);
		}

		public void run() {
			try {
				while (true) {
					//wait for a dirty component
					synchronized (dirtyModels) {
						while (dirtyModels.isEmpty()) {
							dirtyModels.wait();
						}
					}

					//wait, until delay is over
					long now = System.currentTimeMillis();
					long remaining = delay - now + lastUpdate;
					if (remaining > 0) {
						Thread.sleep(remaining);
					}

					//update all
					if (!failed) {
						try {
							SwingUtilities.invokeAndWait(new Runnable() {
								public void run() {
									update();
								}
							});
						} catch (Error err) {
							// X11 crash in event queue, if no server is available (GraphicsEnvironment.isHeadless() does not work)
							failed = true;
						}
					} 
					
					// if event queue not accessible, use this thread
					if (failed) {
						update();
					}
				}
			} catch (Exception exc) {
				//may become a problem
				throw new RuntimeException(exc);
			}
		}
		
		private void update() {
			flushAll();
			lastUpdate = System.currentTimeMillis();
		}
	}
}
