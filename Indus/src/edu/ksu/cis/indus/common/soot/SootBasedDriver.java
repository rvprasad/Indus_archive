
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
 *
 * This software is licensed under the KSU Open Academic License.
 * You should have received a copy of the license with the distribution.
 * A copy can be found at
 *     http://www.cis.ksu.edu/santos/license.html
 * or you can contact the lab at:
 *     SAnToS Laboratory
 *     234 Nichols Hall
 *     Manhattan, KS 66506, USA
 */

package edu.ksu.cis.indus.common.soot;

import edu.ksu.cis.indus.Constants;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.Printer;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

import soot.options.Options;


/**
 * This is generic driver that provides basic support to process a system represented in Jimple.
 * 
 * <p>
 * The classes in which to search for root methods can be configured via setting 1 system property named
 * <code>indus.common.soot.SootBasedDriver.rootClasses</code>.  The syntax of this property a regular expression which will
 * be matched against the name of the class in which the method is declared.  Note if this is unspecified, then root methods
 * are searched only in the classes specified and not the classes which are required.  So, "edu.ksu.cis.indus.Processing"
 * will find root methods occurring in classes defined in class <code>edu.ksu.cis.indus.Processing</code>.  Likewise, the
 * user can control which methods are picked as root methods based on their signature by specifying the pattern via the
 * system property,  <code>indus.common.soot.SootBasedDriver.rootMethods</code>.
 * </p>
 * 
 * <p>
 * The user can provide a root method trapper object to be used to identify root methods.  However, if the user does not
 * provide one,  then an instance of the class named via <code>indus.common.soot.RootMethodTrapper.class</code> property
 * will be used.  This named class should be a subclass of <code>edu.ksu.cis.indus.common.soot.RootMethodTrapper</code>.
 * As reflection is used on to instantiate an object, the specified class by the property should have a no-argument
 * constructor.
 * </p>
 * 
 * <p>
 * The user can provide a statement graph factory object to be used to identify root methods.  However, if the user does not
 * provide one,  then an instance of the class named via
 * <code>indus.common.soot.SootBasedDriver.StmtGraphFactory.class</code> property will be used.  This named class should be
 * a subclass of <code>edu.ksu.cis.indus.common.soot.IStmtGraphFactory</code>. As reflection is used on  to instantiate an
 * object, the specified class by the property should have a no-argument constructor.
 * </p>
 * 
 * <p>Please refer to <code>edu.ksu.cis.indus.Constants</code> for a file-based approach to specifying these properties.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SootBasedDriver {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER;

	static {
		LOGGER = LogFactory.getLog(SootBasedDriver.class);

		final String _rmtClassName = Constants.getRootMethodTrapperClassName();

		try {
			final Object _o = SootBasedDriver.class.getClassLoader().loadClass(_rmtClassName).newInstance();

			if (_o instanceof RootMethodTrapper) {
				DEFAULT_INSTANCE_OF_ROOT_METHOD_TRAPPER = (RootMethodTrapper) _o;
			} else {
				throw new IllegalArgumentException(_rmtClassName + " is not a subclass of SootBasedDriver.RootMethodTrapper.");
			}
		} catch (final ClassNotFoundException _e) {
			LOGGER.fatal("class " + _rmtClassName + " could not be loaded/resolved. Bailing.", _e);
			throw new RuntimeException(_e);
		} catch (final InstantiationException _e) {
			LOGGER.fatal("An instance of class " + _rmtClassName + " could not be created. Bailing.", _e);
			throw new RuntimeException(_e);
		} catch (final IllegalAccessException _e) {
			LOGGER.fatal("No-arg constructor of " + _rmtClassName + " cannot be accessed.  Bailing.", _e);
			throw new RuntimeException(_e);
		}

		final String _nameOfStmtGraphFactoryClass = Constants.getStmtGraphFactoryClassName();

		try {
			final Object _o = SootBasedDriver.class.getClassLoader().loadClass(_nameOfStmtGraphFactoryClass).newInstance();

			if (_o instanceof IStmtGraphFactory) {
				DEFAULT_INSTANCE_OF_STMT_GRAPH_FACTORY = (IStmtGraphFactory) _o;
			} else {
				throw new IllegalArgumentException(_nameOfStmtGraphFactoryClass + " is not a subclass of IStmtGraphFactory.");
			}
		} catch (final ClassNotFoundException _e) {
			LOGGER.fatal("class " + _nameOfStmtGraphFactoryClass + " could not be loaded/resolved. Bailing.", _e);
			throw new RuntimeException(_e);
		} catch (final InstantiationException _e) {
			LOGGER.fatal("An instance of class " + _nameOfStmtGraphFactoryClass + " could not be created. Bailing.", _e);
			throw new RuntimeException(_e);
		} catch (final IllegalAccessException _e) {
			LOGGER.fatal("No-arg constructor of " + _nameOfStmtGraphFactoryClass + " cannot be accessed.  Bailing.", _e);
			throw new RuntimeException(_e);
		}
	}

	/** 
	 * The default object that can be used for trapping root methods.
	 */
	private static final RootMethodTrapper DEFAULT_INSTANCE_OF_ROOT_METHOD_TRAPPER;

	/** 
	 * The default object that can be used for created statement graphs.
	 */
	private static final IStmtGraphFactory DEFAULT_INSTANCE_OF_STMT_GRAPH_FACTORY;

	/** 
	 * This manages basic block graphs of the methods being processed.  Subclasses should initialize this suitably.
	 */
	protected BasicBlockGraphMgr bbm;

	/** 
	 * This is the set of methods which serve as the entry point into the system being analyzed.
	 *
	 * @invariant rootMethods.oclIsTypeOf(SootMethod)
	 */
	protected Collection rootMethods = new HashSet();

	/** 
	 * This provides <code>UnitGraph</code>s required by the analyses.  By defaults this will be initialized to
	 * <code>ExceptionFlowSensitiveStmtGraphFactory</code>.
	 */
	protected IStmtGraphFactory cfgProvider;

	/** 
	 * The list of classes that should be considered as the core of the system.
	 */
	protected List classNames;

	/** 
	 * The scene that contains the classes of the system.
	 */
	protected Scene scene;

	/** 
	 * The logger used by instances of this class and it's subclasses to log messages.
	 */
	private Log logger;

	/** 
	 * This is used to maintain the execution time of each analysis/transformation.  This timing information is printed via
	 * <code>printTimingStats</code>.
	 *
	 * @invariant times.oclIsTypeOf(Map(String, Long))
	 */
	private final Map times = new LinkedHashMap();

	/** 
	 * This traps the root methods.
	 */
	private RootMethodTrapper rootMethodTrapper;

	/** 
	 * The class path that should be added.
	 */
	private String classpathToAdd;

	/**
	 * Creates a new Test object.  This also initializes <code>cfgProvider</code> to <code>CompleteStmtGraphFactory</code>
	 * and <code>bbm</code> to an instance of <code>BasicBlockGraphMgr</code> with <code>cfgProvider</code> as the unit
	 * graph provider.
	 */
	public SootBasedDriver() {
		cfgProvider = getStmtGraphFactory();
		bbm = new BasicBlockGraphMgr();
		bbm.setStmtGraphFactory(cfgProvider);
	}

	/**
	 * Retrieves the basic block graph manager used by the application.
	 *
	 * @return the basic block graph manager.
	 *
	 * @post result != null
	 */
	public final BasicBlockGraphMgr getBbm() {
		return this.bbm;
	}

	/**
	 * Set the names of the classes to be loaded.
	 *
	 * @param s contains the class names.
	 *
	 * @pre s != null and s.oclIsKindOf(Collection(String))
	 */
	public final void setClassNames(final Collection s) {
		classNames = new ArrayList(s);
	}

	/**
	 * Sets the logger to be used.
	 *
	 * @param myLogger is the logger to be used.
	 */
	public final void setLogger(final Log myLogger) {
		logger = myLogger;
	}

	/**
	 * Sets the root method trapper.
	 *
	 * @param trapper to be used to trap root methods.
	 */
	public final void setRootMethodTrapper(final RootMethodTrapper trapper) {
		rootMethodTrapper = trapper;
	}

	/**
	 * Retrieves the root methods in the system.
	 *
	 * @return the collection of root methods.
	 *
	 * @post result != null
	 */
	public final Collection getRootMethods() {
		return Collections.unmodifiableCollection(rootMethods);
	}

	/**
	 * Retrieves the scene used by the application.
	 *
	 * @return the scene.
	 */
	public final Scene getScene() {
		return this.scene;
	}

	/**
	 * Retrieves the unit graph factory to be used by other processes that are driven by this implementation. By default, it
	 * provides an instance of <code>ExceptionFlowSensitiveStmtGraphFactory</code> initialized to prune synchronization
	 * related exceptions.
	 *
	 * @return an unit graph factory
	 *
	 * @post return != null
	 */
	public IStmtGraphFactory getStmtGraphFactory() {
		return DEFAULT_INSTANCE_OF_STMT_GRAPH_FACTORY;
	}

	/**
	 * Adds an entry into the time log of this test.  The subclasses should use this method to add time logs corresponding to
	 * each analysis they test/drive.
	 *
	 * @param name of the analysis for which the timing log is being created.
	 * @param milliseconds taken by the analysis.
	 *
	 * @pre name != null
	 */
	public final void addTimeLog(final String name, final long milliseconds) {
		times.put("[" + times.size() + "]" + name, new Long(milliseconds));
	}

	/**
	 * Records the given classpath in intention of using it while loading classes into the scene.
	 *
	 * @param classpath to be considered.
	 *
	 * @pre classpath != null
	 */
	public final void addToSootClassPath(final String classpath) {
		classpathToAdd = classpath;
	}

	/**
	 * Initialize the driver.  Loads up the classes and sets up the scene.  The given classes are loaded up as application
	 * classes.
	 *
	 * @param options to be used while setting up Soot infrastructure.
	 *
	 * @throws RuntimeException when <code>setClassNames()</code> was not called before using this object.
	 *
	 * @pre options != null
	 */
	public final void initialize(final String[] options) {
		if (classNames == null) {
			throw new RuntimeException("Please call setClassNames() before using this object.");
		}
		writeInfo("Loading classes....");
		scene = loadupClassesAndCollectMains(options);
	}

	/**
	 * Initialize the driver with the soot options available from <code>Util.getSootOptions</code> method.
	 */
	public final void initialize() {
		initialize(Util.getSootOptions());
	}

	/**
	 * Prints the timing statistics into the given stream.
	 *
	 * @pre stream != null
	 */
	public final void printTimingStats() {
		writeInfo("Timing statistics:");

		for (final Iterator _i = times.keySet().iterator(); _i.hasNext();) {
			final Object _e = _i.next();
			writeInfo(_e + " => " + times.get(_e) + "ms");
		}
	}

	/**
	 * Resets internal data structure.
	 */
	public void reset() {
		rootMethods.clear();
		scene = null;
		times.clear();
	}

	/**
	 * Logs the given object via the logging api.  Configure the logging via the logging implementation's configuration
	 * support.
	 *
	 * @param info to be logged.
	 */
	public void writeInfo(final Object info) {
		if (logger != null && logger.isInfoEnabled()) {
			if (info != null) {
				logger.info(info.toString());
			} else {
				logger.info("null");
			}
		}
	}

	/**
	 * Dumps jimple for the classes in the scene.
	 *
	 * @param outputDirectory is the directory in which jimple files will be dumped.
	 * @param jimpleFile <code>true</code> indicates if .jimple file should be dumped; <code>false</code>, indicates
	 * 		  otherwise.
	 * @param classFile <code>true</code> indicates if .class file should be dumped; <code>false</code>, indicates otherwise.
	 */
	protected void dumpJimpleAndClassFiles(final String outputDirectory, final boolean jimpleFile, final boolean classFile) {
		if (!jimpleFile && !classFile) {
			return;
		}

		final Printer _printer = Printer.v();

		for (final Iterator _i = scene.getClasses().iterator(); _i.hasNext();) {
			final SootClass _sc = (SootClass) _i.next();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Dumping jimple for " + _sc);
			}

			for (final Iterator _j = _sc.getMethods().iterator(); _j.hasNext();) {
				final SootMethod _sm = (SootMethod) _j.next();

				if (_sm.isConcrete()) {
					try {
						_sm.retrieveActiveBody();
					} catch (final RuntimeException _e) {
						LOGGER.error("Failed to retrieve body for method " + _sm, _e);
					}
				}
			}

			PrintWriter _writer = null;

			try {
				if (jimpleFile) {
					// write .jimple file
					final File _file = new File(outputDirectory + File.separator + _sc.getName() + ".jimple");
					_writer = new PrintWriter(new FileWriter(_file));
					_printer.printTo(_sc, _writer);
				}

				if (classFile) {
					// write .class file
					_printer.write(_sc, outputDirectory);
				}
			} catch (final IOException _e) {
				LOGGER.error("Error while writing " + _sc, _e);
			} catch (final RuntimeException _e) {
				LOGGER.error("Error while writing class file of " + _sc, _e);
			} finally {
				if (_writer != null) {
					_writer.flush();
					_writer.close();
				}
			}
		}
	}

	/**
	 * Loads up the classes specified via <code>setClassNames()</code> and also collects the possible entry points into the
	 * system being analyzed.  All <code>public static void main()</code> methods defined in <code>public</code> classes
	 * that are named via <code>args</code>are considered as entry points.  It uses the classpath set via
	 * <code>addToSootClassPath</code>.
	 *
	 * @param options to be used while setting up Soot infrastructure.
	 *
	 * @return a soot scene that provides the classes to be analyzed.
	 *
	 * @pre args != null and classNames != null and options != null
	 */
	private Scene loadupClassesAndCollectMains(final String[] options) {
		final Scene _result = Scene.v();
		String _temp = _result.getSootClassPath();
		Options.v().parse(options);

		if (_temp != null) {
			_temp += File.pathSeparator + classpathToAdd + File.pathSeparator + System.getProperty("java.class.path");
		} else {
			_temp = classpathToAdd;
		}
		_result.setSootClassPath(_temp);

		for (final Iterator _i = classNames.iterator(); _i.hasNext();) {
			final SootClass _sc = _result.loadClassAndSupport((String) _i.next());
			_sc.setApplicationClass();
		}

		final Collection _mc = new HashSet();
		_mc.addAll(_result.getClasses());

		RootMethodTrapper _rmt = rootMethodTrapper;

		if (_rmt == null) {
			_rmt = DEFAULT_INSTANCE_OF_ROOT_METHOD_TRAPPER;
		}

		_rmt.setClassNames(Collections.unmodifiableCollection(classNames));

		for (final Iterator _i = _mc.iterator(); _i.hasNext();) {
			final SootClass _sc = (SootClass) _i.next();

			if (_rmt.considerClassForEntryPoint(_sc)) {
				final Collection _methods = _sc.getMethods();

				for (final Iterator _j = _methods.iterator(); _j.hasNext();) {
					final SootMethod _sm = (SootMethod) _j.next();

					if (_rmt.isThisARootMethod(_sm)) {
						rootMethods.add(_sm);
					}
				}
			}
		}
		Util.fixupThreadStartBody(_result);
		return _result;
	}
}

// End of File
