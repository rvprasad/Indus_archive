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

import edu.ksu.cis.indus.common.collections.CollectionUtils;
import edu.ksu.cis.indus.common.collections.SetUtils;
import edu.ksu.cis.indus.common.datastructures.HistoryAwareFIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.graph.SimpleNode;
import edu.ksu.cis.indus.common.graph.SimpleNodeGraph;

import edu.ksu.cis.indus.interfaces.IClassHierarchy;

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.ProcessingController;
import edu.ksu.cis.indus.processing.StaticEnvironment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassHierarchy.class);

	/**
	 * This maintains class to proper ancestor classes relation.
	 */
	private final Map<SootClass, Collection<SootClass>> class2properAncestorClasses = new HashMap<SootClass, Collection<SootClass>>();

	/**
	 * This maintains class to proper ancestor interfaces relation.
	 */
	private final Map<SootClass, Collection<SootClass>> class2properAncestorInterfaces = new HashMap<SootClass, Collection<SootClass>>();

	/**
	 * This maintains class to proper immediate subclass relation.
	 */
	private final Map<SootClass, Collection<SootClass>> class2properChildren = new HashMap<SootClass, Collection<SootClass>>();

	/**
	 * This maintains class to proper subclass relation.
	 */
	private final Map<SootClass, Collection<SootClass>> class2properDescendants = new HashMap<SootClass, Collection<SootClass>>();

	/**
	 * This maintains class to proper parent class relation.
	 */
	private final Map<SootClass, SootClass> class2properParentClass = new HashMap<SootClass, SootClass>();

	/**
	 * This maintains class to proper parent interfaces relation.
	 * 
	 * @invariant class2properParentInterfaces.oclIsKindOf(Map(SootClass, Collection(SootClass)))
	 */
	private final Map<SootClass, Collection<SootClass>> class2properParentInterfaces = new HashMap<SootClass, Collection<SootClass>>();

	/**
	 * This stores the classes in the system.
	 */
	private final Collection<SootClass> classes = new HashSet<SootClass>();

	/**
	 * This maintains class hierarchy as a graph.
	 */
	private SimpleNodeGraph<SootClass> classHierarchy;

	/**
	 * This stores the interfaces in the system.
	 */
	private final Collection<SootClass> interfaces = new HashSet<SootClass>();

	/**
	 * Creates a class hierarchy from the given classes. Any classes required to complete the hierarchy (upwards) will be
	 * included.
	 * 
	 * @param classes of interest
	 * @return the class hierarchy.
	 * @post result.getClasses().union(result.getInterfaces())->includesAll(classes)
	 */
	public static ClassHierarchy createClassHierarchyFrom(final Collection<SootClass> classes) {
		final ClassHierarchy _result = new ClassHierarchy();
		final ProcessingController _pc = new ProcessingController();
		final Collection<SootClass> _temp = new HashSet<SootClass>();

		final IWorkBag<SootClass> _wb = new HistoryAwareFIFOWorkBag<SootClass>(_temp);
		_wb.addAllWork(classes);

		while (_wb.hasWork()) {
			final SootClass _sc = _wb.getWork();
			_wb.addAllWorkNoDuplicates(_sc.getInterfaces());

			if (_sc.hasSuperclass()) {
				_wb.addWorkNoDuplicates(_sc.getSuperclass());
			}
		}

		_pc.setEnvironment(new StaticEnvironment(_temp));
		_result.hookup(_pc);
		_pc.process();
		_result.unhook(_pc);

		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#callback(soot.SootClass)
	 */
	@Override public void callback(final SootClass clazz) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("callback(SootClass clazz = " + clazz + ") - BEGIN");
		}

		final SimpleNode<SootClass> _classNode = classHierarchy.getNode(clazz);

		if (clazz.hasSuperclass()) {
			final SimpleNode<SootClass> _superClassNode = classHierarchy.getNode(clazz.getSuperclass());
			classHierarchy.addEdgeFromTo(_superClassNode, _classNode);
		}

		final Chain _interfaces = clazz.getInterfaces();
		final Iterator<SootClass> _i = _interfaces.iterator();
		final int _iEnd = _interfaces.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final SootClass _interfaceClass = _i.next();
			final SimpleNode<SootClass> _superinterfaceNode = classHierarchy.getNode(_interfaceClass);
			classHierarchy.addEdgeFromTo(_superinterfaceNode, _classNode);
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
	 * Prunes the class hierarchy to only include the given classes. The transitive inheritance relationship between the
	 * retained classes can be preserved via <code>retainTransitiveInheritanceRelation</code>.
	 * 
	 * @param confineToClasses is the collection of classes to confine the hierarchy to.
	 * @param retainTransitiveInheritanceRelation <code>true</code> indicates that the transitive inheritance relationship
	 *            between classes via classes not mentined in <code>confiningClasses</code> should be retained;
	 *            <code>false</code>, otherwise.
	 * @pre confineToClasses != null and confineToClasses.oclIsKindOf(Collection(SootClass))
	 */
	public void confine(final Collection confineToClasses, final boolean retainTransitiveInheritanceRelation) {
		final Collection<SootClass> _classesToRemove = new HashSet<SootClass>();
		_classesToRemove.addAll(classes);
		_classesToRemove.addAll(interfaces);
		_classesToRemove.removeAll(confineToClasses);

		if (retainTransitiveInheritanceRelation) {
			removeClassesAndRetainInheritanceRelation(_classesToRemove);
		} else {
			final Iterator<SootClass> _i = _classesToRemove.iterator();
			final int _iEnd = _classesToRemove.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final SootClass _sc = _i.next();
				final SimpleNode<SootClass> _node = classHierarchy.queryNode(_sc);
				classHierarchy.removeNode(_node);
			}
		}
		classes.retainAll(confineToClasses);
		interfaces.retainAll(confineToClasses);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IClassHierarchy#getClasses()
	 */
	public Collection<SootClass> getClasses() {
		return Collections.unmodifiableCollection(classes);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IClassHierarchy#getClassesInTopologicalOrder(boolean)
	 */
	public List<SootClass> getClassesInTopologicalOrder(final boolean topDown) {
		final List<SootClass> _result = new ArrayList<SootClass>();
		CollectionUtils.transform(classHierarchy.performTopologicalSort(topDown), classHierarchy.getObjectExtractor(),
				_result);
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IIdentification#getIds()
	 */
	public Collection<? extends Comparable<?>> getIds() {
		return Collections.singleton(ID);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IClassHierarchy#getInterfaces()
	 */
	public Collection<SootClass> getInterfaces() {
		return Collections.unmodifiableCollection(interfaces);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IClassHierarchy#getProperAncestorClassesOf(soot.SootClass)
	 */
	public Collection<SootClass> getProperAncestorClassesOf(final SootClass clazz) {
		Collection<SootClass> _result = class2properAncestorClasses.get(clazz);

		if (_result == null) {
			_result = SetUtils.intersection(classes, CollectionUtils.collect(classHierarchy.getReachablesFrom(classHierarchy
					.queryNode(clazz), false), classHierarchy.getObjectExtractor()));

			if (_result.isEmpty()) {
				_result = Collections.emptySet();
			}
			class2properAncestorClasses.put(clazz, _result);
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IClassHierarchy#getProperAncestorInterfacesOf(soot.SootClass)
	 */
	public Collection<SootClass> getProperAncestorInterfacesOf(final SootClass clazz) {
		Collection<SootClass> _result = class2properAncestorInterfaces.get(clazz);

		if (_result == null) {
			_result = SetUtils.intersection(interfaces, CollectionUtils.collect(classHierarchy.getReachablesFrom(
					classHierarchy.queryNode(clazz), false), classHierarchy.getObjectExtractor()));

			if (_result.isEmpty()) {
				_result = Collections.emptySet();
			}
			class2properAncestorInterfaces.put(clazz, _result);
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IClassHierarchy#getProperImmediateSubClassesOf(soot.SootClass)
	 */
	public Collection<SootClass> getProperImmediateSubClassesOf(final SootClass clazz) {
		Collection<SootClass> _result = class2properChildren.get(clazz);

		if (_result == null) {
			_result = CollectionUtils.collect(classHierarchy.queryNode(clazz).getSuccsOf(), classHierarchy
					.getObjectExtractor());

			if (_result.isEmpty()) {
				_result = Collections.emptySet();
			}
			class2properChildren.put(clazz, _result);
		}
		return _result;
	}

	/**
	 * @see IClassHierarchy#getProperParentClassOf(SootClass)
	 */
	public SootClass getProperParentClassOf(final SootClass clazz) {
		SootClass _result = class2properParentClass.get(clazz);

		if (_result == null) {
			_result = SetUtils.intersection(
					classes,
					CollectionUtils
							.collect(classHierarchy.queryNode(clazz).getPredsOf(), classHierarchy.getObjectExtractor()))
					.iterator().next();
			class2properParentClass.put(clazz, _result);
		}
		return _result;
	}

	/**
	 * @see IClassHierarchy#getProperParentInterfacesOf(SootClass)
	 */
	public Collection<SootClass> getProperParentInterfacesOf(final SootClass clazz) {
		Collection<SootClass> _result = class2properParentInterfaces.get(clazz);

		if (_result == null) {
			_result = SetUtils.intersection(interfaces, CollectionUtils.collect(classHierarchy.queryNode(clazz).getPredsOf(),
					classHierarchy.getObjectExtractor()));

			if (_result.isEmpty()) {
				_result = Collections.emptySet();
			}
			class2properParentInterfaces.put(clazz, _result);
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IClassHierarchy#getProperSubclassesOf(soot.SootClass)
	 */
	public Collection<SootClass> getProperSubclassesOf(final SootClass clazz) {
		Collection<SootClass> _result = class2properDescendants.get(clazz);

		if (_result == null) {
			_result = CollectionUtils.collect(classHierarchy.getReachablesFrom(classHierarchy.queryNode(clazz), true),
					classHierarchy.getObjectExtractor());

			if (_result.isEmpty()) {
				_result = Collections.emptySet();
			}
			class2properDescendants.put(clazz, _result);
		}
		return _result;
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
	@Override public void processingBegins() {
		unstable();
		classHierarchy = new SimpleNodeGraph<SootClass>();
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#reset()
	 */
	@Override public void reset() {
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
	 * @see java.lang.Object#toString()
	 */
	@Override public String toString() {
		return new ToStringBuilder(this).append("classHierarchy", classHierarchy).toString();
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregister(this);
	}

	/**
	 * Updates the classes captured by this hierarchy to reflect the relations captured by this hierarchy.
	 */
	public void updateEnvironment() {
		final List<SimpleNode<SootClass>> _nodes = classHierarchy.getNodes();
		final Iterator<SimpleNode<SootClass>> _i = _nodes.iterator();
		final int _iEnd = _nodes.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final SimpleNode<SootClass> _node = _i.next();
			final SootClass _sc = _node.getObject();
			final Collection<SootClass> _parents = CollectionUtils.collect(_node.getPredsOf(), classHierarchy
					.getObjectExtractor());
			final Collection<SootClass> _superClasses = SetUtils.intersection(classes, _parents);

			if (!_superClasses.isEmpty()) {
				assert _superClasses.size() == 1 : "More than one super class on " + _sc;

				final SootClass _superClass = _superClasses.iterator().next();
				_sc.setSuperclass(_superClass);
			}
			_sc.getInterfaces().retainAll(SetUtils.intersection(interfaces, _parents));
		}
	}

	/**
	 * Removes the given classes from the hierarchy while inserting new the inheritance relationship that span across the
	 * deleted classes.
	 * 
	 * @param classesToRemove a collection of classes to remove.
	 * @pre classesToRemove != null
	 */
	private void removeClassesAndRetainInheritanceRelation(final Collection<SootClass> classesToRemove) {
		final Iterator<SootClass> _i = classesToRemove.iterator();
		final int _iEnd = classesToRemove.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final SootClass _sc = _i.next();
			final SimpleNode<SootClass> _node = classHierarchy.queryNode(_sc);
			final Collection<SimpleNode<SootClass>> _succsOf = _node.getSuccsOf();
			final Iterator<SimpleNode<SootClass>> _j = _succsOf.iterator();
			final int _jEnd = _succsOf.size();

			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final SimpleNode<SootClass> _succ = _j.next();
				final Collection<SimpleNode<SootClass>> _predsOf = _node.getPredsOf();
				final Iterator<SimpleNode<SootClass>> _k = _predsOf.iterator();
				final int _kEnd = _predsOf.size();

				for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
					final SimpleNode<SootClass> _pred = _k.next();
					classHierarchy.addEdgeFromTo(_pred, _succ);
				}
			}

			classHierarchy.removeNode(_node);
		}

		final Iterator<SimpleNode<SootClass>> _j = classHierarchy.getNodes().iterator();
		final int _jEnd = classHierarchy.getNodes().size();

		for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
			final SimpleNode<SootClass> _node = _j.next();
			final Collection<SootClass> _parents = CollectionUtils.collect(_node.getPredsOf(), classHierarchy
					.getObjectExtractor());
			final Collection<SootClass> _superClasses = SetUtils.intersection(classes, _parents);

			if (_superClasses.size() > 1) {
				final Iterator<SootClass> _k = _superClasses.iterator();
				final int _kEnd = _superClasses.size();

				for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
					final SootClass _superClass = _k.next();

					if (_superClass.getName().equals("java.lang.Object")) {
						classHierarchy.removeEdgeFromTo(classHierarchy.queryNode(_superClass), _node);
						break;
					}
				}
			}
		}
	}
}

// End of File
