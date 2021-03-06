/*
 * @(#)GPLogConsole.java	1.0 29.01.2003
 *
 * Copyright (C) 2003 luzar
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */
package org.jgraph.pad;

import java.awt.AWTEvent;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URLEncoder;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.SwingUtilities;

import org.jgraph.pad.resources.*;
import org.jgraph.utils.BrowserLauncher;


/** Shows the System.in and System.out in a nice JFrame.
 *
 *  The Frame looks like this:<br>
 *  <img src="doc-files/GPLogConsole.jpg">
 *
 * @author Thomas Genssler (FZI)
 * @author Sven Luzar*/
public class GPLogConsole extends JFrame {

	/** The PrintStream for the System.out
	 *
	 */
	private PrintStream stdout = null;
	/** The PrintStream for the System.err
	 *
	 */
	private PrintStream stderr = null;

	/** Title of the Frame
	 *
	 */
	private String frameTitle = "";

	/** Card Layout for the Window
	 *
	 */
	CardLayout cardLayout = new CardLayout();
	/** Text area for the System.err output
	 */
	JTextArea stderrText = new JTextArea();
	/** ScrollPane for the System.out text area
	 */
	JScrollPane stdoutScrollPane = new JScrollPane();
	/** Text area for the System.out output
	 */
	JTextArea stdoutText = new JTextArea();
	/** ScrollPane for the System.err text area
	 */
	JScrollPane stderrScrollPane = new JScrollPane();
	/** Tabbed pane for the System.out and System.err text area
	 */
	JTabbedPane jTabbedPane1 = new JTabbedPane();
	/** Icon for the Window
	 */
	Image myIcon = null;
	/** If <code>true</code>, the console will become visible when any system output occurs.
	 */
	boolean makeVisibleOnError = false;
	/** PopUpMenu for save and clear the output textareas*/
	InternalPopupMenu popup = new InternalPopupMenu();

	/** creates an instance
	 *
	 */
	public GPLogConsole(
		String title,
		Image icon,
		boolean makeVisible)
		{
		super();
		frameTitle = title;
		myIcon = icon;
		this.makeVisibleOnError = makeVisible;

		if ((frameTitle == null) || (frameTitle.equals(""))) {
			frameTitle = "Test drive";
		}

		this.enableEvents(AWTEvent.WINDOW_EVENT_MASK);

		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**Overriden, in order to be able to deal with window events*/
	protected void processWindowEvent(WindowEvent e) {
		//
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			// only close the window when we are not in embedded mode
			// release resources and exit if we are not running embedded, buttonImage.buttonEdge., as
			// part of another application
			//super.processWindowEvent(buttonEdge);
			this.dispose() ;
		}
	}

