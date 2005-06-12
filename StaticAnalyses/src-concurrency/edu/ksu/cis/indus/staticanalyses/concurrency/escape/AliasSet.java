
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
import edu.ksu.cis.indus.common.datastructures.HistoryAwareFIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.HistoryAwareLIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;

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
	 * This is used to generate unique lock entities.
	 */
	private static int lockEntityCount;

	/** 
	 * This is used to retrieve the representative of the alias set.
	 */
	private static final Transformer REPRESENTATIVE_ALIAS_SET_RETRIEVER =
		new Transformer() {
			public Object transform(final Object input) {
				return ((AliasSet) input).find();
			}
		};

	/** 
	 * This represents the lock entities associated with this alias set.
	 */
	private Collection lockEntities;

	/** 
	 * This is the signatures of the fields of the objects associated with this alias set that are read.
	 *
	 * @invariant writtenThreads.oclIsKindOf(Collection(String))
	 */
	private Collection readFields = new ArrayList();

	/** 
	 * The threads that read fields of the associated object.
	 */
	private Collection readThreads;

	/** 
	 * This represents the ready Entities associated with this alias set.
	 */
	private Collection readyEntities;

	/** 
	 * This represents the ready Entities associated with this alias set.
	 */
	private Collection shareEntities;

	/** 
	 * The threads that write fields of the associated object.
	 */
	private Collection writeThreads;

	/** 
	 * This is the signatures of the fields of the objects associated with this alias set that are written.
	 *
	 * @invariant writtenThreads.oclIsKindOf(Collection(String))
	 */
	private Collection writtenFields = new ArrayList();

	/** 
	 * This maps field signatures to their alias sets.
	 *
	 * @invariant fieldMap.oclIsKindOf(Map(String, AliasSet))
	 */
	private Map fieldMap;

	/** 
	 * This indicates if the object associated with the alias set was accessed.  This is related to read-write info and not to
	 * escape or shared info.
	 */
	private boolean accessed;

	/** 
	 * This indicates if this alias set is associated with a static field.
	 */
	private boolean global;

	/** 
	 * This indicates the alias set participated in a locking operation.
	 */
	private boolean locked;

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
	 * Creates a new instance of this class.
	 */
	private AliasSet() {
		fieldMap = new HashMap();
		shared = false;
		global = false;
		accessed = false;
		readyEntities = null;
		readThreads = new HashSet();
		writeThreads = new HashSet();
		shareEntities = null;
		lockEntities = null;
		multiThreadAccessibility = false;
		readFields = Collections.EMPTY_SET;
		writtenFields = Collections.EMPTY_SET;
	}

	/**
	 * Marks the alias set as participating in a lock operation.
	 */
	public void setLocked() {
		((AliasSet) find()).locked = true;
	}

	/**
	 * Retrieves the threads that read fields of the associated object.
	 *
	 * @return the reading threads.
	 */
	public Collection getReadThreads() {
		return Collections.unmodifiableCollection(((AliasSet) find()).readThreads);
	}

	/**
	 * Retrieves the threads that write fields of the associated object.
	 *
	 * @return the writing threads.
	 */
	public Collection getWriteThreads() {
		return Collections.unmodifiableCollection(((AliasSet) find()).writeThreads);
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
				_clone.readyEntities = (Collection) ((HashSet) readyEntities).clone();
			}

			if (shareEntities != null) {
				_clone.shareEntities = (Collection) ((HashSet) shareEntities).clone();
			}

			if (readFields != Collections.EMPTY_SET) {
				_clone.readFields = (Collection) ((HashSet) readFields).clone();
			}

			if (writtenFields != Collections.EMPTY_SET) {
				_clone.writtenFields = (Collection) ((HashSet) writtenFields).clone();
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
					new ToStringBuilder(this).append("waits", this.waits).append("writtenFields", this.writtenFields)
											   .append("global", this.global).append("readyEntities", this.readyEntities)
											   .append("multiThreadAccess", this.multiThreadAccessibility)
											   .append("shared", this.shared).append("shareEntities", this.shareEntities)
											   .append("notifies", this.notifies).append("readFields", this.readFields)
											   .append("readThreads", readThreads).append("writeThreads", writeThreads)
											   .append("lockEntities", this.lockEntities).append("fieldMap", this.fieldMap)
											   .toString();
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
	 * Retrieves the lock entities of this object.
	 *
	 * @return a collection of objects.
	 */
	Collection getLockEntities() {
		return ((AliasSet) find()).lockEntities;
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
	 * Adds the given field signature to the collection of read fields of this alias set's object.
	 *
	 * @param fieldSig is the field signature.
	 */
	void addReadField(final String fieldSig) {
		final AliasSet _l = (AliasSet) find();

		if (_l.readFields == Collections.EMPTY_SET) {
			_l.readFields = new HashSet();
		}
		_l.readFields.add(fieldSig);
	}

	/**
	 * Adds the given threads as the threads that read a member of an object via the variable associated with this aliasset.
	 *
	 * @param abstractThreads collection of abstract thread objects in which the write occurs.
	 */
	void addReadThreads(final Collection abstractThreads) {
		((AliasSet) find()).readThreads.addAll(abstractThreads);
	}

	/**
	 * Adds the given threads as the threads that wrote a member of an object via the variable associated with this aliasset.
	 *
	 * @param abstractThreads collection of abstract thread objects in which the write occurs.
	 */
	void addWriteThreads(final Collection abstractThreads) {
		((AliasSet) find()).writeThreads.addAll(abstractThreads);
	}

	/**
	 * Adds the given field signature to the collection of written fields of this alias set's object.
	 *
	 * @param fieldSig is the field signature.
	 */
	void addWrittenField(final String fieldSig) {
		final AliasSet _l = (AliasSet) find();

		if (_l.writtenFields == Collections.EMPTY_SET) {
			_l.writtenFields = new HashSet();
		}
		_l.writtenFields.add(fieldSig);
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
	 * Marks the alias set was accessed.
	 */
	void setAccessed() {
		((AliasSet) find()).accessed = true;
	}

	/**
	 * Checks if the alias set was accessed.
	 *
	 * @return <code>true</code> if it was accessed; <code>false</code>, otherwise.
	 */
	boolean isAccessed() {
		return ((AliasSet) find()).accessed;
	}

	/**
	 * Checks if the object associated with this alias set is accessible in multiple threads.
	 *
	 * @return <code>true</code> if the object is shared; <code>false</code>, otherwise.
	 *
	 * @post result == find().shared
	 */
	boolean escapes() {
		return ((AliasSet) find()).multiThreadAccessibility;
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
				_toRep.multiThreadAccessibility |= _fromRep.multiThreadAccessibility;

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

				if (_fromRep.lockEntities != null && _toRep.lockEntities != null) {
					_toRep.lockEntities.addAll(_fromRep.lockEntities);
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
		_wb.addWork(as.find());

		while (_wb.hasWork()) {
			final AliasSet _repr = (AliasSet) _wb.getWork();
			_repr.unifyThreadEscapeInfo(_repr);
			_wb.addAllWorkNoDuplicates(CollectionUtils.collect(_repr.fieldMap.values(), REPRESENTATIVE_ALIAS_SET_RETRIEVER));
		}
	}

	/**
	 * Checks if the object associated with this alias set is accessed between threads.
	 *
	 * @return <code>true</code> if the object is shared; <code>false</code>, otherwise.
	 *
	 * @post result == find().shared
	 */
	boolean shared() {
		return ((AliasSet) find()).shared;
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
			_representative.multiThreadAccessibility |= _represented.multiThreadAccessibility;
			_representative.shared |= _represented.shared;
			_representative.global |= _represented.global;
			_representative.locked |= _represented.locked;
			_representative.accessed |= _represented.accessed;
			_representative.readThreads.addAll(_represented.readThreads);
			_represented.readThreads = null;
			_representative.writeThreads.addAll(_represented.writeThreads);
			_represented.writeThreads = null;

			if (unifyAll && _representative.multiThreadAccessibility) {
				_representative.unifyThreadEscapeInfo(_represented);
			}

			// We need to unify the read/write fields after unification as they are used to calcuate shared entities in 
			// unifyThreadEscapeInfo()
			_representative.handleInfoUnification(_represented);

			_representative.unifyFields(_represented, unifyAll);

			if (_representative.isGlobal()) {
				_representative.setGlobal();
			}
		}
	}

	/**
	 * Checks if the a field of the object associated with alias set was read.
	 *
	 * @return <code>true</code> if it was read; <code>false</code>, otherwise.
	 */
	boolean wasAnyFieldRead() {
		return !((AliasSet) find()).readFields.isEmpty();
	}

	/**
	 * Checks if the a field of the object associated with alias set was written.
	 *
	 * @return <code>true</code> if it was written; <code>false</code>, otherwise.
	 */
	boolean wasAnyFieldWritten() {
		return !((AliasSet) find()).writtenFields.isEmpty();
	}

	/**
	 * Checks if the given field or an object reachable from it was read.
	 *
	 * @param fieldSig is the field signature.
	 * @param recurse <code>true</code> indicates if alias sets reachable from this alias set should be considered;
	 * 		  <code>false</code>, otherwise.
	 *
	 * @return <code>true</code> if this alias set was read or if the given alias set is <code>null</code>;
	 * 		   <code>false</code>, otherwise.
	 */
	boolean wasFieldRead(final String fieldSig, final boolean recurse) {
		boolean _result = wasFieldRead(fieldSig);

		if (!_result && recurse) {
			_result =
				recursiveBooleanPropertyDiscovery(new Transformer() {
						public Object transform(final Object input) {
							return Boolean.valueOf(((AliasSet) input).wasAnyFieldRead());
						}
					});
		}
		return _result;
	}

	/**
	 * Checks if the field of the provided signature was read via the object associated with this alias set.
	 *
	 * @param fieldSig is the field signature.
	 *
	 * @return <code>true</code> if it was read; <code>false</code>, otherwise.
	 */
	boolean wasFieldRead(final String fieldSig) {
		return ((AliasSet) find()).readFields.contains(fieldSig);
	}

	/**
	 * Checks if the given field or an object reachable from it was written.
	 *
	 * @param fieldSig is the field signature.
	 * @param recurse <code>true</code> indicates if alias sets reachable from this alias set should be considered;
	 * 		  <code>false</code>, otherwise.
	 *
	 * @return <code>true</code> if this alias set was written or if the given alias set is <code>null</code>;
	 * 		   <code>false</code>, otherwise.
	 */
	boolean wasFieldWritten(final String fieldSig, final boolean recurse) {
		boolean _result = wasFieldWritten(fieldSig);

		if (!_result && recurse) {
			_result =
				recursiveBooleanPropertyDiscovery(new Transformer() {
						public Object transform(final Object input) {
							return Boolean.valueOf(((AliasSet) input).wasAnyFieldWritten());
						}
					});
		}
		return _result;
	}

	/**
	 * Checks if the field of the provided signature was read via the object associated with this alias set.
	 *
	 * @param fieldSig is the field signature.
	 *
	 * @return <code>true</code> if it was read; <code>false</code>, otherwise.
	 */
	boolean wasFieldWritten(final String fieldSig) {
		return ((AliasSet) find()).writtenFields.contains(fieldSig);
	}

	/**
	 * Returns a new lock entity object.
	 *
	 * @return a new lock entity object.
	 *
	 * @post result != null
	 */
	private static Object getNewLockEntity() {
		return "LockEntity:" + lockEntityCount++;
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
	 * Handles the unification of entity info.
	 *
	 * @param represented is the alias set being unified with this alias set such that this alias set is the
     * representative alias set.
     * @pre represented != null 
	 */
	private void handleInfoUnification(final AliasSet represented) {
		if (lockEntities == null) {
			lockEntities = represented.lockEntities;
		} else if (represented.lockEntities != null) {
			lockEntities.addAll(represented.lockEntities);
		}
		represented.lockEntities = null;

		if (readyEntities == null) {
			readyEntities = represented.readyEntities;
		} else if (represented.readyEntities != null) {
			readyEntities.addAll(represented.readyEntities);
		}
		represented.readyEntities = null;

		if (shareEntities == null) {
			shareEntities = represented.shareEntities;
		} else if (represented.shareEntities != null) {
			shareEntities.addAll(represented.shareEntities);
		}
		readyEntities = null;

		if (readFields == Collections.EMPTY_SET) {
			readFields = represented.readFields;
		} else {
			readFields.addAll(represented.readFields);
		}
		represented.readFields = null;

		if (writtenFields == Collections.EMPTY_SET) {
			writtenFields = represented.writtenFields;
		} else {
			writtenFields.addAll(represented.writtenFields);
		}
		represented.writtenFields = null;
	}

	/**
	 * Discovers a boolean property recursively through the alias set tree.
	 *
	 * @param transformer is used to extract the property.
	 *
	 * @return <code>true</code> if the property holds on an alias set reachable from this alias set; <code>false</code>,
	 * 		   otherwise.
	 *
	 * @pre transformer != null
	 */
	private boolean recursiveBooleanPropertyDiscovery(final Transformer transformer) {
		boolean _result = false;
		final IWorkBag _wb = new HistoryAwareFIFOWorkBag(new HashSet());
		_wb.addWork(find());

		while (_wb.hasWork() && !_result) {
			final AliasSet _rep = (AliasSet) _wb.getWork();
			_result |= ((Boolean) transformer.transform(_rep)).booleanValue();

			if (!_result) {
				final Collection _values = _rep.getFieldMap().values();
				_wb.addAllWorkNoDuplicates(CollectionUtils.collect(_values, REPRESENTATIVE_ALIAS_SET_RETRIEVER));
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
		if ((waits && represented.notifies) || (notifies && represented.waits)) {
			if (readyEntities == null) {
				readyEntities = new HashSet();
			}
			readyEntities.add(getNewReadyEntity());
		}

		if (locked && represented.locked) {
			if (lockEntities == null) {
				lockEntities = new HashSet();
			}
			lockEntities.add(getNewLockEntity());
		}

		if (CollectionUtils.containsAny(readFields, represented.writtenFields)
			  || CollectionUtils.containsAny(writtenFields, represented.readFields)) {
			if (shareEntities == null) {
				shareEntities = new HashSet();
			}
			shareEntities.add(getNewShareEntity());
		}

		shared |= !(shareEntities != null && shareEntities.isEmpty() && readyEntities != null && readyEntities.isEmpty()
		  && lockEntities != null && lockEntities.isEmpty());
	}
}

// End of File
