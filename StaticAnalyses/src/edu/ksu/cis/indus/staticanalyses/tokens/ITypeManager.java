
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

package edu.ksu.cis.indus.staticanalyses.tokens;

import java.util.Collection;
import java.util.Observer;


/**
 * This is the interface to a type manager.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public interface ITypeManager {
	/**
	 * This is a wrapper class that is used in conjunction with observer pattern.  Instances of this class indicate the
	 * creation of a new type.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	final class NewTypeCreated {
		/** 
		 * The new type that was created.
		 */
		private final IType type;

		/**
		 * Creates an instance of this method.
		 *
		 * @param theType that was created.
		 *
		 * @pre theType != null
		 */
		public NewTypeCreated(final IType theType) {
			type = theType;
		}

		/**
		 * Retrieves the created type.
		 *
		 * @return the new type that was created.
		 *
		 * @post result != null
		 */
		public IType getCreatedType() {
			return type;
		}
	}

	/**
	 * Retrieves all the types of the given value.  Depending on the type system, <code>value</code> may be of many types
	 * (for example, due to subtyping).  Hence, we retrieve a collection of types rather than a type.
	 *
	 * @param value whose types are requested.
	 *
	 * @return the types of <code>value</code>.
	 *
	 * @pre value != null
	 * @post result != null
	 * @post result->forall(o | o.oclIsKindOf(IType))
	 */
	Collection getAllTypes(Object value);

	/**
	 * Retreives dynamic token-type relation evaluating implementation.
	 *
	 * @return dynamic token-type relation evaluating implementation.
	 */
	IDynamicTokenTypeRelationEvaluator getDynamicTokenTypeRelationEvaluator();

	/**
	 * Retrieves the specific type of the value.  It may be that the value is of type T1, T2, .. Tn of which Tn is it's
	 * declared type and the rest are types by some sort of subtyping relation.  This method should return Tn.
	 *
	 * @param value whose type is requested.
	 *
	 * @return the type of the value.
	 *
	 * @pre value != null
	 * @post result != null
	 */
	IType getExactType(Object value);

	/**
	 * Retrieves a type in the "user's" type system that corresponds to the type in the representation's type system.  The
	 * user may use a (user's) type system for the tokens that is orthogonal to the type system of the representation  (e.g.
	 * IR) used for the system.  In such cases, this decouples the representation's type system from the user's type system.
	 * We refer to the "user's" type system as the token's type system as it is tightly  coupled with the token management.
	 *
	 * @param type is the representation's type system
	 *
	 * @return the type in token's type system that corresponds to the given type.
	 *
	 * @pre type != null
	 * @post result != null
	 */
	IType getTokenTypeForRepType(Object type);

	/**
	 * Adds an observer.
	 *
	 * @param observer to be added.
	 *
	 * @pre observer != null
	 */
	void addObserver(Observer observer);

	/**
	 * Deletes an observer.
	 *
	 * @param observer to be deleted.
	 *
	 * @pre observer != null
	 */
	void deleteObserver(Observer observer);

	/**
	 * Resets the type manager.
	 */
	void reset();
}

// End of File
