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

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A <code>DesktopPane</code> providing additional functionality: the visible frames can be tiled, cascaded, minimized, restored or closed.
 * @author  Dirk
 */
public class FreakDesktopPane {
	private JDesktopPane delegate = new JDesktopPane();
	private Set frames = new HashSet();

	{
		delegate.setMinimumSize(new Dimension(100, 100));
		delegate.setPreferredSize(new Dimension(100, 100));
	}

	/**
	 * @return  the delegate
	 * @uml.property  name="delegate"
	 */
	public JDesktopPane getDelegate() {
		return delegate;
	}

	/**
	 * Tiles all visible frames with a divide and conquer algorithm:
	 * the visible area is split in two halfs in the middle of the 
	 * longer side. This is done recursively until all frames can be placed
	 * within one area.  
	 */
	public void tile() {
		JInternalFrame[] frames = getAllFrames();

		ArrayList frameList = new ArrayList();
		for (int i = 0; i < frames.length; i++) {
			if (frames[i].isVisible() && !frames[i].isIcon()) {
				try {
					frames[i].setMaximum(false);
					frameList.add(frames[i]);
				} catch (PropertyVetoException e) {
				}
			}
		}

		Insets insets = delegate.getInsets();
		Rectangle bounds = delegate.getBounds();
		tile(frameList, new Rectangle((int)bounds.getMinX() + insets.left, (int)bounds.getMinY() + insets.top, (int)bounds.getMaxX() - insets.right, (int)bounds.getMaxY() - insets.bottom));
	}

	private void tile(ArrayList frames, Rectangle area) {
		if (frames.size() == 0)
			return;
		if (frames.size() == 1) {
			JInternalFrame frame = (JInternalFrame)frames.get(0);
			frame.reshape((int)area.getMinX(), (int)area.getMinY(), (int)area.getWidth(), (int)area.getHeight());
		} else {

			// divide rectangle in two halfs along the longer axis 
			Rectangle subArea1;
			Rectangle subArea2;
			if (area.getWidth() >= area.getHeight()) {
				// divide vertically
				int median = (int)area.getWidth() / 2;
				subArea1 = new Rectangle((int)area.getMinX(), (int)area.getMinY(), median, (int)area.getHeight());
				subArea2 = new Rectangle((int) (area.getMinX() + median + 1), (int)area.getMinY(), (int) (area.getWidth() - median), (int)area.getHeight());
			} else {
				// divide horizontally
				int median = (int)area.getHeight() / 2;
				subArea1 = new Rectangle((int)area.getMinX(), (int)area.getMinY(), (int)area.getWidth(), median);
				subArea2 = new Rectangle((int)area.getMinX(), (int) (area.getMinY() + median + 1), (int)area.getWidth(), (int) (area.getHeight() - median));
			}

			// divide the frames in two sublists
			ArrayList subList1 = new ArrayList();
			ArrayList subList2 = new ArrayList();
			for (int i = 0; i < frames.size() / 2; i++) {
				subList1.add(frames.get(i));
			}
			for (int i = frames.size() / 2; i < frames.size(); i++) {
				subList2.add(frames.get(i));
			}

			// continue recursively with the sublists and subareas 
			tile(subList1, subArea1);
			tile(subList2, subArea2);
		}
	}

	/**
	 * Cascades all visible <code>JInternalFrame</code>s.
	 */
	public void cascade() {
		JInternalFrame[] frames = getAllFrames();
		Arrays.sort(frames);
		if (frames.length == 0)
			return;

		int xOffset = delegate.getInsets().left;
		int yOffset = delegate.getInsets().top;
		int pos = 0;
		int step = getFrameDistance(frames[0]);

		for (int i = 0; i < frames.length; i++) {
			if (frames[i].isVisible() && !frames[i].isIcon()) {
				try {
					if (frames[i].isMaximum())
						frames[i].setMaximum(false);
				} catch (PropertyVetoException e) {
				}

				frames[i].reshape(pos + xOffset, pos + yOffset, FreakInternalFrame.DEFAULT_WIDTH, FreakInternalFrame.DEFAULT_HEIGHT);
				frames[i].toFront();

				pos += step;
			}
		}
	}

	public int getFrameDistance(JInternalFrame frame) {
		return frame.getHeight() - frame.getRootPane().getHeight();
	}

	public void minimizeAll() {
		JInternalFrame[] frames = getAllFrames();
		for (int i = 0; i < frames.length; i++) {
			try {
				frames[i].setIcon(true);
			} catch (PropertyVetoException e) {
			}
		}
	}

	public void restoreAll() {
		JInternalFrame[] frames = getAllFrames();
		for (int i = 0; i < frames.length; i++) {
			try {
				frames[i].setVisible(true);
				frames[i].setIcon(false);
			} catch (PropertyVetoException e) {
			}
		}
	}

	public void closeAll() {
		JInternalFrame[] frames = getAllFrames();
		for (int i = 0; i < frames.length; i++) {
			frames[i].setVisible(false);
		}
	}

	public JInternalFrame[] getAllFrames() {
		return (JInternalFrame[])new ArrayList(frames).toArray(new JInternalFrame[frames.size()]);
	}

	public void remove(JInternalFrame frame) {
		delegate.remove(frame);
		frames.remove(frame);
	}

	public void removeAll() {
		delegate.removeAll();
		frames.clear();
	}

	public void add(JInternalFrame frame) {
		delegate.add(frame);
		frames.add(frame);

		int xOffset = delegate.getInsets().left;
		int yOffset = delegate.getInsets().top;
		int pos = getFrameDistance(frame) * (getAllFrames().length - 1);
		frame.reshape(xOffset + pos, yOffset + pos, FreakInternalFrame.DEFAULT_WIDTH, FreakInternalFrame.DEFAULT_HEIGHT);
	}
}
