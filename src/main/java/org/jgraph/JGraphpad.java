/*
 * @(#)Graphpad.java	1.2 11/11/02
 *
 * Copyright (C) 2001 Gaudenz Alder
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package org.jgraph;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownServiceException;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JWindow;

import org.jgraph.pad.DefaultGraphModelProvider;
import org.jgraph.pad.GraphModelProviderRegistry;
import org.jgraph.pad.resources.ImageLoader;
import org.jgraph.pad.resources.Translator;
import org.jgraph.pad.resources.Version;
import org.jgraph.utils.Utilities;

public class JGraphpad extends Applet {

	// Main method
	public static void main(String[] args) {
		// Logo
		ImageIcon logoIcon = ImageLoader.getImageIcon("splash.gif");
		JWindow frame = new JWindow();

		try {
			//frame.setUndecorated(true); // JDK 1.3
			JLabel lab = new JLabel(logoIcon) {
				public void paint(Graphics g) {
					super.paint(g);

					Graphics2D g2 = (Graphics2D) g;
					g2.setRenderingHint(
						RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);

					g2.setColor(Color.BLACK);
					g2.setFont(new Font("Arial", Font.BOLD, 12));
					g2.drawString("v " + Version.getVersion(), 65, 58);
					g2.setFont(new Font("Arial", Font.BOLD, 10));
					g2.drawString(
						"(c) "
							+ Calendar.getInstance().get(Calendar.YEAR)
							+ " www.jgpd.com",
						10,
						70);
				}
			};

			frame.getContentPane().add(lab, BorderLayout.CENTER);
			lab.setLayout(new BorderLayout());
			lab.setBorder(BorderFactory.createRaisedBevelBorder());
			frame.pack();
			Utilities.center(frame);
			frame.show();

			GraphModelProviderRegistry.addGraphModelProvider(
				new DefaultGraphModelProvider());

			new GPGraphpad();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		} finally {
			frame.dispose();
		}
	}

	// From Applet
	public void init() {
		setLayout(new BorderLayout());
		setBackground(Color.white);
		JButton button = new JButton("Start");
		button.setIcon(GPGraphpad.applicationIcon);
		add(button, BorderLayout.CENTER);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				launchFromApplet();
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});
		button.setPreferredSize(getSize());
		button.revalidate();

		GraphModelProviderRegistry.addGraphModelProvider(
			new DefaultGraphModelProvider());

		launchFromApplet();
	}

	// FIXME is this just tiki code?
	public void launchFromApplet() {
		GPGraphpad pad = new GPGraphpad(this);
		//GPGraphpad.init();
		// Are we a Tiki applet?
		String drawPath = getParameter("drawpath");
		if (drawPath != null && !drawPath.equals("")) {
			try {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				URL tikiURL =
					new URL(
						"http",
						getCodeBase().getHost(),
						getCodeBase().getPort(),
						drawPath);
				pad.addDocument(tikiURL);
			} catch (MalformedURLException ex) {
				JOptionPane.showMessageDialog(
					pad,
					ex.getLocalizedMessage(),
					Translator.getString("Error"),
					JOptionPane.ERROR_MESSAGE);
			} finally {
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
			
		}
	}

	static public boolean post(
		String serverName,
		int portNumber,
		String url,
		String fileName,
		String type,
		String path,
		String content,
		String comment)
		throws MalformedURLException, IOException {

		String sep = "89692781418184";
		while (content.indexOf(sep) != -1)
			sep += "x";

		String message =
			makeMimeForm(fileName, type, path, content, comment, sep);

		// Ask for parameters
		URL server = new URL("http", serverName, portNumber, url);
		URLConnection connection = server.openConnection();

		connection.setAllowUserInteraction(false);
		connection.setDoOutput(true);
		//connection.setDoInput(true);
		connection.setUseCaches(false);

		connection.setRequestProperty(
			"Content-type",
			"multipart/form-data; boundary=" + sep);
		connection.setRequestProperty(
			"Content-length",
			Integer.toString(message.length()));

		//System.out.println(url);
		String replyString = null;
		try {
			DataOutputStream out =
				new DataOutputStream(connection.getOutputStream());
			out.writeBytes(message);
			out.close();
			System.out.println(
				"Wrote " + message.length() + " bytes to\n" + connection);

			try {
				BufferedReader in =
					new BufferedReader(
						new InputStreamReader(connection.getInputStream()));
				String reply = null;
				while ((reply = in.readLine()) != null) {
					if (reply.startsWith("ERROR ")) {
						replyString = reply.substring("ERROR ".length());
					}
				}
				in.close();
			} catch (IOException ioe) {
				replyString = ioe.toString();
				System.out.println(ioe + ": " + connection);
			}
		} catch (UnknownServiceException use) {
			replyString = use.getMessage();
			System.out.println(message);
		}
		if (replyString != null) {
			System.out.println("---- Reply " + replyString);
			/*
			if (replyString.startsWith("URL ")) {
				URL eurl = getURL(replyString.substring("URL ".length()));
				getAppletContext().showDocument(eurl);
			} else if (
				replyString.startsWith("java.io.FileNotFoundException")) {
				// debug; when run from appletviewer, the http connection
				// is not available so write the file content
				if (path.endsWith(".draw") || path.endsWith(".map"))
					System.out.println(content);
			} else
				showStatus(replyString);
			*/
			return false;
		} else {
			System.out.println(url + " saved");
			//showStatus(url + " saved");
			return true;
		}
	}

	//-----------------------------------------------------------------------

	/**
	 * create name="value" MIME form data like:
	 *   -----------------------------1234567
	 *   Content-Disposition: form-data; name="theName"
	 *   
	 *   theValue
	 */

	/**
	 * create name="value" file MIME form data like:
	 *   -----------------------------1234567
	 *   Content-Disposition: form-data; name="theName"; filename="theValue"
	 *   
	 *   theContent
	 */

	static String NL = "\r\n";
	static String NLNL = NL + NL;

	/** Post the given message */
	private static String makeMimeForm(
		String fileName,
		String type,
		String path,
		String content,
		String comment,
		String sep) {

		String binary = "";
		if (type.equals("image/gif")) {
			binary = "Content-Transfer-Encoding: binary" + NL;
		}

		String mime_sep = NL + "--" + sep + NL;

		return "--"
			+ sep
			+ "\r\n"
			+ "Content-Disposition: form-data; name=\"filename\""
			+ NLNL
			+ fileName
			+ mime_sep
			+ "Content-Disposition: form-data; name=\"noredirect\""
			+ NLNL
			+ 1
			+ mime_sep
			+ "Content-Disposition: form-data; name=\"filepath\"; "
			+ "filename=\""
			+ path
			+ "\""
			+ NL
			+ "Content-Type: "
			+ type
			+ NL
			+ binary
			+ NL
			+ content
			+ mime_sep
			+ "Content-Disposition: form-data; name=\"filecomment\""
			+ NLNL
			+ comment
			+ NL
			+ "--"
			+ sep
			+ "--"
			+ NL;
	}

	/** Replace current app with a different URL */
	void exit(GPGraphpad application) {
		application.getFrame().dispose();
		String viewPath = getParameter(VIEWPATH_PARAMETER);
		if (viewPath != null) {
			try {
				//String serverName = getCodeBase().getHost();		//unused code
				//int portNumber = getCodeBase().getPort();			//unused code
				URL url = new URL(getCodeBase(), viewPath);
				getAppletContext().showDocument(url, "_self");
			} catch (MalformedURLException mue) {
				System.out.println(mue);
				//showStatus("Bad URL for viewpath " + viewPath);
			}
		}
	}

	static private String VIEWPATH_PARAMETER = "viewpath";

}
