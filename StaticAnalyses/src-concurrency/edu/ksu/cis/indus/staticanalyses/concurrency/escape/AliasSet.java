
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

package edu.ksu.cis.indus.staticanalyses.concurrency.escape;

import edu.ksu.cis.indus.common.datastructures.FastUnionFindElement;
import edu.ksu.cis.indus.common.datastructures.HistoryAwareLIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.Pair;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;

import soot.Type;


/**
 * This class represents an alias set as specified in the techreport <a
 * href="http://www.cis.ksu.edu/santos/papers/technicalReports/SAnToS-TR2003-6.pdf">Honing the  Detection of Interference
 * and Ready Dependence for Slicing Concurrent Java Programs.</a>  It represents an equivalence class in escape analysis
 * defined in the same document.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
final class AliasSet
  extends FastUnionFindElement
  implements Cloneable {
	/** 
	 * This constant identifies the cells of an array in the field map of it's alias set.
	 */
	static final String ARRAY_FIELD = "$ELT";

	/** 
	 * This is used to generate unique ready entities.
	 */
	private static long readyEntityCount;

	/** 
	 * This is used to generate unique share entities.
	 */
	private static int shareEntityCount;

	/** 
	 * This represents if the variable associated with alias set was accessed (read/written).
	 */
	boolean accessed;

	/** 
	 * This represents the ready Entities associated with this alias set.
	 */
	private Collection readyEntities;

	/** 
	 * This represents the ready Entities associated with this alias set.
	 */
	private Collection shareEntities;

	/** 
	 * This maps field signatures to their alias sets.
	 *
	 * @invariant fieldMap.oclIsKindOf(Map(String, AliasSet))
	 */
	private Map fieldMap;

	/** 
	 * This indicates if this alias set is associated with a static field.
	 */
	private boolean global;

	/** 
	 * This is used to indicate that <code>multiThreadAccess</code> field is being updated.
	 */
	private boolean markingMultiThreadAccess;

	/** 
	 * This is used to indicate that the alias set represents an object that is accessed in multiple threads.
	 */
	private boolean multiThreadAccess;

	/** 
	 * This indicates if the variable associated with this alias set is the receiver of <code>notify()/notifyAll()</code>
	 * call.
	 */
	private boolean notifies;

	/** 
	 * This indicates if the associated variable was read from.
	 */
	private boolean read;

	/** 
	 * This indicates if this object is being unified with itself.
	 */
	private boolean selfUnifying;

	/** 
	 * This indicates if the variable (hence, the object referred to) associated with this alias set shared across threads.
	 */
	private boolean shared;

	/** 
	 * This indicates that this object is being stringified.
	 */
	private boolean stringifying;

	/** 
	 * This indicates if the variable associated with this alias set is the receiver of <code>wait()</code> call.
	 */
	private boolean waits;

	/** 
	 * This indicates if the assocaited variable was written into.
	 */
	private boolean written;

	/**
	 * Creates a new instance of this class.
	 */
	private AliasSet() {
		fieldMap = new HashMap();
		shared = false;
		accessed = false;
		global = false;
		readyEntities = null;
		read = false;
		written = false;
		shareEntities = null;
		markingMultiThreadAccess = false;
		multiThreadAccess = false;
	}

	/**
	 * Clones this alias set.
	 *
	 * @return the clone of this object.
	 *
	 * @throws CloneNotSupportedException is thrown if it is thrown by <code>java.lang.Object.clone()</code>.
	 *
	 * @post result != null and result.set != null and result.fieldMap != self.fieldMap
	 */
	public Object clone()
	  throws CloneNotSupportedException {
		final Object _result;

		if (isGlobal()) {
			//optimization
			_result = find();
		} else if (find() != this) {
			//just work on the representative of the class
			_result = (AliasSet) ((AliasSet) find()).clone();
		} else {
			final AliasSet _clone = (AliasSet) super.clone();

			_clone.fieldMap = (Map) ((HashMap) fieldMap).clone();
			_clone.set = null;

			_result = _clone;
		}

		return _result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		final String _result;

		if (find() != this) {
			_result = ((AliasSet) find()).toString();
		} else {
			if (stringifying) {
				_result = String.valueOf(Integer.toHexString(hashCode()));
			} else {
				stringifying = true;
				_result =
					new ToStringBuilder(this).append("waits", this.waits).append("written", this.written)
											   .append("global", this.global).append("accessed", this.accessed)
											   .append("readyEntities", this.readyEntities)
											   .append("multiThreadAccess", this.multiThreadAccess)
											   .append("shared", this.shared).append("shareEntities", this.shareEntities)
											   .append("notifies", this.notifies).append("read", this.read)
											   .append("fieldMap", fieldMap).toString();
				stringifying = false;
			}
		}
		return _result;
	}

	/**
	 * Creates an alias set suitable for the given type.
	 *
	 * @param type is the type from which Alias set is requested.
	 *
	 * @return the alias set corresponding to the given type.
	 *
	 * @post AliasSet.canHaveAliasSet(type) implies result != null
	 * @post not AliasSet.canHaveAliasSet(type) implies result == null
	 */
	static AliasSet getASForType(final Type type) {
		AliasSet _result = null;

		if (EquivalenceClassBasedEscapeAnalysis.canHaveAliasSet(type)) {
			_result = new AliasSet();
		}

		return _result;
	}

	/**
	 * Fixes up the field maps of the alias sets in the given map.  When alias sets are cloned, the field maps are cloned.
	 * Hence, they are shallow copied.  This method clones the relation between the alias sets among their clones.
	 *
	 * @param clonee2clone maps an representative alias set to it's clone.  This is also an out parameter that will contain
	 * new mappings.
	 *
	 * @throws CloneNotSupportedException when <code>clone()</code> fails.
	 */
	static void fixUpFieldMapsOfClone(final Map clonee2clone)
	  throws CloneNotSupportedException {
		final IWorkBag _wb = new HistoryAwareLIFOWorkBag(new HashSet());

		_wb.addAllWork(clonee2clone.keySet());

		while (_wb.hasWork()) {
			final AliasSet _clonee = (AliasSet) _wb.getWork();
			final AliasSet _clone = (AliasSet) clonee2clone.get(_clonee);
			final Map _cloneeFieldMap = _clonee.fieldMap;
			final Set _cloneeFields = _cloneeFieldMap.keySet();
			final Iterator _i = _cloneeFields.iterator();
			final int _iEnd = _cloneeFields.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final Object _field = _i.next();
				/*
				 * We use the representative alias set as it is possible that a field may have 2 alias sets in different
				 * contexts but the same representative alias set in both contexts.  We don't do the same for clones as they
				 * are representation until they are unified, which happens in the following block. 
				 */
				final Object _cloneeFieldAS = (AliasSet) ((AliasSet) _cloneeFieldMap.get(_field)).find();
				Object _cloneFieldAS = clonee2clone.get(_cloneeFieldAS);

				if (_cloneFieldAS == null) {
					_cloneFieldAS = (AliasSet) ((AliasSet) _cloneeFieldAS).clone();
					clonee2clone.put(_cloneeFieldAS, _cloneFieldAS);
				}
				_clone.fieldMap.put(_field, _cloneFieldAS);
				_wb.addWork(_cloneeFieldAS);
			}
		}

		// Unify the clones to reflect the relation between their originators.
		for (final Iterator _i = clonee2clone.keySet().iterator(); _i.hasNext();) {
			final AliasSet _k1 = (AliasSet) _i.next();

			for (final Iterator _j = clonee2clone.keySet().iterator(); _j.hasNext();) {
				final AliasSet _k2 = (AliasSet) _j.next();

				if (_k1.find() == _k2.find()) {
					final AliasSet _v1 = (AliasSet) clonee2clone.get(_k1);
					final AliasSet _v2 = (AliasSet) clonee2clone.get(_k2);
					unifyAliasSetHelper(_v1, _v2, false);
				}
			}
		}
	}

	/**
	 * Records the access information pertaining to the object associated with this alias set.
	 *
	 * @param value is the access information.  <code>true</code> indicates that the associated variable  was accessed;
	 * 		  <code>false</code>, otherwise.
	 *
	 * @post self.find().accessed == value
	 */
	void setAccessedTo(final boolean value) {
		((AliasSet) find()).accessed = value;
	}

	/**
	 * Retrieves an unmodifiable copy of the field map of this alias set.
	 *
	 * @return the field map.
	 */
	Map getFieldMap() {
		return Collections.unmodifiableMap(((AliasSet) find()).fieldMap);
	}

	/**
	 * Records that the object associated with this alias set is accessible globally via static fields.
	 *
	 * @post isGlobal() == true and isShared() == true and fieldMap.values()->forall(o | o.isGlobal() == true and
	 * 		 o.isShared() == true)
	 */
	void setGlobal() {
		final AliasSet _rep = (AliasSet) find();

		if (_rep.global) {
			return;
		}

		_rep.global = true;
		_rep.shared = true;

		if (_rep.fieldMap != null) {
			for (final Iterator _i = _rep.fieldMap.values().iterator(); _i.hasNext();) {
				final AliasSet _as = (AliasSet) _i.next();
				_as.setGlobal();
			}
		}
	}

	/**
	 * Checks if the object associated with this alias set is accessible globally via static fields.
	 *
	 * @return <code>true</code> if the associated object is accessible globally; <code>false</code>, otherwise.
	 *
	 * @post result == find().global
	 */
	boolean isGlobal() {
		return ((AliasSet) find()).global;
	}

	/**
	 * Marks the object associated with this alias set as appearing in a <code>notify()/notifyAll()</code> call.
	 *
	 * @post find().notifies == true
	 */
	void setNotifies() {
		((AliasSet) find()).notifies = true;
	}

	/**
	 * Marks this object such that it capture the action that the associated variable was read from.
	 */
	void setRead() {
		((AliasSet) find()).read = true;
	}

	/**
	 * Retrieves the ready entity object of this alias set.
	 *
	 * @return the associated readyentity object.
	 *
	 * @post result == self.find().readyEntity
	 */
	Collection getReadyEntities() {
		return ((AliasSet) find()).readyEntities;
	}

	/**
	 * Retrieves the shared entities of this object.
	 *
	 * @return a collection of objects.
	 */
	Collection getShareEntities() {
		return ((AliasSet) find()).shareEntities;
	}

	/**
	 * Marks the object associated with this alias set as appearing in a <code>wait()</code> call.
	 *
	 * @post find().waits == true
	 */
	void setWaits() {
		((AliasSet) find()).waits = true;
	}

	/**
	 * Marks this object such that it capture the action that the associated variable was written into.
	 */
	void setWritten() {
		((AliasSet) find()).written = true;
	}

	/**
	 * Creates a new alias set.
	 *
	 * @return a new alias set.
	 *
	 * @post result != null
	 */
	static AliasSet createAliasSet() {
		return new AliasSet();
	}

	/**
	 * Retrieves the alias set corresponding to the given field of the object represented by this alias set.
	 *
	 * @param field is the signature of the field.
	 *
	 * @return the alias set associated with <code>field</code>.
	 *
	 * @post result == self.find().fieldMap.get(field)
	 */
	AliasSet getASForField(final String field) {
		return (AliasSet) ((AliasSet) find()).fieldMap.get(field);
	}

	/**
	 * Checks if the object associated with this alias set is shared between threads.
	 *
	 * @return <code>true</code> if the object is shared; <code>false</code>, otherwise.
	 *
	 * @post result == find().shared
	 */
	boolean escapes() {
		return ((AliasSet) find()).shared;
	}

	/**
	 * Marks all reachable alias sets as being crossing thread boundary, i.e, visible in multiple threads.
	 */
	void markAsCrossingThreadBoundary() {
		if (find() != this) {
			((AliasSet) find()).markAsCrossingThreadBoundary();
		} else {
			if (markingMultiThreadAccess) {
				return;
			}
			markingMultiThreadAccess = true;
			multiThreadAccess = true;

			for (final Iterator _i = fieldMap.values().iterator(); _i.hasNext();) {
				((AliasSet) _i.next()).markAsCrossingThreadBoundary();
			}
			markingMultiThreadAccess = false;
		}
	}

	/**
	 * Propogates the information from the source alias set to the destination alias set.
	 *
	 * @param from is the source of the information transfer.
	 * @param to is the destination of the information transfer.
	 *
	 * @post to.isShared() == (from.isShared() or from.isShared())
	 * @post to.getReadyEntities().containsAll(from.getReadyEntities())
	 * @post to.getShareEntities().containsAll(from.getShareEntities())
	 */
	static void propogateInfoFromTo(final AliasSet from, final AliasSet to) {
		final IWorkBag _wb = new HistoryAwareLIFOWorkBag(new HashSet());
		_wb.addWork(new Pair(from, to, false));

		while (_wb.hasWork()) {
			final Pair _pair = (Pair) _wb.getWork();
			final AliasSet _fromRep = (AliasSet) ((AliasSet) _pair.getFirst()).find();
			final AliasSet _toRep = (AliasSet) ((AliasSet) _pair.getSecond()).find();

			if (_fromRep != _toRep) {
				_toRep.shared |= _fromRep.shared;

				/*
				 * This is tricky.  A constructor can be called to construct 2 instances in which one is used in
				 * wait/notify but not the other.  This means on top-down propogation of alias set in ECBA, the 2 alias
				 * set of the primary of the <init> method will be rep1 and one may provide a non-null ready entity to rep2
				 * and the other may come and erase it if the check is not made.
				 */
				if (_fromRep.readyEntities != null) {
					if (_toRep.readyEntities == null) {
						_toRep.readyEntities = new HashSet();
					}
					_toRep.readyEntities.addAll(_fromRep.readyEntities);
				}

				if (_fromRep.shareEntities != null) {
					if (_toRep.shareEntities == null) {
						_toRep.shareEntities = new HashSet();
					}

					_toRep.shareEntities.addAll(_fromRep.shareEntities);
				}
			}

			for (final Iterator _i = _toRep.fieldMap.keySet().iterator(); _i.hasNext();) {
				final Object _key = _i.next();
				final AliasSet _to = (AliasSet) _toRep.fieldMap.get(_key);
				final AliasSet _from = (AliasSet) _fromRep.fieldMap.get(_key);

				if ((_to != null) && (_from != null)) {
					_wb.addWork(new Pair(_from, _to, false));
				}
			}
		}
	}

	/**
	 * Records the given alias set represents the given field signature.
	 *
	 * @param field for which the alias set info needs to be recorded.
	 * @param as is the alias set associated with <code>field</code>
	 *
	 * @pre as != null
	 */
	void putASForField(final String field, final AliasSet as) {
		((AliasSet) find()).fieldMap.put(field, as.find());

		if (isGlobal()) {
			as.setGlobal();
		}
	}

	/**
	 * Unifies this object with itself.  This is required when the alias set is occurs in the context of a site which is
	 * executed multiple times, in particular, reachable from a call-site which may be executed multiple times.
	 */
	void selfUnify() {
		final AliasSet _m = (AliasSet) find();

		if (_m == this) {
			if (selfUnifying) {
				return;
			}
			selfUnifying = true;

			_m.shared |= _m.accessed;

			if (_m.waits && _m.notifies) {
				if (_m.readyEntities == null) {
					_m.readyEntities = new HashSet();
				}
				_m.readyEntities.add(getNewReadyEntity());
			}

			if (_m.read && _m.written) {
				if (_m.shareEntities == null) {
					_m.shareEntities = new HashSet();
				}
				_m.shareEntities.add(getNewShareEntity());
			}

			for (final Iterator _i = _m.fieldMap.values().iterator(); _i.hasNext();) {
				final AliasSet _fieldAS = (AliasSet) _i.next();
				_fieldAS.selfUnify();
			}

			selfUnifying = false;
		} else {
			_m.selfUnify();
		}
	}

	/**
	 * Unifies the given alias set with this alias set.
	 *
	 * @param a is the alias set to be unified with this alias set.
	 *
	 * @pre a != null
	 */
	void unifyAliasSet(final AliasSet a) {
		unifyAliasSetHelper(this, a, true);
	}

	/**
	 * Returns a new ready entity object.
	 *
	 * @return a new ready entity object.
	 *
	 * @post result != null
	 */
	private Object getNewReadyEntity() {
		return new String("Entity:" + readyEntityCount++);
	}

	/**
	 * Returns a new share entity object.
	 *
	 * @return a new share entity object.
	 *
	 * @post result != null
	 */
	private Object getNewShareEntity() {
		return new String("Entity:" + shareEntityCount++);
	}

	/**
	 * Unifies the given alias sets.
	 *
	 * @param as1 obviously.
	 * @param as2 obviously.
	 * @param unifyAll <code>true</code> indicates that unification should be multi-thread access sensitive;
	 * 		  <code>false</code>, otherwise.
	 *
	 * @pre as1 != null and as2 != null
	 */
	private static void unifyAliasSetHelper(final AliasSet as1, final AliasSet as2, final boolean unifyAll) {
		final AliasSet _m = (AliasSet) as1.find();
		final AliasSet _n = (AliasSet) as2.find();

		if (_m != _n) {
		    _m.union(_n);
		    
			final AliasSet _rep1 = (AliasSet) _m.find();
			final AliasSet _rep2;

			if (_rep1 == _m) {
				_rep2 = _n;
			} else {
				_rep2 = _m;
			}

			_rep1.waits |= _rep2.waits;
			_rep1.notifies |= _rep2.notifies;
			_rep1.accessed |= _rep2.accessed;
			_rep1.read |= _rep2.read;
			_rep1.written |= _rep2.written;
			_rep1.multiThreadAccess |= _rep2.multiThreadAccess;
			_rep1.shared |= _rep2.shared;

			if (unifyAll && _rep1.multiThreadAccess) {
				unifyEscapeInfo(_rep1, _rep2);
			}

			_rep1.unifyFields(_rep2, unifyAll);

			if (_rep1.global || _rep2.global) {
				_rep1.setGlobal();
			}
		}
	}

	/**
	 * Unify escape and sharing information in the given alias set.
	 *
	 * @param reprAliasSet is one of the alias set involved in the unification.
	 * @param aliasSet is the other alias set involved in the unification.
	 */
	private static void unifyEscapeInfo(final AliasSet reprAliasSet, final AliasSet aliasSet) {
		reprAliasSet.shared |= (reprAliasSet.accessed && aliasSet.accessed);

		if ((reprAliasSet.waits && aliasSet.notifies) || (reprAliasSet.notifies && aliasSet.waits)) {
			if (reprAliasSet.readyEntities == null) {
				reprAliasSet.readyEntities = new HashSet();
			}
			reprAliasSet.readyEntities.add(aliasSet.getNewReadyEntity());
		}

		if ((reprAliasSet.read && aliasSet.written) || (reprAliasSet.written && aliasSet.read)) {
			if (reprAliasSet.shareEntities == null) {
				reprAliasSet.shareEntities = new HashSet();
			}
			reprAliasSet.shareEntities.add(aliasSet.getNewShareEntity());
		}
	}

	/**
	 * Unify the fields of the given alias sets with that of this alias set.
	 *
	 * @param aliasSet is the other alias set involved in the unification.
	 * @param unifyAll <code>true</code> indicates that unification should be multi-thread access sensitive;
	 * 		  <code>false</code>, otherwise.
	 *
	 * @pre aliasSet != null
	 */
	private void unifyFields(final AliasSet aliasSet, final boolean unifyAll) {
		for (final Iterator _i = aliasSet.fieldMap.entrySet().iterator(); _i.hasNext();) {
			final Map.Entry _entry = (Map.Entry) _i.next();
			final Object _key = _entry.getKey();
			final Object _value = _entry.getValue();
			final AliasSet _fieldASInThis = (AliasSet) fieldMap.get(_key);
			final AliasSet _givenFieldAS = (AliasSet) _value;

			if (_fieldASInThis != null) {
				unifyAliasSetHelper(_fieldASInThis, _givenFieldAS, unifyAll);
			} else {
				fieldMap.put(_key, _value);
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.20  2004/08/01 22:58:25  venku
   - ECBA was erroneous for 2 reasons.
     - top-down propagation was not complete. FIXED.
     - cloning of alias sets was not complete. FIXED.
   - optimized certain other aspects of ECBA.
   - removed RufsEscapeAnalysis.

   Revision 1.19  2004/07/30 07:47:35  venku
   - there was a bug in escape analysis cloning and union algorithm.  FIXED.
   Revision 1.18  2004/07/17 19:37:18  venku
   - ECBA was incorrect for the following reasons.
     - it fails if the start sites are not in the same method.
     - it fails if the access in the threads occur in methods other than the
       one in which the new thread is started.
     - The above issues were addressed.
   Revision 1.17  2004/04/22 09:49:03  venku
   - coding conventions.
   Revision 1.16  2004/01/09 00:59:09  venku
   - there is no point in unifying nulls and alias sets.  Hence, non-null
     alias sets are a precondition for unify() method.
   Revision 1.15  2004/01/06 00:17:00  venku
   - Classes pertaining to workbag in package indus.graph were moved
     to indus.structures.
   - indus.structures was renamed to indus.datastructures.
   Revision 1.14  2004/01/03 21:20:06  venku
   - deleted unused methods.
   Revision 1.13  2003/12/13 02:29:08  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.12  2003/12/09 04:22:10  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.11  2003/12/08 12:15:58  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.10  2003/12/02 09:42:38  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.9  2003/10/05 16:22:25  venku
   - Interference dependence is now symbol based.
   - Both interference and ready dependence consider
     loop information in a more sound manner.
   - ripple effect of the above.
   Revision 1.8  2003/10/05 06:31:35  venku
   - Things work.  The bug was the order in which the
     parameter alias sets were being accessed.  FIXED.
   Revision 1.7  2003/10/04 22:53:45  venku
   - backup commit.
   Revision 1.6  2003/09/29 13:34:31  venku
   - #@$#%
   Revision 1.5  2003/09/28 03:17:13  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.4  2003/09/01 12:01:30  venku
   Major:
   - Ready dependence info in ECBA was flaky as it did not consider
     impact of multiple call sites with contradicting wait/notify use of
     the primary.  FIXED.
   Revision 1.3  2003/09/01 07:58:13  venku
   - Only RefType and ArrayType can have AliasSets, not NullType.
     This was fixed in canHaveAliasSet(). FIXED.
   - Propogation only occurs if both src and dest sets are non-null.
     Previous the enclosing context (MethodContext) ensured absence
     of such mismatch, but now MethodContext was relaxed. FIXED.
   Revision 1.2  2003/08/27 12:10:15  venku
   waits and notifies were not being propogated upon unification.
   This will not cause the wait/notify info to raise to the start site.  FIXED.
   Revision 1.1  2003/08/21 01:24:25  venku
    - Renamed src-escape to src-concurrency to as to group all concurrency
      issue related analyses into a package.
    - Renamed escape package to concurrency.escape.
    - Renamed EquivalenceClassBasedAnalysis to EquivalenceClassBasedEscapeAnalysis.
   Revision 1.2  2003/08/11 06:29:07  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.1  2003/08/07 06:39:07  venku
   Major:
    - Moved the package under indus umbrella.
   Minor:
    - changes to accomodate ripple effect from support package.
   Revision 1.1  2003/07/27 20:52:39  venku
   First of the many refactoring while building towards slicer release.
   This is the escape analysis refactored and implemented as per to tech report.
 */
