
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import soot.ArrayType;
import soot.RefType;
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
	 * This holds the reference of the clone object being created.  This is used to handle cycles during cloning.
	 */
	private AliasSet theClone;

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
	 * This is used to handle cycles while propogating information in phase 3.
	 */
	private boolean propogating;

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
		theClone = null;
		shared = false;
		accessed = false;
		global = false;
		propogating = false;
		readyEntities = null;
		read = false;
		written = false;
		shareEntities = null;
		markingMultiThreadAccess = false;
		multiThreadAccess = false;
	}

	/**
	 * Checks if the given type can contribute to aliasing.  Only reference and array types can lead to aliasing.
	 *
	 * @param type to be checked for aliasing support.
	 *
	 * @return <code>true</code> if <code>type</code> can contribute aliasing; <code>false</code>, otherwise.
	 *
	 * @pre type != null
	 */
	public static boolean canHaveAliasSet(final Type type) {
		return type instanceof RefType || type instanceof ArrayType;
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
		Object _result;

		if (theClone != null) {
			_result = theClone;
		} else if (isGlobal()) {
			//optimization
			_result = find();
		} else if (set != null) {
			//just work on the representative of the class
			_result = (AliasSet) ((AliasSet) find()).clone();
		} else {
			theClone = (AliasSet) super.clone();

			// clone() does a shallow copy. So, change the fields in the clone suitably.
			theClone.fieldMap = new HashMap();

			for (final Iterator _i = fieldMap.entrySet().iterator(); _i.hasNext();) {
				final Map.Entry _entry = (Map.Entry) _i.next();
				final AliasSet _temp = (AliasSet) ((AliasSet) _entry.getValue()).clone();
				theClone.fieldMap.put(_entry.getKey(), _temp);
			}

			_result = theClone;
			theClone = null;
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
				_result = "Top";
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
	 * Marks this alias set and all reachable alias sets as being accessed in multiple threads.
	 */
	void markMultiThreadAccess() {
		if (find() != this) {
			((AliasSet) find()).markMultiThreadAccess();
		} else {
			if (markingMultiThreadAccess) {
				return;
			}
			markingMultiThreadAccess = true;
			multiThreadAccess = true;

			for (final Iterator _i = fieldMap.values().iterator(); _i.hasNext();) {
				((AliasSet) _i.next()).markMultiThreadAccess();
			}
			markingMultiThreadAccess = false;
		}
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

		if (canHaveAliasSet(type)) {
			_result = new AliasSet();
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
	Collection getReadyEntity() {
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
	 * Propogates the information from this alias set to the given alias set.
	 *
	 * @param as is the destination of the information transfer.
	 *
	 * @post as.isShared() == (isShared() or as.isShared())
	 * @post as.getEntity() == getEntity()
	 */
	void propogateInfoFromTo(final AliasSet as) {
		if (propogating) {
			return;
		}

		propogating = true;

		final AliasSet _rep1 = (AliasSet) find();
		final AliasSet _rep2 = (AliasSet) as.find();
		_rep2.shared |= _rep1.shared;

		/*
		 * This is tricky.  A constructor can be called to construct 2 instances in which one is used in
		 * wait/notify but not the other.  This means on top-down propogation of alias set in ECBA, the 2 alias
		 * set of the primary of the <init> method will be rep1 and one may provide a non-null ready entity to rep2
		 * and the other may come and erase it if the check is not made.
		 */
		if (_rep1.readyEntities != null) {
			if (_rep2.readyEntities == null) {
				_rep2.readyEntities = new HashSet();
			}
			_rep2.readyEntities.addAll(_rep1.readyEntities);
		}

		if (_rep1.shareEntities != null) {
			if (_rep2.shareEntities == null) {
				_rep2.shareEntities = new HashSet();
			}

			_rep2.shareEntities.addAll(_rep1.shareEntities);
		}

		for (final Iterator _i = _rep2.fieldMap.keySet().iterator(); _i.hasNext();) {
			final Object _key = _i.next();
			final AliasSet _to = (AliasSet) _rep2.fieldMap.get(_key);
			final AliasSet _from = (AliasSet) _rep1.fieldMap.get(_key);

			if ((_to != null) && (_from != null)) {
				_from.propogateInfoFromTo(_to);
			}
		}

		propogating = false;
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
		if (selfUnifying) {
			return;
		}
		selfUnifying = true;

		final AliasSet _m = (AliasSet) find();

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

		for (final Iterator _i = _m.fieldMap.keySet().iterator(); _i.hasNext();) {
			final String _fieldName = (String) _i.next();
			final FastUnionFindElement _field = (FastUnionFindElement) _m.fieldMap.get(_fieldName);

			if (_field != null) {
				((AliasSet) _field).selfUnify();
			}
		}
		selfUnifying = false;
	}

	/**
	 * Unifies the given alias set with this alias set.
	 *
	 * @param a is the alias set to be unified with this alias set.
	 *
	 * @pre a != null
	 */
	void unify(final AliasSet a) {
		final AliasSet _m = (AliasSet) find();
		final AliasSet _n = (AliasSet) a.find();

		if (_m == _n) {
			return;
		}

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

		if (_rep1.multiThreadAccess) {
			unifyEscapeInfo(_rep1, _rep2);
		}

		unifyFields(_rep1, _rep2);

		if (_rep1.global || _rep2.global) {
			_rep1.setGlobal();
		}
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
	 * Unify escape and sharing information in the given alias set.
	 *
	 * @param reprAliasSet is one of the alias set involved in the unification.
	 * @param aliasSet is the other alias set involved in the unification.
	 */
	private void unifyEscapeInfo(final AliasSet reprAliasSet, final AliasSet aliasSet) {
		reprAliasSet.shared |= (reprAliasSet.accessed && aliasSet.accessed);

		if ((reprAliasSet.waits && aliasSet.notifies) || (reprAliasSet.notifies && aliasSet.waits)) {
			if (reprAliasSet.readyEntities == null) {
				reprAliasSet.readyEntities = new HashSet();
			}
			reprAliasSet.readyEntities.add(getNewReadyEntity());
		}

		if ((reprAliasSet.read && aliasSet.written) || (reprAliasSet.written && aliasSet.read)) {
			if (reprAliasSet.shareEntities == null) {
				reprAliasSet.shareEntities = new HashSet();
			}
			reprAliasSet.shareEntities.add(getNewShareEntity());
		}
	}

	/**
	 * Unify the fields of the given alias sets.
	 *
	 * @param aliasSet1 is one of the alias set involved in the unification.
	 * @param aliasSet2 is the other alias set involved in the unification.
	 *
	 * @pre aliasSet1 != null and aliasSet2 != null
	 */
	private void unifyFields(final AliasSet aliasSet1, final AliasSet aliasSet2) {
		final Collection _toBeProcessed = new HashSet();
		final Collection _keySet = new ArrayList(aliasSet2.fieldMap.keySet());
		_toBeProcessed.addAll(_keySet);

		for (final Iterator _i = _keySet.iterator(); _i.hasNext();) {
			final String _fieldName = (String) _i.next();
			final FastUnionFindElement _field = (FastUnionFindElement) aliasSet1.fieldMap.get(_fieldName);

			if (_field != null) {
				final AliasSet _repAS = (AliasSet) _field;
				_toBeProcessed.remove(_fieldName);

				final FastUnionFindElement _temp = (FastUnionFindElement) aliasSet2.fieldMap.get(_fieldName);

				if (_temp != null) {
					_repAS.unify((AliasSet) _temp);
				}
			}
		}

		for (final Iterator _i = _toBeProcessed.iterator(); _i.hasNext();) {
			final String _field = (String) _i.next();
			final AliasSet _rep2AS = (AliasSet) ((FastUnionFindElement) aliasSet2.fieldMap.get(_field));
			aliasSet1.putASForField(_field, _rep2AS);
		}
	}
}

/*
   ChangeLog:
   $Log$
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
