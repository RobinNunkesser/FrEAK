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

import java.awt.Frame;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jgpd.UI.utils.FileWizardEditor;
import org.jgpd.io.utils.FilePackage;

public class File_PackageWizard extends WizardComponentWhole
{
	protected JFileChooser fileChooserRootDir;
	protected JFileChooser fileChooserTarget;
	protected FileWizardEditor editor = null;

	public File_PackageWizard()
	{
		super();
	}

	public File_PackageWizard(	Frame	frame,
								String 	displayTitle)
	{
		super(frame, displayTitle);
	}

	public File_PackageWizard(	Frame	frame,
								String 	displayTitle,
								int		numParts)
	{
		super(frame,
			  displayTitle,
			  numParts);
	}

	public File_PackageWizard(	Frame	frame,
								String 	displayTitle,
								int		width,
								int		height,
								int		numParts,
								FileWizardEditor ed)
	{
		super(frame,
			  displayTitle,
			  width,
			  height,
			  numParts);
		editor = ed; // FIXME this over-coupled
	}

	protected void wizardPartImpl()
	{
		JPanel textPanel1 = new JPanel();
		JLabel textLabelA = new JLabel("In order to select the correct file and indentify");
		JLabel textLabelB = new JLabel("it's package correctly you will be asked to select");
		JLabel textLabelC = new JLabel("the root directory that contains the top level");
		JLabel textLabelD = new JLabel("package of the file.");;
		JLabel textLabelE = new JLabel("For example if the file lies under");
		JLabel textLabelF = new JLabel("/home/jgpd_user/src/org/jgpd/file.txt");
		JLabel textLabelG = new JLabel("and the package of the file is org.jgpd");;
		JLabel textLabelH = new JLabel("on the next screen select /home/jgpd_user/src");
		JLabel textLabelI = new JLabel("as the root directory and then select the actual");
		JLabel textLabelJ = new JLabel("file on the second file selection display.");;
		textPanel1.add(textLabelA);
		textPanel1.add(textLabelB);
		textPanel1.add(textLabelC);
		textPanel1.add(textLabelD);
		textPanel1.add(textLabelE);
		textPanel1.add(textLabelF);
		textPanel1.add(textLabelG);
		textPanel1.add(textLabelH);
		textPanel1.add(textLabelI);
		textPanel1.add(textLabelJ);
		addPanel(textPanel1);
		
		JPanel filePanel1 = new JPanel();
		fileChooserRootDir = new JFileChooser();
		// The open and cancel buttons in the file chooser look stupid in a wizard
		fileChooserRootDir.setControlButtonsAreShown(false);
		filePanel1.add(fileChooserRootDir);
		addPanel(filePanel1);
		
		JPanel textPanel2 = new JPanel();
		JLabel textLabel2 = new JLabel();
		textLabel2.setText("In the next page select the actual file");
		textPanel2.add(textLabel2);
		addPanel(textPanel2);
		
		JPanel filePanel2 = new JPanel();
		fileChooserTarget = new JFileChooser();
		// The open and cancel buttons in the file chooser look stupid in a wizard
		fileChooserTarget.setControlButtonsAreShown(false);
		filePanel2.add(fileChooserTarget);
		addPanel(filePanel2);
		
		JPanel textPanel3 = new JPanel();
		JLabel textLabel3A = new JLabel("Select Finish to use this file");
		
		textPanel3.add(textLabel3A);
		addPanel(textPanel3);
	}

	protected void finish()
	{
		// return file information
		File rootDir = fileChooserRootDir.getCurrentDirectory();
		File target = fileChooserTarget.getSelectedFile();
		
		//Validation
		if (target == null)
		{
			// No file was choosen
			JOptionPane.showMessageDialog(this,
					                      "You have not selected a file",
										  "File Selection error",
										  JOptionPane.ERROR_MESSAGE);
			removePanel(currentSelection);
			currentSelection = 3;
			showPanel();
			updateNavButtons();
			return;
		}
		FilePackage filePack = new FilePackage(rootDir, target);
		if (filePack.isTargetUnderRoot())
		{
			if (editor != null)
			{
				editor.setCurrentFile(filePack);
			}
			super.finish();
		}
		else
		{
			JOptionPane.showMessageDialog(this,
					"The file does not lie under the directory selected",
					"File-directory mismatch",
					JOptionPane.ERROR_MESSAGE);
			removePanel(currentSelection);
			currentSelection = 1;
			showPanel();
			updateNavButtons();
			return;
		}
	}
	
	protected void next()
	{
		if ( currentSelection == 1 )
		{
			// If the root directory has been updated, set the
			// start of navigation in the file select pane to that point
			// But only if a file has not already been selected
			if ( fileChooserTarget.getSelectedFile() == null )
			{
				fileChooserTarget.setCurrentDirectory(fileChooserRootDir.getCurrentDirectory());
			}
		}
		super.next();
	}
}
	