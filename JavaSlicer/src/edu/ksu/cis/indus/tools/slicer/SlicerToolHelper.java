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

package edu.ksu.cis.indus.tools.slicer;

import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;
import edu.ksu.cis.indus.common.soot.Util;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.slicer.SliceCollector;
import edu.ksu.cis.indus.slicer.SliceGotoProcessor;
import edu.ksu.cis.indus.slicer.transformations.ExecutableSlicePostProcessorAndModifier;
import edu.ksu.cis.indus.tools.slicer.processing.ISlicePostProcessor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootClass;
import soot.SootMethod;

/**
 * This is a helper class for the slicer tool.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class SlicerToolHelper {

	/**
	 * Logger for this class
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(SlicerToolHelper.class);

	/**
	 * Creates an instance of this class.
	 */
	private SlicerToolHelper() {
		super();
	}

	/**
	 * Applies the given post processor o the slice calculated by the given tool.
	 * 
	 * @param tool of interest.
	 * @param processor to be applied.
	 * @pre tool != null and processor != null
	 */
	public static void applyPostProcessor(final SlicerTool<?> tool, final ISlicePostProcessor processor) {
		final SliceCollector _collector = tool.getSliceCollector();
		final Collection<SootMethod> _methods = _collector.getMethodsInSlice();
		final SliceGotoProcessor _gotoProcessor = new SliceGotoProcessor(_collector);
		final BasicBlockGraphMgr _bbgMgr = tool.getBasicBlockGraphManager();
		processor.process(_methods, _bbgMgr, _collector);
		_gotoProcessor.process(_methods, _bbgMgr);
	}

	/**
	 * Loads the configuration in the named file.
	 * 
	 * @param configFileName is the name of the configuration file to load; if this is <code>null</code> then the default
	 *            configuration file will be loaded.
	 * @return the configuration as stored in the named file.
	 * @throws IllegalStateException if the named file or the default file cannot be found (in that order) or when there is an
	 *             while operating on the file.
	 */
	public static String loadConfigurationInFile(final String configFileName) {
		InputStream _inStream = null;
		String _result = null;

		final URL _filename;
		if (configFileName == null) {
			LOGGER.warn("Trying to use default configuration.");
			_filename = ClassLoader.getSystemResource("edu/ksu/cis/indus/tools/slicer/default_slicer_configuration.xml");
		} else {
			try {
				_filename = new File(configFileName).toURI().toURL();
			} catch (final MalformedURLException _e) {
				LOGGER.warn("The specified path " + configFileName + " does not exist.");
				throw new IllegalArgumentException("The specified path is invalid. - " + configFileName, _e);
			}
		}

		try {
			_inStream = _filename.openStream();
			_result = IOUtils.toString(_inStream);
		} catch (final IOException _e2) {
			LOGGER.error("Could not retrieve a handle to configuration file or an ."
					+ "IO error while reading configuration file. ", _e2);
			throw new IllegalStateException(_e2);
		} finally {
			IOUtils.closeQuietly(_inStream);
		}
		return _result;
	}

	/**
	 * Optimizes the slice calculated by the given tool for space. This method should be called after residualization.
	 * 
	 * @param tool in which the slice should be optimized.
	 * @param classesToRetain is the collection of FQN of classes that need to be retained in the slice.
	 * @return the unspecified classes that were retained.
	 * @pre tool != null and classesToRetain != null
	 * @post result != null
	 */
	public static Collection<SootClass> optimizeForSpaceAfterResidualization(final SlicerTool<?> tool,
			final Collection<String> classesToRetain) {
		final Collection<SootClass> _classesToErase = new HashSet<SootClass>();
		final Collection<SootClass> _classes = tool.getSystem().getClasses();
		final Iterator<SootClass> _i = _classes.iterator();
		final int _iEnd = _classes.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final SootClass _sc = _i.next();

			if (_sc.getMethods().size() == 0 && _sc.getFields().size() == 0 && !classesToRetain.contains(_sc.getName())) {
				_classesToErase.add(_sc);
			}
		}

		final Collection<SootClass> _c = Util.eraseClassesFrom(_classesToErase, tool.getSystem());
		_classesToErase.removeAll(_c);
		return _c;
	}

	/**
	 * Optimizes the slice calculated by the given tool for space. This method should be called before residualization.
	 * 
	 * @param tool in which the slice should be optimized.
	 * @param classesToRetain is the collection of FQN of classes that need to be retained in the slice.
	 * @pre tool != null and classesToRetain != null
	 */
	public static void optimizeForSpaceBeforeResidualization(final SlicerTool<?> tool,
			final Collection<String> classesToRetain) {
		final IEnvironment _system = tool.getSystem();
		final ExecutableSlicePostProcessorAndModifier _processor = new ExecutableSlicePostProcessorAndModifier(_system,
				classesToRetain);
		applyPostProcessor(tool, _processor);
	}
}

// End of File
