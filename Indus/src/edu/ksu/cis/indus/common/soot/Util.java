
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

import edu.ksu.cis.indus.common.datastructures.HistoryAwareFIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.HistoryAwareLIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.graph.INode;
import edu.ksu.cis.indus.common.graph.SimpleNodeGraph;

import edu.ksu.cis.indus.interfaces.IEnvironment;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

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

import soot.jimple.DoubleConstant;
import soot.jimple.FloatConstant;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.LongConstant;
import soot.jimple.NullConstant;
import soot.jimple.VirtualInvokeExpr;


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
		CollectionUtils.transform(_tsch, SimpleNodeGraph.OBJECT_EXTRACTOR);
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
				"-p", "jb", "use-original-names:false", "jb.ls", "enabled:true", "jb.ulp", "enabled:false",
				"unsplit-original-locals:false",
			};

		return _options;
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
	private static boolean hasSuperclass(final SootClass sc) {
		boolean _result = false;

		if (!sc.getName().equals("java.lang.Object")) {
			_result = sc.hasSuperclass();
		}
		return _result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.21  2004/03/29 01:55:16  venku
   - refactoring.
     - history sensitive work list processing is a common pattern.  This
       has been captured in HistoryAwareXXXXWorkBag classes.
   - We rely on views of CFGs to process the body of the method.  Hence, it is
     required to use a particular view CFG consistently.  This requirement resulted
     in a large change.
   - ripple effect of the above changes.

   Revision 1.20  2004/03/21 02:54:28  venku
   - unit graph cannot be modified outside it's constructor or subclasses.
     Moved the method to prune exception based edges to ExceptionFlowSensitiveUnitGraph.
   Revision 1.19  2004/03/08 02:10:14  venku
   - enabled preliminary support to prune exception based intraprocedural
     control flow edges.
   Revision 1.18  2004/03/07 00:42:49  venku
   - added a new method to extract the options to be used by
     soot to use Indus.
   Revision 1.17  2004/02/27 07:53:04  venku
   - logging.
   Revision 1.16  2004/02/26 08:31:26  venku
   - refactoring - moved OFAnalyzer.isReferenceType() to Util.
   Revision 1.15  2004/02/05 18:21:02  venku
   - moved getClassesInTopologicalSortedOrder() into Util.
   - logging.
   - getClassesInTopologicalSortedOrder() was collecting the
     retain methods rather than the methods from which
     to retain. FIXED.
   Revision 1.14  2004/01/25 09:02:46  venku
   - coding convention.
   Revision 1.13  2004/01/24 01:42:50  venku
   - added methods to filter graphs based on identical-ness
     of signature.
   - added method to extract default "Value"s for soot types.
   Revision 1.12  2004/01/20 17:10:44  venku
   - added a new method to collect ancestors of a given class.
   Revision 1.11  2004/01/19 22:44:04  venku
   - added method declarations in interfaces to be found by
     findMethodsInClassesAndInterfaces().
   Revision 1.10  2004/01/19 11:38:03  venku
   - moved findMethodInSuperClasses into Util.
   Revision 1.9  2004/01/08 23:44:09  venku
   - SootClass.hasSuperclass() is finicky.  I have introduced a
     simple local version.
   Revision 1.8  2003/12/31 10:32:26  venku
   - safe fixing of thread method is desired.  FIXED.
   Revision 1.7  2003/12/31 10:05:08  venku
   - only application classes can be modified. So,
     we shall make Thread one.
   Revision 1.6  2003/12/31 10:01:16  venku
   - removed unused code.
   Revision 1.5  2003/12/31 09:52:20  venku
   - removed unused code.
   Revision 1.4  2003/12/31 09:34:22  venku
   - formatting and clover directives.
   Revision 1.3  2003/12/31 09:30:18  venku
   - removed unused code.
   Revision 1.2  2003/12/13 02:28:53  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.1  2003/12/09 04:22:03  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.1  2003/12/08 12:15:48  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.6  2003/11/06 05:04:02  venku
   - renamed WorkBag to IWorkBag and the ripple effect.
   Revision 1.5  2003/11/02 20:14:33  venku
   - thread body is fixed external to the driver in Util.
   Revision 1.4  2003/09/28 03:16:20  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.3  2003/08/11 07:13:58  venku
     empty log message
   Revision 1.2  2003/08/11 04:20:19  venku
   - Pair and Triple were changed to work in optimized and unoptimized mode.
   - Ripple effect of the previous change.
   - Documentation and specification of other classes.
   Revision 1.1  2003/08/07 06:42:16  venku
    Major:
     - Moved the package under indus umbrella.
     - Renamed isEmpty() to hasWork() in IWorkBag.
 */