	/** Initialises the Swing components
	 *
	 */
	private void jbInit() throws Exception {
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				this_windowClosing(e);
			}
		});
		this.setTitle(frameTitle);
		this.getContentPane().setLayout(cardLayout);
		if (myIcon != null)
			this.setIconImage(myIcon);

		//re-direct stderr and stdout
		redirect();

		stderrText.setForeground(Color.red);
		stderrText.setBackground(Color.lightGray);
		stderrText.setEditable(false);
		stderrText.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				stderrText_mouseClicked(e);
			}
		});
		stdoutText.setForeground(Color.black);
		stdoutText.setEditable(false);
		stdoutText.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				stdoutText_mouseClicked(e);
			}
		});
		jTabbedPane1.setTabPlacement(JTabbedPane.BOTTOM);
		jTabbedPane1.setMinimumSize(new Dimension(400, 400));
		jTabbedPane1.setPreferredSize(new Dimension(400, 400));
		this.getContentPane().add(jTabbedPane1, "jTabbedPane1"/*#Frozen*/);
		jTabbedPane1.add(stdoutScrollPane, Translator.getString("StandardOut"/*#Finished:Original="Standard out"*/));
		jTabbedPane1.add(stderrScrollPane, Translator.getString("StandardError"/*#Finished:Original="Standard error"*/));
		stderrScrollPane.getViewport().add(stderrText, null);
		stdoutScrollPane.getViewport().add(stdoutText, null);

		// make sure the last updated log is always in front
		stdoutText.getDocument().addDocumentListener(
			new MyDocumentListener(this, jTabbedPane1, stdoutScrollPane));

		stderrText.getDocument().addDocumentListener(
			new MyDocumentListener(this, jTabbedPane1, stderrScrollPane));

		this.pack();

	}

	/* Sets the new OutputStream for System.out and System.err
	*
	*/
	private void redirect() {

		stdout = System.out;
		stderr = System.err;

		try {
			System.setOut(new JTextAreaOutputStream(stdoutText, stdout, true));
			//    System.out.println("Standard out has been re-directed");
			System.setErr(new JTextAreaOutputStream(stderrText, stderr, true));
			//    System.err.println("Standard error has been re-directed");
		} catch (Exception ex){
			System.err .println("Error while re-directing the output. Ignoring...");
		}
	}
	/** disposes this window
	 */
	void this_windowDispose(WindowEvent e) {
		this.dispose();
	}
	/** closes this window
	 */
	void this_windowClosing(WindowEvent e) {
		this_windowDispose(e);
		System.exit(0);
	}
	/** Shows the popup menu for the System.out textarea
	 */
	void stdoutText_mouseClicked(MouseEvent e) {
		if (e.getModifiers() == InputEvent.META_MASK) {
			popup.setTextArea(stdoutText);
			popup.show(this.stdoutText, e.getX(), e.getY());
		}

	}

	/** Shows the popup menu for the System.err textarea
	 */
	void stderrText_mouseClicked(MouseEvent e) {
		if (e.getModifiers() == InputEvent.META_MASK) {
			popup.setTextArea(stderrText);
			popup.show(this.stderrText, e.getX(), e.getY());
		}

	}

}

/** Document listener to detect changes at the
 *  text areas and switches the right one text area
 *  to front.*/

class MyDocumentListener implements DocumentListener {
	/** The Tabbed pane to switch the right one text area to front
	 */
	private JTabbedPane paneToSwitch = null;
	/** The component which is in front
	 */
	private Component componentInFront = null;
	/** The parent container
	 */
	private GPLogConsole lc = null;

	/** creats an instance of this listener
	 *
	 */
	public MyDocumentListener(GPLogConsole l, JTabbedPane paneToSwitch, 
								Component inFront) {
		this.paneToSwitch = paneToSwitch;
		this.componentInFront = inFront;
		this.lc = l;
	}
	/** Calls getInFront()
	 * @see #getInFront
	 *
	 */
	public void changedUpdate(DocumentEvent e) {
		getInFront();
	}

	/** Calls getInFront()
	 * @see #getInFront
	 *
	 */
	public void insertUpdate(DocumentEvent e) {
		getInFront();
		if (lc.makeVisibleOnError){
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					lc.setVisible(true);
					lc.toFront();		
				}
			});
		}
	}

	/** Calls getInFront()
	 * @see #getInFront
	 *
	 */
	public void removeUpdate(DocumentEvent e) {
		getInFront();
	}
	/** Switches the rights one text area to front
	 */
	void getInFront() {
		// bring the attached component in front
		paneToSwitch.setSelectedComponent(this.componentInFront);
	}
}

/** Internal Popup Menu with a clear and a save button to
 *  clear or save the text areas.*/
