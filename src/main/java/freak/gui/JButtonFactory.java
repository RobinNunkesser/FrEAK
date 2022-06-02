/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.gui;

import javax.swing.*;
import java.awt.*;

/**
 * A bugfix for MacOS Aqua look and feel.
 * @author  Stefan
 */
public abstract class JButtonFactory {
	private static JButtonFactoryDelegate instance = new DefaultJButtonFactory();

	public static JButton newButton() {
		return instance.newButton();
	}
	public static JButton newButton(Icon arg0) {
		return instance.newButton(arg0);
	}
	public static JButton newButton(String arg0) {
		return instance.newButton(arg0);
	}
	public static JButton newButton(Action arg0) {
		return instance.newButton(arg0);
	}
	public static JButton newButton(String arg0, Icon arg1) {
		return instance.newButton(arg0, arg1);
	}

	public static void activateBugFix() {
		instance = new MacOSJButtonFactory();
	}

	private static interface JButtonFactoryDelegate {
		public JButton newButton();
		public JButton newButton(Icon arg0);
		public JButton newButton(String arg0);
		public JButton newButton(Action arg0);
		public JButton newButton(String arg0, Icon arg1);
	}

	private static class DefaultJButtonFactory implements JButtonFactoryDelegate {
		public JButton newButton() {
			return new JButton();
		}
		public JButton newButton(Icon arg0) {
			return new JButton(arg0);
		}
		public JButton newButton(String arg0) {
			return new JButton(arg0);
		}
		public JButton newButton(Action arg0) {
			return new JButton(arg0);
		}
		public JButton newButton(String arg0, Icon arg1) {
			return new JButton(arg0, arg1);
		}
	}

	private static class MacOSJButtonFactory implements JButtonFactoryDelegate {
		public JButton newButton() {
			return new MacOSJButton();
		}
		public JButton newButton(Icon arg0) {
			return new MacOSJButton(arg0);
		}
		public JButton newButton(String arg0) {
			return new MacOSJButton(arg0);
		}
		public JButton newButton(Action arg0) {
			return new MacOSJButton(arg0);
		}
		public JButton newButton(String arg0, Icon arg1) {
			return new MacOSJButton(arg0, arg1);
		}
	}

	private static class MacOSJButton extends JButton {
		private static int height = new JButton("A").getPreferredSize().height;

		public MacOSJButton() {
			super();
		}
		public MacOSJButton(Icon arg0) {
			super(arg0);
		}
		public MacOSJButton(String arg0) {
			super(arg0);
		}
		public MacOSJButton(Action arg0) {
			super(arg0);
		}
		public MacOSJButton(String arg0, Icon arg1) {
			super(arg0, arg1);
		}
		public void setIcon(Icon arg0) {
			super.setIcon(arg0);
			int width = new JButton(getText()).getPreferredSize().width + arg0.getIconWidth() + getIconTextGap();
			setPreferredSize(new Dimension(width, height));
			setMinimumSize(new Dimension(width, height));
		}
	}
}
