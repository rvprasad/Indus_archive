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

package edu.ksu.cis.indus.common.collections;

import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;

import java.util.ArrayList;
import java.util.EmptyStackException;

/**
 * A stack implementation.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <T> is the type of object stored in this stack.
 */
public final class Stack<T>
		extends ArrayList<T>
		implements Cloneable {

	/**
	 * The serial version ID.
	 */
	private static final long serialVersionUID = -3292985677102986589L;

	/**
	 * The index at which the top of the stack occurs.
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
	 * {@inheritDoc}
	 */
	@Override public void clear() {
		super.clear();
		top = -1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional @Override public Stack<T> clone() {
		@SuppressWarnings("unchecked") final Stack<T> _stack = (Stack) super.clone();
		return _stack;
	}

	/**
	 * Checks if the stack is empty.
	 * 
	 * @return <code>true</code> if the stack is empty; <code>false</code>, otherwise.
	 */
	@Functional public boolean empty() {
		return top == -1;
	}

	/**
	 * Retrieves the top of the stack element without popping it.
	 * 
	 * @return the top of the stack element.
	 * @throws EmptyStackException when the stack is empty.
	 */
	@Functional public T peek() {
		if (empty()) {
			throw new EmptyStackException();
		}
		return get(top);
	}

	/**
	 * Pops the element at the top of this stack.
	 * 
	 * @return the top of the stack element.
	 * @throws EmptyStackException when the stack is empty.
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
	 * Pushes the given item onto this stack.
	 * 
	 * @param item to be pushed onto the stack.
	 * @return the item that was pushed.
	 */
	public T push(@NonNull @Immutable final T item) {
		add(++top, item);
		return item;
	}

	/**
	 * Retrieves the index at which the given input occurs on this stack.
	 * 
	 * @param <T1> is the type of the input.
	 * @param o is the input to search for.
	 * @return the index of the input from the bottom of the stack; -1, if the input does not exist in the stack.
	 */
	@Functional public <T1 extends T> int search(final T1 o) {
		for (int _i = top; _i >= 0; _i--) {
			if (get(_i).equals(o)) {
				return _i + 1;
			}
		}
		return -1;
	}
}

// End of File
