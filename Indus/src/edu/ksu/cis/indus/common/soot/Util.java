
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

import edu.ksu.cis.indus.common.datastructures.HistoryAwareFIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.HistoryAwareLIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.graph.INode;
import edu.ksu.cis.indus.common.graph.IObjectDirectedGraph;
import edu.ksu.cis.indus.common.graph.SimpleNodeGraph;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IEnvironment;

import edu.ksu.cis.indus.processing.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;

import org.apache.commons.collections.CollectionUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.ArrayType;
import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.DoubleType;
import soot.FloatType;
import soot.IntType;
import soot.Local;
import soot.LongType;
import soot.Modifier;
import soot.NullType;
import soot.PatchingChain;
import soot.RefLikeType;
import soot.RefType;
import soot.Scene;
import soot.ShortType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.VoidType;

import soot.jimple.DoubleConstant;
import soot.jimple.FloatConstant;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.LongConstant;
import soot.jimple.NullConstant;
import soot.jimple.VirtualInvokeExpr;

import soot.tagkit.Host;


/**
 * General utility class providing common chore methods.
 *
 * @author <a href="mailto:rvprasad@cis.ksu.edu">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public final class Util {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(Util.class);

	///CLOVER:OFF

	/**
	 * A private constructor to prevent the instantiation of this class.
	 */
	private Util() {
	}

	///CLOVER:ON

	/**
	 * Retrieves all ancestors (classes/interfaces) of the given class.
	 *
	 * @param sootClass for which the ancestors are requested.
	 *
	 * @return a collection of classes.
	 *
	 * @pre sootClass != null
	 * @post result != null and result.oclIsKindOf(Collection(SootClass))
	 */
	public static Collection getAncestors(final SootClass sootClass) {
		final Collection _result = new HashSet();
		final Collection _temp = new HashSet();
		final IWorkBag _wb = new HistoryAwareLIFOWorkBag(_result);
		_wb.addWork(sootClass);

		while (_wb.hasWork()) {
			final SootClass _work = (SootClass) _wb.getWork();

			if (_work.hasSuperclass()) {
				final SootClass _superClass = _work.getSuperclass();
				_temp.add(_superClass);
			}
			_temp.addAll(_work.getInterfaces());

			for (final Iterator _i = _temp.iterator(); _i.hasNext();) {
				final SootClass _sc = (SootClass) _i.next();

				_wb.addWorkNoDuplicates(_sc);
			}
		}
		_result.remove(sootClass);
		return _result;
	}

	/**
	 * Retreives the class in the topologically sorted top-down order.
	 *
	 * @param classes to be ordered.
	 * @param topDown indicates if the sorting needs to be done top down(<code>true</code>)or bottom up(<code>false</code>).
	 *
	 * @return a sequence of classes ordered in topological order based on hierarcy relation.
	 *
	 * @pre classes != null and classes.oclIsKindOf(Collection(SootClass))
	 * @post result != null
	 * @post result->forall(o | result->subSequence(1, result.indexOf(o))->includes(o.getSuperClass()) and
	 * 		 result->subSequence(1, result.indexOf(o))->includesAll(o.getInterfaces())
	 * @post result->forall(o | result->subSequence(result.indexOf(o), result->size())->excludes(o.getSuperClass()) and
	 * 		 result->subSequence(result.indexOf(o) result->size())->excludesAll(o.getInterfaces())
	 */
	public static List getClassesInTopologicallySortedOrder(final Collection classes, final boolean topDown) {
		final SimpleNodeGraph _sng = new SimpleNodeGraph();
		final IWorkBag _wb = new HistoryAwareLIFOWorkBag(new HashSet());
		_wb.addAllWork(classes);

		while (_wb.hasWork()) {
			final SootClass _sc = (SootClass) _wb.getWork();

			final INode _sn = _sng.getNode(_sc);
			final Collection _temp = CollectionUtils.intersection(_sc.getInterfaces(), classes);

			for (final Iterator _i = _temp.iterator(); _i.hasNext();) {
				final SootClass _interface = (SootClass) _i.next();
				_sng.addEdgeFromTo(_sng.getNode(_interface), _sn);

				_wb.addWorkNoDuplicates(_interface);
			}

			if (_sc.hasSuperclass()) {
				final SootClass _superClass = _sc.getSuperclass();
				_sng.addEdgeFromTo(_sng.getNode(_superClass), _sn);
				_wb.addWorkNoDuplicates(_superClass);
			}
		}

		final List _tsch = _sng.performTopologicalSort(topDown);
		CollectionUtils.transform(_tsch, IObjectDirectedGraph.OBJECT_EXTRACTOR);
		return _tsch;
	}

	/**
	 * Provides the class which injects the given method into the specific branch of the inheritence hierarchy which contains
	 * the given class.
	 *
	 * @param sc is the class in or above which the method may be defined.
	 * @param method is the name of the method (not the fully classified name).
	 * @param parameterTypes list of type of the parameters of the method.
	 * @param returnType return type of the method.
	 *
	 * @return if such a method exists, the class that injects the method is returned. <code>null</code> is returned,
	 * 		   otherwise.
	 *
	 * @pre sc != null and method != null and parameterTypes != null and returnType != null
	 * @pre parameterTypes->forall(o | o.oclIsKindOf(Type))
	 */
	public static SootClass getDeclaringClass(final SootClass sc, final String method, final List parameterTypes,
		final Type returnType) {
		SootClass _contains = sc;

		while (!_contains.declaresMethod(method, parameterTypes, returnType)) {
			if (hasSuperclass(_contains)) {
				_contains = _contains.getSuperclass();
			} else {
				_contains = null;
				break;
			}
		}

		return _contains;
	}

	/**
	 * Retrieves the default value for the given type.
	 *
	 * @param type for which the default value is requested.
	 *
	 * @return the default value
	 *
	 * @throws IllegalArgumentException when an invalid type is provided.
	 *
	 * @pre type != null
	 * @post result != null
	 */
	public static Value getDefaultValueFor(final Type type) {
		Value _result = null;

		if (type instanceof RefLikeType) {
			_result = NullConstant.v();
		} else if (type instanceof IntType) {
			_result = IntConstant.v(0);
		} else if (type instanceof CharType) {
			_result = IntConstant.v(0);
		} else if (type instanceof ByteType) {
			_result = IntConstant.v(0);
		} else if (type instanceof BooleanType) {
			_result = IntConstant.v(0);
		} else if (type instanceof DoubleType) {
			_result = DoubleConstant.v(0);
		} else if (type instanceof FloatType) {
			_result = FloatConstant.v(0);
		} else if (type instanceof LongType) {
			_result = LongConstant.v(0);
		} else if (type instanceof ShortType) {
			_result = IntConstant.v(0);
		} else {
			LOGGER.error("Illegal type specified." + type);
			throw new IllegalArgumentException("Illegal type specified." + type);
		}

		return _result;
	}

	/**
	 * Checks if one class is the descendent of another.  It is assumed that a class cannot be it's own ancestor.
	 *
	 * @param child class whose ancestor is of interest.
	 * @param ancestor fully qualified name of the ancestor class.
	 *
	 * @return <code>true</code> if a class by the name of <code>ancestor</code> is indeed the ancestor of <code>child</code>
	 * 		   class; false otherwise.
	 *
	 * @pre child != null and ancestor != null
	 * @post result == (child.evaluationType().allSuperTypes()->forall(o | o.name() = ancestor))
	 */
	public static boolean isDescendentOf(final SootClass child, final String ancestor) {
		boolean _retval = false;
		SootClass _temp = child;

		while (!_retval) {
			if (_temp.getName().equals(ancestor)) {
				_retval = true;
			} else {
				if (hasSuperclass(_temp)) {
					_temp = _temp.getSuperclass();
				} else {
					break;
				}
			}
		}

		if (!_retval) {
			_retval = implementsInterface(child, ancestor);
		}

		return _retval;
	}

	/**
	 * Checks if one class is the descendent of another.  It is assumed that a class cannot be it's own ancestor.
	 *
	 * @param child class whose ancestor is of interest.
	 * @param ancestor the ancestor class.
	 *
	 * @return <code>true</code> if <code>ancestor</code> class is indeed the ancestor of <code>child</code> class; false
	 * 		   otherwise.
	 *
	 * @pre child != null and ancestor != null
	 * @post result == child.oclIsKindOf(ancestor)
	 */
	public static boolean isDescendentOf(final SootClass child, final SootClass ancestor) {
		return isDescendentOf(child, ancestor.getName());
	}

	/**
	 * Checks if the given classes are on the same class hierarchy branch.  This means either one of the classes should be
	 * the subclass of the other class.
	 *
	 * @param class1 one of the two classes to be checked for relation.
	 * @param class2 one of the two classes to be checked for relation.
	 *
	 * @return <code>true</code> if <code>class1</code> is reachable from <code>class2</code>; <code>false</code>, otherwise.
	 *
	 * @pre class1 != null and class2 != null
	 * @post result == (class1.oclIsKindOf(class2) or class2.oclIsKindOf(class1))
	 */
	public static boolean isHierarchicallyRelated(final SootClass class1, final SootClass class2) {
		return isDescendentOf(class1, class2.getName()) || isDescendentOf(class2, class1.getName());
	}

	/**
	 * Retrieves the hosts which are tagged with a tag named <code>tagName</code>.
	 *
	 * @param hosts is the collection of hosts to filter.
	 * @param tagName is the name of the tag to filter <code>hosts</code>.
	 *
	 * @return a collection of hosts.
	 *
	 * @pre hosts != null and tagName != null
	 * @pre hosts.oclIsKindOf(Host)
	 * @post result != null and result.oclIsKindOf(Collection(Host))
	 * @post result->forall(o | hosts->contains(o) and o.hasTag(tagName))
	 */
	public static Collection getHostsWithTag(final Collection hosts, final String tagName) {
		Collection _result = new ArrayList();

		for (final Iterator _i = hosts.iterator(); _i.hasNext();) {
			final Host _host = (Host) _i.next();

			if (_host.hasTag(tagName)) {
				_result.add(_host);
			}
		}

		if (_result.isEmpty()) {
			_result = Collections.EMPTY_LIST;
		}
		return _result;
	}

	/**
	 * Checks if the method invoked at the invocation site is one of the <code>notify</code> methods in
	 * <code>java.lang.Object</code> class based  on the given call graph.
	 *
	 * @param stmt in which the invocation occurs.
	 * @param method in which <code>stmt</code> occurs.
	 * @param cgi to be used in the check.
	 *
	 * @return <code>true</code> if the method invoked at the invocation site is one of the <code>notify</code> methods in
	 * 		   <code>java.lang.Object</code> class; <code>false</code>, otherwise.
	 */
	public static boolean isNotifyInvocation(final InvokeStmt stmt, final SootMethod method, final ICallGraphInfo cgi) {
		final InvokeExpr _expr = stmt.getInvokeExpr();
		final SootMethod _sm = _expr.getMethod();
		boolean _result = isNotifyMethod(_sm);

		if (_result && method != null && cgi != null) {
			_result = wasMethodInvocationHelper(_sm, stmt, method, cgi);
		}
		return _result;
	}

	/**
	 * Checks if the given method is one of the <code>notify</code> methods in <code>java.lang.Object</code> class.
	 *
	 * @param method to be checked.
	 *
	 * @return <code>true</code> if the method is <code>notify</code> methods in <code>java.lang.Object</code> class;
	 * 		   <code>false</code>, otherwise.
	 *
	 * @pre method != null
	 */
	public static boolean isNotifyMethod(final SootMethod method) {
		return method.getDeclaringClass().getName().equals("java.lang.Object")
		  && (method.getName().equals("notify") || method.getName().equals("notifyAll"));
	}

	/**
	 * Retrieves the type object for the given primitive (non-array) type in the given scene.
	 *
	 * @param typeName is the name of the type.
	 * @param scene contains the classes that make up the system.
	 *
	 * @return the type object
	 *
	 * @throws MissingResourceException when the named type does not exists.
	 *
	 * @pre typeName != null and scene != null and typeName.indexOf("[") = -1
	 */
	public static Type getPrimitiveTypeFor(final String typeName, final Scene scene) {
		final Type _result;

		if (typeName.equals("int")) {
			_result = IntType.v();
		} else if (typeName.equals("char")) {
			_result = CharType.v();
		} else if (typeName.equals("byte")) {
			_result = ByteType.v();
		} else if (typeName.equals("long")) {
			_result = LongType.v();
		} else if (typeName.equals("short")) {
			_result = ShortType.v();
		} else if (typeName.equals("float")) {
			_result = FloatType.v();
		} else if (typeName.equals("double")) {
			_result = DoubleType.v();
		} else if (typeName.equals("boolean")) {
			_result = BooleanType.v();
		} else if (typeName.equals("void")) {
			_result = VoidType.v();
		} else {
			final SootClass _sc = scene.getSootClass(typeName);

			if (_sc == null) {
				final String _msg = typeName + " is not available in the System.";
				LOGGER.error(_msg);
				throw new MissingResourceException("Given type is not available in the System", typeName, null);
			}
			_result = _sc.getType();
		}
		return _result;
	}

	/**
	 * Checks if the given type is a valid reference type.
	 *
	 * @param t is the type to checked.
	 *
	 * @return <code>true</code> if <code>t</code> is a valid reference type; <code>false</code>, otherwise.
	 *
	 * @pre t != null
	 */
	public static boolean isReferenceType(final Type t) {
		return t instanceof RefType || t instanceof ArrayType || t instanceof NullType;
	}

	/**
	 * Checks if type <code>t1</code> is the same/sub-type of type <code>t2</code>.
	 *
	 * @param t1 is the type to be checked for equivalence or sub typing.
	 * @param t2 is the type against which the check is conducted.
	 * @param env in which these types exists.
	 *
	 * @return <code>true</code> if <code>t1</code> is same as or sub type of <code>t2</code>; <code>false</code>, otherwise.
	 *
	 * @post result == t1.oclIsKindOf(t2)
	 */
	public static boolean isSameOrSubType(final Type t1, final Type t2, final IEnvironment env) {
		boolean _result = false;

		if (t1.equals(t2)) {
			_result = true;
		} else if (t1 instanceof RefType && t2 instanceof RefType) {
			_result =
				isDescendentOf(env.getClass(((RefType) t1).getClassName()), env.getClass(((RefType) t2).getClassName()));
		}
		return _result;
	}

	/**
	 * Retrieve the soot options to be used when using Indus modules.  These options should be used via
	 * <code>Options.v().parse(getSootOptions())</code>.  These options are setup according to the requirement of the
	 * analyses in the project.
	 *
	 * @return the soot options.
	 *
	 * @post result != null.
	 */
	public static String[] getSootOptions() {
		final String[] _options =
			{
				"-p", "jb", "enabled:true,use-original-names:false", "-p", "jb.ls", "enabled:true", "-p", "jb.a",
				"enabled:true,only-stack-locals:true", "-p", "jb.ulp", "enabled:false",
			};

		return _options;
	}

	/**
	 * Checks if the given method is <code>java.lang.Thread.start()</code> method.
	 *
	 * @param method to be checked.
	 *
	 * @return <code>true</code> if the method is <code>java.lang.Thread.start()</code> method; <code>false</code>,
	 * 		   otherwise.
	 *
	 * @pre method != null
	 */
	public static boolean isStartMethod(final SootMethod method) {
		return method.getName().equals("start") && method.getDeclaringClass().getName().equals("java.lang.Thread")
		  && method.getReturnType() instanceof VoidType && method.getParameterCount() == 0;
	}

	/**
	 * Retrieves the type object for the given type in the given scene.
	 *
	 * @param typeName is the name of the type.
	 * @param scene contains the classes that make up the system.
	 *
	 * @return the type object.
	 *
	 * @pre typeName != null and scene != null
	 */
	public static Type getTypeFor(final String typeName, final Scene scene) {
		final Type _result;
		final int _i = typeName.indexOf("[", 0);

		if (_i == -1) {
			_result = getPrimitiveTypeFor(typeName, scene);
		} else {
			_result = ArrayType.v(getPrimitiveTypeFor(typeName.substring(0, _i), scene), typeName.length() - _i);
		}
		return _result;
	}

	/**
	 * Checks if the method invoked at the invocation site is one of the <code>wait</code> methods in
	 * <code>java.lang.Object</code> class based  on the given call graph.
	 *
	 * @param stmt in which the invocation occurs.
	 * @param method in which <code>stmt</code> occurs.
	 * @param cgi to be used in the check.
	 *
	 * @return <code>true</code> if the method invoked at the invocation site is one of the <code>wait</code> methods in
	 * 		   <code>java.lang.Object</code> class; <code>false</code>, otherwise.
	 */
	public static boolean isWaitInvocation(final InvokeStmt stmt, final SootMethod method, final ICallGraphInfo cgi) {
		final InvokeExpr _expr = stmt.getInvokeExpr();
		final SootMethod _sm = _expr.getMethod();
		boolean _result = isWaitMethod(_sm);

		if (_result && method != null && cgi != null) {
			_result = wasMethodInvocationHelper(_sm, stmt, method, cgi);
		}
		return _result;
	}

	/**
	 * Checks if the given method is one of the <code>wait</code> methods in <code>java.lang.Object</code> class.
	 *
	 * @param method to be checked.
	 *
	 * @return <code>true</code> if the method is <code>wait</code> methods in <code>java.lang.Object</code> class;
	 * 		   <code>false</code>, otherwise.
	 *
	 * @pre method != null
	 */
	public static boolean isWaitMethod(final SootMethod method) {
		return method.getDeclaringClass().getName().equals("java.lang.Object") && method.getName().equals("wait");
	}

	/**
	 * Collects all the method with signature identical to <code>method</code> in the superclasses/interfaces of
	 * <code>method</code>'s declaring class.
	 *
	 * @param method that needs to be included in the heirarchy to make the slice executable.
	 *
	 * @return a collection of methods.
	 *
	 * @pre method != null
	 * @post result != null and result.oclIsKindOf(Collection(SootMethod))
	 */
	public static Collection findMethodInSuperClassesAndInterfaces(final SootMethod method) {
		final IWorkBag _toProcess = new HistoryAwareFIFOWorkBag(new HashSet());
		Collection _result = new HashSet();
		_toProcess.addWork(method.getDeclaringClass());

		final List _parameterTypes = method.getParameterTypes();
		final Type _retType = method.getReturnType();
		final String _methodName = method.getName();

		while (_toProcess.hasWork()) {
			final SootClass _sc = (SootClass) _toProcess.getWork();

			if (_sc.declaresMethod(_methodName, _parameterTypes, _retType)) {
				_result.add(_sc.getMethod(_methodName, _parameterTypes, _retType));
			}

			if (_sc.hasSuperclass()) {
				final SootClass _superClass = _sc.getSuperclass();

				_toProcess.addWorkNoDuplicates(_superClass);
			}

			for (final Iterator _i = _sc.getInterfaces().iterator(); _i.hasNext();) {
				final SootClass _interface = (SootClass) _i.next();

				_toProcess.addWorkNoDuplicates(_interface);
			}
		}

		if (_result.isEmpty()) {
			_result = Collections.EMPTY_SET;
		}
		return _result;
	}

	/**
	 * Fixes the body of <code>java.lang.Thread.start()</code> (only if it is native) to call <code>run()</code> on the
	 * target or self.  This is required to complete the call graph.  This leaves the body untouched if it is not native.
	 *
	 * @param scm is the scene in which the alteration occurs.
	 */
	public static void fixupThreadStartBody(final Scene scm) {
		boolean _flag = false;

		for (final Iterator _i = scm.getClasses().iterator(); _i.hasNext();) {
			final SootClass _sc = (SootClass) _i.next();

			if (Util.implementsInterface(_sc, "java.lang.Runnable")) {
				_flag = true;
				break;
			}
		}

		if (_flag) {
			final SootClass _declClass = scm.getSootClass("java.lang.Thread");
			final SootMethod _sm = _declClass.getMethodByName("start");

			if (!_sm.isConcrete()) {
				_declClass.setApplicationClass();
				_declClass.setPhantom(false);
				_sm.setModifiers(_sm.getModifiers() & ~Modifier.NATIVE);
				_sm.setPhantom(false);

				final Jimple _jimple = Jimple.v();
				final JimpleBody _threadStartBody = _jimple.newBody(_sm);
				final PatchingChain _sl = _threadStartBody.getUnits();
				final Local _thisRef = _jimple.newLocal("$this", RefType.v(_declClass.getName()));
				_threadStartBody.getLocals().add(_thisRef);
				// adds $this := @this;
				_sl.addFirst(_jimple.newIdentityStmt(_thisRef, _jimple.newThisRef(RefType.v(_declClass))));

				// adds $this.virtualinvoke[java.lang.Thread.run()]:void;
				final VirtualInvokeExpr _ve =
					_jimple.newVirtualInvokeExpr(_thisRef, _declClass.getMethodByName("run"), Collections.EMPTY_LIST);
				_sl.addLast(_jimple.newInvokeStmt(_ve));
				_sl.addLast(_jimple.newReturnVoidStmt());
				_sm.setActiveBody(_threadStartBody);
			}
		}
	}

	/**
	 * Checks if the given class has a super class.  <code>java.lang.Object</code> will not have a super class, but others
	 * will.
	 *
	 * @param sc is the class to  be tested.
	 *
	 * @return <code>true</code> if <code>sc</code> has a superclass; <code>false</code>, otherwise.
	 *
	 * @pre sc != null
	 * @post sc.getName().equals("java.lang.Object") implies result = true
	 * @post (not sc.getName().equals("java.lang.Object")) implies result = false
	 */
	public static boolean hasSuperclass(final SootClass sc) {
		boolean _result = false;

		if (!sc.getName().equals("java.lang.Object")) {
			_result = sc.hasSuperclass();
		}
		return _result;
	}

	/**
	 * Checks if the given class implements the named interface.
	 *
	 * @param child is the class to be tested for implementation.
	 * @param ancestor is the fully qualified name of the interface to be checked for implementation.
	 *
	 * @return <code>true</code> if <code>child</code> implements the named interface; <code>false</code>, otherwise.
	 *
	 * @post result == (child.evaluationType().allSuperTypes()->forall(o | o.name() = ancestor))
	 */
	public static boolean implementsInterface(final SootClass child, final String ancestor) {
		boolean _result = false;
		SootClass _temp = child;

		while (!_result && (_temp.getInterfaceCount() > 0 || hasSuperclass(_temp))) {
			if (_temp.implementsInterface(ancestor)) {
				_result = true;
			} else {
				_temp = _temp.getSuperclass();
			}
		}
		return _result;
	}

	/**
	 * Removes methods from <code>methods</code> which have same signature as any methods in <code>methodsToRemove</code>.
	 * This is the counterpart of <code>retainMethodsWithSignature</code>.
	 *
	 * @param methods is the collection of methods to be modified.
	 * @param methodsToRemove is the collection of methods to match signatures with those in <code>methods</code>.
	 *
	 * @pre methods != null and methodsToRemove != null
	 * @pre methods.oclIsKindOf(Collection(SootMethod))
	 * @pre methodsToRemove.oclIsKindOf(Collection(SootMethod))
	 */
	public static void removeMethodsWithSameSignature(final Collection methods, final Collection methodsToRemove) {
		final Collection _removeSet = new HashSet();
		_removeSet.addAll(methods);
		retainMethodsWithSameSignature(_removeSet, methodsToRemove);
		methods.removeAll(_removeSet);
	}

	/**
	 * Retains methods from <code>methods</code> which have same signature as any methods in <code>methodsToRemove</code>.
	 * This is the counterpart of <code>removeMethodsWithSignature</code>.
	 *
	 * @param methods is the collection of methods to be modified.
	 * @param methodsToRetain is the collection of methods to match signatures with those in <code>methods</code>.
	 *
	 * @pre methods != null and methodsToRetain!= null
	 * @pre methods.oclIsKindOf(Collection(SootMethod))
	 * @pre methodsToRetain.oclIsKindOf(Collection(SootMethod))
	 */
	public static void retainMethodsWithSameSignature(final Collection methods, final Collection methodsToRetain) {
		final Collection _retainSet = new HashSet();

		for (final Iterator _j = methods.iterator(); _j.hasNext();) {
			final SootMethod _abstractMethod = (SootMethod) _j.next();

			for (final Iterator _k = methodsToRetain.iterator(); _k.hasNext();) {
				final SootMethod _method = (SootMethod) _k.next();

				if (_abstractMethod.getSubSignature().equals(_method.getSubSignature())) {
					_retainSet.add(_abstractMethod);
				}
			}
		}
		methods.retainAll(_retainSet);
	}

	/**
	 * This is a helper method to check if <code>invokedMethod</code> is called at the site in the given statement and method
	 * in the given callgraph.
	 *
	 * @param invokedMethod is the target method.
	 * @param stmt containing the invocation site.
	 * @param method containing <code>stmt</code>.
	 * @param cgi to be used for method resolution.
	 *
	 * @return <code>true</code> if <code> invokedMethod</code> is invoked; <code>false</code>, otherwise.
	 *
	 * @pre invokedMethod != null and stmt != null and method != null and cgi != null
	 */
	private static boolean wasMethodInvocationHelper(final SootMethod invokedMethod, final InvokeStmt stmt,
		final SootMethod method, final ICallGraphInfo cgi) {
		final Context _context = new Context();
		_context.setRootMethod(method);
		_context.setStmt(stmt);

		boolean _result = false;
		final Collection _callees = cgi.getCallees(stmt.getInvokeExpr(), _context);
		final Iterator _iter = _callees.iterator();
		final int _iterEnd = _callees.size();

		for (int _iterIndex = 0; _iterIndex < _iterEnd && !_result; _iterIndex++) {
			final SootMethod _callee = (SootMethod) _iter.next();
			_result |= _callee.equals(invokedMethod);
		}
		return _result;
	}

    /**
     * Returns the class, starting from the given class and above it in the class hierarchy, that declares the given method.
     *
     * @param sc the class from which to start the search in the class hierarchy.  This parameter cannot be
     * 		  <code>null</code>.
     * @param sm the method to search for in the class hierarchy.  This parameter cannot be <code>null</code>.
     *
     * @return the <code>SootMethod</code> corresponding to the implementation of <code>sm</code>.
     *
     * @throws IllegalStateException if <code>sm</code> is not available in the given branch of the class hierarchy.
     *
     * @pre sc != null and sm != null
     * @post result != null
     */
    public static SootMethod findDeclaringMethod(final SootClass sc, final SootMethod sm) {
    	final SootMethod _result;
    
    	if (sc.declaresMethod(sm.getName(), sm.getParameterTypes(), sm.getReturnType())) {
    		_result = sc.getMethod(sm.getName(), sm.getParameterTypes(), sm.getReturnType());
    	} else if (sc.hasSuperclass()) {
    		_result = findDeclaringMethod(sc.getSuperclass(), sm);
    	} else {
    		throw new IllegalStateException("Method " + sm + " not available in class " + sc + ".");
    	}
    	return _result;
    }
}

// End of File
