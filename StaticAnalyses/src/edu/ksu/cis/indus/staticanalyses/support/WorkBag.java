
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

package edu.ksu.cis.indus.staticanalyses.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;


/**
 * This is a generic container of objects.  The order in which the objects are added and removed can be configured.  At
 * present, it supports LIFO and FIFO ordering.  This affects the order in which the <code>getWork()</code> will return the
 * work added to this bag.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad</a>
 * @version $Revision$
 *
 * @since Created: Thu Jul 25 18:37:24 2002.
 */
public class WorkBag {
	/**
	 * This is used to indicate Last-In-First-Out ordering on the work pieces stored in this container.
	 */
	public static final int LIFO = 1;

	/**
	 * This is sed to indicate First-In-First-Out ordering on the work pieces stored in this container.
	 */
	public static final int FIFO = 2;

	/**
	 * This will store work pieces.
	 *
	 * @invariant container.oclIsKindOf(Bag(Object))
	 */
	private ICollectionWrapper container;

	/**
	 * Creates an instance of this class.
	 *
	 * @param order is the requested ordering on the work pieces that will stored in this bag.
	 *
	 * @throws IllegalArgumentException when any order other than<code>LIFO</code> or <code>FIFO</code> is specified.
	 *
	 * @pre order == FIFO or order == LIFO
	 */
	public WorkBag(final int order) {
		if (order == LIFO) {
			container = new CWStack();
		} else if (order == FIFO) {
			container = new CWQueue();
		} else {
			throw new IllegalArgumentException("Invalid order specified.");
		}
	}

	/**
	 * A generic interface to be implemented by container classes.  This interface will be used by the bag to operate the
	 * container objects.
	 */
	protected interface ICollectionWrapper {
		/**
		 * Returns the filled status of this collection.
		 *
		 * @return <code>true</code> if the collection is empty; <code>false</code>, otherwise.
		 *
		 * @post result == (self->size() == 0)
		 */
		boolean isEmpty();

		/**
		 * Stores an object.
		 *
		 * @param o the object to be stored.
		 *
		 * @post self->includes(o)
		 * @invariant self->includesAll(self$pre)
		 */
		void add(Object o);

		/**
		 * Stores all the objects in the given collection.
		 *
		 * @param c the collection containing the objects to be stored.
		 *
		 * @invariant c.oclIsTypeOf(Bag(Object))
		 * @post self->includesAll(c)
		 * @invariant self->includesAll(self$pre)
		 */
		void addAll(Collection c);

		/**
		 * Stores all the objects in the given collection and ensures that duplicates do not exist.
		 *
		 * @param c the collection containing the objects to be stored.
		 *
		 * @invariant c.oclIsTypeOf(Bag(Object))
		 * @invariant self->includesAll(self$pre)
		 * @post self->includesAll(c) and self->forall( o | self->count(o) = 1)
		 */
		void addAllNoDuplicates(Collection c);

		/**
		 * Removes all objects from this collection.
		 *
		 * @post self->isEmpty() == true
		 */
		void clear();

		/**
		 * Checks if the given object is stored in this collection.
		 *
		 * @param o the object to be checked for containment.
		 *
		 * @return <code>true</code> if this collection contains <code>o</code>; <code>false</code>, otherwise.
		 *
		 * @post result == self->includes(o)
		 */
		boolean contains(Object o);

		/**
		 * Returns a stored object.
		 *
		 * @return a stored object.
		 *
		 * @post self$pre->exists(o | result == o)
		 */
		Object get();
	}

	/**
	 * An abstract implementation of the <code>ICollectionWrapper</code> interface.  <code>add(Object)</code> needs to be
	 * implemented by the extending class.  Also, the container needs to be initialized in the constructor of the extending
	 * class.
	 *
	 * @invariant self.oclIsTypeOf(Collection(Object))
	 *
	 * @see ICollectionWrapper
	 */
	protected abstract class AbstractCollectionWrapper
	  implements ICollectionWrapper {
		/**
		 * The container that will store the objects.
		 *
		 * @invariant container.isOclKindOf(Bag(Object))
		 */
		protected Collection container;

		/**
		 * Returns the filled status of this collection.
		 *
		 * @return <code>true</code> if the collection is empty; <code>false</code>, otherwise.
		 *
		 * @post result == (self->size() == 0)
		 */
		public boolean isEmpty() {
			return container.isEmpty();
		}

		/**
		 * Add all elements in <code>c</code> to the wrapped container.  This does not ensure that there are no duplicates in
		 * the container.
		 *
		 * @param c is the collection of the elements to be added.
		 *
		 * @invariant c.oclIsKindOf(Bag(Object))
		 * @invariant self->includesAll(self$pre)
		 * @post self->includesAll(c)
		 */
		public void addAll(final Collection c) {
			container.addAll(c);
		}

		/**
		 * Add all elements in <code>c</code> to the wrapped container, but ensures that there are no duplicates in the
		 * container.
		 *
		 * @param c is the collection of the elements to be added.
		 *
		 * @invariant c.oclIsKindOf(Bag(Object))
		 * @invariant self->includesAll(self$pre)
		 * @post self->includesAll(c)
		 * @post c->forall(o | self->count(o) = 1)
		 */
		public void addAllNoDuplicates(final Collection c) {
			for (Iterator i = c.iterator(); i.hasNext();) {
				Object element = i.next();

				if (container.contains(element)) {
					continue;
				}
				add(element);
			}
		}

		/**
		 * Removes all objects from <code>container</code>.
		 *
		 * @post self->size() == 0
		 */
		public void clear() {
			container.clear();
		}

		/**
		 * Checks if the given object is stored in this collection.
		 *
		 * @param o the object to be checked for containment.
		 *
		 * @return <code>true</code> if this collection contains <code>o</code>; <code>false</code>, otherwise.
		 *
		 * @post result == self->includes(o)
		 */
		public boolean contains(final Object o) {
			return container.contains(o);
		}
	}


