
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

package edu.ksu.cis.indus.toolkits.bandera;

import edu.ksu.cis.bandera.tool.ToolConfigurationView;
import edu.ksu.cis.bandera.tool.ToolIconView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import soot.IntType;
import soot.Scene;
import soot.SootMethod;


/**
 * The OFAToolTest provides JUnit test cases for the OFATool.
 *
 * @author Todd Wallentine tcw AT cis ksu edu
 * @version $Revision$ - $Date$
 */
public class OFAToolTest
  extends TestCase {
	/**
	 * Test the OFATool setConfiguration method to make sure it doesn't throw an exception when given non-empty configuration
	 * Strings.  This has a whole slew of possible configs.
	 */
	public void setNonEmptyConfigurations() {
		final OFATool _cgt = new OFATool();
		final String[] _configurationStrings =
			new String[] { "a", "1", "#", ".", "<configuration/>", "someConfigurationString" };

		for (int _i = 0; _i < _configurationStrings.length; _i++) {
			_cgt.setConfiguration(_configurationStrings[_i]);
		}
	}

	/**
	 * Test the OFATool to make sure an instance can be created without exception.
	 */
	public void testCreation() {
		try {
			new OFATool();
		} catch (final Exception _e) {
			fail("Could not create an instance of the tool");
		}
	}

	/**
	 * Test the OFATool getConfiguration method to make sure it doesn't throw an exception when called and that it returns
	 * null.
	 */
	public void testGetConfiguration() {
		final OFATool _cgt = new OFATool();
		final String _configuration = _cgt.getConfiguration();
		assertNull("The configuration String should be null.", _configuration);
	}

	/**
	 * Test the OFATool getInputParameterList to make sure it doesn't throw an exception and returns the correct List of
	 * parameters (non-null, size >= 2, contains ENTRY_POINTS_INPUT_KEY and SCENE_INPUT_KEY).
	 */
	public void testGetInputParameterList() {
		final OFATool _cgt = new OFATool();
		final List _inputParameterList = _cgt.getInputParameterList();
		assertNotNull("The input parameter list should not be null.", _inputParameterList);
		assertTrue("The input parameter list is not big enough.", _inputParameterList.size() >= 1);
		assertTrue("The input parameter list should contain the scene key.",
			_inputParameterList.contains(OFATool.SCENE_INPUT_KEY));
		assertTrue("The input parameter list should contain the entry points key.",
			_inputParameterList.contains(OFATool.ENTRY_POINTS_INPUT_KEY));
	}

	/**
	 * Test the OFATool getOutputParameterList to make sure it doesn't throw an exception and returns the correct List of
	 * parameters (non-null, size >= 1, contains REACHABLE_CLASSES_AND_FIELDS_OUTPUT_KEY and CALL_GRAPH_OUTPUT_KEY).
	 */
	public void testGetOutputParameterList() {
		final OFATool _cgt = new OFATool();
		final List _outputParameterList = _cgt.getOutputParameterList();
		assertNotNull("The output parameter list should not be null.", _outputParameterList);
		assertTrue("The output parameter list is not big enough.", _outputParameterList.size() >= 1);
		assertTrue("The output parameter list should contain callgraph key.",
			_outputParameterList.contains(OFATool.CALL_GRAPH_OUTPUT_KEY));
		assertTrue("The output parameter list should contain the reachable class and fields key.",
			_outputParameterList.contains(OFATool.REACHABLE_CLASSES_AND_FIELDS_OUTPUT_KEY));
	}

	/**
	 * Test the OFATool quit method to make sure it doesn't throw an exception when called.
	 */
	public void testQuit() {
		final OFATool _cgt = new OFATool();
		_cgt.quit();
	}

	/**
	 * Test the OFATool setConfiguration method to make sure it doesn't throw an exception when call with an empty
	 * configuration String.
	 */
	public void testSetEmptyConfiguration() {
		final OFATool _cgt = new OFATool();
		_cgt.setConfiguration("");
	}

	/**
	 * Test the OFATool setInputMap to make sure it throws an IllegalArgumentException when called with an empty input Map.
	 */
	public void testSetEmptyInputMap() {
		final OFATool _cgt = new OFATool();
		final String _message = "Calling setInputMap with an empty Map should throw an IllegalArgumentException.";

		try {
			final Map _inputMap = new HashMap(0);
			_cgt.setInputMap(_inputMap);
			fail(_message);
		} catch (final IllegalArgumentException _iae) {
			// success
		} catch (final Exception e) {
			fail(_message + "  Got instead: " + e.toString());
		}
	}

	/**
	 * Test the OFATool setConfiguration method to make sure it doesn't throw an exception when call with a null
	 * configuration String.
	 */
	public void testSetNullConfiguration() {
		final OFATool _cgt = new OFATool();
		_cgt.setConfiguration(null);
	}

	/**
	 * Test the OFATool setInputMap to make sure it throws an IllegalArgumentException when called with a null input Map.
	 */
	public void testSetNullInputMap() {
		final OFATool _cgt = new OFATool();
		final String _message = "Calling setInputMap with a null Map should throw an IllegalArgumentException.";

		try {
			final Map _inputMap = null;
			_cgt.setInputMap(_inputMap);
			fail(_message);
		} catch (final IllegalArgumentException _iae) {
			// success
		} catch (final Exception _e) {
			fail(_message + "  Got instead: " + _e.toString());
		}
	}

	/**
	 * Test the OFATool getToolConfigurationView method to make sure it doesn't throw an exception when called and that it
	 * returns null.
	 */
	public void testToolConfigurationView() {
		final OFATool _cgt = new OFATool();
		final ToolConfigurationView _tcv = _cgt.getToolConfigurationView();
		assertNull("The ToolConfigurationView should be null.", _tcv);
	}

	/**
	 * Test the OFATool getToolIconView method to make sure it doesn't throw an exception when called and that it returns
	 * null.
	 */
	public void testToolIconView() {
		final OFATool _cgt = new OFATool();
		final ToolIconView _tiv = _cgt.getToolIconView();
		assertNull("The ToolIconView should be null.", _tiv);
	}

	/**
	 * Test the OFATool setInputMap to make sure it throws an IllegalArgumentException when called with an input Map with a
	 * empty scene and empty entry point set.
	 */
	public void testsetInputMapWithEmptySceneAndEmptyEntryPoints() {
		final OFATool _cgt = new OFATool();
		final String _message =
			"Calling setInputMap with an invalid Map (empty scene, empty entry points) "
			+ "should throw an IllegalArgumentException.";

		try {
			final Map _inputMap = new HashMap(1);
			_inputMap.put(OFATool.ENTRY_POINTS_INPUT_KEY, new HashSet(0));
			_inputMap.put(OFATool.SCENE_INPUT_KEY, Scene.v());
			_cgt.setInputMap(_inputMap);
			fail(_message);
		} catch (final IllegalArgumentException _iae) {
			// success
		} catch (final Exception e) {
			fail(_message + "  Got instead: " + e.toString());
		}
	}

	/**
	 * Test the OFATool setInputMap to make sure it throws an IllegalArgumentException when called with an input Map with a
	 * empty scene and not entry point set.
	 */
	public void testsetInputMapWithEmptySceneAndNoEntryPoints() {
		final OFATool _cgt = new OFATool();
		final String _message =
			"Calling setInputMap with an invalid Map (empty scene, no entry points) "
			+ "should throw an IllegalArgumentException.";

		try {
			final Map _inputMap = new HashMap(1);
			_inputMap.put(OFATool.SCENE_INPUT_KEY, Scene.v());
			_cgt.setInputMap(_inputMap);
			fail(_message);
		} catch (final IllegalArgumentException _iae) {
			// success
		} catch (final Exception e) {
			fail(_message + "  Got instead: " + e.toString());
		}
	}

	/**
	 * Test the OFATool setInputMap to make sure it throws an IllegalArgumentException when called with an input Map with a
	 * empty scene and null entry point set.
	 */
	public void testsetInputMapWithEmptySceneAndNullEntryPoints() {
		final OFATool _cgt = new OFATool();
		final String _message =
			"Calling setInputMap with an invalid Map (empty scene, null entry points)"
			+ " should throw an IllegalArgumentException.";

		try {
			final Map _inputMap = new HashMap(1);
			_inputMap.put(OFATool.ENTRY_POINTS_INPUT_KEY, null);
			_inputMap.put(OFATool.SCENE_INPUT_KEY, Scene.v());
			_cgt.setInputMap(_inputMap);
			fail(_message);
		} catch (final IllegalArgumentException _iae) {
			// success
		} catch (final Exception e) {
			fail(_message + "  Got instead: " + e.toString());
		}
	}

	/**
	 * Test the OFATool setInputMap to make sure it throws an IllegalArgumentException when called with an input Map with a
	 * empty scene and Object entry point set.
	 */
	public void testsetInputMapWithEmptySceneAndObjectEntryPoints() {
		final OFATool _cgt = new OFATool();
		final String _message =
			"Calling setInputMap with an invalid Map (empty scene, Object entry points)"
			+ " should throw an IllegalArgumentException.";

		try {
			final Map _inputMap = new HashMap(1);
			_inputMap.put(OFATool.ENTRY_POINTS_INPUT_KEY, new Object());
			_inputMap.put(OFATool.SCENE_INPUT_KEY, Scene.v());
			_cgt.setInputMap(_inputMap);
			fail(_message);
		} catch (final IllegalArgumentException _iae) {
			// success
		} catch (final Exception e) {
			fail(_message + "  Got instead: " + e.toString());
		}
	}

	/**
	 * Test the OFATool setInputMap to make sure it throws an IllegalArgumentException when called with an input Map with a
	 * empty scene and valid entry point set.
	 */
	public void testsetInputMapWithEmptySceneAndValidEntryPoints() {
		final OFATool _cgt = new OFATool();
		final String _message =
			"Calling setInputMap with an invalid Map (empty scene, valid entry points)"
			+ " should throw an IllegalArgumentException.";

		try {
			final Map _inputMap = new HashMap(1);
			final Set _entryPoints = new HashSet(1);
			final SootMethod _sm = new SootMethod("someMethod", new ArrayList(0), IntType.v());
			_entryPoints.add(_sm);
			_inputMap.put(OFATool.ENTRY_POINTS_INPUT_KEY, _entryPoints);
			_inputMap.put(OFATool.SCENE_INPUT_KEY, Scene.v());
			_cgt.setInputMap(_inputMap);
			fail(_message);
		} catch (final IllegalArgumentException _iae) {
			// success
		} catch (final Exception e) {
			fail(_message + "  Got instead: " + e.toString());
		}
	}

	/**
	 * Test the OFATool setInputMap to make sure it throws an IllegalArgumentException when called with an input Map with a
	 * no scene and empty entry point set.
	 */
	public void testsetInputMapWithNoSceneAndEmptyEntryPoints() {
		final OFATool _cgt = new OFATool();
		final String _message =
			"Calling setInputMap with an invalid Map (no scene, empty entry points)"
			+ " should throw an IllegalArgumentException.";

		try {
			final Map _inputMap = new HashMap(1);
			_inputMap.put(OFATool.ENTRY_POINTS_INPUT_KEY, new HashSet(0));
			_cgt.setInputMap(_inputMap);
			fail(_message);
		} catch (final IllegalArgumentException _iae) {
			// success
		} catch (final Exception e) {
			fail(_message + "  Got instead: " + e.toString());
		}
	}

	/**
	 * Test the OFATool setInputMap to make sure it throws an IllegalArgumentException when called with an input Map with a
	 * no scene and null entry point set.
	 */
	public void testsetInputMapWithNoSceneAndNullEntryPoints() {
		final OFATool _cgt = new OFATool();
		final String _message =
			"Calling setInputMap with an invalid Map (no scene, null entry points) should throw an IllegalArgumentException.";

		try {
			final Map _inputMap = new HashMap(1);
			_inputMap.put(OFATool.ENTRY_POINTS_INPUT_KEY, null);
			_cgt.setInputMap(_inputMap);
			fail(_message);
		} catch (final IllegalArgumentException _iae) {
			// success
		} catch (final Exception e) {
			fail(_message + "  Got instead: " + e.toString());
		}
	}

	/**
	 * Test the OFATool setInputMap to make sure it throws an IllegalArgumentException when called with an input Map with a
	 * no scene and an Object as entry point set.
	 */
	public void testsetInputMapWithNoSceneAndObjectEntryPoints() {
		final OFATool _cgt = new OFATool();
		final String _message =
			"Calling setInputMap with an invalid Map (no scene, Object entry points)"
			+ " should throw an IllegalArgumentException.";

		try {
			final Map _inputMap = new HashMap(1);
			_inputMap.put(OFATool.ENTRY_POINTS_INPUT_KEY, new Object());
			_cgt.setInputMap(_inputMap);
			fail(_message);
		} catch (final IllegalArgumentException _iae) {
			// success
		} catch (final Exception e) {
			fail(_message + "  Got instead: " + e.toString());
		}
	}

	/**
	 * Test the OFATool setInputMap to make sure it throws an IllegalArgumentException when called with an input Map with a
	 * no scene and valid entry point set.
	 */
	public void testsetInputMapWithNoSceneAndValidEntryPoints() {
		final OFATool _cgt = new OFATool();
		final String _message =
			"Calling setInputMap with an invalid Map (no scene, valid entry points)"
			+ " should throw an IllegalArgumentException.";

		try {
			final Map _inputMap = new HashMap(1);
			final Set _entryPoints = new HashSet(1);
			final SootMethod _sm = new SootMethod("someMethod", new ArrayList(0), IntType.v());
			_entryPoints.add(_sm);
			_inputMap.put(OFATool.ENTRY_POINTS_INPUT_KEY, _entryPoints);
			_cgt.setInputMap(_inputMap);
			fail(_message);
		} catch (final IllegalArgumentException _iae) {
			// success
		} catch (final Exception e) {
			fail(_message + "  Got instead: " + e.toString());
		}
	}

	/**
	 * Test the OFATool setInputMap to make sure it throws an IllegalArgumentException when called with an input Map with a
	 * null scene and empty entry point set.
	 */
	public void testsetInputMapWithNullSceneAndEmptyEntryPoints() {
		final OFATool _cgt = new OFATool();
		final String _message =
			"Calling setInputMap with an invalid Map (null scene, empty entry points)"
			+ " should throw an IllegalArgumentException.";

		try {
			final Map _inputMap = new HashMap(1);
			_inputMap.put(OFATool.ENTRY_POINTS_INPUT_KEY, new HashSet(0));
			_inputMap.put(OFATool.SCENE_INPUT_KEY, null);
			_cgt.setInputMap(_inputMap);
			fail(_message);
		} catch (final IllegalArgumentException _iae) {
			// success
		} catch (final Exception e) {
			fail(_message + "  Got instead: " + e.toString());
		}
	}

	/**
	 * Test the OFATool setInputMap to make sure it throws an IllegalArgumentException when called with an input Map with a
	 * null scene and not entry point set.
	 */
	public void testsetInputMapWithNullSceneAndNoEntryPoints() {
		final OFATool _cgt = new OFATool();
		final String _message =
			"Calling setInputMap with an invalid Map (null scene, no entry points) should throw an IllegalArgumentException.";

		try {
			final Map _inputMap = new HashMap(1);
			_inputMap.put(OFATool.SCENE_INPUT_KEY, null);
			_cgt.setInputMap(_inputMap);
			fail(_message);
		} catch (final IllegalArgumentException _iae) {
			// success
		} catch (final Exception e) {
			fail(_message + "  Got instead: " + e.toString());
		}
	}

	/**
	 * Test the OFATool setInputMap to make sure it throws an IllegalArgumentException when called with an input Map with a
	 * null scene and null entry point set.
	 */
	public void testsetInputMapWithNullSceneAndNullEntryPoints() {
		final OFATool _cgt = new OFATool();
		final String _message =
			"Calling setInputMap with an invalid Map (null scene, null entry points)"
			+ " should throw an IllegalArgumentException.";

		try {
			final Map _inputMap = new HashMap(1);
			_inputMap.put(OFATool.ENTRY_POINTS_INPUT_KEY, null);
			_inputMap.put(OFATool.SCENE_INPUT_KEY, null);
			_cgt.setInputMap(_inputMap);
			fail(_message);
		} catch (final IllegalArgumentException _iae) {
			// success
		} catch (final Exception e) {
			fail(_message + "  Got instead: " + e.toString());
		}
	}

	/**
	 * Test the OFATool setInputMap to make sure it throws an IllegalArgumentException when called with an input Map with a
	 * null scene and Object entry point set.
	 */
	public void testsetInputMapWithNullSceneAndObjectEntryPoints() {
		final OFATool _cgt = new OFATool();
		final String _message =
			"Calling setInputMap with an invalid Map (null scene, Object entry points)"
			+ " should throw an IllegalArgumentException.";

		try {
			final Map _inputMap = new HashMap(1);
			_inputMap.put(OFATool.ENTRY_POINTS_INPUT_KEY, new Object());
			_inputMap.put(OFATool.SCENE_INPUT_KEY, null);
			_cgt.setInputMap(_inputMap);
			fail(_message);
		} catch (final IllegalArgumentException _iae) {
			// success
		} catch (final Exception e) {
			fail(_message + "  Got instead: " + e.toString());
		}
	}

	/**
	 * Test the OFATool setInputMap to make sure it throws an IllegalArgumentException when called with an input Map with a
	 * null scene and valid entry point set.
	 */
	public void testsetInputMapWithNullSceneAndValidEntryPoints() {
		final OFATool _cgt = new OFATool();
		final String _message =
			"Calling setInputMap with an invalid Map (null scene, valid entry points)"
			+ " should throw an IllegalArgumentException.";

		try {
			final Map _inputMap = new HashMap(1);
			final Set _entryPoints = new HashSet(1);
			final SootMethod _sm = new SootMethod("someMethod", new ArrayList(0), IntType.v());
			_entryPoints.add(_sm);
			_inputMap.put(OFATool.ENTRY_POINTS_INPUT_KEY, _entryPoints);
			_inputMap.put(OFATool.SCENE_INPUT_KEY, null);
			_cgt.setInputMap(_inputMap);
			fail(_message);
		} catch (final IllegalArgumentException _iae) {
			// success
		} catch (final Exception e) {
			fail(_message + "  Got instead: " + e.toString());
		}
	}

	/**
	 * Test the OFATool setInputMap to make sure it throws an IllegalArgumentException when called with an input Map with a
	 * Object scene and empty entry point set.
	 */
	public void testsetInputMapWithObjectSceneAndEmptyEntryPoints() {
		final OFATool _cgt = new OFATool();
		final String _message =
			"Calling setInputMap with an invalid Map (Object scene, empty entry points)"
			+ " should throw an IllegalArgumentException.";

		try {
			final Map _inputMap = new HashMap(1);
			_inputMap.put(OFATool.ENTRY_POINTS_INPUT_KEY, new HashSet(0));
			_inputMap.put(OFATool.SCENE_INPUT_KEY, new Object());
			_cgt.setInputMap(_inputMap);
			fail(_message);
		} catch (final IllegalArgumentException _iae) {
			// success
		    ;
		} catch (final Exception e) {
			fail(_message + "  Got instead: " + e.toString());
		}
	}

	/**
	 * Test the OFATool setInputMap to make sure it throws an IllegalArgumentException when called with an input Map with a
	 * Object scene and not entry point set.
	 */
	public void testsetInputMapWithObjectSceneAndNoEntryPoints() {
		final OFATool _cgt = new OFATool();
		final String _message =
			"Calling setInputMap with an invalid Map (Object scene, no entry points)"
			+ " should throw an IllegalArgumentException.";

		try {
			final Map _inputMap = new HashMap(1);
			_inputMap.put(OFATool.SCENE_INPUT_KEY, new Object());
			_cgt.setInputMap(_inputMap);
			fail(_message);
		} catch (final IllegalArgumentException _iae) {
			// success
		} catch (final Exception e) {
			fail(_message + "  Got instead: " + e.toString());
		}
	}

	/**
	 * Test the OFATool setInputMap to make sure it throws an IllegalArgumentException when called with an input Map with a
	 * Object scene and null entry point set.
	 */
	public void testsetInputMapWithObjectSceneAndNullEntryPoints() {
		final OFATool _cgt = new OFATool();
		final String _message =
			"Calling setInputMap with an invalid Map (Object scene, null entry points)"
			+ " should throw an IllegalArgumentException.";

		try {
			final Map _inputMap = new HashMap(1);
			_inputMap.put(OFATool.ENTRY_POINTS_INPUT_KEY, null);
			_inputMap.put(OFATool.SCENE_INPUT_KEY, new Object());
			_cgt.setInputMap(_inputMap);
			fail(_message);
		} catch (final IllegalArgumentException _iae) {
			// success
		} catch (final Exception e) {
			fail(_message + "  Got instead: " + e.toString());
		}
	}

	/**
	 * Test the OFATool setInputMap to make sure it throws an IllegalArgumentException when called with an input Map with a
	 * Object scene and Object entry point set.
	 */
	public void testsetInputMapWithObjectSceneAndObjectEntryPoints() {
		final OFATool _cgt = new OFATool();
		final String _message =
			"Calling setInputMap with an invalid Map (Object scene, Object entry points)"
			+ " should throw an IllegalArgumentException.";

		try {
			final Map _inputMap = new HashMap(1);
			_inputMap.put(OFATool.ENTRY_POINTS_INPUT_KEY, new Object());
			_inputMap.put(OFATool.SCENE_INPUT_KEY, new Object());
			_cgt.setInputMap(_inputMap);
			fail(_message);
		} catch (final IllegalArgumentException _iae) {
			// success
		} catch (final Exception e) {
			fail(_message + "  Got instead: " + e.toString());
		}
	}

	/**
	 * Test the OFATool setInputMap to make sure it throws an IllegalArgumentException when called with an input Map with a
	 * Object scene and valid entry point set.
	 */
	public void testsetInputMapWithObjectSceneAndValidEntryPoints() {
		final OFATool _cgt = new OFATool();
		final String _message =
			"Calling setInputMap with an invalid Map (Object scene, valid entry points)"
			+ " should throw an IllegalArgumentException.";

		try {
			final Map _inputMap = new HashMap(1);
			final Set _entryPoints = new HashSet(1);
			final SootMethod _sm = new SootMethod("someMethod", new ArrayList(0), IntType.v());
			_entryPoints.add(_sm);
			_inputMap.put(OFATool.ENTRY_POINTS_INPUT_KEY, _entryPoints);
			_inputMap.put(OFATool.SCENE_INPUT_KEY, new Object());
			_cgt.setInputMap(_inputMap);
			fail(_message);
		} catch (final IllegalArgumentException _iae) {
			// success
		} catch (final Exception e) {
			fail(_message + "  Got instead: " + e.toString());
		}
	}

	/**
	 * Test the OFATool setInputMap to make sure it throws an IllegalArgumentException when called with an input Map with a
	 * valid scene and empty entry point set.
	 */
	public void testsetInputMapWithValidSceneAndEmptyEntryPoints() {
		final OFATool _cgt = new OFATool();
		final String _message =
			"Calling setInputMap with an invalid Map (valid scene, empty entry points)"
			+ " should throw an IllegalArgumentException.";

		try {
			final Map _inputMap = new HashMap(1);
			_inputMap.put(OFATool.ENTRY_POINTS_INPUT_KEY, new HashSet(0));
			_inputMap.put(OFATool.SCENE_INPUT_KEY, Scene.v());
			_cgt.setInputMap(_inputMap);
			fail(_message);
		} catch (final IllegalArgumentException _iae) {
			// success
		} catch (final Exception e) {
			fail(_message + "  Got instead: " + e.toString());
		}
	}

	/**
	 * Test the OFATool setInputMap to make sure it throws an IllegalArgumentException when called with an input Map with a
	 * valid scene and not entry point set.
	 */
	public void testsetInputMapWithValidSceneAndNoEntryPoints() {
		final OFATool _cgt = new OFATool();
		final String _message =
			"Calling setInputMap with an invalid Map (valid scene, no entry points)"
			+ " should throw an IllegalArgumentException.";

		try {
			final Map _inputMap = new HashMap(1);
			_inputMap.put(OFATool.SCENE_INPUT_KEY, Scene.v());
			_cgt.setInputMap(_inputMap);
			fail(_message);
		} catch (final IllegalArgumentException _iae) {
			// success
		} catch (final Exception e) {
			fail(_message + "  Got instead: " + e.toString());
		}
	}

	/**
	 * Test the OFATool setInputMap to make sure it throws an IllegalArgumentException when called with an input Map with a
	 * valid scene and null entry point set.
	 */
	public void testsetInputMapWithValidSceneAndNullEntryPoints() {
		final OFATool _cgt = new OFATool();
		final String _message =
			"Calling setInputMap with an invalid Map (valid scene, null entry points)"
			+ " should throw an IllegalArgumentException.";

		try {
			final Map _inputMap = new HashMap(1);
			_inputMap.put(OFATool.ENTRY_POINTS_INPUT_KEY, null);
			_inputMap.put(OFATool.SCENE_INPUT_KEY, Scene.v());
			_cgt.setInputMap(_inputMap);
			fail(_message);
		} catch (final IllegalArgumentException _iae) {
			// success
		} catch (final Exception e) {
			fail(_message + "  Got instead: " + e.toString());
		}
	}

	/**
	 * Test the OFATool setInputMap to make sure it throws an IllegalArgumentException when called with an input Map with a
	 * valid scene and Object entry point set.
	 */
	public void testsetInputMapWithValidSceneAndObjectEntryPoints() {
		final OFATool _cgt = new OFATool();
		final String _message =
			"Calling setInputMap with an invalid Map (valid scene, Object entry points)"
			+ " should throw an IllegalArgumentException.";

		try {
			final Map _inputMap = new HashMap(1);
			_inputMap.put(OFATool.ENTRY_POINTS_INPUT_KEY, new Object());
			_inputMap.put(OFATool.SCENE_INPUT_KEY, Scene.v());
			_cgt.setInputMap(_inputMap);
			fail(_message);
		} catch (final IllegalArgumentException _iae) {
			// success
		} catch (final Exception _e) {
			fail(_message + "  Got instead: " + _e.toString());
		}
	}
}

// End of File
