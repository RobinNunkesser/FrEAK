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

import freak.core.control.*;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * @author  Stefan
 */
public class LoadSaveManager {
	private static SingleExtensionFileFilter filter = new SingleExtensionFileFilter(".freak", "Freak Schedule");

	private AbstractRunFrame parent;
	private File lastFile;
	private boolean lastFileLegal;

	public LoadSaveManager(AbstractRunFrame parent) {
		this.parent = parent;
	}

	public void newObject() {
		lastFileLegal = false;
	}

	public FreakFile load() {
		JFileChooser c = new JFileChooser();
		c.setFileFilter(filter);
		int accepted = c.showOpenDialog(parent);
		if (accepted != JFileChooser.APPROVE_OPTION)
			return null;

		File f = c.getSelectedFile();
		lastFile = f;
		lastFileLegal = true;

		return doLoad();
	}

	public void save(FreakFile data) {
		if (lastFileLegal) {
			doSave(data);
		} else {
			saveAs(data);
		}
	}

	public void saveAs(FreakFile data) {
		JFileChooser c = new JFileChooser();
		c.setFileFilter(filter);
		int accepted = c.showSaveDialog(parent);
		if (accepted != JFileChooser.APPROVE_OPTION)
			return;

		File f = c.getSelectedFile();
		lastFile = filter.fixExtension(f);
		lastFileLegal = true;

		doSave(data);
	}

	private FreakFile doLoad() {
		try {
			return FreakFile.read(new FileInputStream(lastFile));
		} catch (Exception exc) {
			throw new RuntimeException(exc);
		}
	}

	private void doSave(FreakFile data) {
		try {
			data.write(new FileOutputStream(lastFile));
		} catch (Exception exc) {
			throw new RuntimeException(exc);
		}
	}
}
