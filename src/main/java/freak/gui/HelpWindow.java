/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

/**
 * Provides minimal browser functionality to show the help pages of freak.
 * 
 * @author Stefan
 */
public class HelpWindow extends JFrame {
	private static final String BASE = "/freak/gui/help/";
	public static final String INDEX_PAGE = "index.html";

	private static final Icon backIcon = new ImageIcon(HelpWindow.class.getResource("/toolbarButtonGraphics/navigation/Back24.gif"));
	private static final Icon forwardIcon = new ImageIcon(HelpWindow.class.getResource("/toolbarButtonGraphics/navigation/Forward24.gif"));
	private static final Icon indexIcon = new ImageIcon(HelpWindow.class.getResource("/toolbarButtonGraphics/navigation/Home24.gif"));

	private Stack backBuffer = new Stack();
	private Stack forwardBuffer = new Stack();
	private URL currentURL;

	private JButton back = new JButton(backIcon);
	private JButton forward = new JButton(forwardIcon);
	private JButton index = new JButton(indexIcon);
	private JLabel addressLabel = new JLabel("Address:");
	private JTextField address = new JTextField();
	private JEditorPane htmlView = new JEditorPane();

	public HelpWindow(Dialog owner) {
	//	super(owner);
	}
	
	public HelpWindow(Frame owner) {
	//	super(owner);
	}
	
	{
		initComponents();
	}

	private void initComponents() {
		setTitle("FrEAK - Help");

		JScrollPane scrollPane = new JScrollPane();
		JToolBar topPanel = new JToolBar();

		scrollPane.setPreferredSize(new Dimension(800, 600));
		htmlView.setEditable(false);
		topPanel.setFloatable(false);

		GridBagLayout topLayout = new GridBagLayout();
		GridBagConstraints c;

		c = new GridBagConstraints();
		c.insets = new Insets(6, 12, 5, 0);
		topLayout.setConstraints(back, c);

		c = new GridBagConstraints();
		c.insets = new Insets(6, 2, 5, 0);
		topLayout.setConstraints(forward, c);

		c = new GridBagConstraints();
		c.insets = new Insets(6, 5, 5, 0);
		topLayout.setConstraints(index, c);

		c = new GridBagConstraints();
		c.insets = new Insets(6, 17, 6, 0);
		topLayout.setConstraints(addressLabel, c);

		c = new GridBagConstraints();
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(6, 11, 5, 11);
		topLayout.setConstraints(address, c);

		topPanel.setLayout(topLayout);

		scrollPane.setBorder(new EmptyBorder(0, 0, 18, 0));
		htmlView.setBorder(new EmptyBorder(12, 12, 12, 12));

		topPanel.add(back);
		topPanel.add(forward);
		topPanel.add(index);
		topPanel.add(addressLabel);
		topPanel.add(address);
		getContentPane().add(scrollPane);
		getContentPane().add(topPanel, BorderLayout.NORTH);
		scrollPane.setViewportView(htmlView);

		htmlView.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					goToAbsolutePage(e.getURL());
				}
			}

		});
		back.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				back();
			}
		});
		forward.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				forward();
			}
		});
		index.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				goToPage(INDEX_PAGE);
			}
		});
		address.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				goToAbsolutePage(address.getText());
			}
		});

		back.setEnabled(false);
		forward.setEnabled(false);
	}

	public void goToPage(String page) {
		String absolutePage = BASE + page;

		//Class.getResource cannot cope with anchors
		int anchorSeparator = absolutePage.indexOf('#');
		URL url;
		if (anchorSeparator != -1) {
			String resource = absolutePage.substring(0, anchorSeparator);
			String anchor = absolutePage.substring(anchorSeparator);
			try {
				url = new URL(HelpWindow.class.getResource(resource) + anchor);
			} catch (MalformedURLException exc) {
				url = null;
			}
		} else {
			url = HelpWindow.class.getResource(absolutePage);
		}

		if (url != null) {
			goToAbsolutePage(url);
		} else {
			htmlView.setEditorKit(htmlView.getEditorKitForContentType("text/plain"));
			htmlView.setDocument(htmlView.getEditorKit().createDefaultDocument());
			htmlView.setText("The help pages cannot be found. You might try to reinstall FrEAK.\n\nMissing Page was:\n" + absolutePage);
		}
	}

	private void forward() {
		backBuffer.push(currentURL);
		currentURL = (URL)forwardBuffer.pop();

		displayPage();
	}

	private void back() {
		forwardBuffer.push(currentURL);
		currentURL = (URL)backBuffer.pop();

		displayPage();
	}

	private void goToAbsolutePage(String page) {
		try {
			URL url = new URL(page);
			goToAbsolutePage(url);
		} catch (MalformedURLException exc) {
			htmlView.setEditorKit(htmlView.getEditorKitForContentType("text/plain"));
			htmlView.setDocument(htmlView.getEditorKit().createDefaultDocument());
			htmlView.setText("Malformed URL:\n" + page);
		}
	}

	private void goToAbsolutePage(URL url) {
		if (currentURL != null && !currentURL.equals(url)) {
			backBuffer.push(currentURL);
			forwardBuffer.clear();
		}
		currentURL = url;

		displayPage();
	}

	private void displayPage() {
		address.setText(String.valueOf(currentURL));
		try {
			htmlView.setPage(currentURL);
		} catch (IOException exc) {
			htmlView.setEditorKit(htmlView.getEditorKitForContentType("text/plain"));
			htmlView.setDocument(htmlView.getEditorKit().createDefaultDocument());
			htmlView.setText("Unavailable Page:\n" + currentURL);
		}
		checkEnable();
	}

	private void checkEnable() {
		forward.setEnabled(!forwardBuffer.isEmpty());
		back.setEnabled(!backBuffer.isEmpty());
	}
}
