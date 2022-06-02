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

import java.io.File;

/**
 * A general file filter for file dialogs of java swing.
 * @author  Stefan
 */
public class SingleExtensionFileFilter extends javax.swing.filechooser.FileFilter {
	private String extension;
	private String description;

	/**
	 * Creates a new file filter.
	 *
	 * @param extension The extension of the file type, like .zip
	 * @param description The description of the file type shown in file dialogs.
	 */
	public SingleExtensionFileFilter(String extension, String description) {
		this.extension = extension;
		this.description = description;
	}
	/**
	 * Determines whether a file matches the filter pattern (has the correct
	 * extension). Directorys are always accepted.
	 */
	public boolean accept(File f) {
		if (f == null)
			return false;
		if (f.isDirectory())
			return true;
		return f.getAbsolutePath().toLowerCase().endsWith(extension.toLowerCase());
	}
	/**
	 * Returns the description of this file format.
	 * @uml.property  name="description"
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * Returns f, if f already has the right extension or a file named
	 * f+extension, if not.
	 */
	public File fixExtension(File f) {
		if (f.getAbsolutePath().toLowerCase().endsWith(extension)) return f;
		return new File(f.getAbsolutePath() + extension);
	}
}