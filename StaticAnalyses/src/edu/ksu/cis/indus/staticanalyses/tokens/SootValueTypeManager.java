
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

package edu.ksu.cis.indus.staticanalyses.tokens;

import edu.ksu.cis.indus.common.Constants;
import edu.ksu.cis.indus.common.soot.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import soot.RefType;
import soot.SootClass;
import soot.Type;
import soot.Value;


/**
 * This class manages Soot value types and the corresponding types in user's type system.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SootValueTypeManager
  implements ITypeManager {
	/**
	 * This maps soot types to user's type.
	 */
	private final Map sootType2Type = new HashMap(Constants.getNumOfClassesInApplication());

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITypeManager#getAllTypes(java.lang.Object)
	 */
	public Collection getAllTypes(final Object value) {
		final Value _theValue = (Value) value;
		final Type _type = _theValue.getType();
		Collection _result;

		if (_type instanceof RefType) {
			_result = new ArrayList();
			_result.add(getTypeForIRType(_type));

			for (final Iterator _i = Util.getAncestors(((RefType) _type).getSootClass()).iterator(); _i.hasNext();) {
				_result.add(getTypeForIRType(((SootClass) _i.next()).getType()));
			}
		} else {
			_result = Collections.singleton(getTypeForIRType(_type));
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITypeManager#getExactType(java.lang.Object)
	 */
	public IType getExactType(final Object value) {
		return getTypeForIRType(((Value) value).getType());
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITypeManager#getTypeForIRType(soot.Type)
	 */
	public IType getTypeForIRType(final Type sootType) {
		IType _result = (IType) sootType2Type.get(sootType);

		if (_result == null) {
			_result = new IType() {
						;
					};
			sootType2Type.put(sootType, _result);
		}
		return _result;
	}

	/**
	 * Resets the manager.
	 */
	public void reset() {
		sootType2Type.clear();
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2004/04/20 06:53:17  venku
   - documentation.

   Revision 1.1  2004/04/16 20:10:39  venku
   - refactoring
    - enabled bit-encoding support in indus.
    - ripple effect.
    - moved classes to related packages.
 */
