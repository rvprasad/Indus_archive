
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

import edu.ksu.cis.indus.interfaces.IPoolable;

import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;

import org.apache.commons.pool.impl.SoftReferenceObjectPool;


/**
 * This class represents a peice of work to inject a set of tokens into a flow graph node.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SendTokensWork
  extends AbstractTokenProcessingWork
  implements IPoolable {
	/**
	 * This is the work pool of work peices that will be reused upon request.
	 *
	 * @invariant POOL.borrowObject().oclIsKindOf(SendTokensWork)
	 */
	private static final ObjectPool POOL =
		new SoftReferenceObjectPool(new BasePoolableObjectFactory() {
				/**
				 * @see org.apache.commons.pool.PoolableObjectFactory#makeObject()
				 */
				public Object makeObject() {
					return new SendTokensWork(null);
				}
			});

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(SendTokensWork.class);

	/**
	 * The flow graph node associated with this work.
	 */
	private IFGNode node;

	/**
	 * Creates an instance of this class.
	 *
	 * @param tokenSet to be used by this work object to store the tokens whose flow should be instrumented.
	 */
	SendTokensWork(final ITokens tokenSet) {
		super(tokenSet);
	}

	/**
	 * Creates a new <code>SendTokensWork</code> instance.
	 *
	 * @param toNode the node into which the tokens need to be injected.
	 * @param tokensToBeSent a collection containing the tokens to be injected.
	 *
	 * @return a work peice with the given data embedded in it.
	 *
	 * @throws RuntimeException occurs when pooling fails.  This is beyond our control.
	 *
	 * @pre toNode != null and tokensToBeSent != null
	 * @post result != null
	 */
	public static final SendTokensWork getWork(final IFGNode toNode, final ITokens tokensToBeSent) {
		try {
			final SendTokensWork _result = (SendTokensWork) POOL.borrowObject();
			_result.node = toNode;
			_result.tokens = (ITokens) tokensToBeSent.getClone();
			return _result;
		} catch (final Exception _e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("How can this happen?", _e);
			}
			throw new RuntimeException(_e);
		}
	}

	/**
	 * Injects the tokens into the associated node.
	 */
	public final void execute() {
		node.injectTokens(tokens);
	}

	/**
	 * Ignored.
	 *
	 * @see edu.ksu.cis.indus.interfaces.IPoolable#setPool(org.apache.commons.pool.ObjectPool)
	 */
	public void setPool(final ObjectPool pool) {
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IPoolable#returnToPool()
	 */
	public void returnToPool() {
		try {
			if (node instanceof AbstractFGNode) {
				((AbstractFGNode) node).forgetSendTokensWork();
			}
			node = null;
			tokens = null;
			POOL.returnObject(this);
		} catch (final Exception _e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("How can this happen?", _e);
			}
			throw new RuntimeException(_e);
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2004/04/16 20:10:39  venku
   - refactoring
    - enabled bit-encoding support in indus.
    - ripple effect.
    - moved classes to related packages.
   Revision 1.9  2004/04/02 21:59:54  venku
   - refactoring.
     - all classes except OFAnalyzer is package private.
     - refactored work class hierarchy.
   Revision 1.8  2003/12/02 09:42:37  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.7  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.6  2003/08/21 12:43:28  venku
   Previous values were not cleared when the work peice was retrieved from the pool - FIXED.
   Revision 1.5  2003/08/18 11:07:46  venku
   Name change for pooling support.
   Revision 1.4  2003/08/18 08:34:20  venku
   Well, used the object pool as available from jakarta commons implementation.
   Revision 1.3  2003/08/18 07:09:13  venku
   The way objects were removed from the pool was incorrect.  I had used get() instead of remove().
   This will not remove the object from the pool.  This has been fixed.
   Revision 1.2  2003/08/18 01:01:18  venku
   Trying to fix CVS's erratic behavior.
   Revision 1.1  2003/08/17 11:19:13  venku
   Placed the simple SendTokensWork class into a separate file.
   Extended it with work pool support.
   Amended AbstractTokenProcessingWork and WorkList to enable work pool support.
 */
