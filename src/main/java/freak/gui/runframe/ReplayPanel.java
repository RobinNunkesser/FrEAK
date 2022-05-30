/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.gui.runframe;

import java.awt.*;
import javax.swing.*;

/**
 * Shows the blinking R.
 * @author  Stefan
 */
class ReplayPanel extends JLabel {
	private volatile boolean blink = false;
	private volatile boolean rVisible = true;
	private Thread blinkThread;

	/**
	 * Creates a new instance of ReplayLabel with size (30,30).
	 */
	public ReplayPanel() {
		super("R");
		setFont(new Font("SansSerif", Font.BOLD, 50));
		setForeground(Color.WHITE); //Uebertrieben Sportschauig ;)
	}

	/**
	 * @param blink  the blink to set
	 * @uml.property  name="blink"
	 */
	public synchronized void setBlink(boolean blink) {
		if (this.blink != blink) {
			if (blink) {
				this.blink = true;
				startBlinkThread();
			} else {
				this.blink = false;
				stopBlinkThread();
			}
		}
	}

	private void startBlinkThread() {
		blinkThread = new Thread() {
			public void run() {
				while (true) {
					blink();
					repaint();

					try {
						Thread.sleep(1000);
					} catch (InterruptedException exc) {
						return;
					}
				}
			}
		};
		blinkThread.setDaemon(true);
		blinkThread.start();
	}

	private void stopBlinkThread() {
		blinkThread.interrupt();
		try {
			blinkThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		blinkThread = null;

		rVisible = true;
		repaint();
	}

	private void blink() {
		rVisible = !rVisible;
	}

	public void paintComponent(Graphics g) {
		if (rVisible) {
			super.paintComponent(g);
		}
	}
}
