
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

package edu.ksu.cis.indus.staticanalyses.impl;

import edu.ksu.cis.indus.common.datastructures.FIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.HistoryAwareFIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.graph.INode;
import edu.ksu.cis.indus.common.graph.IObjectDirectedGraph;
import edu.ksu.cis.indus.common.graph.SimpleNodeGraph;

import edu.ksu.cis.indus.interfaces.IClassHierarchy;

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.ProcessingController;
import edu.ksu.cis.indus.processing.StaticEnvironment;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.MapUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootClass;

import soot.util.Chain;


/**
 * This is an implementation of class hierarchy analysis.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class ClassHierarchy
  extends AbstractProcessor
  implements IClassHierarchy {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(ClassHierarchy.class);

	/** 
	 * This stores the classes in the system.
	 *
	 * @invariant classes.oclIsKindOf(Set(SootClass))
	 */
	private final Collection classes = new HashSet();

	/** 
	 * This stores the interfaces in the system.
	 *
	 * @invariant interfaces.oclIsKindOf(Set(SootClass))
	 */
	private final Collection interfaces = new HashSet();

	/** 
	 * This maintains class to proper ancestor classes relation.
	 *
	 * @invariant class2properAncestorClasses.oclIsKindOf(Map(SootClass, Collection(SootClass)))
	 */
	private final Map class2properAncestorClasses = new HashMap();

	/** 
	 * This maintains class to proper ancestor interfaces relation.
	 *
	 * @invariant class2properAncestorInterfaces.oclIsKindOf(Map(SootClass, Collection(SootClass)))
	 */
	private final Map class2properAncestorInterfaces = new HashMap();

	/** 
	 * This maintains class to proper immediate subclass relation.
	 *
	 * @invariant class2properChildren.oclIsKindOf(Map(SootClass, Collection(SootClass)))
	 */
	private final Map class2properChildren = new HashMap();

	/** 
	 * This maintains class to proper subclass relation.
	 *
	 * @invariant class2properDescendants.oclIsKindOf(Map(SootClass, Collection(SootClass)))
	 */
	private final Map class2properDescendants = new HashMap();

	/** 
	 * This maintains class to proper parent class relation.
	 *
	 * @invariant class2properParent.oclIsKindOf(Map(SootClass, SootClass))
	 */
	private final Map class2properParentClass = new HashMap();

	/** 
	 * This maintains class to proper parent interfaces relation.
	 *
	 * @invariant class2properParentInterfaces.oclIsKindOf(Map(SootClass, Collection(SootClass)))
	 */
	private final Map class2properParentInterfaces = new HashMap();

	/** 
	 * This maintains class hierarchy as a graph.
	 */
	private SimpleNodeGraph classHierarchy;

	/** 
	 * This indicates this is a minimal or class hierarchy tree.
	 */
	private final boolean minimal;

	/**
	 * Creates an instance of this class.  The user can create a hierarchy trimmed down to only the classes that are
	 * processed.
	 *
	 * @param minimalHierarchy <code>true</code> indicates construct the hierarchy consisting of only the classes that are
	 * 		  processed; <code>false</code> indicates construct a hierarchy as defined by the relation between processed and
	 * 		  unprocessed classes.
	 */
	public ClassHierarchy(final boolean minimalHierarchy) {
		this.minimal = minimalHierarchy;
	}

	/**
	 * Creates a class hierarchy from the given classes.
	 *
	 * @param minimalHierarchy <code>true</code> indicates that only the given classes should be considered;
	 * 		  <code>false</code>, indicates  classes not mentioned but required to complete the hierarchy should also be
	 * 		  considered.
	 * @param classes of interest
	 *
	 * @return the class hierarchy.
	 *
	 * @post minimalHierarchy implies (classes.containsAll(result.getClasses())) and
	 * 		 classes.contains(result.getInterfaces()))
	 * @post result.getClasses().union(result.getInterfaces())->includesAll(classes)
	 */
	public static ClassHierarchy createClassHierarchyFrom(final boolean minimalHierarchy, final Collection classes) {
		final ClassHierarchy _result = new ClassHierarchy(minimalHierarchy);
		final ProcessingController _pc = new ProcessingController();
		final Collection _temp = new HashSet();

		if (!minimalHierarchy) {
			final IWorkBag _wb = new HistoryAwareFIFOWorkBag(_temp);
			_wb.addAllWork(classes);

			while (_wb.hasWork()) {
				final SootClass _sc = (SootClass) _wb.getWork();
				_wb.addAllWorkNoDuplicates(_sc.getInterfaces());

				if (_sc.hasSuperclass()) {
					_wb.addWorkNoDuplicates(_sc.getSuperclass());
				}
			}
		}

		_pc.setEnvironment(new StaticEnvironment(_temp));
		_result.hookup(_pc);
		_pc.process();
		_result.unhook(_pc);
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IClassHierarchy#getClasses()
	 */
	public Collection getClasses() {
		return Collections.unmodifiableCollection(classes);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IClassHierarchy#getClassesInTopologicalOrder(boolean)
	 */
	public List getClassesInTopologicalOrder(final boolean topDown) {
		return (List) CollectionUtils.collect(classHierarchy.performTopologicalSort(topDown),
			IObjectDirectedGraph.OBJECT_EXTRACTOR);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IIdentification#getIds()
	 */
	public Collection getIds() {
		return Collections.singleton(ID);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IClassHierarchy#getInterfaces()
	 */
	public Collection getInterfaces() {
		return Collections.unmodifiableCollection(interfaces);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IClassHierarchy#getProperAncestorClassesOf(soot.SootClass)
	 */
	public Collection getProperAncestorClassesOf(final SootClass clazz) {
		Collection _result = (Collection) MapUtils.getObject(class2properAncestorClasses, clazz);

		if (_result == null) {
			_result =
				CollectionUtils.intersection(classes,
					CollectionUtils.collect(classHierarchy.getReachablesFrom(classHierarchy.queryNode(clazz), false),
						IObjectDirectedGraph.OBJECT_EXTRACTOR));

			if (_result.isEmpty()) {
				_result = Collections.EMPTY_SET;
			}
			class2properAncestorClasses.put(clazz, _result);
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IClassHierarchy#getProperAncestorInterfacesOf(soot.SootClass)
	 */
	public Collection getProperAncestorInterfacesOf(final SootClass clazz) {
		Collection _result = (Collection) MapUtils.getObject(class2properAncestorInterfaces, clazz);

		if (_result == null) {
			_result =
				CollectionUtils.intersection(interfaces,
					CollectionUtils.collect(classHierarchy.getReachablesFrom(classHierarchy.queryNode(clazz), false),
						IObjectDirectedGraph.OBJECT_EXTRACTOR));

			if (_result.isEmpty()) {
				_result = Collections.EMPTY_SET;
			}
			class2properAncestorInterfaces.put(clazz, _result);
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IClassHierarchy#getProperImmediateSubClassesOf(soot.SootClass)
	 */
	public Collection getProperImmediateSubClassesOf(final SootClass clazz) {
		Collection _result = (Collection) MapUtils.getObject(class2properChildren, clazz);

		if (_result == null) {
			_result =
				CollectionUtils.collect(classHierarchy.queryNode(clazz).getSuccsOf(), IObjectDirectedGraph.OBJECT_EXTRACTOR);

			if (_result.isEmpty()) {
				_result = Collections.EMPTY_SET;
			}
			class2properChildren.put(clazz, _result);
		}
		return _result;
	}

	/**
	 * @see IClassHierarchy#getProperParentClassOf(SootClass)
	 */
	public SootClass getProperParentClassOf(final SootClass clazz) {
		SootClass _result = (SootClass) MapUtils.getObject(class2properParentClass, clazz);

		if (_result == null) {
			_result =
				(SootClass) CollectionUtils.intersection(classes,
					CollectionUtils.collect(classHierarchy.queryNode(clazz).getPredsOf(),
						IObjectDirectedGraph.OBJECT_EXTRACTOR)).iterator().next();
			class2properParentClass.put(clazz, _result);
		}
		return _result;
	}

	/**
	 * @see IClassHierarchy#getProperParentInterfacesOf(SootClass)
	 */
	public Collection getProperParentInterfacesOf(final SootClass clazz) {
		Collection _result = (Collection) MapUtils.getObject(class2properParentInterfaces, clazz);

		if (_result == null) {
			_result =
				CollectionUtils.intersection(interfaces,
					CollectionUtils.collect(classHierarchy.queryNode(clazz).getPredsOf(),
						IObjectDirectedGraph.OBJECT_EXTRACTOR));

			if (_result.isEmpty()) {
				_result = Collections.EMPTY_SET;
			}
			class2properParentInterfaces.put(clazz, _result);
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IClassHierarchy#getProperSubclassesOf(soot.SootClass)
	 */
	public Collection getProperSubclassesOf(final SootClass clazz) {
		Collection _result = (Collection) MapUtils.getObject(class2properDescendants, clazz);

		if (_result == null) {
			_result =
				CollectionUtils.collect(classHierarchy.getReachablesFrom(classHierarchy.queryNode(clazz), true),
					IObjectDirectedGraph.OBJECT_EXTRACTOR);

			if (_result.isEmpty()) {
				_result = Collections.EMPTY_SET;
			}
			class2properDescendants.put(clazz, _result);
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#callback(soot.SootClass)
	 */
	public void callback(final SootClass clazz) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("callback(SootClass clazz = " + clazz + ") - BEGIN");
		}

		final INode _classNode = classHierarchy.getNode(clazz);

		if (clazz.hasSuperclass()) {
			final INode _superClassNode = classHierarchy.getNode(clazz.getSuperclass());
			classHierarchy.addEdgeFromTo(_superClassNode, _classNode);
		}

		if (!minimal) {
			final Chain _interfaces = clazz.getInterfaces();
			final Iterator _i = _interfaces.iterator();
			final int _iEnd = _interfaces.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final SootClass _interfaceClass = (SootClass) _i.next();
				final INode _superinterfaceNode = classHierarchy.getNode(_interfaceClass);
				classHierarchy.addEdgeFromTo(_superinterfaceNode, _classNode);
			}
		}

		if (clazz.isInterface()) {
			interfaces.add(clazz);
		} else {
			classes.add(clazz);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("callback() - END");
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#consolidate()
	 */
	public void consolidate() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("consolidate() - BEGIN");
		}

		if (minimal) {
			createMinimalClassHierarchyGraph();
		}

		stable();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("consolidate() - END");
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void hookup(final ProcessingController ppc) {
		ppc.register(this);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#processingBegins()
	 */
	public void processingBegins() {
		unstable();
		classHierarchy = new SimpleNodeGraph();
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#reset()
	 */
	public void reset() {
		classes.clear();
		interfaces.clear();
		class2properChildren.clear();
		class2properDescendants.clear();
		class2properAncestorClasses.clear();
		class2properAncestorInterfaces.clear();
		class2properParentClass.clear();
		class2properParentInterfaces.clear();
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregister(this);
	}

	/**
	 * Creates minimal class hierarchy.
	 */
	private void createMinimalClassHierarchyGraph() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("createMinimalClassHierarchyGraph() - BEGIN");
		}

		final IWorkBag _wb = new FIFOWorkBag();

		for (final Iterator _i = IteratorUtils.chainedIterator(classes.iterator(), interfaces.iterator()); _i.hasNext();) {
			final SootClass _sc = (SootClass) _i.next();

			final INode _classNode = classHierarchy.getNode(_sc);

			if (_sc.hasSuperclass()) {
				SootClass _super = _sc;

				do {
					_super = _super.getSuperclass();
				} while (!classes.contains(_super) && _super.hasSuperclass());

				if (classes.contains(_super)) {
					final INode _superClassNode = classHierarchy.getNode(_super);
					classHierarchy.addEdgeFromTo(_superClassNode, _classNode);
				}
			}

			_wb.clear();
			_wb.addAllWork(_sc.getInterfaces());

			while (_wb.hasWork()) {
				final SootClass _interfaceClass = (SootClass) _wb.getWork();

				if (interfaces.contains(_interfaceClass)) {
					final INode _interfaceClassNode = classHierarchy.getNode(_interfaceClass);
					classHierarchy.addEdgeFromTo(_interfaceClassNode, _classNode);
				} else {
					_wb.addAllWorkNoDuplicates(_interfaceClass.getInterfaces());
				}
			}
		}

		removeRedundancy();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("createMinimalClassHierarchyGraph() - END");
		}
	}

	/**
	 * Removes redundant relation in the hierarchy.
	 */
	private void removeRedundancy() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("removeRedundancy() - BEGIN");
		}

		final Collection _col = new HashSet();

		for (final Iterator _j = classHierarchy.getNodes().iterator(); _j.hasNext();) {
			final INode _class = (INode) _j.next();
			final Collection _preds = _class.getPredsOf();
			final Iterator _k = _preds.iterator();
			final int _kEnd = _preds.size();

			_col.clear();

			for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
				final INode _n1 = (INode) _k.next();
				final Iterator _l = _preds.iterator();
				final int _lEnd = _preds.size();

				for (int _lIndex = 0; _lIndex < _lEnd; _lIndex++) {
					final INode _n2 = (INode) _l.next();

					if (_n1 != _n2 && classHierarchy.getReachablesFrom(_n2, false).contains(_n1)) {
						_col.add(_n1);
					}
				}
			}

			final Iterator _m = _col.iterator();
			final int _mEnd = _col.size();

			for (int _mIndex = 0; _mIndex < _mEnd; _mIndex++) {
				final INode _n1 = (INode) _m.next();
				classHierarchy.removeEdgeFromTo(_n1, _class);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("removeRedundancy() - END");
		}
	}
}

// End of File