	/**
	 * This class imposes FIFO ordering on the stored objects.
	 *
	 * @invariant self.oclIsTypeOf(Sequence(Object))
	 *
	 * @see AbstractCollectionWrapper
	 */
	private class CWQueue
	  extends AbstractCollectionWrapper {
		/**
		 * The container that will store the objects.
		 *
		 * @invariant queue.oclIsKindOf(Sequence(Object))
		 */
		List queue = new ArrayList();

		/**
		 * Creates a new <code>CWQueue</code> instance.
		 *
		 * @post self.AbstractCollecitonwrapper::container == queue.asBag()
		 */
		CWQueue() {
			super.container = queue;
		}

		/**
		 * Adds the object to the end of the sequence.
		 *
		 * @param o the object to be stored.
		 *
		 * @invariant self->includesAll(self$pre)
		 * @post self->includes(o) and self->last == o
		 */
		public void add(final Object o) {
			queue.add(o);
		}

		/**
		 * Returns the first object in the sequence.
		 *
		 * @return the first object.
		 *
		 * @post result == self->first
		 */
		public Object get() {
			return queue.remove(0);
		}
	}


	/**
	 * This class imposes LIFO ordering on the stored objects.
	 *
	 * @invariant self.oclIsTypeOf(Sequence(Object))
	 *
	 * @see AbstractCollectionWrapper
	 */
	private class CWStack
	  extends AbstractCollectionWrapper {
		/**
		 * The container that will store the objects.
		 *
		 * @invariant stack.oclIsKindOf(Bag(Object))
		 */
		private Stack stack = new Stack();

		/**
		 * Creates a new <code>CWStack</code> instance.
		 *
		 * @post self::AbstractCollectionWrapper.container == stack->asBag(Object)
		 */
		CWStack() {
			super.container = stack;
		}

		/**
		 * Adds the object to the beginning of the sequence.
		 *
		 * @param o the object to be stored.
		 *
		 * @invariant self->includesAll(self$pre)
		 * @post self->includes(o) and self->first == o
		 */
		public void add(final Object o) {
			stack.push(o);
		}

		/**
		 * Returns the first object in the sequence.
		 *
		 * @return the first object.
		 *
		 * @post result == self->first
		 */
		public Object get() {
			return stack.pop();
		}
	}

	/**
	 * Returns a work pieces.
	 *
	 * @return a work piece.
	 *
	 * @post self$pre->exists(o | result == o)
	 */
	public Object getWork() {
		return container.get();
	}

	/**
	 * Adds all the work pieces in <code>c</code> to the bag.  This will not check if the work piece exists in the bag.
	 *
	 * @param o the work pieces to be added.
	 *
	 * @invariant self->includesAll(self$pre)
	 * @post self->includesAll(c)
	 */
	public void addAllWork(final Object[] o) {
		container.addAll(Arrays.asList(o));
	}

	/**
	 * Adds all the work pieces in <code>c</code> to the bag.  This will not check if the work piece exists in the bag.
	 *
	 * @param c the work pieces to be added.
	 *
	 * @invariant self->includesAll(self$pre)
	 * @post self->includesAll(c)
	 */
	public void addAllWork(final Collection c) {
		container.addAll(c);
	}

	/**
	 * Adds all the work pieces in <code>c</code> to the bag, if they do not exist in the bag.
	 *
	 * @param o the work pieces to the added.
	 */
	public void addAllWorkNoDuplicates(final Object[] o) {
		container.addAllNoDuplicates(Arrays.asList(o));
	}

	/**
	 * Adds all the work pieces in <code>c</code> to the bag, if they do not exist in the bag.
	 *
	 * @param c the work pieces to be added.
	 *
	 * @invariant self->includesAll(self$pre)
	 * @post self->includesAll(c)
	 * @post self->forall( o | self->count() = 1)
	 */
	public void addAllWorkNoDuplicates(final Collection c) {
		container.addAllNoDuplicates(c);
	}

	/**
	 * Adds a new work to the bag.  This will not check if the work exists in the bag.
	 *
	 * @param o the work to be added.
	 *
	 * @invariant self->includesAll(self$pre)
	 * @post self->includes(o)
	 */
	public void addWork(final Object o) {
		container.add(o);
	}

	/**
	 * Adds a new work to the bag, if it does not exist in the bag.
	 *
	 * @param o the work to be added.
	 *
	 * @invariant self->includesAll(self$pre)
	 * @post self->includes(o)
	 * @post self->forall( o | self->count() = 1)
	 */
	public void addWorkNoDuplicates(final Object o) {
		if (!container.contains(o)) {
			container.add(o);
		}
	}

	/**
	 * Removes all work pieces in this bag.
	 *
	 * @post hasWork() == true
	 */
	public void clear() {
		container.clear();
	}

	/**
	 * Checks if there is any work in this bag.
	 *
	 * @return <code>true</code> if the bag is non-empty; <code>false</code>, otherwise.
	 *
	 * @post result == (self->size() != 0)
	 */
	public boolean hasWork() {
		return !container.isEmpty();
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2003/08/11 04:20:19  venku
   - Pair and Triple were changed to work in optimized and unoptimized mode.
   - Ripple effect of the previous change.
   - Documentation and specification of other classes.

   Revision 1.1  2003/08/07 06:42:16  venku
   Major:
    - Moved the package under indus umbrella.
    - Renamed isEmpty() to hasWork() in WorkBag.

   Revision 1.7  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
