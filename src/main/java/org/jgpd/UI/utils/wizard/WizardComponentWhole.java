/*
 * 
 *
 * Copyright (C) 2003-2004 David Benson
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

package org.jgpd.UI.utils.wizard;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class WizardComponentWhole extends JDialog
{
	protected Vector wizardPages = new Vector(); // the wizard component parts collection
	protected int currentSelection = 0;
	protected WizardNavPanel navPanel = null;
	
	public WizardComponentWhole()
	{
		this(null, new String(" "), 700, 400, 1);
	}

	public WizardComponentWhole(Frame  frame,
								String 	displayTitle)
	{
		this(frame, displayTitle, 700, 400, 1);
	}

	public WizardComponentWhole(Frame   frame,
								String 	displayTitle,
								int		numParts)
	{
		this(frame, displayTitle, 700, 400, numParts);
	}

	public WizardComponentWhole(Frame   frame,
								String 	displayTitle,
								int		width,
								int		height,
								int		numParts)
	{
		super(frame, displayTitle, true); // modal
		Container fContentPane = this.getContentPane();
		wizardPartImpl();
		
		setSize(width,height);
		fContentPane.setLayout(new BorderLayout());
		navPanel = new WizardNavPanel(this);
		fContentPane.add(navPanel, BorderLayout.SOUTH);
		showPanel();
	}
	
	protected void addPanel(JPanel panel)
	{
		wizardPages.addElement(panel);
	}
	
	protected int getNumPanels()
	{
		return wizardPages.size();
	}
	
	protected void wizardPartImpl()
	{
		// FIXME, flag an exception if I enter this function.
	}
	
    protected void removePanel(int panelNum)
	{
    	JPanel panel = (JPanel)wizardPages.get(panelNum);
    	this.remove(panel);
    	
    }

	protected void showPanel()
	{
		JPanel panel = (JPanel)wizardPages.get(currentSelection);
		panel.setVisible(true);
		
		this.getContentPane().add(panel,BorderLayout.CENTER);
		this.validate();
		this.repaint();
	}
	
	protected void finish()
	{
		setVisible(false);
		this.dispose();
	}
	
	protected void cancel()
	{
		setVisible(false);
		this.dispose();
	}
	
	protected void next()
	{
		removePanel(currentSelection++);
		showPanel();
	}
	
	protected void previous()
	{
		removePanel(currentSelection--);
		showPanel();
	}
	
	protected boolean isFirstPanel()
	{
		if (currentSelection == 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	protected boolean isLastPanel()
	{
		if (currentSelection == (wizardPages.size())-1)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	protected void updateNavButtons()
	{
		navPanel.updateButtons();
	}

	protected class WizardNavPanel extends JPanel implements ActionListener
	{
		private JButton nextButton = new JButton();
		private JButton prevButton = new JButton();
		private JButton cancelButton = new JButton();
		private JButton finishButton = new JButton();
		private WizardComponentWhole wizardWhole = null;

		public WizardNavPanel(WizardComponentWhole whole)
		{
			super();
			this.setSize(400,100);
			setupButtons();
			wizardWhole = whole;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (e.getActionCommand().equals("Cancel"))
			{
				wizardWhole.cancel();
			}
			else if (e.getActionCommand().equals("Finish"))
			{
				wizardWhole.finish();
			} 
			else if (e.getActionCommand().equals("Next"))
			{
				wizardWhole.next();
				updateButtons();
			}
			else if (e.getActionCommand().equals("Previous"))
			{
				wizardWhole.previous();
				updateButtons();
			}
		}
		
		public void updateButtons()
		{
			if (wizardWhole.isFirstPanel())
			{
				prevButton.setEnabled(false);
			}
			else
			{
				prevButton.setEnabled(true);
			}
			if (wizardWhole.isLastPanel())
			{
				nextButton.setEnabled(false);
				finishButton.setEnabled(true);
			}
			else
			{
				nextButton.setEnabled(true);
				finishButton.setEnabled(false);
			}
		}
		
		protected void setupButtons()
		{
			this.setLayout(new FlowLayout());
			
			Insets insets = new Insets( 0, 0, 0, 0 );
			GridBagConstraints constraints = new GridBagConstraints( 0, 0, 0, 0, 0.0, 0.0,
					GridBagConstraints.WEST,
					GridBagConstraints.NONE,
					insets, 0, 0 );
			
			prevButton.setText("Previous");
			prevButton.setActionCommand("Previous");
			prevButton.addActionListener(this);
			this.add(prevButton);
			prevButton.setEnabled(false);
			
			
			insets = new Insets( 10, 35, 10, 20 );
			GridBagConstraints constraints1 = new GridBagConstraints( 0, 1, 0, 0, 0.0, 0.0,
					GridBagConstraints.WEST,
					GridBagConstraints.NONE,
					insets, 0, 0 );
			nextButton.setText("Next");
			nextButton.setActionCommand("Next");
			nextButton.addActionListener(this);
			this.add(nextButton);
			
			GridBagConstraints constraints2 = new GridBagConstraints( 2, 0, 0, 0, 0.0, 0.0,
					GridBagConstraints.WEST,
					GridBagConstraints.NONE,
					insets, 0, 0 );
			finishButton.setText("Finish");
			finishButton.setActionCommand("Finish");
			finishButton.addActionListener(this);
			this.add(finishButton);
			finishButton.setEnabled(false);
			
			GridBagConstraints constraints3 = new GridBagConstraints( 3, 0, 0, 0, 0.0, 0.0,
					GridBagConstraints.WEST,
					GridBagConstraints.NONE, insets, 0, 0 );
			cancelButton.setText("Cancel");
			cancelButton.setActionCommand("Cancel");
			cancelButton.addActionListener(this);
			this.add(cancelButton);
		}
	}
}