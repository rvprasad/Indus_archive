
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

package edu.ksu.cis.indus.staticanalyses.processors;

import edu.ksu.cis.indus.common.CollectionsUtilities;
import edu.ksu.cis.indus.common.datastructures.Pair;

import edu.ksu.cis.indus.interfaces.IUseDefInfo;

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import org.apache.commons.collections.map.LazyMap;

import soot.Local;
import soot.SootField;
import soot.SootMethod;
import soot.ValueBox;

import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;


/**
 * This class provides use-def information of static fields.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class StaticFieldUseDefInfo
  extends AbstractProcessor
  implements IUseDefInfo {
	/** 
	 * This maps def site to use sites.
	 *
	 * @invariant def2usesMap.oclIsKindOf(Map(SootField, Map(Pair(DefinitionStmt, SootMethod), Collection(Pair(Stmt,
	 * 			  SootMethod)))))
	 */
	private final Map def2usesMap;

	/** 
	 * This is shadow (LazyMap) of <code>def2usesMap</code> to be used when updating it.
	 */
	private final Map def2usesMapShadow;

	/** 
	 * This maps use site to def sites.
	 *
	 * @invariant use2defsMap.oclIsKindOf(Map(SootField, Map(Pair(Stmt, SootMethod), Collection(Pair(DefinitionStmt,
	 * 			  SootMethod)))))
	 */
	private final Map use2defsMap;

	/** 
	 * This is shadow (LazyMap) of <code>use2defsMap</code> to be used when updating it.
	 */
	private final Map use2defsMapShadow;

	/**
	 * Creates a new StaticFieldUseDefInfo object.
	 */
	public StaticFieldUseDefInfo() {
		def2usesMap = new HashMap();
		def2usesMapShadow = LazyMap.decorate(def2usesMap, CollectionsUtilities.HASH_MAP_FACTORY);
		use2defsMap = new HashMap();
		use2defsMapShadow = LazyMap.decorate(use2defsMap, CollectionsUtilities.HASH_MAP_FACTORY);
		unstable();
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IUseDefInfo#getDefs(soot.jimple.Stmt, soot.SootMethod)
	 */
	public Collection getDefs(final Stmt useStmt, final SootMethod method) {
		final Collection _result;

		if (useStmt.containsFieldRef()) {
			final FieldRef _fieldRef = useStmt.getFieldRef();
			final Map _temp = (Map) MapUtils.getObject(use2defsMap, _fieldRef.getField(), Collections.EMPTY_MAP);
			_result = (Collection) MapUtils.getObject(_temp, new Pair(useStmt, method), Collections.EMPTY_SET);
		} else {
			_result = Collections.EMPTY_SET;
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IUseDefInfo#getDefs(soot.Local, soot.jimple.Stmt, soot.SootMethod)
	 */
	public Collection getDefs(final Local local, final Stmt useStmt, final SootMethod method) {
		throw new UnsupportedOperationException("This method is not supported in this implementation");
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IIdentification#getId()
	 */
	public Object getId() {
		return GLOBAL_USE_DEF_ID;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IUseDefInfo#getUses(soot.jimple.DefinitionStmt, soot.SootMethod)
	 */
	public Collection getUses(final DefinitionStmt defStmt, final SootMethod method) {
		final Collection _result;

		if (defStmt.containsFieldRef()) {
			final FieldRef _fieldRef = defStmt.getFieldRef();
			final Map _temp = (Map) MapUtils.getObject(def2usesMap, _fieldRef.getField(), Collections.EMPTY_MAP);
			_result = (Collection) MapUtils.getObject(_temp, new Pair(defStmt, method), Collections.EMPTY_SET);
		} else {
			_result = Collections.EMPTY_SET;
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.ValueBox, edu.ksu.cis.indus.processing.Context)
	 */
	public void callback(final ValueBox vBox, final Context context) {
		final StaticFieldRef _value = (StaticFieldRef) vBox.getValue();
		final Stmt _stmt = context.getStmt();
		final SootMethod _currentMethod = context.getCurrentMethod();
		final SootField _field = _value.getField();

		if (_stmt.getUseBoxes().contains(vBox)) {
			final Map _temp = (Map) use2defsMapShadow.get(_field);
			_temp.put(new Pair(_stmt, _currentMethod), null);
		} else {
			final Map _temp = (Map) def2usesMapShadow.get(_field);
			_temp.put(new Pair(_stmt, _currentMethod), null);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#consolidate()
	 */
	public void consolidate() {
		final Collection _keySet = CollectionUtils.intersection(def2usesMap.keySet(), use2defsMap.keySet());
		final Iterator _i = _keySet.iterator();
		final int _iEnd = _keySet.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final SootField _field = (SootField) _i.next();
			final Map defsite2uses = CollectionsUtilities.getMapFromMap(def2usesMap, _field);
			final Map usesite2defs = CollectionsUtilities.getMapFromMap(use2defsMap, _field);
			final Set _usesiteKeySet = usesite2defs.keySet();
			final Set _defsiteKeySet = defsite2uses.keySet();

			if (!_defsiteKeySet.isEmpty() && !_usesiteKeySet.isEmpty()) {
				final Iterator _j = _usesiteKeySet.iterator();
				final int _jEnd = _usesiteKeySet.size();

				for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
					usesite2defs.put(_j.next(), _defsiteKeySet);
				}

				final Iterator _k = _defsiteKeySet.iterator();
				final int _kEnd = _defsiteKeySet.size();

				for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
					defsite2uses.put(_k.next(), _usesiteKeySet);
				}
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void hookup(final ProcessingController ppc) {
		ppc.register(StaticFieldRef.class, this);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#processingBegins()
	 */
	public void processingBegins() {
		unstable();
	}

	/**
	 * Resets internal data structures.
	 */
	public void reset() {
		unstable();
		def2usesMap.clear();
		use2defsMap.clear();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		final StringBuffer _result =
			new StringBuffer("Statistics for Static Field Reference Based Use Def analysis as calculated by "
				+ getClass().getName() + "\n");
		int _edgeCount = 0;

		final StringBuffer _temp = new StringBuffer();

		for (final Iterator _i = use2defsMap.entrySet().iterator(); _i.hasNext();) {
			final Map.Entry _entry = (Map.Entry) _i.next();
			final Object _entity = _entry.getKey();
			_result.append("For " + _entity + "\n ");

			for (final Iterator _k = ((Map) _entry.getValue()).entrySet().iterator(); _k.hasNext();) {
				final Map.Entry _entry1 = (Map.Entry) _k.next();
				final Object _use = _entry1.getKey();
				final Collection _defs = (Collection) _entry1.getValue();
				int _localEdgeCount = 0;

				if (_defs != null) {
					for (final Iterator _j = (_defs).iterator(); _j.hasNext();) {
						final Object _def = _j.next();
						_temp.append("\t\t" + _use + " <== " + _def + "\n");
					}
					_localEdgeCount += (_defs).size();
				}

				final Object _key = _entry1.getKey();
				_result.append("\tFor " + _key + "[");

				if (_key != null) {
					_result.append(_key.hashCode());
				} else {
					_result.append(0);
				}
				_result.append("] there are " + _localEdgeCount + " use-defs.\n");
				_result.append(_temp);
				_temp.delete(0, _temp.length());
				_edgeCount += _localEdgeCount;
			}
		}
		_result.append("A total of " + _edgeCount + " use-defs.");
		return _result.toString();
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregister(StaticFieldRef.class, this);
		stable();
	}
}

// End of File
