
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
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

import edu.ksu.cis.indus.interfaces.IEnvironment;

import edu.ksu.cis.indus.processing.Environment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import soot.ArrayType;
import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.DoubleType;
import soot.FloatType;
import soot.G;
import soot.IntType;
import soot.LongType;
import soot.Scene;
import soot.ShortType;
import soot.SootClass;
import soot.SootMethod;
import soot.VoidType;

import soot.jimple.DoubleConstant;
import soot.jimple.FloatConstant;
import soot.jimple.IntConstant;
import soot.jimple.LongConstant;
import soot.jimple.NullConstant;


/**
 * This class tests <code>Util</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class UtilTest
  extends TestCase {
	/**
	 * The scene.
	 */
	private Scene scene;

	/**
	 * The soot class.
	 */
	private SootClass sc;

	/**
	 * Tests <code>findMethodInSuperClassesAndInterfaces</code>.
	 */
	public final void testFindMethodInSuperClassesAndInterfaces() {
		final Collection _temp = Util.findMethodInSuperClassesAndInterfaces(sc.getMethodByName("add"));
		assertFalse(_temp.isEmpty());

		final List _params = new ArrayList();
		_params.add(scene.loadClassAndSupport("java.lang.Object").getType());
		assertTrue(_temp.contains(scene.loadClassAndSupport("java.util.Collection").getMethod("add", _params)));
		assertFalse(_temp.isEmpty());
	}

	/**
	 * Tests <code>fixupThreadStartBody</code>.
	 */
	public final void testFixupThreadStartBody() {
		final SootClass _thread = scene.loadClassAndSupport("java.lang.Thread");
		Util.fixupThreadStartBody(scene);

		final SootMethod _sm = _thread.getMethodByName("start");
		assertFalse(_sm.isNative());
		assertTrue(_sm.getActiveBody() != null);
		assertTrue(_sm.isConcrete());
	}

	/**
	 * Tests <code>getAncestors</code>.
	 */
	public final void testGetAncestors() {
		final Collection _classes = Util.getAncestors(sc);
		final Collection _ancestors = new ArrayList();
		_ancestors.add("java.lang.Object");
		_ancestors.add("java.util.Collection");
		_ancestors.add("java.util.Set");
		_ancestors.add("java.lang.Cloneable");
		_ancestors.add("java.io.Serializable");
		_ancestors.add("java.util.AbstractSet");
		_ancestors.add("java.util.AbstractCollection");
		assertTrue(_ancestors.size() == _classes.size());

		for (final Iterator _i = _classes.iterator(); _i.hasNext();) {
			final SootClass _class = (SootClass) _i.next();
			_ancestors.remove(_class.getName());
		}
		assertTrue(_ancestors.isEmpty());
	}

	/**
	 * Tests <code>getDeclaringClass</code>.
	 */
	public final void testGetDeclaringClass() {
		final SootClass _sc = Util.getDeclaringClass(sc, "notify", Collections.EMPTY_LIST, VoidType.v());
		assertTrue(_sc.getName().equals("java.lang.Object"));
	}

	/**
	 * Tests <code>getDefaultValueFor</code>.
	 */
	public final void testGetDefaultValueFor() {
		assertTrue(Util.getDefaultValueFor(IntType.v()).equals(IntConstant.v(0)));
		assertTrue(Util.getDefaultValueFor(FloatType.v()).equals(FloatConstant.v(0)));
		assertTrue(Util.getDefaultValueFor(DoubleType.v()).equals(DoubleConstant.v(0)));
		assertTrue(Util.getDefaultValueFor(CharType.v()).equals(IntConstant.v(0)));
		assertTrue(Util.getDefaultValueFor(BooleanType.v()).equals(IntConstant.v(0)));
		assertTrue(Util.getDefaultValueFor(ByteType.v()).equals(IntConstant.v(0)));
		assertTrue(Util.getDefaultValueFor(LongType.v()).equals(LongConstant.v(0)));
		assertTrue(Util.getDefaultValueFor(ShortType.v()).equals(IntConstant.v(0)));
		assertTrue(Util.getDefaultValueFor(ArrayType.v(CharType.v(), 2)).equals(NullConstant.v()));
	}

	/**
	 * Tests <code>testImplementsInterface</code>.
	 */
	public final void testImplementsInterface() {
		assertTrue(Util.implementsInterface(sc, "java.util.Collection"));
		assertFalse(Util.implementsInterface(sc, "java.text.CharacterIterator"));
	}

	/**
	 * Tests <code>isDescendentOfSootClass(SootClass)</code>.
	 */
	public final void testIsDescendentOfSootClassSootClass() {
		assertTrue(Util.isDescendentOf(sc, scene.getSootClass("java.util.AbstractSet")));
		assertFalse(Util.isDescendentOf(scene.getSootClass("java.util.AbstractSet"), sc));
		assertFalse(Util.isDescendentOf(sc, scene.loadClassAndSupport("java.util.AbstractList")));
	}

	/**
	 * Tests <code>isDescendentOfSootClass(String)</code>.
	 */
	public final void testIsDescendentOfSootClassString() {
		assertTrue(Util.isDescendentOf(sc, "java.util.AbstractSet"));
		assertFalse(Util.isDescendentOf(sc, "java.util.AbstractList"));
	}

	/**
	 * Tests <code>isHierarchicallyRelated</code>.
	 */
	public final void testIsHierarchicallyRelated() {
		assertTrue(Util.isHierarchicallyRelated(sc, scene.getSootClass("java.util.AbstractSet")));
		assertFalse(Util.isHierarchicallyRelated(sc, scene.loadClassAndSupport("java.util.AbstractList")));
		assertTrue(Util.isHierarchicallyRelated(scene.getSootClass("java.util.AbstractSet"), sc));
	}

	/**
	 * Tests <code>isSameOrSubType</code>.
	 */
	public final void testIsSameOrSubType() {
		final IEnvironment _env = new Environment(scene);
		assertTrue(Util.isSameOrSubType(sc.getType(), scene.getSootClass("java.util.AbstractSet").getType(), _env));
		assertFalse(Util.isSameOrSubType(scene.getSootClass("java.util.AbstractSet").getType(), sc.getType(), _env));
		assertFalse(Util.isSameOrSubType(sc.getType(), scene.loadClassAndSupport("java.util.AbstractList").getType(), _env));
		assertFalse(Util.isSameOrSubType(scene.getSootClass("java.util.AbstractList").getType(), sc.getType(), _env));
	}

	/**
	 * Tests <code>removeMethodsWithSameSignature</code>.
	 */
	public final void testRemoveMethodsWithSameSignature() {
		final SootClass _object = scene.getSootClass("java.lang.Object");
		Collection _t1 = new ArrayList(sc.getMethods());
		int _preSize = _t1.size();
		Util.removeMethodsWithSameSignature(_t1, _object.getMethods());
		assertTrue(_t1.size() == _preSize);
		_t1 = new ArrayList(_object.getMethods());
		_preSize = _t1.size();
		Util.removeMethodsWithSameSignature(_t1, _object.getMethods());
		assertTrue(_t1.size() != _preSize);
		assertTrue(_t1.isEmpty());
	}

	/**
	 * Tests <code>retainMethodsWithSameSignature</code>.
	 */
	public final void testRetainMethodsWithSameSignature() {
		final SootClass _object = scene.getSootClass("java.lang.Object");
		Collection _t1 = new ArrayList(sc.getMethods());
		int _preSize = _t1.size();
		Util.retainMethodsWithSameSignature(_t1, _object.getMethods());

		assertTrue(_t1.size() != _preSize);
		assertTrue(_t1.isEmpty());
		_t1 = new ArrayList(_object.getMethods());
		_preSize = _t1.size();
		Util.retainMethodsWithSameSignature(_t1, _object.getMethods());
		assertTrue(_t1.size() == _preSize);
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		scene = Scene.v();
		sc = scene.loadClassAndSupport("java.util.HashSet");
	}

	/**
	 * @see TestCase#tearDown()
	 */
	protected void tearDown()
	  throws Exception {
		G.reset();
		scene = null;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2004/01/28 22:45:07  venku
   - added new test cases for testing classes in soot package.
 */
