package edu.ksu.cis.indus.common.collections;

/**
 * DOCUMENT ME!
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <I>
 * @param <O>
 */
public interface ITransformer<I, O> {

	/**
	 * DOCUMENT ME!
	 * 
	 * @param input DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public O transform(I input);

}

// End of File
