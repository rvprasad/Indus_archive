
/*
 * Bandera, a Java(TM) analysis and transformation toolkit
 * Copyright (C) 2002, 2003, 2004.
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://www.cis.ksu.edu/santos/bandera).
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
 *                http://www.cis.ksu.edu/santos/bandera
 */

package edu.ksu.cis.bandera.staticanalyses.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;


// WorkBag.java

/**
 * This is a generic container of objects.  The order in which the objects are added and removed can be configured.  At
 * present, it supports LIFO and FIFO ordering.  This affects the order in which the <code>getWork()</code> will return the
 * work added to this bag.    Created: Thu Jul 25 18:37:24 2002.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad</a>
 * @version $Revision$
 */
public class WorkBag {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(WorkBag.class);

	/**
	 * Used to indicate Last-In-First-Out ordering on the work pieces stored in this container.
	 */
	public static final int LIFO = 1;

	/**
	 * Used to indicate First-In-First-Out ordering on the work pieces stored in this container.
	 */
	public static final int FIFO = 2;

	/**
	 * This will store work pieces.
	 *
	 * @invariant container.oclType = Bag(Object)
	 */
	private CollectionWrapper container;

	/**
	 * Creates a new <code>WorkBag</code> instance.
	 *
	 * @param order is the requested ordering on the work pieces that will stored in this bag.  It has to be either
	 * 		  <code>LIFO</code> or <code>FIFO</code>.
	 *
	 * @throws IllegalArgumentException when any order other than <code>LIFO</code> or <code>FIFO</code> is specified.
	 */
	public WorkBag(int order) {
		if(order == LIFO) {
			container = new CWStack();
		} else if(order == FIFO) {
			container = new CWQueue();
		} else {
			throw new IllegalArgumentException("Invalid order specification.");
		}
	}

	/**
	 * A generic interface to be implemented by container classes.  This interface will be used by the bag to operate the
	 * container objects.
	 */
	protected interface CollectionWrapper {
		/**
		 * Returns the filled status of this collection.
		 *
		 * @return <code>true</code> if the collection is empty; <code>false</code>, otherwise.
		 *
		 * @post result = self->isEmpty()
		 */
		public boolean isEmpty();

		/**
		 * Stores an object.
		 *
		 * @param o the object to be stored.
		 *
		 * @post self->includes(o)
		 */
		public void add(Object o);

		/**
		 * Stores all the objects in the given collection.
		 *
		 * @param c the collection containing the objects to be stored.
		 *
		 * @invariant c : Bag(java.lang.Object)
		 * @post self->includesAll(c)
		 */
		public void addAll(Collection c);

		/**
		 * Stores all the objects in the given collection and ensures that duplicates do not exist.
		 *
		 * @param c the collection containing the objects to be stored.
		 *
		 * @invariant c : Bag(Object)
		 * @post self->includesAll(c) and self->forall( o | self->count(o) = 1)
		 */
		public void addAllNoDuplicates(Collection c);

		/**
		 * Removes all objects from this collection.
		 *
		 * @post self->isEmpty()
		 */
		public void clear();

		/**
		 * Checks if the given object is stored in this collection.
		 *
		 * @param o the object to be checked for containment.
		 *
		 * @return <code>true</code> if this collection contains <code>o</code>; <code>false</code>, otherwise.
		 *
		 * @post result = self->includes(o)
		 */
		public boolean contains(Object o);

		/**
		 * Returns a stored object.
		 *
		 * @return a stored object.
		 *
		 * @post self->exists(o | result = o)
		 */
		public Object get();
	}

	/**
	 * An Abstract implementation of the <code>CollectionWrapper</code> interface.  <code>add(Object)</code> needs to be
	 * implemented by the extending class.  Also, the container needs to be initialized in the constructor of the extending
	 * class.
	 */
	protected abstract class AbstractCollectionWrapper
	  implements CollectionWrapper {
		/**
		 * The container that will store the objects.
		 *
		 * @invariant container.oclType = Bag(Object)
		 */
		protected Collection container;

		/**
		 * Returns the filled status of this collection.
		 *
		 * @return <code>true</code> if the collection is empty; <code>false</code>, otherwise.
		 *
		 * @post result = container.isEmpty()
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
		 * @invariant container.includesAll(container$pre)
		 * @post container.containsAll(c)
		 */
		public void addAll(Collection c) {
			container.addAll(c);
		}

		/**
		 * Add all elements in <code>c</code> to the wrapped container, but ensures that there are no duplicates in the
		 * container.
		 *
		 * @param c is the collection of the elements to be added.
		 *
		 * @invariant c.oclType = Bag(Object)
		 * @invariant container.includesAll(container$pre)
		 * @post container.containsAll(c)
		 * @post c->forall(o | container->count(o) = 1)
		 */
		public void addAllNoDuplicates(Collection c) {
			for(Iterator i = c.iterator(); i.hasNext();) {
				Object element = (Object) i.next();

				if(container.contains(element)) {
					continue;
				}
				add(element);
			}
		}

		/**
		 * Removes all objects from <code>container</code>.
		 *
		 * @post container.size() = 0
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
		 * @post result = container.contains(o)
		 */
		public boolean contains(Object o) {
			return container.contains(o);
		}
	}

