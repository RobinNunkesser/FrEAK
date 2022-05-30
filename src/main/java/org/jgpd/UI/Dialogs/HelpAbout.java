/*	1.2 01.02.2003
 *
 * Copyright (C) 2003 sven.luzar
 * Copyright (C) 2003 David Benson
 *
 * JGpd is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * JGpd is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JGpd; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package org.jgpd.UI.Dialogs;

import java.awt.event.ActionEvent;
import java.util.MissingResourceException;

import javax.swing.ImageIcon;
import javax.swing.JDialog;

import org.jgraph.GPGraphpad;
import org.jgraph.pad.GPAboutDialog;
import org.jgraph.pad.resources.ImageLoader;
import org.jgraph.pad.resources.Translator;
import org.jgraph.pad.actions.AbstractActionDefault;

/**Shows the homepage.
 *
 * @author David Benson
 * @version 1.0
 *
 */
public class HelpAbout extends AbstractActionDefault {

	/** The about dialog for JGpd
	 */
	protected JDialog aboutDlg;

	/**
	 * Constructor for HelpAbout.
	 * @param graphpad
	 */
	public HelpAbout(GPGraphpad graphpad) {
		super(graphpad);
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		if (aboutDlg == null)
		{
			String iconName = Translator.getString("Logo");
			ImageIcon logoIcon = ImageLoader.getImageIcon(iconName);

			try
			{
				String title = Translator.getString("AboutFrameTitle");
				aboutDlg = new GPAboutDialog(graphpad.getFrame(), title, logoIcon);
			} catch (MissingResourceException mre) {
				aboutDlg =
					new GPAboutDialog(
						graphpad.getFrame(),
						"About JGpd",
						logoIcon);
			}
		}
		aboutDlg.show();
	}
	/** Empty implementation.
	 *  This Action should be available
	 *  each time.
	 */
	public void update(){};

}
