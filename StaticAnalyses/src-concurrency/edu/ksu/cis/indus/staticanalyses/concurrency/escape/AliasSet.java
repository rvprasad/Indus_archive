
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
			_result = ((AliasSet) find()).clone();
		} else {
			final AliasSet _clone = (AliasSet) super.clone();

			_clone.fieldMap = (Map) ((HashMap) fieldMap).clone();
			_clone.fieldMap.clear();

			if (readyEntities != null) {
				_clone.readyEntities = (HashSet) ((HashSet) readyEntities).clone();
			}

			if (shareEntities != null) {
				_clone.shareEntities = (HashSet) ((HashSet) shareEntities).clone();
			}
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
				_result = Integer.toHexString(hashCode());
			} else {
				stringifying = true;
				_result =
					new ToStringBuilder(this).append("waits", this.waits).append("written", this.written)
											   .append("global", this.global).append("accessed", this.accessed)
											   .append("readyEntities", this.readyEntities)
											   .append("multiThreadAccess", this.multiThreadAccess)
											   .append("shared", this.shared).append("shareEntities", this.shareEntities)
											   .append("notifies", this.notifies).append("read", this.read)
											   .append("fieldMap", this.fieldMap).toString();
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

		_rep.global = true;
		_rep.shared = true;
		_rep.multiThreadAccess = true;

		if (_rep.fieldMap != null) {
			for (final Iterator _i = _rep.fieldMap.values().iterator(); _i.hasNext();) {
				final AliasSet _as = (AliasSet) _i.next();

				if (!_as.isGlobal()) {
					_as.setGlobal();
				}
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
		final IWorkBag _wb = new HistoryAwareLIFOWorkBag(new HashSet());
		_wb.addWork(find());

		while (_wb.hasWork()) {
			final AliasSet _as = (AliasSet) _wb.getWork();
			_as.multiThreadAccess = true;

			for (final Iterator _i = _as.fieldMap.values().iterator(); _i.hasNext();) {
				_wb.addWork(((AliasSet) _i.next()).find());
			}
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
		_wb.addWork(new Pair(from, to));

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

				for (final Iterator _i = _toRep.getFieldMap().keySet().iterator(); _i.hasNext();) {
					final String _field = (String) _i.next();
					final AliasSet _to = _toRep.getASForField(_field);
					final AliasSet _from = _fromRep.getASForField(_field);

					if ((_to != null) && (_from != null)) {
						_wb.addWork(new Pair(_from, _to));
					}
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
		((AliasSet) find()).fieldMap.put(field, as);

		if (isGlobal()) {
			as.setGlobal();
		}
	}

	/**
	 * Unifies the given object with itself.  This is required when the alias set is occurs in the context of a site which is
	 * executed multiple times, in particular, reachable from a call-site which may be executed multiple times.
	 *
	 * @param as the alias set to be unified with itself.
	 *
	 * @pre as != null
	 */
	static void selfUnify(final AliasSet as) {
		final Collection _processed = new HashSet();
		final IWorkBag _wb = new HistoryAwareLIFOWorkBag(_processed);
		_wb.addWork(as);

		while (_wb.hasWork()) {
			final AliasSet _m = (AliasSet) _wb.getWork();
			final AliasSet _repr = (AliasSet) _m.find();

			if (_repr != _m) {
				_processed.add(_repr);
			}

			_repr.shared |= _repr.accessed;

			if (_repr.waits && _repr.notifies) {
				if (_repr.readyEntities == null) {
					_repr.readyEntities = new HashSet();
				}
				_repr.readyEntities.add(getNewReadyEntity());
			}

			if (_repr.read && _repr.written) {
				if (_repr.shareEntities == null) {
					_repr.shareEntities = new HashSet();
				}
				_repr.shareEntities.add(getNewShareEntity());
			}

			_wb.addAllWorkNoDuplicates(_repr.fieldMap.values());
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
	 * Unifies the given alias sets.
	 *
	 * @param as1 obviously.
	 * @param as2 obviously.
	 * @param unifyAll <code>true</code> indicates that unification should be multi-thread access sensitive;
	 * 		  <code>false</code>, otherwise.
	 *
	 * @pre as1 != null and as2 != null
	 */
	static void unifyAliasSetHelper(final AliasSet as1, final AliasSet as2, final boolean unifyAll) {
		final AliasSet _m = (AliasSet) as1.find();
		final AliasSet _n = (AliasSet) as2.find();

		if (_m != _n) {
			_m.union(_n);

			final AliasSet _representative = (AliasSet) _m.find();
			final AliasSet _represented;

			if (_representative == _m) {
				_represented = _n;
			} else {
				_represented = _m;
			}

			_representative.waits |= _represented.waits;
			_representative.notifies |= _represented.notifies;
			_representative.accessed |= _represented.accessed;
			_representative.read |= _represented.read;
			_representative.written |= _represented.written;
			_representative.multiThreadAccess |= _represented.multiThreadAccess;
			_representative.shared |= _represented.shared;
			_representative.global |= _represented.global;

			if (_represented.readyEntities != null) {
				if (_representative.readyEntities == null) {
					_representative.readyEntities = _represented.readyEntities;
				} else {
					_representative.readyEntities.addAll(_represented.readyEntities);
				}
			}

			if (_represented.shareEntities != null) {
				if (_representative.shareEntities == null) {
					_representative.shareEntities = _represented.shareEntities;
				} else {
					_representative.shareEntities.addAll(_represented.shareEntities);
				}
			}

			if (unifyAll && _representative.multiThreadAccess) {
				unifyEscapeInfo(_representative, _represented);
			}

			_representative.unifyFields(_represented, unifyAll);

			if (_representative.isGlobal()) {
				_representative.setGlobal();
			}
		}
	}

	/**
	 * Returns a new ready entity object.
	 *
	 * @return a new ready entity object.
	 *
	 * @post result != null
	 */
	private static Object getNewReadyEntity() {
		return new String("ReadyEntity:" + readyEntityCount++);
	}

	/**
	 * Returns a new share entity object.
	 *
	 * @return a new share entity object.
	 *
	 * @post result != null
	 */
	private static Object getNewShareEntity() {
		return new String("ShareEntity:" + shareEntityCount++);
	}

	/**
	 * Unify escape and sharing information in the given alias set.
	 *
	 * @param representative is one of the alias set involved in the unification.
	 * @param represented is the other alias set involved in the unification.
	 */
	private static void unifyEscapeInfo(final AliasSet representative, final AliasSet represented) {
		representative.shared |= (representative.accessed && represented.accessed);

		if ((representative.waits && represented.notifies) || (representative.notifies && represented.waits)) {
			if (representative.readyEntities == null) {
				representative.readyEntities = new HashSet();
			}
			representative.readyEntities.add(getNewReadyEntity());
		}

		if ((representative.read && represented.written) || (representative.written && represented.read)) {
			if (representative.shareEntities == null) {
				representative.shareEntities = new HashSet();
			}
			representative.shareEntities.add(getNewShareEntity());
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
			final String _field = (String) _entry.getKey();
			final AliasSet _fieldAS = (AliasSet) _entry.getValue();
			final AliasSet _repAS = getASForField(_field);

			if (_repAS != null) {
				unifyAliasSetHelper(_repAS, _fieldAS, unifyAll);
			} else {
				putASForField(_field, _fieldAS);
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.24  2004/08/04 10:51:11  venku
   - INTERIM commit to enable working acorss sites.
   Revision 1.23  2004/08/02 10:30:26  venku
   - resolved few more issues in escape analysis.
   Revision 1.22  2004/08/02 07:33:45  venku
   - small but significant change to the pair manager.
   - ripple effect.
   Revision 1.21  2004/08/01 23:27:08  venku
   - changed output of toString().
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
