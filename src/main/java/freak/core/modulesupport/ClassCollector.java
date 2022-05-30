/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: $Date: 2007/12/11 17:32:08 $
 */

package freak.core.modulesupport;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.security.CodeSource;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import freak.core.graph.Operator;
import freak.core.searchspace.SearchSpace;

/**
 * The class collector provides functions to collects classes of the modules 
 * in FrEAK. 
 *
 * @author Heiko, Patrick, Dirk, Michael, Robin
 */
public class ClassCollector {
	
	private static String startedFrom=null;

	private static Collection jarFreakClassFilesBuffer;

	/**
	 * A file filter which accepts all files with suffix .class.
	 */
	public static class ClassFileFilter implements FileFilter {
		public boolean accept(File pathname) {
			return pathname.getName().endsWith(".class");
		}
	}

	/**
	 * A file filter which accepts all file objects which are directories.
	 */
	public class FolderFileFilter implements FileFilter {
		public boolean accept(File pathname) {
			return pathname.isDirectory();
		}
	}

	/**
	 * A file filter which accepts all folders and files with suffix .class.
	 */
	public static class FolderClassFileFilter implements FileFilter {
		public boolean accept(File pathname) {
			if (pathname.isDirectory()) {
				return true;
			}
			return pathname.getAbsolutePath().endsWith(".class");
		}
	}

	/**
	 * This method returns a list of all class paths.
	 * @return a list of all class paths.
	 */
	public static String[] getClassPaths() {
		return System.getProperty("java.class.path").split(System.getProperty("path.separator"));
	}

	/*
	 * Buffers classes in Jarfile for performance
	 */
	private static Collection getJarFreakModuleClassnames(String jarfile) {
		if (jarFreakClassFilesBuffer == null) {
			jarFreakClassFilesBuffer = new LinkedList();
			try {
				JarFile jar = new JarFile(jarfile);
				// get list of files in zipfile
				Enumeration zipfile = jar.entries();

				while (zipfile.hasMoreElements()) {
					String filename = zipfile.nextElement().toString();
					if (filename.length() > 6 && filename.startsWith("freak/") && filename.endsWith(".class")) {
						jarFreakClassFilesBuffer.add(filename.substring(0, filename.length() - 6).replace('/', '.'));
					}
				}
			} catch (java.io.IOException e) {
				e.printStackTrace();
			}
		}
		return jarFreakClassFilesBuffer;
	}

	private static void addClassesInJARFile(String jarfile, String prefix, Collection classes, Class lookFor, boolean recursive) {
		Collection jarContent = getJarFreakModuleClassnames(jarfile);
		Iterator it = jarContent.iterator();
		while (it.hasNext()) {
			String classname = (String)it.next();

			// Filter files in prefix folder
			if (classname.length() > prefix.length() && classname.startsWith(prefix, 0) && !(classname.startsWith("freak.gui"))) {
				
				boolean filematch = recursive;
				// if not recursive the string without path should not have seperators
				if (!recursive) {
					String nameWithoutPath = classname.substring(prefix.length());
					if (nameWithoutPath.indexOf('.') == -1)
						filematch = true;
				}

				if (filematch) {
					try {
						Class c = Class.forName(classname);
						if ((lookFor.isAssignableFrom(c)) && (!Modifier.isAbstract(c.getModifiers()))) {
							classes.add(c);
						}
					} catch (ClassNotFoundException e) {
						System.err.println("The class " + classname + " couldn't be found.!");
						e.printStackTrace();
					}
				}
			}
		}
	}

