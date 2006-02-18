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

import edu.ksu.cis.indus.common.collections.CollectionUtils;
import edu.ksu.cis.indus.common.collections.ITransformer;
import edu.ksu.cis.indus.common.collections.SetUtils;
import edu.ksu.cis.indus.common.datastructures.FastUnionFindElement;
import edu.ksu.cis.indus.common.datastructures.HistoryAwareFIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.HistoryAwareLIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Triple;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;

import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.jimple.InvokeStmt;

/**
 * This class represents an alias set as specified in the techreport <a
 * href="http://www.cis.ksu.edu/santos/papers/technicalReports/SAnToS-TR2003-6.pdf">Honing the Detection of Interference and
 * Ready Dependence for Slicing Concurrent Java Programs.</a> It represents an equivalence class in escape analysis defined
 * in the same document.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
final class AliasSet
		extends FastUnionFindElement<AliasSet>
		implements Cloneable {

	/**
	 * This is used to generate unique lock entities.
	 */
	private static int lockEntityCount;

	/**
	 * This is used to generate unique ready entities.
	 */
	private static long readyEntityCount;

	/**
	 * DOCUMENT ME!
	 */
	private static int referenceEntityCount;

	/**
	 * This is used to retrieve the representative of the alias set.
	 */
	private static final ITransformer<AliasSet, AliasSet> REPRESENTATIVE_ALIAS_SET_RETRIEVER = new ITransformer<AliasSet, AliasSet>() {

		public AliasSet transform(final AliasSet input) {
			return input.find();
		}
	};

	/**
	 * This is used to generate unique share entities.
	 */
	private static int shareEntityCount;

	/**
	 * This indicates if the object associated with the alias set was accessed. This is related to read-write info and not to
	 * escape or shared info.
	 */
	private boolean accessed;

	/**
	 * This maps field signatures to their alias sets.
	 */
	private Map<String, AliasSet> fieldMap;

	/**
	 * DOCUMENT ME!
	 */
	private Collection<Object> intraProcRefEntities;

	/**
	 * This indicates the alias set participated in a locking operation.
	 */
	private boolean locked;

	/**
	 * This represents the lock entities associated with this alias set.
	 */
	private Collection<Object> lockEntities;

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
	 * This is the signatures of the fields of the objects associated with this alias set that are read.
	 */
	private Collection<String> readFields;

	/**
	 * The threads that read fields of the associated object.
	 */
	private Collection<Triple<InvokeStmt, SootMethod, SootClass>> readThreads;

	/**
	 * This represents the read-write entities associated with this alias set.
	 */
	private Collection<Object> readwriteEntities;

	/**
	 * This represents the ready Entities associated with this alias set.
	 */
	private Collection<Object> readyEntities;

	/**
	 * This is the collection of field signatures of fields of this alias set's object that are shared across multiple threads
	 * via read-write access.
	 */
	private Collection<String> sigsOfRWSharedFields;

	/**
	 * This is the collection of field signatures of fields of this alias set's object that are shared across multiple threads
	 * via write-write access.
	 */
	private Collection<String> sigsOfWWSharedFields;

	/**
	 * This indicates that this object is being stringified.
	 */
	private boolean stringifying;

	/**
	 * This indicates if the variable associated with this alias set is the receiver of <code>wait()</code> call.
	 */
	private boolean waits;

	/**
	 * The threads that write fields of the associated object.
	 */
	private Collection<Triple<InvokeStmt, SootMethod, SootClass>> writeThreads;

	/**
	 * This represents the write-write entities associated with this alias set.
	 */
	private Collection<Object> writewriteEntities;

	/**
	 * This is the signatures of the fields of the objects associated with this alias set that are written.
	 */
	private Collection<String> writtenFields;

	/**
	 * Creates a new instance of this class.
	 */
	private AliasSet() {
		fieldMap = new HashMap<String, AliasSet>();
		accessed = false;
		readyEntities = null;
		readThreads = new HashSet<Triple<InvokeStmt, SootMethod, SootClass>>();
		writeThreads = new HashSet<Triple<InvokeStmt, SootMethod, SootClass>>();
		readwriteEntities = null;
		writewriteEntities = null;
		sigsOfRWSharedFields = null;
		sigsOfWWSharedFields = null;
		lockEntities = null;
		intraProcRefEntities = null;
		multiThreadAccessibility = false;
		readFields = Collections.emptySet();
		writtenFields = Collections.emptySet();
	}

	/**
	 * Creates a new alias set.
	 * 
	 * @return a new alias set.
	 * @post result != null
	 */
	static AliasSet createAliasSet() {
		return new AliasSet();
	}

	/**
	 * Creates an alias set suitable for the given type.
	 * 
	 * @param type is the type from which Alias set is requested.
	 * @return the alias set corresponding to the given type.
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
	 * Unifies the given object with itself. This is required when the alias set is occurs in the context of a site which is
	 * executed multiple times, in particular, reachable from a call-site which may be executed multiple times.
	 * 
	 * @param as the alias set to be unified with itself.
	 * @pre as != null
	 */
	static void selfUnify(final AliasSet as) {
		final Collection<AliasSet> _processed = new HashSet<AliasSet>();
		final IWorkBag<AliasSet> _wb = new HistoryAwareLIFOWorkBag<AliasSet>(_processed);
		_wb.addWork(as.find());

		while (_wb.hasWork()) {
			final AliasSet _repr = _wb.getWork();
			_repr.unifyThreadEscapeInfo(_repr);
			_wb.addAllWorkNoDuplicates(CollectionUtils.collect(_repr.fieldMap.values(), REPRESENTATIVE_ALIAS_SET_RETRIEVER));
		}
	}

	/**
	 * Returns a new lock entity object.
	 * 
	 * @return a new lock entity object.
	 * @post result != null
	 */
	private static Object getNewLockEntity() {
		return "LockEntity:" + lockEntityCount++;
	}

	/**
	 * Returns a new ready entity object.
	 * 
	 * @return a new ready entity object.
	 * @post result != null
	 */
	private static Object getNewReadyEntity() {
		return "ReadyEntity:" + readyEntityCount++;
	}

	/**
	 * Returns a new lock entity object.
	 * 
	 * @return a new lock entity object.
	 * @post result != null
	 */
	private static Object getNewReferenceEntity() {
		return "ReferenceEntity:" + referenceEntityCount++;
	}

	/**
	 * Returns a new share entity object.
	 * 
	 * @return a new share entity object.
	 * @post result != null
	 */
	private static Object getNewShareEntity() {
		return "ShareEntity:" + shareEntityCount++;
	}

	/**
	 * Clones this alias set.
	 * 
	 * @return the clone of this object.
	 * @throws CloneNotSupportedException is thrown if it is thrown by <code>java.lang.Object.clone()</code>.
	 * @post result != null and result.set != null and result.fieldMap != self.fieldMap
	 */
	@Override public AliasSet clone() throws CloneNotSupportedException {
		final AliasSet _result;

		if (find() != this) {
			// just work on the representative of the class
			_result = find().clone();
		} else {
			final AliasSet _clone = (AliasSet) super.clone();

			_clone.fieldMap = (Map) ((HashMap<String, AliasSet>) fieldMap).clone();
			_clone.fieldMap.clear();

			if (readyEntities != null) {
				_clone.readyEntities = (Collection) ((HashSet<Object>) readyEntities).clone();
			}

			if (readwriteEntities != null) {
				_clone.readwriteEntities = (Collection) ((HashSet<Object>) readwriteEntities).clone();
			}

			if (writewriteEntities != null) {
				_clone.writewriteEntities = (Collection) ((HashSet<Object>) writewriteEntities).clone();
			}

			final Collection<String> _emptySet = Collections.emptySet();
			if (readFields != _emptySet) {
				_clone.readFields = (Collection) ((HashSet<String>) readFields).clone();
			}

			if (writtenFields != _emptySet) {
				_clone.writtenFields = (Collection) ((HashSet<String>) writtenFields).clone();
			}

			_clone.readThreads = new HashSet<Triple<InvokeStmt, SootMethod, SootClass>>(readThreads);
			_clone.writeThreads = new HashSet<Triple<InvokeStmt, SootMethod, SootClass>>(writeThreads);

			_clone.set = null;

			_result = _clone;
		}

		return _result;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override public String toString() {
		final String _result;

		if (find() != this) {
			_result = find().toString();
		} else {
			if (stringifying) {
				_result = Integer.toHexString(hashCode());
			} else {
				stringifying = true;
				_result = new ToStringBuilder(this).append("multiThreadAccess", this.multiThreadAccessibility).append(
						"accessed", this.accessed).append("notifies", this.notifies).append("waits", this.waits).append(
						"locked", this.locked).append("writtenFields", this.writtenFields).append("readFields",
						this.readFields).append("readyEntities", this.readyEntities)
						.append("lockEntities", this.lockEntities).append("rwEntities", this.readwriteEntities).append(
								"wwEntities", this.writewriteEntities).append("intraProcRefEntities",
								this.intraProcRefEntities).append("sigsOfSharedFields", sigsOfRWSharedFields).append(
								"sigsOfWriteWriteSharedFields", sigsOfWWSharedFields).append("readThreads", readThreads)
						.append("writeThreads", writeThreads).append("fieldMap", this.fieldMap).toString();
				stringifying = false;
			}
		}
		return _result;
	}

	/**
	 * Adds the given field signature to the collection of read fields of this alias set's object.
	 * 
	 * @param fieldSig is the field signature.
	 */
	void addReadField(final String fieldSig) {
		final AliasSet _l = find();

		final Collection<String> _emptySet = Collections.emptySet();
		if (_l.readFields == _emptySet) {
			_l.readFields = new HashSet<String>();
		}
		_l.readFields.add(fieldSig);
	}

	/**
	 * Adds the given threads as the threads that read a member of an object via the variable associated with this aliasset.
	 * 
	 * @param abstractThreads collection of abstract thread objects in which the write occurs.
	 */
	void addReadThreads(final Collection<Triple<InvokeStmt, SootMethod, SootClass>> abstractThreads) {
		find().readThreads.addAll(abstractThreads);
	}

	/**
	 * Adds the given threads as the threads that wrote a member of an object via the variable associated with this aliasset.
	 * 
	 * @param abstractThreads collection of abstract thread objects in which the write occurs.
	 */
	void addWriteThreads(final Collection<Triple<InvokeStmt, SootMethod, SootClass>> abstractThreads) {
		find().writeThreads.addAll(abstractThreads);
	}

	/**
	 * Adds the given field signature to the collection of written fields of this alias set's object.
	 * 
	 * @param fieldSig is the field signature.
	 */
	void addWrittenField(final String fieldSig) {
		final AliasSet _l = find();
		final Collection<String> _emptySet = Collections.emptySet();
		if (_l.writtenFields == _emptySet) {
			_l.writtenFields = new HashSet<String>();
		}
		_l.writtenFields.add(fieldSig);
	}

	/**
	 * DOCUMENT ME!
	 */
	void eraseIntraThreadRefEntities() {
		if (intraProcRefEntities != null) {
			intraProcRefEntities.clear();
		}
	}

	/**
	 * Checks if the object associated with this alias set is accessible in multiple threads.
	 * 
	 * @return <code>true</code> if the object is shared; <code>false</code>, otherwise.
	 */
	boolean escapes() {
		return find().multiThreadAccessibility;
	}

	/**
	 * Retrieves the end point of the specified access path.
	 * 
	 * @param accesspath is a sequence of primaries in the access path. This should not include the primary for this alias
	 *            set. It should contain the stringized form of the field signature or <code>IEscapeInfo.ARRAY_FIELD</code>.
	 * @return the alias set that corresponds to the end point of the access path. <code>null</code> is returned if no such
	 *         path exists.
	 */
	AliasSet getAccessPathEndPoint(final String[] accesspath) {
		AliasSet _result = this;
		for (final String _pathElement : accesspath) {
			final AliasSet _as = _result.getFieldMap().get(_pathElement);

			if (_as != null) {
				_result = _as.find();
			} else {
				_result = null;
				break;
			}
		}
		return _result;
	}

	/**
	 * Retrieves the alias set corresponding to the given field of the object represented by this alias set.
	 * 
	 * @param field is the signature of the field.
	 * @return the alias set associated with <code>field</code>.
	 * @post result == self.find().fieldMap.get(field)
	 */
	AliasSet getASForField(final String field) {
		return find().fieldMap.get(field);
	}

	/**
	 * Retrieves an unmodifiable copy of the field map of this alias set.
	 * 
	 * @return the field map.
	 */
	Map<String, AliasSet> getFieldMap() {
		return Collections.unmodifiableMap(find().fieldMap);
	}

	/**
	 * Retrieves the alias set that is reachable from <code>root</code> and that corresponds to <code>ref</code>, an
	 * alias set reachable from this alias set. This method can be used to retrieve the alias set in the site-context
	 * corresponding to an alias side in the callee method context.
	 * 
	 * @param root the alias set to start point in the search context.
	 * @param ref the alias set in end point in the reference (this) context.
	 * @param processed a collection of alias set pairs that is used during the search. The contents of this alias set is not
	 *            relevant to the caller.
	 * @return the alias set reachable from <code>root</code> and that corresponds to <code>ref</code>. This will be
	 *         <code>null</code> if there is no such alias set.
	 * @pre root != null and ref != null and processed != null
	 */
	AliasSet getImageOfRefUnderRoot(final AliasSet root, final AliasSet ref,
			final Collection<Pair<AliasSet, AliasSet>> processed) {
		AliasSet _result = null;

		if (ref.find() == find()) {
			_result = root;
		} else {
			processed.add(new Pair<AliasSet, AliasSet>(find(), root.find()));

			final Set<String> _keySet = getFieldMap().keySet();
			final Iterator<String> _i = _keySet.iterator();
			final int _iEnd = _keySet.size();

			for (int _iIndex = 0; _iIndex < _iEnd && _result == null; _iIndex++) {
				final String _key = _i.next();
				final AliasSet _as1 = getASForField(_key);
				final AliasSet _as2 = root.getASForField(_key);

				if (_as1 != null && _as2 != null) {
					final Pair<AliasSet, AliasSet> _pair = new Pair<AliasSet, AliasSet>(_as1.find(), _as2.find());

					if (!processed.contains(_pair)) {
						_result = _as1.getImageOfRefUnderRoot(_as2, ref, processed);
					}
				}
			}
		}
		return _result;
	}

	/**
	 * Retrieves the reference entities of this object.
	 * 
	 * @return a collection of objects.
	 */
	Collection<Object> getIntraProcRefEntities() {
		final Collection<Object> _collection = find().intraProcRefEntities;
		final Collection<Object> _result;
		if (_collection != null) {
			_result = Collections.unmodifiableCollection(_collection);
		} else {
			_result = null;
		}
		return _result;
	}

	/**
	 * Retrieves the lock entities of this object.
	 * 
	 * @return a collection of objects.
	 */
	Collection<Object> getLockEntities() {
		final Collection<Object> _collection = find().lockEntities;
		final Collection<Object> _result;
		if (_collection != null) {
			_result = Collections.unmodifiableCollection(_collection);
		} else {
			_result = null;
		}
		return _result;
	}

	/**
	 * Retrieves the threads that read fields of the associated object.
	 * 
	 * @return the reading threads.
	 */
	Collection<Triple<InvokeStmt, SootMethod, SootClass>> getReadThreads() {
		final Collection<Triple<InvokeStmt, SootMethod, SootClass>> _collection = find().readThreads;
		final Collection<Triple<InvokeStmt, SootMethod, SootClass>> _result;
		if (_collection != null) {
			_result = Collections.unmodifiableCollection(_collection);
		} else {
			_result = Collections.emptySet();
		}
		return _result;

	}

	/**
	 * Retrieves the shared entities of this object.
	 * 
	 * @return a collection of objects.
	 */
	Collection<Object> getReadWriteShareEntities() {
		final Collection<Object> _collection = find().readwriteEntities;
		final Collection<Object> _result;
		if (_collection != null) {
			_result = Collections.unmodifiableCollection(_collection);
		} else {
			_result = null;
		}
		return _result;
	}

	/**
	 * Retrieves the ready entity object of this alias set.
	 * 
	 * @return the associated readyentity object.
	 * @post result == self.find().readyEntity
	 */
	Collection<Object> getReadyEntities() {
		final Collection<Object> _collection = find().readyEntities;
		final Collection<Object> _result;
		if (_collection != null) {
			_result = Collections.unmodifiableCollection(_collection);
		} else {
			_result = null;
		}
		return _result;
	}

	/**
	 * Retrieves the threads that write fields of the associated object.
	 * 
	 * @return the writing threads.
	 */
	Collection<Triple<InvokeStmt, SootMethod, SootClass>> getWriteThreads() {
		final Collection<Triple<InvokeStmt, SootMethod, SootClass>> _collection = find().writeThreads;
		final Collection<Triple<InvokeStmt, SootMethod, SootClass>> _result;
		if (_collection != null) {
			_result = Collections.unmodifiableCollection(_collection);
		} else {
			_result = Collections.emptySet();
		}
		return _result;
	}

	/**
	 * Retrieves the shared entities pertaining to write-write sharing of the associated object.
	 * 
	 * @return a collection of objects.
	 */
	Collection<Object> getWriteWriteShareEntities() {
		final Collection<Object> _collection = find().writewriteEntities;
		final Collection<Object> _result;
		if (_collection != null) {
			_result = Collections.unmodifiableCollection(_collection);
		} else {
			_result = null;
		}
		return _result;
	}

	/**
	 * Checks if the alias set was accessed.
	 * 
	 * @return <code>true</code> if it was accessed; <code>false</code>, otherwise.
	 */
	boolean isAccessed() {
		return find().accessed;
	}

	/**
	 * Checks if the object associated with this alias set is accessed by multiple threads for locks and unlocks.
	 * 
	 * @return <code>true</code> if the object is lock-unlock shared; <code>false</code>, otherwise.
	 */
	boolean lockUnlockShared() {
		final AliasSet _rep = find();
		return _rep.lockEntities != null && !_rep.lockEntities.isEmpty();
	}

	/**
	 * Marks all reachable alias sets as being crossing thread boundary, i.e, visible in multiple threads.
	 */
	void markAsCrossingThreadBoundary() {
		final IWorkBag<AliasSet> _wb = new HistoryAwareLIFOWorkBag<AliasSet>(new HashSet<AliasSet>());
		_wb.addWork(find());

		while (_wb.hasWork()) {
			final AliasSet _as = _wb.getWork();
			_as.multiThreadAccessibility = true;

			for (final Iterator<AliasSet> _i = _as.fieldMap.values().iterator(); _i.hasNext();) {
				_wb.addWork(_i.next().find());
			}
		}
	}

	/**
	 * Checks if the object associated with this alias set is accessed by multiple methods.
	 * 
	 * @return <code>true</code> if the object is method shared; <code>false</code>, otherwise.
	 */
	boolean methodShared() {
		final AliasSet _rep = find();
		return _rep.intraProcRefEntities != null && !_rep.intraProcRefEntities.isEmpty();
	}

	/**
	 * Propogates the information from the this alias set to the destination alias set.
	 * 
	 * @param to is the destination of the information transfer.
	 * @post to.isShared() == (isShared() or to.isShared())
	 * @post to.getReadyEntities().containsAll(getReadyEntities())
	 * @post to.getShareEntities().containsAll(getShareEntities())
	 */
	void propogateInfoFromTo(final AliasSet to) {
		final IWorkBag<Pair<AliasSet, AliasSet>> _wb = new HistoryAwareLIFOWorkBag<Pair<AliasSet, AliasSet>>(
				new HashSet<Pair<AliasSet, AliasSet>>());
		_wb.addWork(new Pair<AliasSet, AliasSet>(this, to));

		while (_wb.hasWork()) {
			final Pair<AliasSet, AliasSet> _pair = _wb.getWork();
			final AliasSet _fromRep = _pair.getFirst().find();
			final AliasSet _toRep = _pair.getSecond().find();

			if (_fromRep != _toRep) {
				_toRep.multiThreadAccessibility |= _fromRep.multiThreadAccessibility;

				/*
				 * This is tricky. A constructor can be called to construct 2 instances in which one is used in wait/notify
				 * but not the other. This means on top-down propogation of alias set in ECBA, the 2 alias set of the primary
				 * of the <init> method will be rep1 and one may provide a non-null ready entity to rep2 and the other may
				 * come and erase it if the check is not made.
				 */
				if (_fromRep.readyEntities != null) {
					if (_toRep.readyEntities == null) {
						_toRep.readyEntities = new HashSet<Object>();
					}
					_toRep.readyEntities.addAll(_fromRep.readyEntities);
				}

				if (_fromRep.readwriteEntities != null) {
					if (_toRep.readwriteEntities == null) {
						_toRep.readwriteEntities = new HashSet<Object>();
					}

					_toRep.readwriteEntities.addAll(_fromRep.readwriteEntities);
				}

				if (_fromRep.writewriteEntities != null) {
					if (_toRep.writewriteEntities == null) {
						_toRep.writewriteEntities = new HashSet<Object>();
					}
					_toRep.writewriteEntities.addAll(_fromRep.writewriteEntities);
				}

				if (_fromRep.lockEntities != null) {
					if (_toRep.lockEntities == null) {
						_toRep.lockEntities = new HashSet<Object>();
					}
					_toRep.lockEntities.addAll(_fromRep.lockEntities);
				}

				if (_fromRep.intraProcRefEntities != null) {
					if (_toRep.intraProcRefEntities == null) {
						_toRep.intraProcRefEntities = new HashSet<Object>();
					}
					_toRep.intraProcRefEntities.addAll(_fromRep.intraProcRefEntities);
				}

				if (_fromRep.sigsOfRWSharedFields != null) {
					if (_toRep.sigsOfRWSharedFields == null) {
						_toRep.sigsOfRWSharedFields = new HashSet<String>();
					}
					_toRep.sigsOfRWSharedFields.addAll(_fromRep.sigsOfRWSharedFields);
				}

				if (_fromRep.sigsOfWWSharedFields != null) {
					if (_toRep.sigsOfWWSharedFields == null) {
						_toRep.sigsOfWWSharedFields = new HashSet<String>();
					}
					_toRep.sigsOfWWSharedFields.addAll(_fromRep.sigsOfWWSharedFields);
				}

				for (final Iterator<String> _i = _toRep.getFieldMap().keySet().iterator(); _i.hasNext();) {
					final String _field = _i.next();
					final AliasSet _to = _toRep.getASForField(_field);
					final AliasSet _from = _fromRep.getASForField(_field);

					if ((_to != null) && (_from != null)) {
						_wb.addWork(new Pair<AliasSet, AliasSet>(_from, _to));
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
	 * @pre as != null
	 */
	void putASForField(final String field, final AliasSet as) {
		find().fieldMap.put(field, as);
	}

	/**
	 * Checks if the object associated with this alias set is accessed by multiple threads for reads and writes.
	 * 
	 * @return <code>true</code> if the object is read-write shared; <code>false</code>, otherwise.
	 */
	boolean readWriteShared() {
		final AliasSet _rep = find();
		return _rep.readwriteEntities != null && !_rep.readwriteEntities.isEmpty();
	}

	/**
	 * Checks if the object associated with this alias set is accessed by multiple threads for read and writes of the
	 * specified field.
	 * 
	 * @param fieldSignature is the signature of the field.
	 * @return <code>true</code> if the object is shared via read-write of the given field; <code>false</code>,
	 *         otherwise.
	 */
	boolean readWriteShared(final String fieldSignature) {
		final AliasSet _rep = find();
		return readWriteShared() && _rep.sigsOfRWSharedFields.contains(fieldSignature);
	}

	/**
	 * Marks the alias set was accessed.
	 */
	void setAccessed() {
		find().accessed = true;
	}

	/**
	 * Marks the alias set as participating in a lock operation.
	 */
	void setLocked() {
		find().locked = true;
	}

	/**
	 * Marks the object associated with this alias set as appearing in a <code>notify()/notifyAll()</code> call.
	 * 
	 * @post find().notifies == true
	 */
	void setNotifies() {
		find().notifies = true;
	}

	/**
	 * Marks the object associated with this alias set as appearing in a <code>wait()</code> call.
	 * 
	 * @post find().waits == true
	 */
	void setWaits() {
		find().waits = true;
	}

	/**
	 * Unifies the given alias set with this alias set.
	 * 
	 * @param a is the alias set to be unified with this alias set.
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
	 *            <code>false</code>, otherwise.
	 * @pre as2 != null
	 */
	void unifyAliasSetHelper(final AliasSet as2, final boolean unifyAll) {
		final AliasSet _m = find();
		final AliasSet _n = as2.find();

		if (_m != _n) {
			_m.union(_n);

			final AliasSet _representative = _m.find();
			final AliasSet _represented;

			if (_representative == _m) {
				_represented = _n;
			} else {
				_represented = _m;
			}

			_representative.waits |= _represented.waits;
			_representative.notifies |= _represented.notifies;
			_representative.multiThreadAccessibility |= _represented.multiThreadAccessibility;
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
		} else if (unifyAll && _m.multiThreadAccessibility) {
			_m.unifyThreadEscapeInfo(_n);
		}
	}

	/**
	 * Checks if the object associated with this alias set is accessed by multiple threads for waits and notifys.
	 * 
	 * @return <code>true</code> if the object is wait-notify shared; <code>false</code>, otherwise.
	 */
	boolean waitNotifyShared() {
		final AliasSet _rep = find();
		return _rep.readyEntities != null && !_rep.readyEntities.isEmpty();
	}

	/**
	 * Checks if the a field of the object associated with alias set was read.
	 * 
	 * @return <code>true</code> if it was read; <code>false</code>, otherwise.
	 */
	boolean wasAnyFieldRead() {
		return !find().readFields.isEmpty();
	}

	/**
	 * Checks if the a field of the object associated with alias set was written.
	 * 
	 * @return <code>true</code> if it was written; <code>false</code>, otherwise.
	 */
	boolean wasAnyFieldWritten() {
		return !find().writtenFields.isEmpty();
	}

	/**
	 * Checks if the field of the provided signature was read via the object associated with this alias set.
	 * 
	 * @param fieldSig is the field signature.
	 * @return <code>true</code> if it was read; <code>false</code>, otherwise.
	 */
	boolean wasFieldRead(final String fieldSig) {
		return find().readFields.contains(fieldSig);
	}

	/**
	 * Checks if the given field or an object reachable from it was read.
	 * 
	 * @param fieldSig is the field signature.
	 * @param recurse <code>true</code> indicates if alias sets reachable from this alias set should be considered;
	 *            <code>false</code>, otherwise.
	 * @return <code>true</code> if this alias set was read or if the given alias set is <code>null</code>;
	 *         <code>false</code>, otherwise.
	 */
	boolean wasFieldRead(final String fieldSig, final boolean recurse) {
		boolean _result = wasFieldRead(fieldSig);

		if (!_result && recurse) {
			_result = recursiveBooleanPropertyDiscovery(fieldSig, new ITransformer<AliasSet, Boolean>() {

				public Boolean transform(final AliasSet input) {
					return Boolean.valueOf(input.wasAnyFieldRead());
				}
			});
		}
		return _result;
	}

	/**
	 * Checks if the field of the provided signature was read via the object associated with this alias set.
	 * 
	 * @param fieldSig is the field signature.
	 * @return <code>true</code> if it was read; <code>false</code>, otherwise.
	 */
	boolean wasFieldWritten(final String fieldSig) {
		return find().writtenFields.contains(fieldSig);
	}

	/**
	 * Checks if the given field or an object reachable from it was written.
	 * 
	 * @param fieldSig is the field signature.
	 * @param recurse <code>true</code> indicates if alias sets reachable from this alias set should be considered;
	 *            <code>false</code>, otherwise.
	 * @return <code>true</code> if this alias set was written or if the given alias set is <code>null</code>;
	 *         <code>false</code>, otherwise.
	 */
	boolean wasFieldWritten(final String fieldSig, final boolean recurse) {
		boolean _result = wasFieldWritten(fieldSig);

		if (!_result && recurse) {
			_result = recursiveBooleanPropertyDiscovery(fieldSig, new ITransformer<AliasSet, Boolean>() {

				public Boolean transform(final AliasSet input) {
					return Boolean.valueOf(input.wasAnyFieldWritten());
				}
			});
		}
		return _result;
	}

	/**
	 * Checks if the object associated with this alias set is accessed by multiple threads for writes.
	 * 
	 * @return <code>true</code> if the object is write-write shared; <code>false</code>, otherwise.
	 */
	boolean writeWriteShared() {
		final AliasSet _rep = find();
		return _rep.writewriteEntities != null && !_rep.writewriteEntities.isEmpty();
	}

	/**
	 * Checks if the object associated with this alias set is shared by multiple threads for writes of the specified field.
	 * 
	 * @param fieldSignature is the signature of the field.
	 * @return <code>true</code> if the object is shared via write-write of the given field; <code>false</code>,
	 *         otherwise.
	 */
	boolean writeWriteShared(final String fieldSignature) {
		final AliasSet _rep = find();
		return writeWriteShared() && _rep.sigsOfWWSharedFields.contains(fieldSignature);
	}

	/**
	 * Handles the unification of entity info.
	 * 
	 * @param represented is the alias set being unified with this alias set such that this alias set is the representative
	 *            alias set.
	 * @pre represented != null
	 */
	private void handleInfoUnification(final AliasSet represented) {
		if (lockEntities == null) {
			lockEntities = represented.lockEntities;
		} else if (represented.lockEntities != null) {
			lockEntities.addAll(represented.lockEntities);
		}
		represented.lockEntities = null;

		if (intraProcRefEntities == null) {
			intraProcRefEntities = represented.intraProcRefEntities;
			if (intraProcRefEntities == null) {
				intraProcRefEntities = new HashSet<Object>();
				intraProcRefEntities.add(getNewReferenceEntity());
			}
		} else if (represented.intraProcRefEntities != null) {
			intraProcRefEntities.addAll(represented.intraProcRefEntities);
		}
		represented.intraProcRefEntities = null;

		if (readyEntities == null) {
			readyEntities = represented.readyEntities;
		} else if (represented.readyEntities != null) {
			readyEntities.addAll(represented.readyEntities);
		}
		represented.readyEntities = null;

		if (readwriteEntities == null) {
			readwriteEntities = represented.readwriteEntities;
		} else if (represented.readwriteEntities != null) {
			readwriteEntities.addAll(represented.readwriteEntities);
		}
		represented.readwriteEntities = null;

		if (writewriteEntities == null) {
			writewriteEntities = represented.writewriteEntities;
		} else if (represented.writewriteEntities != null) {
			writewriteEntities.addAll(represented.writewriteEntities);
		}
		represented.writewriteEntities = null;

		if (sigsOfRWSharedFields == null) {
			sigsOfRWSharedFields = represented.sigsOfRWSharedFields;
		} else if (represented.sigsOfRWSharedFields != null) {
			sigsOfRWSharedFields.addAll(represented.sigsOfRWSharedFields);
		}
		represented.sigsOfRWSharedFields = null;

		if (sigsOfWWSharedFields == null) {
			sigsOfWWSharedFields = represented.sigsOfWWSharedFields;
		} else if (represented.sigsOfWWSharedFields != null) {
			sigsOfWWSharedFields.addAll(represented.sigsOfWWSharedFields);
		}
		represented.sigsOfWWSharedFields = null;

		final Collection<String> _emptySet = Collections.emptySet();
		if (readFields == _emptySet) {
			readFields = represented.readFields;
		} else {
			readFields.addAll(represented.readFields);
		}
		represented.readFields = null;

		if (writtenFields == _emptySet) {
			writtenFields = represented.writtenFields;
		} else {
			writtenFields.addAll(represented.writtenFields);
		}
		represented.writtenFields = null;
	}

	/**
	 * Discovers a boolean property recursively through the alias set tree.
	 * 
	 * @param fieldSig is the signature of the field whose alias set serves as the anchor for recursion.
	 * @param transformer is used to extract the property.
	 * @return <code>true</code> if the property holds on an alias set reachable from this alias set; <code>false</code>,
	 *         otherwise.
	 * @pre transformer != null and fieldSig != null
	 */
	private boolean recursiveBooleanPropertyDiscovery(final String fieldSig, final ITransformer<AliasSet, Boolean> transformer) {
		boolean _result = false;
		final Object _fieldAS = find().fieldMap.get(fieldSig);

		if (_fieldAS != null) {
			final IWorkBag<AliasSet> _wb = new HistoryAwareFIFOWorkBag<AliasSet>(new HashSet<AliasSet>());
			_wb.addWork((AliasSet) _fieldAS);

			while (_wb.hasWork() && !_result) {
				final AliasSet _rep = _wb.getWork();
				_result |= (transformer.transform(_rep)).booleanValue();

				if (!_result) {
					final Collection<AliasSet> _values = _rep.getFieldMap().values();
					_wb.addAllWorkNoDuplicates(CollectionUtils.collect(_values, REPRESENTATIVE_ALIAS_SET_RETRIEVER));
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
	 *            <code>false</code>, otherwise.
	 * @pre aliasSet != null
	 */
	private void unifyFields(final AliasSet aliasSet, final boolean unifyAll) {
		for (final Iterator<Map.Entry<String, AliasSet>> _i = aliasSet.fieldMap.entrySet().iterator(); _i.hasNext();) {
			final Map.Entry<String, AliasSet> _entry = _i.next();
			final String _field = _entry.getKey();
			final AliasSet _fieldAS = _entry.getValue();
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
	 * @pre represented != null
	 */
	private void unifyThreadEscapeInfo(final AliasSet represented) {
		if ((waits && represented.notifies) || (notifies && represented.waits)) {
			if (readyEntities == null) {
				readyEntities = new HashSet<Object>();
			}

			if (readyEntities.isEmpty()) {
				if (represented.readyEntities != null && !represented.readyEntities.isEmpty()) {
					readyEntities.addAll(represented.readyEntities);
				} else {
					readyEntities.add(getNewReadyEntity());
				}
			}
		}

		if (locked && represented.locked) {
			if (lockEntities == null) {
				lockEntities = new HashSet<Object>();
			}

			if (lockEntities.isEmpty()) {
				if (represented.lockEntities != null && !represented.lockEntities.isEmpty()) {
					lockEntities.addAll(represented.lockEntities);
				} else {
					lockEntities.add(getNewLockEntity());
				}

			}
		}

		if (CollectionUtils.containsAny(readFields, represented.writtenFields)
				|| CollectionUtils.containsAny(writtenFields, represented.readFields)) {
			if (readwriteEntities == null) {
				readwriteEntities = new HashSet<Object>();
				sigsOfRWSharedFields = new HashSet<String>();
			}

			if (readwriteEntities.isEmpty()) {
				if (represented.readwriteEntities != null && !represented.readwriteEntities.isEmpty()) {
					readwriteEntities.addAll(represented.readwriteEntities);
				} else {
					readwriteEntities.add(getNewShareEntity());
				}
			}
			sigsOfRWSharedFields.addAll(SetUtils.intersection(readFields, represented.writtenFields));
			sigsOfRWSharedFields.addAll(SetUtils.intersection(writtenFields, represented.readFields));
		}

		if (CollectionUtils.containsAny(writtenFields, represented.writtenFields)) {
			if (writewriteEntities == null) {
				writewriteEntities = new HashSet<Object>();
				sigsOfWWSharedFields = new HashSet<String>();
			}

			if (writewriteEntities.isEmpty()) {
				if (represented.writewriteEntities != null && !represented.writewriteEntities.isEmpty()) {
					writewriteEntities.addAll(represented.writewriteEntities);
				} else {
					writewriteEntities.add(getNewShareEntity());
				}
			}
			sigsOfWWSharedFields.addAll(SetUtils.intersection(writtenFields, represented.writtenFields));
		}
	}
}

// End of File
