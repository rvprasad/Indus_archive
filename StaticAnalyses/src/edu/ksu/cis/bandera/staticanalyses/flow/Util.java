package edu.ksu.cis.bandera.bfa;


import ca.mcgill.sable.soot.NoSuchMethodException;
import ca.mcgill.sable.soot.SootClass;
import ca.mcgill.sable.soot.Type;
import ca.mcgill.sable.soot.VoidType;
import ca.mcgill.sable.util.List;
import ca.mcgill.sable.util.VectorList;

import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

//Util.java

/**
 * <p>General utility class providing common chore methods.</p>
 *
 * @author <a href="mailto:rvprasad@cis.ksu.edu">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */

public class Util {

	/**
	 * <p>An empty list to be used for queries on no parameter method.  The contents of this list should not be altered.</p>
	 *
	 */
	final private static List emptyParamList = new VectorList();

	/**
	 * <p>An instance of <code>Logger</code> used for logging facility.</p>
	 *
	 */
	private static final Logger logger = LogManager.getLogger(Util.class);

	/**
	 * <p>A private constructor to prevent the instantiation of this class.</p>
	 */
	private Util() {
	}

	/**
	 * <p>Creates a new object which is of type <code>ca.mcgill.sable.util.Collection</code> and copies the contents of
	 * <code>src</code> into it.</p>
	 *
	 * @param targetType name of the class which implements <code>ca.mcgill.sable.util.Collection</code> interface and which
	 * will be the actual type of the returned object.
	 * @param src an object which implements <code>java.util.Collection</code> interface and contains values that need to be
	 * copied.
	 * @return an instance of type <code>targetType</code> which contains all the values in collection <code>src</code>.
	 */
	public static ca.mcgill.sable.util.Collection convert(String targetType, Collection src) {
		ca.mcgill.sable.util.Collection retval = null;
		try {
			Class collect = Class.forName(targetType);
			retval = (ca.mcgill.sable.util.Collection)collect.newInstance();
			if (src != null) {
				Iterator i = src.iterator();
				while (i.hasNext()) {
					retval.add(i.next());
				} // end of while (i.hasNext())
			} // end of if (c != null)
		} catch (ClassCastException e) {
			logger.warn(targetType + " does not implement java.util.Collection.", e);
		} catch (ClassNotFoundException e) {
			logger.warn("The class named " + targetType + " is not available in the class path.", e);
		} catch (Exception e) {
			logger.warn("Error instantiating an object of class " + targetType + ".", e);
		} // end of catch

		return retval;
	}

	/**
	 * <p>Creates a new object which is of type <code>java.util.Collection</code> and copies the contents of the
	 * <code>src</code> into it.</p>
	 *
	 * @param targetType name of the class which implements <code>java.util.Collection</code> interface and which will be the
	 * actual type of the returned object.
	 * @param src an object which implements <code>ca.mcgill.sable.util.Collection</code> interface and contains values that
	 * need to be copied.
	 * @return an instance of type <code>targetType</code> which contains all the values in collection <code>src</code>.
	 */
	public static Collection convert(String targetType, ca.mcgill.sable.util.Collection src) {
		Collection retval = null;
		try {
			Class collect = Class.forName(targetType);
			retval = (Collection)collect.newInstance();
			if (src != null) {
				ca.mcgill.sable.util.Iterator i = src.iterator();
				while (i.hasNext()) {
					retval.add(i.next());
				} // end of while (i.hasNext())

			} // end of if (c != null)
		} catch (ClassCastException e) {
			logger.warn(targetType + " does not implement java.util.Collection.", e);
		} catch (ClassNotFoundException e) {
			logger.warn("The class named " + targetType + " is not available in the class path.", e);
		} catch (Exception e) {
			logger.warn("Error instantiating an object of class " + targetType + ".", e);
		} // end of catch

		return retval;
	}

	/**
	 * <p>Provides the <code>SootClass</code> which injects the given method into the specific branch of the inheritence
	 * hierarchy which contains <code>sc<code>.  This is a shorthand version of Util.getDeclaringClass() where the
	 * <code>parameterTypes</code> is empty and the returnType is <code>VoidType</code>.</p>
	 *
	 * @param sc class in or above which the method may be defined.
	 * @param method name of the method (not the fully classified name).
	 * @return If there is such a class then a <code>SootClass</code> object is returned; <code>null</code> otherwise.
	 * @throws <code>ca.mcgill.sable.soot.NoSuchMethodException</code> is thrown when no such method is declared in the given
	 * hierarchy.
	 */
	public static SootClass getDeclaringClassFromName(SootClass sc, String method) {
		return getDeclaringClass(sc, method, emptyParamList, VoidType.v());
	}

	/**
	 * <p>Provides the <code>SootClass</code> which injects the given method into the specific branch of the inheritence
	 * hierarchy which contains <code>sc</code>.</p>
	 *
	 * @param sc class that defines the branch in which the injecting class exists.
	 * @param method name of the method (not the fully classified name).
	 * @param parameterTypes list of type of the parameters of the method.
	 * @param returnType return type of the method.
	 * @return If there is such a class then a <code>SootClass</code> object is returned; <code>null</code> otherwise.
	 * @throws <code>ca.mcgill.sable.soot.NoSuchMethodException</code> is thrown when no such method is declared in the given
	 * hierarchy.
	 */
	public static SootClass getDeclaringClass(SootClass sc, String method, List parameterTypes, Type returnType) {
		SootClass contains = sc;
		while(!contains.declaresMethod(method, parameterTypes, returnType)) {
			if (contains.hasSuperClass()) {
				contains = contains.getSuperClass();
			} else {
				throw new NoSuchMethodException(sc + " does not define " + method + ".");
			} // end of else
		}
		return contains;
	}

	/**
	 * <p>Checks if one class is an ancestor of another.  It is assumed that a class cannot be it's own ancestor.</p>
	 *
	 * @param child  class whose ancestor is of interest.
	 * @param ancestor  fully qualified name of the ancestor class.
	 * @return <code>true</code> if a class by the name of <code>ancestor</code> is indeed the ancestor of <code>child</code>;
	 * false otherwise.
	 */
	public static boolean isAncestorOf(SootClass child, String ancestor) {
		boolean retval = false;
		while ((retval == false) && child.hasSuperClass()) {
			if (child.getName().equals(ancestor)) {
				retval = true;
			} else {
				child = child.getSuperClass();
			}
		}
		return retval;
	}
}// Util