class InternalPopupMenu extends JPopupMenu {
	/** Menu item for clearing the text area
	 */
	JMenuItem jMenuItemClearWindow = new JMenuItem(Translator.getString("ClearOutput"/*#Finished:Original="Clear output"*/));
	/** Menu item for saving the text area
	 */
	JMenuItem jMenuItemSaveToFile = new JMenuItem(Translator.getString("SaveToFile"/*#Finished:Original="Save to file..."*/));
	/** Menu item for emailing the text area contents to tech support
	 */
	JMenuItem jMenuItemSendEmail = new JMenuItem(Translator.getString("Error.EmailTechSupport"));
	/** The current textarea
	 */
	private JTextArea currentWindow = null;
	/** creates an instance
	 */
	public InternalPopupMenu() {
		super();
		this.add(jMenuItemClearWindow);
		this.addSeparator();
		this.add(jMenuItemSaveToFile);
		this.add(jMenuItemSendEmail);

		jMenuItemClearWindow
			.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearWindow();
			}
		});
		jMenuItemSaveToFile
			.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveWindowToFile();
			}
		});
		jMenuItemSendEmail
			.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String email = Translator.getString("Error.EmailTechSupportAddress");
				String subj = "?subject=runtime error message&body=";
				String msg = null;
				try {
					// UTF8 is not exactly to spec for mailto: encoding 
					// but it will work. only alternative is attaching
					// the javamail package at ~1.5Mb
					msg = URLEncoder.encode(currentWindow.getText(), "UTF-8");
				}
				catch (java.io.UnsupportedEncodingException ue){
					System.out.println(ue);	
				}

				try {
					BrowserLauncher.openURL("mailto:"+email+subj+msg);
				} 
				catch (IOException ioe){
					System.out.println(ioe);	
				}
			}
		});
		
		//jMenuItemSaveToFile.setEnabled(false);//.disable();
	}

	/** Sets the current text area
	 *
	 */
	public void setTextArea(JTextArea ta) {
		currentWindow = ta;
	}
	/** clears the window
	 */
	private void clearWindow() {
		currentWindow.setText("");
	}
	/** Shows a file chooser and saves the
	  * file to the selected name
	 */
	private void saveWindowToFile() {
		JFileChooser fileDlg = new JFileChooser();
		//fileDlg.setFileFilter(filter);
		// no file selected.
		if (JFileChooser.APPROVE_OPTION != fileDlg.showSaveDialog(null)) {
			System.out.println("No file selected"/*#Frozen*/);
			return;
		}
		File f = fileDlg.getSelectedFile();

		if (!f.canWrite())
			System.err.println("Can't write to file " + f.getAbsolutePath()/*#Frozen*/);
		try {
			PrintStream os = new PrintStream(new FileOutputStream(f));
			os.println(currentWindow.getText());
			os.close();

			clearWindow();
		} catch (FileNotFoundException e) {
			System.err.println("Can't write to file " + f.getAbsolutePath()/*#Frozen*/);
		}
	}
}

/**A PrintStream for the text area output.
 *
 * @author Sven Luzar
 * */
class JTextAreaOutputStream extends PrintStream {
	/** the target for this printstream
	*/
	private JTextArea target = null;

	/** the original PrintStream to forward this
	 *  stream to the original stream
	 */
	private PrintStream orig = null;

	/** Flag is true if the stream should forward
	 *  the output to the original stream
	 *
	 */
	private boolean showOrig = false;

	/**creates an instance
	 *
	 */
	public JTextAreaOutputStream(
		JTextArea t,
		PrintStream orig,
		boolean showOrig) {
		super(new ByteArrayOutputStream());
		target = t;

		this.showOrig = showOrig;
		this.orig = orig;
	}
	/** writes a boolean value to the target
	 */
	public void print(boolean b) {
		if (showOrig)
			orig.print(b);
		if (b)
			target.append("true"/*#Frozen*/);
		else
			target.append("false"/*#Frozen*/);
		target.setCaretPosition(target.getText().length());
	}

	/** writes a boolean value to the target
	 *
	 */
	public void println(boolean b) {
		if (showOrig)
			orig.println(b);

		if (b)
			target.append("true\n"/*#Frozen*/);
		else
			target.append("false\n"/*#Frozen*/);
		target.setCaretPosition(target.getText().length());
	}

