
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
	 * This is used to indicate that the alias set represents an object that is accessible in multiple threads. This differs
	 * from <code>shared</code> as this captures the exposure of the object in multiple threads and not the access.
	 */
	private boolean multiThreadAccessibility;

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
	 * This indicates if the variable (hence, the object referred to) associated with this alias set is shared via access
	 * across threads. This is different from <code>multiThreadAccess</code> as this captures access in multiple threads and
	 * not accessibility.
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
		multiThreadAccessibility = false;
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
											   .append("multiThreadAccess", this.multiThreadAccessibility)
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
	 * Checks if the given alias set is side-affected. Basically, it checks if the variables associated with this alias set
	 * were written to.
	 *
	 * @param paramAS is the alias set to be checked.
	 * @param recurse <code>true</code> indicates if alias sets reachable from this alias set should be considered;
	 * 		  <code>false</code>, otherwise.
	 *
	 * @return <code>true</code> if this alias set is side-affected; <code>false</code>, otherwise.
	 *
	 * @pre paramAS != null
	 */
	static boolean isSideAffected(final AliasSet paramAS, final boolean recurse) {
		boolean _result = paramAS == null;

		if (!_result) {
			_result = paramAS.isSideAffected(new HashSet(), recurse);
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
	 * Retrieves the end point of the specified access path.
	 *
	 * @param accesspath is a sequence of primaries in the access path.  This should not include the  primary for this alias
	 * 		  set. It should contain the stringized form of the field signature or <code>IEscapeInfo.ARRAY_FIELD</code>.
	 *
	 * @return the alias set that corresponds to the end point of the access path.  <code>null</code> is returned if no such
	 * 		   path exists.
	 */
	AliasSet getAccessPathEndPoint(final String[] accesspath) {
		AliasSet _result = this;
		final int _length = accesspath.length;

		for (int _i = 0; _i < _length; _i++) {
			final AliasSet _as = (AliasSet) _result.getFieldMap().get(accesspath[_i]);

			if (_as != null) {
				_result = (AliasSet) _as.find();
			} else {
				_result = null;
				break;
			}
		}
		return _result;
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
		_rep.multiThreadAccessibility = true;

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
			_as.multiThreadAccessibility = true;

			for (final Iterator _i = _as.fieldMap.values().iterator(); _i.hasNext();) {
				_wb.addWork(((AliasSet) _i.next()).find());
			}
		}
	}

	/**
	 * Propogates the information from the this alias set to the destination alias set.
	 *
	 * @param to is the destination of the information transfer.
	 *
	 * @post to.isShared() == (isShared() or to.isShared())
	 * @post to.getReadyEntities().containsAll(getReadyEntities())
	 * @post to.getShareEntities().containsAll(getShareEntities())
	 */
	void propogateInfoFromTo(final AliasSet to) {
		final IWorkBag _wb = new HistoryAwareLIFOWorkBag(new HashSet());
		_wb.addWork(new Pair(this, to));

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

				if (_repr.readyEntities.isEmpty()) {
					_repr.readyEntities.add(getNewReadyEntity());
				}
			}

			if (_repr.read && _repr.written) {
				if (_repr.shareEntities == null) {
					_repr.shareEntities = new HashSet();
				}

				if (_repr.shareEntities.isEmpty()) {
					_repr.shareEntities.add(getNewShareEntity());
				}
			}

			_wb.addAllWorkNoDuplicates(_repr.fieldMap.values());
		}
	}

	/**
	 * Retrieves the alias set that is reachable from <code>root</code> and that corresponds to <code>ref</code>, an alias
	 * set reachable from this alias set. This method can be used to retrieve the alias set in the site-context
	 * corresponding to an alias side in the callee method context.
	 *
	 * @param root the alias set to start point in the search context.
	 * @param ref the alias set in end point in the reference (this) context.
	 * @param processed a collection of alias set pairs that is used during the search.  The contents of this alias set is
	 * 		  not relevant to the caller.
	 *
	 * @return the alias set reachable from <code>root</code> and that corresponds to <code>ref</code>.  This will be
	 * 		   <code>null</code> if there is no such alias set.
	 *
	 * @pre root != null and ref != null and processed != null
	 */
	AliasSet getImageOfRefUnderRoot(final AliasSet root, final AliasSet ref, final Collection processed) {
		AliasSet _result = null;

		// THINK: Should the following condition be ref.find() == find() && root.escapes()?
		if (ref.find() == find()) {
			_result = root;
		} else {
			processed.add(new Pair(find(), root.find()));

			final Set _keySet = getFieldMap().keySet();
			final Iterator _i = _keySet.iterator();
			final int _iEnd = _keySet.size();

			for (int _iIndex = 0; _iIndex < _iEnd && _result == null; _iIndex++) {
				final String _key = (String) _i.next();
				final AliasSet _as1 = getASForField(_key);
				final AliasSet _as2 = root.getASForField(_key);

				if (_as1 != null && _as2 != null) {
					final Pair _pair = new Pair(_as1.find(), _as2.find());

					if (!processed.contains(_pair)) {
						_result = _as1.getImageOfRefUnderRoot(_as2, ref, processed);
					}
				}
			}
		}
		return _result;
	}

	/**
	 * Unifies the given alias set with this alias set.
	 *
	 * @param a is the alias set to be unified with this alias set.
	 *
	 * @pre a != null
	 */
	void unifyAliasSet(final AliasSet a) {
		unifyAliasSetHelper(a, true);
	}

	/**
	 * Unifies the given alias set with this alias set.
	 *
	 * @param as2 obviously.
	 * @param unifyAll <code>true</code> indicates that unification should be multi-thread access sensitive;
	 * 		  <code>false</code>, otherwise.
	 *
	 * @pre as2 != null
	 */
	void unifyAliasSetHelper(final AliasSet as2, final boolean unifyAll) {
		final AliasSet _m = (AliasSet) find();
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
			_representative.multiThreadAccessibility |= _represented.multiThreadAccessibility;
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

			if (unifyAll) {
				if (_representative.multiThreadAccessibility) {
					_representative.unifyThreadEscapeInfo(_represented);
				}
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
		return "ReadyEntity:" + readyEntityCount++;
	}

	/**
	 * Returns a new share entity object.
	 *
	 * @return a new share entity object.
	 *
	 * @post result != null
	 */
	private static Object getNewShareEntity() {
		return "ShareEntity:" + shareEntityCount++;
	}

	/**
	 * Checks if this alias set is side-affected.  Basically, it checks if the variables associated with this alias set were
	 * written to.
	 *
	 * @param visitedASs is a collection of alias sets that are traversed while checking for side-effect.
	 * @param recurse <code>true</code> indicates if alias sets reachable from this alias set should be considered;
	 * 		  <code>false</code>, otherwise.
	 *
	 * @return <code>true</code> if this alias set is side-affected; <code>false</code>, otherwise.
	 *
	 * @pre visitedASs != null and visitedASs.oclIsKindOf(Collection(AliasSet))
	 */
	private boolean isSideAffected(final Collection visitedASs, final boolean recurse) {
		boolean _result = false;
		final AliasSet _rep = (AliasSet) find();

		if (!visitedASs.contains(_rep)) {
			final Collection _fieldASs = _rep.getFieldMap().values();
			final Iterator _i = _fieldASs.iterator();
			final int _iEnd = _fieldASs.size();

			for (int _iIndex = 0; _iIndex < _iEnd && !_result; _iIndex++) {
				final AliasSet _fieldAS = (AliasSet) _i.next();
				_result |= _fieldAS.written;
				visitedASs.add(_fieldAS);
			}

			if (!_result && recurse) {
				for (int _iIndex = 0; _iIndex < _iEnd && !_result; _iIndex++) {
					final AliasSet _fieldAS = (AliasSet) _i.next();
					_result |= _fieldAS.isSideAffected(visitedASs, recurse);
				}
			}
		}
		return _result;
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
				_repAS.unifyAliasSetHelper(_fieldAS, unifyAll);
			} else {
				putASForField(_field, _fieldAS);
			}
		}
	}

	/**
	 * Unify thread escape and sharing information in the given alias set.
	 *
	 * @param represented is the other alias set involved in the unification.
	 *
	 * @pre represented != null
	 */
	private void unifyThreadEscapeInfo(final AliasSet represented) {
		shared |= (accessed && represented.accessed);

		if ((waits && represented.notifies) || (notifies && represented.waits)) {
			if (readyEntities == null) {
				readyEntities = new HashSet();
			}

			if (readyEntities.isEmpty()) {
				readyEntities.add(getNewReadyEntity());
			}
		}

		if ((read && represented.written) || (written && represented.read)) {
			if (shareEntities == null) {
				shareEntities = new HashSet();
			}

			if (shareEntities.isEmpty()) {
				shareEntities.add(getNewShareEntity());
			}
		}
	}
}

// End of File
