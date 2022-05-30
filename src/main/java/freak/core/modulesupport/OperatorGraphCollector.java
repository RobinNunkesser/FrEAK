/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.core.modulesupport;

import freak.core.control.*;
import freak.core.graph.*;
import freak.gui.graph.*;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Collects the predefined operator graphs.
 * @author  Heiko, Michael
 */
public class OperatorGraphCollector {

	private Schedule schedule;

	/**
	 * A file filter which accepts all files with suffix .fop.
	 */
	private class GraphFileFilter implements FileFilter {
		public boolean accept(File pathname) {
			//return false;
			return pathname.getAbsolutePath().endsWith(".fop");
		}
	}

	public OperatorGraphCollector(Schedule schedule) {
		this.schedule = schedule;
	}

	/**
	 * This method returns a list of all class paths.
	 *
	 * @return a list of all class paths.
	 */
	private String[] getClassPaths() {
		return System.getProperty("java.class.path").split(System.getProperty("path.separator"));
	}

	private boolean isCorrectOperatorGraph(File file) {
		try {
			return isCorrectOperatorGraph(new FileInputStream(file));
		} catch (Exception exc) {
			return false;
		}
	}

	private boolean isCorrectOperatorGraph(InputStream fis) {
		try {
			OperatorGraphFile ogFile = OperatorGraphFile.read(fis);
			FreakGraphModel model = ogFile.generateGraph(schedule);
			model.getOperatorGraph().testSchedule(schedule);
			model.getOperatorGraph().removeFromEventController();
		} catch (Exception exc) {
			return false;
		}
		return true;
	}
        
  /**
   * @param path Should end with "/", but not start with one.
   */
  private void getResourceListing(ArrayList infos, String lookFor, String path)  {
      try {
      URL dirURL = this.getClass().getClassLoader().getResource(path);
      if (dirURL != null && dirURL.getProtocol().equals("file")) {
          File[] allFiles = new File(dirURL.toURI()).listFiles(new GraphFileFilter());
		if (allFiles != null) {
			for (int i = 0; i < allFiles.length; i++) {
				if ((lookFor == null) || (lookFor.equals(allFiles[i].getName()))) {
					if (isCorrectOperatorGraph(allFiles[i])) {
                                                ModuleInfo info = new ModuleInfo("", "", allFiles[i].getAbsolutePath());
						String name = allFiles[i].getName().substring(0, allFiles[i].getName().indexOf('.'));
						info.setName(name);
                                                System.out.println("Adding " + name);
						infos.add(info);
					}
				}
			}
		}
      } 

      if (dirURL == null) {
        String me = this.getClass().getName().replace(".", "/")+".class";
        dirURL = this.getClass().getClassLoader().getResource(me);
      }
      
      if (dirURL.getProtocol().equals("jar")) {
        String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
        JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
        Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
        Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
        while(entries.hasMoreElements()) {
          String name = entries.nextElement().getName();          
          if (name.startsWith(path) && name.endsWith(".fop")) { //filter according to the path
              if ((lookFor == null) || (name.endsWith(lookFor))) {                                                      
                  if (isCorrectOperatorGraph(jar.getInputStream(jar.getJarEntry(name)))) {
                ModuleInfo info = new ModuleInfo("", "", name);
                name = name.substring(path.length());
                info.setName(name.substring(0,name.indexOf('.')));
                infos.add(info);}
              }
          }
        }      
      } 
        
      } catch (Exception ex) {
          Logger.getLogger(OperatorGraphCollector.class.getName()).log(Level.SEVERE, null, ex);
      }
  }

	/**
	 * This method returns a list of all predefined graphs which are compatible
	 * with the environment chosen so far. If lookFor is not null, only graphs
	 * are returned whose names equal lookFor. 
	 * @param lookFor the graph to be looked for, null if all compatible graphs should be returned. 
	 * @return the list of graphs
	 */
	public ModuleInfo[] getPredefinedGraphs(String lookFor) {
		ArrayList infos = new ArrayList();
                getResourceListing(infos, lookFor, "freak/module/graph/common/");
                String searchspaceString = schedule.getGenotypeSearchSpace().getClass().getName();			
                searchspaceString = searchspaceString.substring(searchspaceString.lastIndexOf(".") + 1).toLowerCase();
                getResourceListing(infos, lookFor, "freak/module/graph/"+searchspaceString+"/");
		Object[] o = infos.toArray();
		java.util.Arrays.sort(o);
		ModuleInfo[] mi = new ModuleInfo[o.length];
		for (int i = 0; i < o.length; i++) {
			mi[i] = (ModuleInfo)o[i];
		}
		return mi;
	}

}
