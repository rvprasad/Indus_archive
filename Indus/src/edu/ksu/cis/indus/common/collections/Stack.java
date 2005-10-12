package edu.ksu.cis.indus.common.collections;

import java.util.ArrayList;
import java.util.EmptyStackException;

/**
 * DOCUMENT ME!
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <T> DOCUMENT ME!
 */
public final class Stack<T>
		extends ArrayList<T>
		implements Cloneable {

	/**
	 * DOCUMENT ME!
	 */
	private static final long serialVersionUID = -3292985677102986589L;

	/**
	 * DOCUMENT ME!
	 */
	private int top;

	/**
	 * Creates an instance of this class.
	 */
	public Stack() {
		super();
		top = -1;
	}

	/**
	 * @see java.util.ArrayList#clone()
	 */
	@Override public Stack<T> clone() {
		return (Stack) super.clone();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public boolean empty() {
		return top == -1;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public T peek() {
		if (empty()) {
			throw new EmptyStackException();
		}
		return get(top);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public T pop() {
		if (empty()) {
			throw new EmptyStackException();
		}
		final T _item = remove(top);
		top--;
		return _item;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param item
	 * @return DOCUMENT ME!
	 */
	public T push(T item) {
		add(++top, item);
		return item;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param <T1>
	 * @param o DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public <T1 extends T> int search(T1 o) {
		for (int _i = top; _i >= 0; _i--) {
			if (get(_i).equals(o)) {
				return _i + 1;
			}
		}
		return -1;
	}
}

// End of File