	/** writes the value to the target
	 *
	 */
	public void print(char c) {
		if (showOrig)
			orig.print(c);

		char[] tmp = new char[1];
		tmp[0] = c;
		target.append(new String(tmp));
		target.setCaretPosition(target.getText().length());
	}

	/** writes the value to the target
	 *
	 */
	public void println(char c) {
		if (showOrig)
			orig.println(c);

		char[] tmp = new char[2];
		tmp[0] = c;
		tmp[1] = '\n';
		target.append(new String(tmp));
		target.setCaretPosition(target.getText().length());
	}

	/** writes the value to the target
	 *
	 */
	public void print(char[] s) {
		if (showOrig)
			orig.print(s);

		target.append(new String(s));
		target.setCaretPosition(target.getText().length());
	}

	/** writes the value to the target
	 *
	 */
	public void println(char[] s) {
		if (showOrig)
			orig.println(s);

		target.append(new String(s) + "\n"/*#Frozen*/);
		target.setCaretPosition(target.getText().length());
	}

	/** writes the value to the target
	 *
	 */
	public void print(double d) {
		if (showOrig)
			orig.print(d);

		target.append(Double.toString(d));
		target.setCaretPosition(target.getText().length());
	}
	/** writes the value to the target
	 *
	 */
	public void println(double d) {
		if (showOrig)
			orig.println(d);

		target.append(Double.toString(d) + "\n"/*#Frozen*/);
		target.setCaretPosition(target.getText().length());
	}

	/** writes the value to the target
	 *
	 */
	public void print(float f) {
		if (showOrig)
			orig.print(f);

		target.append(Float.toString(f));
		target.setCaretPosition(target.getText().length());
	}
	/** writes the value to the target
	 *
	 */
	public void println(float f) {
		if (showOrig)
			orig.println(f);

		target.append(Float.toString(f) + "\n"/*#Frozen*/);
		target.setCaretPosition(target.getText().length());
	}

	/** writes the value to the target
	 *
	 */
	public void print(int i) {
		if (showOrig)
			orig.print(i);

		target.append(Integer.toString(i));
		target.setCaretPosition(target.getText().length());
	}
	/** writes the value to the target
	 *
	 */
	public void println(int i) {
		if (showOrig)
			orig.println(i);

		target.append(Integer.toString(i) + "\n"/*#Frozen*/);
		target.setCaretPosition(target.getText().length());
	}

	/** writes the value to the target
	 *
	 */
	public void print(long l) {
		if (showOrig)
			orig.print(l);

		target.append(Long.toString(l));
		target.setCaretPosition(target.getText().length());
	}
	/** writes the value to the target
	 *
	 */
	public void println(long l) {
		if (showOrig)
			orig.println(l);

		target.append(Long.toString(l) + "\n"/*#Frozen*/);
		target.setCaretPosition(target.getText().length());
	}

	/** writes the value to the target
	 *
	 */
	public void print(Object o) {
		if (showOrig)
			orig.print(o);

		target.append(o.toString());
		target.setCaretPosition(target.getText().length());
	}
	/** writes the value to the target
	 *
	 */
	public void println(Object o) {
		if (showOrig)
			orig.println(o);

		target.append(o.toString() + "\n"/*#Frozen*/);
		target.setCaretPosition(target.getText().length());
	}

	/** writes the value to the target
	 *
	 */
	public void print(String s) {
		if (showOrig)
			orig.print(s);

		target.append(s);
		target.setCaretPosition(target.getText().length());
	}

	/** writes the value to the target
	 *
	 */
	public void println(String s) {
		if (showOrig)
			orig.println(s);

		target.append(s + "\n"/*#Frozen*/);
		target.setCaretPosition(target.getText().length());
	}

	/** writes the value to the target
	 *
	 */
	public void println() {
		if (showOrig)
			orig.println();

		target.append(new String("\n"/*#Frozen*/));
		target.setCaretPosition(target.getText().length());
	}

}