	private static void addClassesInFolder(File folder, String prefix, Collection classes, Class lookFor, boolean recursive) {
		// we are just interested in folders and files with suffix .class
		File[] files;
		if (recursive) {
			files = folder.listFiles(new FolderClassFileFilter());
		} else {
			files = folder.listFiles(new ClassFileFilter());
		}
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				String s = prefix + files[i].getName() + ".";
				addClassesInFolder(files[i], s, classes, lookFor, recursive);
			} else {
				// the suffix is cut off
				String s = files[i].getName();
				s = s.substring(0, s.length() - 6);
				try {
					Class c = Class.forName(prefix + s);
					if ((lookFor.isAssignableFrom(c)) && (!Modifier.isAbstract(c.getModifiers()))) {
						classes.add(c);
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * This method inspects the package module.<code>folder</code>. <code>
	 * lookFor </code> is either a class or an interface. All classes in this
	 * package are investigated and it is checked whether they implement <code>
	 * lookFor </code> (whether they are subclasses of <code> lookFor </code>
	 * respectively). All classes which fulfill this requirement are returned.
	 * @param folder the folder to be inspected.
	 * @param lookFor the class or interface to be looked for.
	 * @return a vector of classes which implement or extend <code> lookFor </code>.
	 */
	Collection getClasses(String folder, Class lookFor, boolean recursive) {
		// the classes which are found are saved in the following vector
		Collection classes = new Vector();

		String prefix = "freak.module." + folder + ".";

		String[] classpaths = getClassPaths();

		// check jar file
		// on Mac OS X the classpaths.length is 2 if started from a jar file, because
		// /System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Classes/.compatibility/14compatibility.jar
		// is part of the classpath
		// if (classpaths.length <= 2 && classpaths[0].length() > 4 && classpaths[0].toLowerCase().endsWith(".jar")) {
		// NEW CHECK
		if (getStartedFrom().toLowerCase().endsWith(".jar")) {
			//Only parse JAR files if FrEAK is started from the only one.
			addClassesInJARFile(getStartedFrom(), prefix, classes, lookFor, recursive);

			//now look in the resource directory
			String resDir = getStartedFrom().substring(0, getStartedFrom().lastIndexOf("/") + 1) + "resource";
			classpaths = new String[] { resDir };
		}

		// we check for all class paths whether they contain the package freak
		for (int cp = 0; cp < classpaths.length; cp++) {
			// we check the the specified folder and all its subfolders
			File moduleFolder = new File(classpaths[cp] + "/freak/module");
			// the folder to be inspected
			String folderName = folder.replace('.', '/');
			File inspectedFolder = new File(moduleFolder, folderName);
			// if the folder doesn't exist we try the next one
			if (inspectedFolder.exists()) {
				addClassesInFolder(inspectedFolder, prefix, classes, lookFor, recursive);
			}
		}
		return classes;
	}

	private SortedMap getGroupedOperatorClassesInJARFile(SearchSpace searchspace, String jarfile) {
		Collection classnames = getJarFreakModuleClassnames(jarfile);
		//prepare string for matchings
		String searchspaceString = searchspace.getClass().getName();
		searchspaceString = searchspaceString.substring(searchspaceString.lastIndexOf(".") + 1).toLowerCase();

		// for better performance compile pattern only once
		Pattern searchspacePattern = Pattern.compile("freak\\.module\\.operator\\.[a-z]*\\." + searchspaceString + "\\.[a-zA-Z\\.]*");
		Pattern commonPattern = Pattern.compile("freak\\.module\\.operator\\.[a-z]*\\.common\\.[a-zA-Z\\.]*");
		Pattern miscPattern = Pattern.compile("freak\\.module\\.operator\\.[a-z]*\\.[a-zA-Z]*");
	
		// create groups for operators
		SortedMap groups = new TreeMap();

		Iterator it = classnames.iterator();
		while (it.hasNext()) {
			String classname = (String)it.next();
			if (searchspacePattern.matcher(classname).matches() || commonPattern.matcher(classname).matches() || miscPattern.matcher(classname).matches()) {
				//get operator type by directory (mutation, crossover, ...)
				//freak.module.operator.[operatortype].
				String operatorType = classname.substring(22, classname.indexOf('.', 22));

				try {
					Class c = Class.forName(classname);

					addOperatorToGroups(c, groups, operatorType);
				} catch (ClassNotFoundException e) {
					System.err.println("The class " + classname + " couldn't be found!");
					e.printStackTrace();
				}
			}
		}
		return groups;
	}

	/**
	 * @param classname is the name of the class to be checked an added. 
	 * @param groups is a SortedMap using operator types as keys and containing lists of operators of the corresponding operator types
	 */
	private void addOperatorToGroups(Class c, SortedMap groups, String key) {
		if ((Operator.class.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers()))) {

			//search a group with the same operator type/directory
			if (!groups.containsKey(key)) {
				// create list for new type of operator
				groups.put(key, new LinkedList());
			}
			// add class to the list corresponding to the operator type
			List list = (List)groups.get(key);
			list.add(c);
		}
	}

	/**
	 * @return a map of lists with entries for each operator type containing classes of operators belonging to the specified search space.
	 */
	SortedMap getGroupedOperatorClasses(SearchSpace searchspace) {
		String[] classpaths = getClassPaths();
		// if started from the jar file
		// on Mac OS X the classpaths.length is 2 if started from a jar file, because
		// /System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Classes/.compatibility/14compatibility.jar
		// is part of the classpath
		// if (classpaths.length <= 2 && classpaths[0].length() > 4 && classpaths[0].toLowerCase().endsWith(".jar")) {
		// NEW CHECK
		if (getStartedFrom().toLowerCase().endsWith(".jar")) {
			return getGroupedOperatorClassesInJARFile(searchspace, getStartedFrom());
		}
		
		// create groups
		SortedMap groups = new TreeMap();
		
		// we check for all class paths whether they contain the package freak
		for (int cp = 0; cp < classpaths.length; cp++) {
			File moduleFolder = new File(classpaths[cp] + "/freak/module");
			// We check all subdirectories of operator if they contain operators
			// which work on the search space searchspace.
			File inspectedFolder = new File(moduleFolder, "operator");
			if (inspectedFolder.exists() && inspectedFolder.isDirectory()) {
				File[] files = inspectedFolder.listFiles(new FolderFileFilter());
				for (int i = 0; i < files.length; i++) {
					File opFolder = new File(inspectedFolder, files[i].getName());
					// check subdirectory
					if (opFolder.exists() && opFolder.isDirectory()) {
						LinkedList classes = new LinkedList();
						
						// There are three different locations where operators can be found.
						// in the given subdirectory, e.g. split.RandomSplit
						addClassesInFolder(opFolder, "freak.module.operator." + files[i].getName() + ".", classes, Operator.class, false);
						
						// in the common folder, e.g. crossover.common.OrderCrossover
						File commonFolder = new File(opFolder, "common");
						if (commonFolder.exists() && commonFolder.isDirectory()) {
							addClassesInFolder(commonFolder, "freak.module.operator." + files[i].getName() + ".common.", classes, Operator.class, true);
						}
						
						// in the folder belonging to the search space e.g.
						// mutation.cycle.KOpt
						String searchspaceString = searchspace.getClass().getName();
						searchspaceString = searchspaceString.substring(searchspaceString.lastIndexOf(".") + 1).toLowerCase();
						File searchspaceFolder = new File(opFolder, searchspaceString);
						if (searchspaceFolder.exists() && searchspaceFolder.isDirectory()) {
							addClassesInFolder(searchspaceFolder, "freak.module.operator." + files[i].getName() + "." + searchspaceString + ".", classes, Operator.class, true);
						}
						
						for (Iterator iter = classes.iterator(); iter.hasNext();) {
							addOperatorToGroups((Class)iter.next(), groups, files[i].getName());
						}
					}
				}
			}
		}
		return groups;
	}

	/**
	 * This method inspects all classes in the package freak and in one of its
	 * subpackages. A list of all classes which implement the interface <code>
	 * lookFor</code> or extend the class <code>lookFor</code> respectively is
	 * returned.
	 * @param lookFor the class or interface to be looked for.
	 * @return a vector of classes which implement or extend <code>lookFor</code>.
	 */
	public static Class[] getAllImplementors(Class lookFor) {
		Vector classes = new Vector();
		String[] classpaths = getClassPaths();

		// on Mac OS X the classpaths.length is 2 if started from a jar file, because
		// /System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Classes/.compatibility/14compatibility.jar
		// is part of the classpath
		// if (classpaths.length <= 2 && classpaths[0].length() > 4 && classpaths[0].toLowerCase().endsWith(".jar")) {
		// NEW CHECK
		if (getStartedFrom().toLowerCase().endsWith(".jar")) {

			//Only parse JAR files if FrEAK is started from the only one.
			addClassesInJARFile(getStartedFrom(), "freak.", classes, lookFor, true);

			//now look in the resource directory
			String resDir = getStartedFrom().substring(0, getStartedFrom().lastIndexOf("/") + 1) + "resource";
			classpaths = new String[] { resDir };
		}

		for (int cp = 0; cp < classpaths.length; cp++) {
			File freakFolder = new File(classpaths[cp] + "/freak");
			String prefix = "freak.";
			// if the folder doesn't exist we try the next one
			if (freakFolder.exists()) {
				addClassesInFolder(freakFolder, prefix, classes, lookFor, true);
			}
		}
		Class[] result = new Class[classes.size()];
		for (int i = 0; i < classes.size(); i++) {
			result[i] = (Class)classes.elementAt(i);
		}
		return result;
	}

	/**
	 * @return the startIdentifier
	 */
	public static String getStartedFrom() {
		if (startedFrom==null) {
		     try
	         {
		    	 Class qc = freak.Freak.class;
		    	 CodeSource source = qc.getProtectionDomain().getCodeSource();
		    	 if ( source != null ) startedFrom = source.getLocation().toURI().toString().substring(5);
	         }
		     catch ( Exception e ) {}
		}
		return startedFrom;
	}

}