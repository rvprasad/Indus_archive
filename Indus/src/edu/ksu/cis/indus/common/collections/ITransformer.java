package edu.ksu.cis.indus.common.collections;

/**
 * A transformer.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <I> is the type of input object to the transformer.
 * @param <O> is the type of output object to the transformer.
 */
public interface ITransformer<I, O> {

	/**
	 * Transforms the given object.
	 * 
	 * @param input is the object to be transformed
	 * @return the transformed object.
	 */
	O transform(I input);

}

// End of File
