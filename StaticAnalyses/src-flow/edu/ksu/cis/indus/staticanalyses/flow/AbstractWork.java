
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

package edu.ksu.cis.indus.staticanalyses.flow;

import java.util.Collection;
import java.util.HashSet;


/**
 * A piece of work that can be processed by <code>WorkList</code>.
 * 
 * <p>
 * Created: Tue Jan 22 02:54:57 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public abstract class AbstractWork {
	/**
	 * The collection of values to be processed.
	 */
	protected final Collection values = new HashSet();

	/**
	 * The flow graph node associated with this work.
	 */
	protected IFGNode node;

	/**
	 * Creates a new <code>AbstractWork</code> instance.
	 */
	protected AbstractWork() {
	}

	/**
	 * The actual work that needs to be done when this work is executed should be in this method.
	 */
	public abstract void execute();

	/**
	 * Associates a flow graph node with this work.
	 *
	 * @param flowNode the flow graph node to be associated.
	 *
	 * @pre flowNode != null
	 */
	public final void setFGNode(final IFGNode flowNode) {
		this.node = flowNode;
	}

	/**
	 * Adds a value to the collection of values associated with this work.
	 *
	 * @param o the value to be added.
	 *
	 * @pre o != null
	 */
	public final void addValue(final Object o) {
		values.add(o);
	}

	/**
	 * Adds a collection of values to the collection of values associated with this work.
	 *
	 * @param valuesToBeProcessed the collection of values to be added for processing.
	 *
	 * @pre valuesToBeProcessed != null
	 */
	public final void addValues(final Collection valuesToBeProcessed) {
		this.values.addAll(valuesToBeProcessed);
	}

	/**
	 * Perform any sort of cleanup chores. This will/should be called after the work has been executed.
	 */
	protected void finished() {
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.5  2003/08/21 10:53:52  venku
   Changed the value collection into a set.
   Revision 1.4  2003/08/18 11:08:00  venku
   Name change for pooling support.

   Revision 1.3  2003/08/17 11:19:13  venku
   Placed the simple SendValuesWork class into a separate file.
   Extended it with work pool support.
   Amended AbstractWork and WorkList to enable work pool support.

   Revision 1.2  2003/08/17 09:59:03  venku
   Spruced up documentation and specification.
   Documentation changes to FieldVariant.

   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.

   Revision 0.9  2003/05/22 22:18:50  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
