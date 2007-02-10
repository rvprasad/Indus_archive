/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

package edu.ksu.cis.indus.staticanalyses.tokens;

import java.util.Collection;
import java.util.Observer;

/**
 * This is the interface to a type manager.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <R> is the type of the representation types.
 * @param <V> is the type of the value object (in the representation).
 */
public interface ITypeManager<R, V> {

	/**
	 * This is a wrapper class that is used in conjunction with observer pattern. Instances of this class indicate the
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
		 * @pre theType != null
		 */
		public NewTypeCreated(final IType theType) {
			type = theType;
		}

		/**
		 * Retrieves the created type.
		 * 
		 * @return the new type that was created.
		 * @post result != null
		 */
		public IType getCreatedType() {
			return type;
		}
	}

	/**
	 * Adds an observer.
	 * 
	 * @param observer to be added.
	 * @pre observer != null
	 */
	void addObserver(Observer observer);

	/**
	 * Deletes an observer.
	 * 
	 * @param observer to be deleted.
	 * @pre observer != null
	 */
	void deleteObserver(Observer observer);

	/**
	 * Retrieves all the types of the given value. Depending on the type system, <code>value</code> may be of many types
	 * (for example, due to subtyping). Hence, we retrieve a collection of types rather than a type.
	 * 
	 * @param value whose types are requested.
	 * @return the types of <code>value</code>.
	 * @pre value != null
	 * @post result != null
	 */
	Collection<IType> getAllTypes(V value);

	/**
	 * Retreives dynamic token-type relation evaluating implementation.
	 * 
	 * @return dynamic token-type relation evaluating implementation.
	 */
	IDynamicTokenTypeRelationDetector<V> getDynamicTokenTypeRelationEvaluator();

	/**
	 * Retrieves the specific type of the value. It may be that the value is of type T1, T2, .. Tn of which Tn is it's
	 * declared type and the rest are types by some sort of subtyping relation. This method should return Tn.
	 * 
	 * @param value whose type is requested.
	 * @return the type of the value.
	 * @pre value != null
	 * @post result != null
	 */
	IType getExactType(V value);

	/**
	 * Retrieves a type in the "user's" type system that corresponds to the type in the representation's type system. The user
	 * may use a (user's) type system for the tokens that is orthogonal to the type system of the representation (e.g. IR)
	 * used for the system. In such cases, this decouples the representation's type system from the user's type system. We
	 * refer to the "user's" type system as the token's type system as it is tightly coupled with the token management.
	 * 
	 * @param type is the representation's type system
	 * @return the type in token's type system that corresponds to the given type.
	 * @pre type != null
	 * @post result != null
	 */
	IType getTokenTypeForRepType(R type);

	/**
	 * Resets the type manager.
	 */
	void reset();
}

// End of File
