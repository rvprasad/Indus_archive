
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

import edu.ksu.cis.indus.common.datastructures.IWorkBag;

import edu.ksu.cis.indus.staticanalyses.tokens.ITokenFilter;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Flow graph node associated with value associated variants.  This class provides the basic behavior required by the nodes
 * in the flow graph.  It is required that the nodes be able to keep track of the successor nodes and the set of values.
 * However, an implementation may transform the existing values as new values arrive, or change successors as new successors
 * are added.  Hence, all imlementing classes are required to implement <code>IFGNode.onNewSucc</code> and
 * <code>IFGNode.onNewTokens</code> methods.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public abstract class AbstractFGNode
  implements IFGNode {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(AbstractFGNode.class);

	/**
	 * The set of immediate successor nodes, i.e., there is direct edge from this node to the successor nodes, of this node.
	 * The elements in the set are of type <code>IFGNode</code>.
	 *
	 * @invariant succs != null
	 */
	protected final Set succs = new HashSet();

	/**
	 * A filter that controls the outflow of values from this node.
	 */
	protected ITokenFilter filter;

	/**
	 * The set of tokens that will be used to store tokens at this node.
	 *
	 * @invariant tokens != null
	 */
	protected final ITokens tokens;

	/**
	 * The worklist associated with the enclosing instance of the framework.  This is required if subclasses will want to
	 * generate new work depending on the new values or new successors that may occur.
	 *
	 * @invariant worklist != null
	 */
	protected final IWorkBag worklist;

	/**
	 * Creates a new <code>AbstractFGNode</code> instance.
	 *
	 * @param worklistToUse The worklist associated with the enclosing instance of the framework.
	 * @param tokenSet to be used to store the tokens at this node.
	 *
	 * @pre worklistToUse != null and tokenSet != null
	 */
	protected AbstractFGNode(final IWorkBag worklistToUse, final ITokens tokenSet) {
		this.worklist = worklistToUse;
		tokens = tokenSet;
	}

	/**
	 * This method will throw <code>UnsupprotedOperationException</code>.
	 *
	 * @return (This method raises an exception.)
	 *
	 * @throws UnsupportedOperationException as this method is not supported by this class but should be implemented by
	 * 		   subclasses.
	 */
	public Object getClone() {
		throw new UnsupportedOperationException("Parameterless prototype() method is not supported.");
	}

	/**
	 * This method will throw <code>UnsupprotedOperationException</code>.
	 *
	 * @param param <i>ignored</i>.
	 *
	 * @return (This method raises an exception.)
	 *
	 * @throws UnsupportedOperationException as this method is not supported by this class but should be implemented by
	 * 		   subclasses.
	 */
	public Object getClone(final Object param) {
		throw new UnsupportedOperationException("prototype(param1) method is not supported.");
	}

	/**
	 * Sets the filter on this node.
	 *
	 * @param filterToUse to be used by this node.
	 */
	public void setFilter(final ITokenFilter filterToUse) {
		this.filter = filterToUse;
	}

	/**
	 * Retrieves the set of tokens accumulated in this node.
	 *
	 * @return the set of tokens.
	 *
	 * @post result != null
	 */
	public final ITokens getTokens() {
		return this.tokens;
	}

	/**
	 * Retrieves the values that have accumulated at this node.
	 *
	 * @return the values accumulated at this node.
	 *
	 * @post result != null
	 */
	public final Collection getValues() {
		return tokens.getValues();
	}

	/**
	 * Adds a successor node to this node.
	 *
	 * @param node the node to be added as successor to this node.
	 *
	 * @pre node != null
	 */
	public void addSucc(final IFGNode node) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Adding " + node + " as the successor to " + this);
		}
		succs.add(node);
		onNewSucc(node);
	}

	/**
	 * Returns a stringized representation of this object.
	 *
	 * @return the stringized representation of this object.
	 *
	 * @post result != null
	 */
	public String toString() {
		return "IFGNode:" + hashCode();
	}

	/**
	 * Injects a set of values into the set of values associated with this node.
	 *
	 * @param newTokens the collection of<code>Object</code>s to be added as successors to this node.
	 *
	 * @pre values != null
	 */
	protected final void addTokens(final ITokens newTokens) {
		final ITokens _diffTokens = newTokens.diffTokens(tokens);
		tokens.addTokens(newTokens);
		onNewTokens(_diffTokens);
	}

	/**
	 * Returns a collection containing the set difference between values in this node and the given node.  The values in this
	 * node provide A in A \ B whereas B is provided by the <code>src</code>.
	 *
	 * @param src the node containing the values of set B in A \ B.
	 *
	 * @return a collection of values in that exist in this node and not in <code>src</code>.
	 *
	 * @pre src != null
	 */
	protected ITokens diffTokens(final IFGNode src) {
		return tokens.diffTokens(src.getTokens());
	}

	/**
	 * Adds a new work to the worklist to propogate the values in this node to <code>succ</code>.  Only the difference values
	 * are propogated.
	 *
	 * @param succ the successor node that was added to this node.
	 *
	 * @pre succ != null
	 */
	protected void onNewSucc(final IFGNode succ) {
		ITokens _temp = diffTokens(succ);

		if (filter != null) {
			_temp = filter.filter(_temp);
		}

		if (!_temp.isEmpty()) {
			worklist.addWork(SendTokensWork.getWork(succ, _temp));
		}
	}

	/**
	 * Adds a new work to the worklist to propogate <code>values</code> in this node to it's successor nodes.
	 *
	 * @param newTokens the values to be propogated to the successor node.  The collection contains object of
	 * 		  type<code>Object</code>.
	 *
	 * @pre newTokens != null
	 */
	protected void onNewTokens(final ITokens newTokens) {
		ITokens _temp = newTokens;

		if (filter != null) {
			_temp = filter.filter(_temp);
		}

		for (final Iterator _i = succs.iterator(); _i.hasNext();) {
			final IFGNode _succ = (IFGNode) _i.next();

			if (!diffTokens(_succ).isEmpty()) {
				worklist.addWork(SendTokensWork.getWork(_succ, _temp));
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.6  2003/12/02 09:42:36  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.5  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.4  2003/08/26 16:53:34  venku
   diffValues() used to get values() on src inside the loop.  However,
   this was loop invariant, hence, has been hoisted outside the loop.
   Revision 1.3  2003/08/17 09:59:03  venku
   Spruced up documentation and specification.
   Documentation changes to FieldVariant.
   Revision 1.2  2003/08/16 02:50:22  venku
   Spruced up documentation and specification.
   Moved onNewXXX() methods from IFGNode to AbstractFGNode.
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
   Revision 0.10  2003/05/22 22:18:32  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