	/**
	 * This class implements FIFO ordering on the stored objects.
	 */
	private class CWQueue
	  extends AbstractCollectionWrapper {
		/**
		 * The container that will store the objects.
		 *
		 * @invariant queue.oclType = Sequence(Object)
		 */
		List queue = new ArrayList();

		/**
		 * Creates a new <code>CWQueue</code> instance.
		 *
		 * @post self.AbstractCollecitonwrapper::container = queue.asBag()
		 */
		CWQueue() {
			super.container = queue;
		}

		/**
		 * Stores an object.
		 *
		 * @param o the object to be stored.
		 *
		 * @invariant queue.includesAll(queue$pre)
		 * @post queue->includes(o) and queue->last = o
		 */
		public void add(Object o) {
			queue.add(o);
		}

		/**
		 * Returns the first-stored object.
		 *
		 * @return the first-stored object.
		 *
		 * @post result = queue->first
		 */
		public Object get() {
			return queue.remove(0);
		}
	}

	/**
	 * This class implements LIFO ordering on the stored objects.
	 */
	private class CWStack
	  extends AbstractCollectionWrapper {
		/**
		 * The container that will store the objects.
		 *
		 * @invariant stack.oclType = Bag(Object)
		 */
		private Stack stack = new Stack();

		/**
		 * Creates a new <code>CWStack</code> instance.
		 *
		 * @post self::AbstractCollectionWrapper.container = stack->asBag(Object)
		 */
		CWStack() {
			super.container = stack;
		}

		/**
		 * Stores an object.
		 *
		 * @param o the object to be stored.
		 *
		 * @invariant stack.includesAll(stack$pre)
		 * @invariant stack.includesAll(stack$pre)
		 * @post stack->includes(o) and stack->first = o
		 */
		public void add(Object o) {
			stack.push(o);
		}

		/**
		 * Returns the last-stored object.
		 *
		 * @return the last-stored object.
		 *
		 * @post result = stack->first
		 */
		public Object get() {
			return stack.pop();
		}
	}

	/**
	 * Returns the filled status of this bag.
	 *
	 * @return <code>true</code> if the bag is empty; <code>false</code>, otherwise.
	 *
	 * @post result = container.isEmpty()
	 */
	public boolean isEmpty() {
		return container.isEmpty();
	}

	/**
	 * Returns a work pieces.
	 *
	 * @return a work piece.
	 *
	 * @post result = container.get()
	 */
	public Object getWork() {
		return container.get();
	}

	/**
	 * Adds all the work pieces in <code>c</code> to the bag.  This will not check if the work piece exists in the bag.
	 *
	 * @param o the work pieces to be added.
	 */
	public void addAllWork(Object o[]) {
		container.addAll(Arrays.asList(o));
	}

	/**
	 * Adds all the work pieces in <code>c</code> to the bag.  This will not check if the work piece exists in the bag.
	 *
	 * @param c the work pieces to be added.
	 *
	 * @invariant container->includesAll(container$pre)
	 * @post container.containsAll(c)
	 */
	public void addAllWork(Collection c) {
		container.addAll(c);
	}

	/**
	 * Adds all the work pieces in <code>c</code> to the bag, if they do not exist in the bag.
	 *
	 * @param o the work pieces to the added.
	 */
	public void addAllWorkNoDuplicates(Object o[]) {
		container.addAllNoDuplicates(Arrays.asList(o));
	}

	/**
	 * Adds all the work pieces in <code>c</code> to the bag, if they do not exist in the bag.
	 *
	 * @param c the work pieces to be added.
	 *
	 * @invariant container->includesAll(container$pre)
	 * @post container.containsAll(c)
	 * @post container->forall( o | container->count() = 1)
	 */
	public void addAllWorkNoDuplicates(Collection c) {
		container.addAllNoDuplicates(c);
	}

	/**
	 * Adds a new work to the bag.  This will not check if the work exists in the bag.
	 *
	 * @param o the work to be added.
	 *
	 * @invariant container->includesAll(container$pre)
	 * @post container.contains(o)
	 */
	public void addWork(Object o) {
		container.add(o);
	}

	/**
	 * Adds a new work to the bag, if it does not exist in the bag.
	 *
	 * @param o the work to be added.
	 *
	 * @invariant container->includesAll(container$pre)
	 * @post container.contains(o)
	 * @post container->forall( o | container->count() = 1)
	 */
	public void addWorkNoDuplicates(Object o) {
		if(!container.contains(o)) {
			container.add(o);
		}
	}

	/**
	 * Removes all work pieces in this bag.
	 *
	 * @post container.size() = 0
	 */
	public void clear() {
		container.clear();
	}
}

/*****
 ChangeLog:

$Log$

*****/
