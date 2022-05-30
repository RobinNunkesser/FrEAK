/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.gui.runframe;

public abstract class AbstractRunFrame extends javax.swing.JFrame  {
	
	public void showHelpPage(String page) {}
	public void editorClosed() {}
	public void viewFilenameInTitle(String s) {}

}
