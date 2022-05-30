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

import freak.core.control.Schedule;
import freak.core.fitness.FitnessFunction;
import freak.core.fitness.FitnessTransformer;
import freak.core.graph.CompatibleWithDifferentSearchSpaces;
import freak.core.graph.Finish;
import freak.core.graph.Initialization;
import freak.core.graph.Operator;
import freak.core.graph.Start;
import freak.core.mapper.Mapper;
import freak.core.observer.Observer;
import freak.core.parametercontroller.ParameterController;
import freak.core.populationmanager.PopulationManager;
import freak.core.postprocessor.Postprocessor;
import freak.core.searchspace.SearchSpace;
import freak.core.stoppingcriterion.StoppingCriterion;
import freak.core.view.View;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * The module collector collects data about the modules in FrEAK and  provides methods to instantiate all available modules of various types. 
 * @author  Heiko, Patrick, Dirk, Michael
 */
public class ModuleCollector extends ClassCollector {

	private Schedule schedule;

	/**
	 * Creates a new <code>ModuleCollector</code>.
	 * @param schedule a backlink to a schedule object.
	 */
	public ModuleCollector(Schedule schedule) {
		this.schedule = schedule;
	}

	/**
	 * This method takes a class and a list of parameters as input and tries to
	 * create a new object of this class by calling the constructor with the
	 * given parameters.
	 * @param c the class to be instantiated.
	 * @param param the parameters with which the constructor is to be called.
	 * @return the created object of class c (null if the creation failed).
	 * @throws UnsupportedEnvironmentException if the module doesn't fit into the current schedule.
	 */
	public Module newModule(Class c, Object[] param) throws UnsupportedEnvironmentException, InvocationTargetException {
		Constructor[] constr = c.getConstructors();
		Module module = null;
		// We try to create a new module by calling the available constructors
		// until we have found a constructor working properly with the
		// parameters specified in param.
		for (int i = 0; i < constr.length; i++) {
			try {
				module = (Module)constr[i].newInstance(param);
				// test if the module can be used in the current schedule
				module.testSchedule(schedule);
				// call initialize
				module.initialize();
				return module;
			} catch (IllegalArgumentException e) {
				// We have called the wrong constructor.
				// Let's try the next one...
			} catch (InvocationTargetException e) {
				// The constructor has the expected signature. But it caused an
				// exception.
				if ((e.getTargetException() != null) && (e.getTargetException() instanceof UnsupportedEnvironmentException)) {
					// The module isn't compatible with the environment chosen
					// so far.
					throw new UnsupportedEnvironmentException(c.toString());
				}
				// no UnsupportedEnvironmentException, so we forward the
				// original exception
				throw e;
			} catch (InstantiationException e) {
				// tried to instantiate an abstract class
				return null;
			} catch (UnsupportedEnvironmentException e) {
				throw e;
			} catch (Exception e) {
				// The constructor has the expected signature. But it doesn't
				// work properly.
				System.err.println("The constructor of " + c.getName() + " doesn't work properly.\n" + e.toString());
				e.printStackTrace();
			}
		}
		System.err.println("->The class " + c.getName() + " doesn't have a constructor with the expected signature!");
		return null;
	}

	/**
	 * This method gets a <code>String</code> and a list of parameters as
	 * input. It tries to load the class specified by the <code>String
	 * </code> and to create a new object of this class by calling <code>
	 * newModule(Class, Object[]) </code>.
	 * @param mod the <code>String</code> which contains the class to be instantiated.
	 * @param param the parameters with which the constructor is to be called.
	 * @param sesp the currently chosen search space
	 * @return the created object (null if the creation failed).
	 */
	public Module newModuleByString(String mod, Object[] param, SearchSpace sesp) throws UnsupportedEnvironmentException, InvocationTargetException {
		Class c = null;
		// We try to get the class specified by mi. If we succeed we call
		// newModule(Class, Object[]) to create a new object of this class.

		try {
			c = Class.forName(mod);
		} catch (ClassNotFoundException e) {
		}
		if (c == null) {
			return null;
		}

		Module module = newModule(c, param);

		if (!(module instanceof CompatibleWithDifferentSearchSpaces)) {
			// The module doesn't implement CompatibleWithDifferentSearchSpaces.
			// We have to check whether it lies in a package which indicates
			// that it supports the current search space.
			String s = sesp.getClass().getName().toLowerCase();
			s = s.substring(s.lastIndexOf(".") + 1);
			int pos = mod.indexOf(s);
			if (pos == -1) {
				throw new UnsupportedEnvironmentException(module.getName() + " is not compatible with the search space " + sesp.getName() + ".");
			}
		}
		return module;
	}

