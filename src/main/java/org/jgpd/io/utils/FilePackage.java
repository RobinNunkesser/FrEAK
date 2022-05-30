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

package org.jgpd.io.utils;

import java.io.File;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class FilePackage
{
	protected File rootDir = null;         // Directory containing top of package tree
	protected File target = null;		   // actual target file
	protected String packName = new String("Not defined");      // package of file
	protected String description = null;
	
	public FilePackage(File root, File tar)
	{
		rootDir = root;
		target = tar;

		if ( (rootDir != null) && (target != null) )
		{
			calculatePackage();
		}
	}

	public FilePackage(Node node)
	{
		// Get attributes
		NamedNodeMap attrMap = node.getAttributes();
		Node nameNode = attrMap.getNamedItem("description");
		if (nameNode != null )
		{
			String name = nameNode.getNodeValue();
			setDescription(name);
		}

		nameNode = attrMap.getNamedItem("root");
		if (nameNode != null )
		{
			String rt = nameNode.getNodeValue();
			setRootDir(new File(rt));
		}
	
		nameNode = attrMap.getNamedItem("target");
		if (nameNode != null )
		{
			String tar = nameNode.getNodeValue();
			setTarget(new File(tar));
		}
		
		if ( (rootDir != null) && (target != null) )
		{
			calculatePackage();
		}
	}

	private void calculatePackage()
	{
		// The package consist of the target file string, minus the prefix
		// of the rootDir string, minus the file name itself.

		// why doesn't String.replace() work?
		if ( isTargetUnderRoot() )
		{
			String dir = rootDir.getAbsolutePath();
			String tar = target.getAbsolutePath();
			int dirLength = dir.length();
			int targetLength = tar.length();

			// Get the relative path from the root directory to the file
			packName = tar.substring( (dirLength+1), targetLength );
			int packLen = packName.length();
			int sepLength = (File.separator).length();  // string size of system separator
			int separatorPos = packName.indexOf(File.separator);
			
			while (separatorPos != -1)
			{
				String firstHalf = packName.substring( 0, separatorPos );
				String secondHalf = packName.substring( separatorPos + sepLength , packLen );
				packName = firstHalf + "." + secondHalf;
				packLen = packName.length();
				separatorPos = packName.indexOf(File.separator);
			}
		}
	}

	public boolean isTargetUnderRoot()
	{
		if ( (rootDir != null) && (target != null) )
		{
			String dir = rootDir.getAbsolutePath();
			String tar = target.getAbsolutePath();

			int dirLength = dir.length();
			int targetLength = tar.length();

			if ( targetLength > dirLength )
			{
				// Absolute path must be at least 2 characters longer than dir
				String prefixTarget = tar.substring( 0, dirLength );
				if ( prefixTarget.equals(dir) )
				{
					return true;
				}
				else
				{
					return false;
				}
			}
			else
			{
				return false;
			}
		}
		else
		{
			// Variables not created just return false
			return false;
		}
	}

    public String writeXML(String indent)
    {
    	String xml = indent + "<FilePackage "; 
    	
        if ( description != null )
        {
			xml += "description=\"" +
    		getDescription() +
    		"\" ";
        }
    	
    	if ( rootDir != null )
    	{
			xml += "root=\"" + getRootDir().getAbsolutePath() +
			"\" ";
    	}
    		
		if ( target != null )
		{
			xml += "target=\"" + getTarget().getAbsolutePath() +
			"\" ";
		} 	
    	
		xml += "/>\n";

    	return xml;
    }

	public File getRootDir()
	{
		return rootDir;
	}

	public void setRootDir(File dir)
	{
		this.rootDir = dir;
		calculatePackage();
	}

	public File getTarget()
	{
		return target;
	}

	public void setTarget(File tar)
	{
		this.target = tar;
		calculatePackage();
	}

	public String getPackage()
	{
		return packName;
	}

	public String toString()
	{
		return packName;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public void setDescription(String desc)
	{
		description = desc;
	}

}
