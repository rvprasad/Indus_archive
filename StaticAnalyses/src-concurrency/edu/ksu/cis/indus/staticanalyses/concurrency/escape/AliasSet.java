
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (C) 2003, 2004, 2005
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://indus.projects.cis.ksu.edu/).
 * It is understood that any modification not identified as such is
 * not covered by the preceding statement.
 *
 * This work is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this toolkit; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * Java is a trademark of Sun Microsystems, Inc.
 *
 * To submit a bug report, send a comment, or get the latest news on
 * this project and other SAnToS projects, please visit the web-site
 *                http://indus.projects.cis.ksu.edu/
 */

package edu.ksu.cis.indus.staticanalyses.concurrency.escape;

import soot.RefLikeType;
import soot.Type;

import edu.ksu.cis.indus.staticanalyses.support.FastUnionFindElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;


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
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(AliasSet.class);

	/**
	 * This serves as a counter for each entity elements created in each run.
	 */
	static long entityCount = 0;

	/**
	 * This constant identifies the cells of an array in the field map of it's alias set.
	 */
	static final String ARRAY_FIELD = "$ELT";

	/**
	 * This holds the reference of the clone object being created.  This is used to handle cycles during cloning.
	 */
	private AliasSet theClone;

	/**
	 * This maps field signatures to their alias sets.
	 *
	 * @invariant fieldMap.oclIsKindOf(Map(String, AliasSet))
	 */
	private Map fieldMap;

	/**
	 * This represents the unique entity associated with this alias set.
	 */
	private Object entity;

	/**
	 * This represents if the variable associated with alias set was accessed (read/written).
	 */
	private boolean accessed;

	/**
	 * This indicates if this alias set is associated with a static field.
	 */
	private boolean global;

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
	 * This indicates if the variable (hence, the object referred to) associated with this alias set shared across threads.
	 */
	private boolean shared;

	/**
	 * This indicates if the variable associated with this alias set is the receiver of <code>wait()</code> call.
	 */
	private boolean waits;

	/**
	 * Creates a new AliasSet object.
	 */
	private AliasSet() {
		this.fieldMap = new HashMap();
		theClone = null;
		shared = false;
		accessed = false;
		global = false;
		propogating = false;
		entity = null;
	}

	/**
	 * Checks if the given type can contribute to aliasing.  Only reference types can lead to aliasing.
	 *
	 * @param type to be checked for aliasing support.
	 *
	 * @return <code>true</code> if <code>type</code> can contribute aliasing; <code>false</code>, otherwise.
	 *
	 * @pre type != null
	 */
	public static boolean canHaveAliasSet(final Type type) {
		return type instanceof RefLikeType;
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
		if (theClone != null) {
			return theClone;
		}

		//optimization
		if (isGlobal()) {
			return find();
		}

		//just work on the representative of the class
		if (set != null) {
			return (AliasSet) ((AliasSet) find()).clone();
		}

		theClone = (AliasSet) super.clone();

		// clone() does a shallow copy. So, change the fields in the clone suitably.
		theClone.fieldMap = new HashMap();

		for (Iterator i = fieldMap.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			AliasSet temp = (AliasSet) ((AliasSet) entry.getValue()).clone();
			theClone.fieldMap.put(entry.getKey(), temp);
		}

		Object result = theClone;
		theClone = null;

		return result;
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
		AliasSet result = null;

		if (canHaveAliasSet(type)) {
			result = new AliasSet();
		}
		return result;
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
	 * Retrieves the entity object of this alias set.
	 *
	 * @return the associated entity object.
	 *
	 * @post result == self.find().entity
	 */
	Object getEntity() {
		return ((AliasSet) find()).entity;
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
		AliasSet rep = (AliasSet) find();

		if (rep.global) {
			return;
		}
		rep.global = true;
		rep.shared = true;

		if (rep.fieldMap != null) {
			for (Iterator i = rep.fieldMap.values().iterator(); i.hasNext();) {
				AliasSet as = (AliasSet) i.next();
				as.setGlobal();
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
	 * Checks if the object associated with this alias set is shared between threads.
	 *
	 * @return <code>true</code> if the object is shared; <code>false</code>, otherwise.
	 *
	 * @post result == find().shared
	 */
	boolean isShared() {
		return ((AliasSet) find()).shared;
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

		AliasSet rep1 = (AliasSet) find();
		AliasSet rep2 = (AliasSet) as.find();
		rep2.shared |= rep1.shared;
		rep2.entity = rep1.entity;

		for (Iterator i = rep2.fieldMap.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			AliasSet to = (AliasSet) ((AliasSet) rep2.fieldMap.get(entry.getKey())).find();
			AliasSet from = (AliasSet) ((AliasSet) rep1.fieldMap.get(entry.getKey())).find();
			from.propogateInfoFromTo(to);
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
		((AliasSet) find()).fieldMap.put(field, as);

		if (isGlobal()) {
			as.setGlobal();
		}
	}

	/**
	 * Unifies the given alias set with this alias set.
	 *
	 * @param a is the alias set to be unified with this alias set.
	 * @param unifyAll indicates if all elements of the alias set should be unified. <code>true</code>, indicates all
	 * 		  elements. <code>false</code> indicates all elements except shared and entity.
	 */
	void unify(final AliasSet a, final boolean unifyAll) {
		if (a == null) {
			LOGGER.warn("Unification with null requested.");
		}

		AliasSet m = (AliasSet) find();
		AliasSet n = (AliasSet) a.find();

		if (m == n) {
			return;
		}
		m.union(n);

		AliasSet rep1 = (AliasSet) m.find();
		AliasSet rep2;

		if (rep1 == m) {
			rep2 = n;
		} else {
			rep2 = m;
		}

		if (unifyAll) {
			rep1.shared |= rep1.accessed && rep2.accessed;

			if (rep1.entity == null && ((rep1.waits && rep2.notifies) || (rep1.notifies && rep2.waits))) {
				rep1.entity = new String("Entity:" + entityCount++);
			}
		} else {
			rep1.shared |= rep2.shared;
		}

		rep1.waits |= rep2.waits;
		rep1.notifies |= rep2.notifies;
		rep1.accessed |= rep2.accessed;

		Collection toBeProcessed = new HashSet();
		Collection keySet = new ArrayList(rep2.fieldMap.keySet());
		toBeProcessed.addAll(keySet);

		for (Iterator i = keySet.iterator(); i.hasNext();) {
			String fieldName = (String) i.next();
			FastUnionFindElement field = (FastUnionFindElement) rep1.fieldMap.get(fieldName);

			if (field != null) {
				AliasSet repAS = (AliasSet) field;
				toBeProcessed.remove(fieldName);

				FastUnionFindElement temp = (FastUnionFindElement) rep2.fieldMap.get(fieldName);

				if (temp != null) {
					repAS.unify((AliasSet) temp, unifyAll);
				}
			}
		}

		for (Iterator i = toBeProcessed.iterator(); i.hasNext();) {
			String field = (String) i.next();
			AliasSet rep2AS = (AliasSet) ((FastUnionFindElement) rep2.fieldMap.get(field)).find();
			rep1.putASForField(field, rep2AS);
		}

		if (rep1.global || rep2.global) {
			rep1.setGlobal();
		}
	}
}

/*
   ChangeLog:
   $Log$
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