	/**
	 * This method gets a <code> ModuleInfo </code> and a list of parameters as
	 * input. It tries to load the class specified by the <code> ModuleInfo
	 * </code> and to create a new object of this class by calling <code>
	 * newModule(Class, Object[]) </code>.
	 * @param mi the <code> ModuleInfo </code> which contains the class to be instantiated.
	 * @param param the parameters with which the constructor is to be called.
	 * @return the created object (null if the creation failed).
	 */
	private Module newModule(ModuleInfo mi, Object[] param) throws UnsupportedEnvironmentException, InvocationTargetException {
		Class c = null;
		// We try to get the class specified by mi. If we succeed we call
		// newModule(Class, Object[]) to create a new object of this class.
		try {
			c = Class.forName(mi.getClassName());
		} catch (ClassNotFoundException e) {
			System.err.println("The class " + mi.getClassName() + " couldn't be found.!");
			e.printStackTrace();
		}
		if (c == null) {
			return null;
		}
		return newModule(c, param);
	}

	/**
	 * This method creates instances of all specified classes and outputs
	 * an array of <code>Module</code>s sorted by their names.
	 *
	 * @param c the collection containing the classes to be transformed.
	 * @param param the parameters with which the constructor is to be called.
	 * @return an array of <code>Module</code> generated from the given classes.
	 */
	private Module[] instantiateModules(Collection c, Object[] param) {
		// the module information is saved in the following ArrayList
		Collection modules = new ArrayList();
		// We try to create a new object of every class in c.
		Iterator it = c.iterator();
		while (it.hasNext()) {
			Class cl = (Class)it.next();
			try {
				// We call the constructor with the parameters param.
				Module module = newModule(cl, param);
				if (module != null) {
					modules.add(module);
				}
			} catch (UnsupportedEnvironmentException e) {
				// current module is not supported and thus it is not returned.
			} catch (InvocationTargetException e) {
				// some other exception while instantiating the module.
				System.err.println("The following error occured during the instantiation of " + cl + ": " + e.getCause());
				e.getCause().printStackTrace();
			}
		}
		// Now we create an array of Modules.
		Module[] m = (Module[])modules.toArray(new Module[modules.size()]);
		
		// sort the modules by their names
		Arrays.sort(m, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				return ((Module)arg0).getName().compareTo(((Module)arg1).getName());
			}
		});
		
		return m;
	}
	
	/**
	 * This method returns the name of the class of the given module. The name
	 * is returned without the package and converted to lower case letters.
	 * @param mod the module whose class name should be returned.
	 * @return the class name of mod.
	 */
	private String getClassName(Module mod) {
		String s = mod.getClass().getName();
		s = s.substring(s.lastIndexOf(".") + 1);
		return s.toLowerCase();
	}

	/**
	 * Returns a tree model in which all operators are saved which work on the
	 * given search space. The operators are arranged hierarchically, just like 
	 * in the package structure.
	 * 
	 * @param searchspace the search space with which the operators must be compatible.
	 * @return the tree model containing the data.
	 */
	public DefaultTreeModel getTreeModelOfOperators(SearchSpace searchspace) {
		SortedMap groups = getGroupedOperatorClasses(searchspace);

		DefaultMutableTreeNode root = new DefaultMutableTreeNode("operators");
		Iterator it = groups.keySet().iterator();

		// now create the tree for the graph editor
		while (it.hasNext()) {
			String operatorType = (String)it.next();
			
			DefaultMutableTreeNode operatorTypeNode = new DefaultMutableTreeNode(operatorType);
			root.add(operatorTypeNode);
			
			List operators = (List)groups.get(operatorType);

			// now we create ModuleInfos for all operators 
			List moduleInfos = createModuleInfosFromClasses(operators);

			// sort operators alphabetically by names 
			// contained in the ModuleInfos
			Collections.sort(moduleInfos, new Comparator() {
				public int compare(Object o1, Object o2) {
					return (((ModuleInfo)o1).getName()).compareTo(((ModuleInfo)o2).getName());
				}
			});
			
			// add sorted operators to node 
			for (Iterator it2 = moduleInfos.iterator(); it2.hasNext();) {
				operatorTypeNode.add(new DefaultMutableTreeNode(it2.next()));
			}
		}
		return new DefaultTreeModel(root);
	}
	
	private List createModuleInfosFromClasses(List classes) {
		List moduleInfos = new LinkedList();
		
		for (Iterator it2 = classes.iterator(); it2.hasNext();) {
			Class moduleClass = (Class)it2.next();
				
			// check for UnsupportedEnvironmentExceptions
			try {
				// instantiate module
				Module module = newModule(moduleClass, new Object[] { schedule.getOperatorGraph()});
				// extract ModuleInfo
				ModuleInfo mi = new ModuleInfo(module.getName(), module.getDescription(), moduleClass.getName());
				// add ModuleInfo to list
				moduleInfos.add(mi);
			} catch (UnsupportedEnvironmentException e) {
				// the module is not compatible
			} catch (InvocationTargetException e) {
				System.err.println("The class " + moduleClass + " couldn't be instantiated!");
				e.printStackTrace();
			}
		}
		return moduleInfos;
	}
	
	/**
	 * Creates a new <code> Operator </code> which is specified by the given
	 * <code> ModuleInfo </code>.
	 * @param mi the <code> ModuleInfo </code> which specifies the <code> Operator </code> to be created.
	 * @return the created <code> Operator </code>.
	 */
	public Operator newOperator(ModuleInfo mi) {
		Operator operator = null;
		try {
			operator = (Operator)newModule(mi, new Object[] { schedule.getOperatorGraph()});
		} catch (UnsupportedEnvironmentException e1) {
			return null;
		} catch (InvocationTargetException e) {
			return null;
		} catch (RuntimeException e1) {
			e1.printStackTrace();
		}
		return operator;
	}

	/**
	 * This method returns a list of available search spaces.
	 * @return a list of available search spaces.
	 */
	public Module[] getSearchSpaces() {
		// get a list of all classes which implement the SearchSpace interface
		Collection classes = getClasses("searchspace", SearchSpace.class, true);
		return instantiateModules(classes, new Object[] { schedule });
	}

	/**
	 * This method returns a list of fitness functions which are defined on the
	 * given search space.
	 * @param searchspace the search space on which the functions must be defined.
	 * @return a list of available fitness functions.
	 */
	public Module[] getFitnessFunctions(SearchSpace searchspace) {
		// Get a list of all classes which implement the FitnessFunction
		// interface and which are placed in the right packages.
		Collection classes = getClasses("fitness." + getClassName(searchspace), FitnessFunction.class, true);
		return instantiateModules(classes, new Object[] { schedule });
	}

	public Module[] getPostprocessors(SearchSpace searchspace) {
		// Get a list of all classes which implement the Postprocessor
		// interface and which are placed in the right packages.
		Collection classes = getClasses("postprocessor." + getClassName(searchspace), Postprocessor.class, true);
		return instantiateModules(classes, new Object[] { schedule });
	}
	
	/**
	 * This method returns a list of available fitness transformers.
	 * @return a list of available fitness transformers.
	 */
	public Module[] getFitnessTransformers() {
		Collection classes = getClasses("fitness.transformer", FitnessTransformer.class, true);
		return instantiateModules(classes, new Object[] { schedule });
	}

	/**
	 * This method returns a list of available parameter controllers.
	 * @return a list of available parameter controllers.
	 */
	public Module[] getParameterControllers() {
		Collection classes = getClasses("parametercontroller", ParameterController.class, true);
		return instantiateModules(classes, new Object[] { schedule.getOperatorGraph()});
	}

	/**
	 * This method returns a list of available stopping criteria.
	 * @return a list of available stopping criteria.
	 */
	public Module[] getStoppingCriteria() {
		Collection classes = getClasses("stoppingcriterion", StoppingCriterion.class, true);
		return instantiateModules(classes, new Object[] { schedule });
	}

	/**
	 * This method returns a list of available population managers.
	 * @return a list of available population manager.
	 */
	public Module[] getPopulationManagers() {
		Collection classes = getClasses("populationmanager", PopulationManager.class, true);
		return instantiateModules(classes, new Object[] { schedule });
	}

	/**
	 * This method returns a list of available search space mappers.
	 * @return a list of available mappers.
	 */	
	public Module[] getMapper(SearchSpace searchspace) {
		Collection classes = getClasses("mapper." + getClassName(searchspace), Mapper.class, true);
		return instantiateModules(classes, new Object[] { schedule });
	}
	
	/**
	 * This method returns a list of available initialization operators.
	 * @return a list of available initialization operators.
	 */
	public Module[] getInitializationOperators() {
		Collection classes = getClasses("operator.initialization", Initialization.class, true);
		Module[] mlist = instantiateModules(classes, new Object[] { schedule.getOperatorGraph()});
		for (int i = 0; i < mlist.length; i++) 
			((Initialization)mlist[i]).hideNameProperty();
		return mlist;
	}

	/**
	 * This method returns a list of available observers.
	 * @return a list of available observers.
	 */
	public Module[] getObservers() {
		// get a list of all classes which implement the Observer interface
		Collection classes = getClasses("observer", Observer.class, true);
		return instantiateModules(classes, new Object[] { schedule });
	}

	/**
	 * This method returns a list of available views.
	 * @return a list of available views.
	 */
	public Module[] getViews() {
		// get a list of all classes which implement the View interface
		Collection classes = getClasses("view", View.class, true);
		return instantiateModules(classes, new Object[] { schedule });
	}

	public void checkPackageOfModule(Module module) throws IncompatibleModuleException {
		if (module instanceof FitnessTransformer) {
			Collection classes = getClasses("fitness.transformer", FitnessTransformer.class, true);
			if (!(classes.contains(module.getClass()))) throw new IncompatibleModuleException(schedule.getPhenotypeSearchSpace());
		} else if (module instanceof FitnessFunction) {
			Collection classes = getClasses("fitness." + getClassName(schedule.getPhenotypeSearchSpace()), FitnessFunction.class, true);
			if (!(classes.contains(module.getClass()))) throw new IncompatibleModuleException(schedule.getPhenotypeSearchSpace());
		} else if (module instanceof Mapper) {
			Collection classes = getClasses("mapper." + getClassName(schedule.getPhenotypeSearchSpace()), Mapper.class, true);
			if (!(classes.contains(module.getClass()))) throw new IncompatibleModuleException(schedule.getPhenotypeSearchSpace());
		} else if (module instanceof Operator && !(module instanceof Start) && !(module instanceof Finish)) {
			SortedMap groups = getGroupedOperatorClasses(schedule.getGenotypeSearchSpace());
			
			boolean found = false;
			for (Iterator iter1 = groups.keySet().iterator(); iter1.hasNext();) {
				List operators = (List)groups.get(iter1.next());
				for (Iterator iter2 = operators.iterator(); iter2.hasNext();) {
					// compare classes of modules 
					if (((Class)iter2.next()).equals(module.getClass())) found = true;
				}
			}
			if (!found) throw new IncompatibleModuleException(schedule.getGenotypeSearchSpace());
		}
	}

}