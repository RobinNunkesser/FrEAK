package org.jgraph.pad.actions;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JOptionPane;

import org.jgraph.GPGraphpad;
import org.jgraph.pad.resources.Translator;

/**
 * @author sven.luzar
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class FileOpenURL extends AbstractActionFile {

	/**
	 * Constructor for FileOpen.
	 * @param graphpad
	 * @param name
	 */
	public FileOpenURL(GPGraphpad graphpad) {
		super(graphpad);
	}

	/** Shows a file chooser with the
	 *  file filters from the file formats
	 *  to select a file.
	 *
	 *  Furthermore the method uses the selected
	 *  file format for the read process.
	 *
	 *  @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 *  @see GraphModelProviderRegistry
	 */
	public void actionPerformed(ActionEvent e) {
		String name =
			JOptionPane.showInputDialog(Translator.getString("URLDialog", new Object[]{"foo.gpd"}));

		// canceled?
		if (name == null)
			return;

		// open the graphpad
		try {
			graphpad.addDocument(new URL(name));
		} catch (MalformedURLException ex) {
			JOptionPane.showMessageDialog(
				graphpad,
				ex.getLocalizedMessage(),
				Translator.getString("Error"),
				JOptionPane.ERROR_MESSAGE);
		}
	}

	/** Empty implementation.
	 *  This Action should be available
	 *  each time.
	 */
	public void update() {
	};

}
