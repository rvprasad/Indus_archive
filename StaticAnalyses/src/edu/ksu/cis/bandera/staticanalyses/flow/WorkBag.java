package edu.ksu.cis.bandera.bfa;


import java.util.Collection;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

// WorkBag.java

/**
 * <p>This is a generic container of objects.  The order in which the objects are added and removed can be configured.  At
 * present, it supports LIFO and FIFO ordering.  This affects the order in which the <code>getWork()</code> will return the
 * work added to this bag.</p>
 *
 * <p>Created: Thu Jul 25 18:37:24 2002.</p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad</a>
 * @version $Revision$
 */
public class WorkBag {

	/**
	 * <p>An instance of <code>Logger</code> used for logging purposes.</p>
	 */
	private static final Logger logger = LogManager.getLogger(WorkBag.class);

	/**
	 * <p>Used to indicate Last-In-First-Out ordering on the work pieces stored in this container.</p>
	 *
	 */
	public static final int LIFO = 1;

    /**
	 * <p>Used to indicate First-In-First-Out ordering on the work pieces stored in this container.</p>
	 *
	 */
	public static final int FIFO = 2;

	/**
	 * <p>This will store work pieces.</p>
	 *
	 */
	private CollectionWrapper container;

	/**
	 * <p>Creates a new <code>WorkBag</code> instance.</p>
	 *
	 * @param order the requested ordering on the pieces that will stored in this bag.  It has to be either LIFO or FIFO.
	 */
	public WorkBag (int order){
		if (order == LIFO) {
			container = new CWStack();
		} else if (order == FIFO) {
			container = new CWQueue();
		} else {
			throw new IllegalArgumentException("Invalid order specification.");
		} // end of else

	}

	/**
	 * <p>Adds a new work to the bag.  This will not check if the work exists in the bag.</p>
	 *
	 * @param o the work to be added.
	 */
	public void addWork(Object o) {
		container.putWork(o);
	}

	/**
	 * <p>Adds a new work to the bag, if it does not exist in the bag.</p>
	 *
	 * @param o the work to be added.
	 */
	public void addWorkNoDuplicates(Object o) {
		if (!container.contains(o)) {
			container.putWork(o);
		} // end of if (!container.contains(o))
	}

	/**
	 * <p>Returns a work pieces.</p>
	 *
	 * @return a work piece.
	 */
	public Object getWork() {
		return container.getWork();
	}

	/**
	 * <p>Removes all work pieces in this bag.</p>
	 *
	 */
	public void clear() {
		container.clear();
	}

	/**
	 * <p>Returns the filled status of this bag.</p>
	 *
	 * @return  <code>true</code> if the bag is empty; <code>false</code>, otherwise.
	 */
	public boolean isEmpty() {
		return container.isEmpty();
	}

	/**
	 * <p>A generic interface to be implemented by container classes.  This interface will be used by the bag to operate the
	 * container objects.</p>
	 *
	 */
	protected interface CollectionWrapper {
		/**
		 * <p>Returns a stored object.</p>
		 *
		 * @return a stored object.
		 */
		public Object getWork();

		/**
		 * <p>Stores an object.</p>
		 *
		 * @param o the object to be stored.
		 */
		public void putWork(Object o);

		/**
		 * <p>Removes all objects from this collection.</p>
		 *
		 */
		public void clear();

		/**
		 * <p>Checks if the given object is stored in this collection.</p>
		 *
		 * @param o the object to be checked for containment.
		 * @return <code>true</code> if this collection contains <code>o</code>; <code>false</code>, otherwise.
		 */
		public boolean contains(Object o);

		/**
		 * <p>Returns the filled status of this collection.</p>
		 *
		 * @return  <code>true</code> if the collection is empty; <code>false</code>, otherwise.
		 */
		public boolean isEmpty();
	}

	/**
	 * <p>An Abstract implementation of the <code>CollectionWrapper</code> interface.</p>
	 *
	 */
	protected abstract class AbstractCollectionWrapper implements CollectionWrapper {

		/**
		 * <p>The container that will store the objects.</p>
		 *
		 */
		protected Collection container;

		/**
		 * <p>Removes all objects from <code>container</code>.</p>
		 *
		 */
		public void clear() {
			container.clear();
		}

		/**
		 * <p>Returns the filled status of this collection.</p>
		 *
		 * @return  <code>true</code> if the collection is empty; <code>false</code>, otherwise.
		 */
		public boolean isEmpty() {
			return container.isEmpty();
		}

		/**
		 * <p>Checks if the given object is stored in this collection.</p>
		 *
		 * @param o the object to be checked for containment.
		 * @return <code>true</code> if this collection contains <code>o</code>; <code>false</code>, otherwise.
		 */
		public boolean contains(Object o) {
			return container.contains(o);
		}

	}

	/**
	 * <p>This class implements LIFO ordering on the stored objects.</p>
	 *
	 */
	private class CWStack extends AbstractCollectionWrapper {

		/**
		 * <p>The container that will store the objects.</p>
		 *
		 */
		private Stack stack = new Stack();

		/**
		 * <p>Creates a new <code>CWStack</code> instance.</p>
		 *
		 */
		CWStack() {
			container = stack;
		}

		/**
		 * <p>Returns the last-stored object.</p>
		 *
		 * @return the last-stored object.
		 */
		public Object getWork() {
			return stack.pop();
		}

		/**
		 * <p>Stores an object.</p>
		 *
		 * @param o the object to be stored.
		 */
		public void putWork(Object o) {
			stack.push(o);
		}
	}

	/**
	 * <p>This class implements FIFO ordering on the stored objects.</p>
	 *
	 */
	private class CWQueue extends AbstractCollectionWrapper {

		/**
		 * <p>The container that will store the objects.</p>
		 *
		 */
		List queue = new ArrayList();

		/**
		 * <p>Creates a new <code>CWQueue</code> instance.</p>
		 *
		 */
		CWQueue() {
			container = queue;
		}

		/**
		 * <p>Returns the first-stored object.</p>
		 *
		 * @return the first-stored object.
		 */
		public Object getWork() {
			return queue.get(0);
		}

		/**
		 * <p>Stores an object.</p>
		 *
		 * @param o the object to be stored.
		 */
		public void putWork(Object o) {
			queue.add(o);
		}
	}

}// WorkBag
