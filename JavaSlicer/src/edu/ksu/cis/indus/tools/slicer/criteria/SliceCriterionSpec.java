
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

package edu.ksu.cis.indus.tools.slicer.criteria;

import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.slicer.CriteriaSpecHelper;
import edu.ksu.cis.indus.slicer.ISliceCriterion;
import edu.ksu.cis.indus.slicer.SliceCriteriaFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.MissingResourceException;

import org.apache.commons.lang.builder.ToStringBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.ValueBox;

import soot.jimple.Stmt;


/**
 * This class is the criterion specification.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
final class SliceCriterionSpec
  implements Cloneable {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(SliceCriterionSpec.class);

	/**
	 * The singleton slice criteria factory.
	 */
	private static final SliceCriteriaFactory CRITERIA_FACTORY = new SliceCriteriaFactory();

	/**
	 * The sequence of names of parameter types of the method immediately enclosing the criterion.
	 *
	 * @invariant parameterTypeNames.oclIsKindOf(Sequence(String))
	 */
	private List parameterTypeNames;

	/**
	 * The name of the class immediately enclosing the criterion.
	 */
	private String className;

	/**
	 * The name of the method immediately enclosing the criterion.
	 */
	private String methodName;

	/**
	 * The name of the return type of the method  immediately enclosing the criterion.
	 */
	private String returnTypeName;

	/**
	 * This indicates if all expressions in the statement should be considered as slice criterion.
	 */
	private boolean considerEntireStmt;

	/**
	 * This indicates if the execution of the statement should be considered as slice criterion.
	 */
	private boolean considerExecution;

	/**
	 * The index of the use or def value box, in the statement, that is the slice criterion.
	 */
	private int exprIndex = -1;  // Initialization is required due to a limitation of JiBX (known problem 3 on JiBX site)

	/**
	 * The index of the statement, in the method body, in which the slice criterion occurs.
	 */
	private int stmtIndex;

	/**
	 * Creates an instance of this class.
	 */
	private SliceCriterionSpec() {
		parameterTypeNames = createParameterTypeNamesContainer();
	}

	/**
	 * Retrieves the criteria represented by this spec relative to the given scene.
	 *
	 * @param scene relative to which the criteria will be generated.
	 *
	 * @return the collection of slice criterion.
	 *
	 * @throws MissingResourceException when the any of the types used in the criterion specification cannot be found.
	 * @throws IllegalStateException when the method in the spec does not have a body or the specified statment/expr does not
	 * 		   exists.
	 */
	public Collection getCriteria(final Scene scene) {
		trim();

		final SootClass _sc = scene.getSootClass(className);

		if (_sc == null) {
			final String _msg = className + " is not available in the System.";
			LOGGER.error(_msg);
			throw new MissingResourceException("Given class not available in the System.", className, null);
		}

		final List _parameterTypes = new ArrayList();

		for (final Iterator _i = parameterTypeNames.iterator(); _i.hasNext();) {
			final String _name = (String) _i.next();
			_parameterTypes.add(Util.getTypeFor(_name, scene));
		}

		final SootMethod _sm = _sc.getMethod(methodName, _parameterTypes, Util.getTypeFor(returnTypeName, scene));
		_sc.setApplicationClass();

		final Body _body = _sm.retrieveActiveBody();

		if (_body == null) {
			final String _msg = returnTypeName + " " + methodName + "(" + parameterTypeNames + ") does not have a body.";
			LOGGER.error(_msg);
			throw new IllegalStateException(_msg);
		}

		final List _stmts = Collections.list(Collections.enumeration(_body.getUnits()));

		if (_stmts.size() < stmtIndex + 1) {
			final String _msg =
				returnTypeName + " " + methodName + "(" + parameterTypeNames + ") has only " + _stmts.size()
				+ " statements. [" + stmtIndex + "]";
			LOGGER.error(_msg);
			throw new IllegalStateException(_msg);
		}

		final Collection _result;
		final Stmt _stmt = (Stmt) _stmts.get(stmtIndex);

		if (exprIndex == -1) {
			_result = CRITERIA_FACTORY.getCriterion(_sm, _stmt, considerEntireStmt);
		} else {
			_result = CRITERIA_FACTORY.getCriterion(_sm, _stmt, (ValueBox) _stmt.getUseAndDefBoxes().get(exprIndex));
		}

		if (considerExecution) {
			for (final Iterator _i = _result.iterator(); _i.hasNext();) {
				((ISliceCriterion) _i.next()).setConsiderExecution(true);
			}
		}
		return _result;
	}

	/**
	 * Extracts the names of the given type.
	 *
	 * @param parameterTypes is the collection of types whose names should be extracted.
	 *
	 * @return the names of the type.
	 *
	 * @pre paramterTypes != null and parameterTypes.oclIsKindOf(Sequence(Type))
	 * @post result != null and result.oclIsKindOf(Collection(String))
	 */
	public static List getNamesOfTypes(final List parameterTypes) {
		final List _result = new ArrayList();

		for (final Iterator _i = parameterTypes.iterator(); _i.hasNext();) {
			_result.add(((Type) _i.next()).toString());
		}
		return _result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		trim();
		return new ToStringBuilder(this).append("className", this.className).append("returnTypeName", this.returnTypeName)
										  .append("methodName", this.methodName)
										  .append("parameterTypeNames", this.parameterTypeNames)
										  .append("stmtIndex", this.stmtIndex).append("exprIndex", this.exprIndex)
										  .append("considerExecution", this.considerExecution)
										  .append("considerEntireStmt", this.considerEntireStmt).toString();
	}

	/**
	 * Retrieves the specification for the given criterion relative to <code>scene</code>.
	 *
	 * @param criterion for which the specification should be generated.
	 *
	 * @return the specification.
	 *
	 * @throws IllegalStateException when an error occurs while creating the criterion.
	 *
	 * @pre criterion != null and scene != null
	 * @post result != null
	 */
	static Collection getCriterionSpec(final ISliceCriterion criterion) {
		final SootMethod _method = CriteriaSpecHelper.getOccurringMethod(criterion);

		final String _className = _method.getDeclaringClass().getJavaStyleName();
		final String _methodName = _method.getName();
		final Body _body = _method.retrieveActiveBody();

		if (_body == null) {
			final String _msg = _method.getSignature() + " does not have a body.";
			LOGGER.error(_msg);
			throw new IllegalStateException(_msg);
		}

		final List _stmts = Collections.list(Collections.enumeration(_body.getUnits()));
		final Stmt _occurringStmt = CriteriaSpecHelper.getOccurringStmt(criterion);
		final int _stmtIndex = _stmts.indexOf(_occurringStmt);
		final boolean _considerExecution = CriteriaSpecHelper.isConsiderExecution(criterion);

		final SliceCriterionSpec _spec = new SliceCriterionSpec();
		_spec.className = _className;
		_spec.methodName = _methodName;
		_spec.parameterTypeNames = getNamesOfTypes(_method.getParameterTypes());
		_spec.returnTypeName = _method.getReturnType().toString();
		_spec.stmtIndex = _stmtIndex;
		_spec.exprIndex = -1;
		_spec.considerExecution = _considerExecution;
		_spec.considerEntireStmt = false;

		final Collection _result = new ArrayList();
		final ValueBox _occurringExprBox = CriteriaSpecHelper.getOccurringExpr(criterion);

		if (_occurringExprBox != null) {
			final Value _expr = _occurringExprBox.getValue();
			final int _size = _expr.getUseBoxes().size();

			if (_size == 0) {
				_spec.exprIndex = _occurringStmt.getUseAndDefBoxes().indexOf(_occurringExprBox);
				_result.add(_spec);
			} else {
				for (int _i = _size - 1; _i >= 0; _i--) {
					try {
						final SliceCriterionSpec _temp = (SliceCriterionSpec) _spec.clone();
						_temp.exprIndex = _i;
						_result.add(_temp);
					} catch (final CloneNotSupportedException _e) {
						final String _msg = "Low level Error while creating criterion specification.";
						LOGGER.error(_msg);
						throw new IllegalStateException(_msg);
					}
				}
			}
		} else {
			_result.add(_spec);
		}

		return _result;
	}

	/**
	 * Creates a container for names of paramter types. This is used mainly during deserialization.
	 *
	 * @return the container.
	 *
	 * @post result != null
	 */
	private static List createParameterTypeNamesContainer() {
		return new ArrayList();
	}

	/**
	 * Tests if the expression index is valid in this spec. This is used to serialize/deserialize.
	 *
	 * @return <code>true</code> if an element capturing the expression index should be output; <code>false</code>, otherwise.
	 */
	private boolean testExpr() {
		return exprIndex != -1;
	}

	/**
	 * Trims the string data in the specification.
	 */
	private void trim() {
		if (className != null) {
			className = className.trim();
		}

		if (methodName != null) {
			methodName = methodName.trim();
		}

		if (returnTypeName != null) {
			returnTypeName = returnTypeName.trim();
		}

		for (final ListIterator _i = parameterTypeNames.listIterator(); _i.hasNext();) {
			final String _str = (String) _i.next();

			if (_str != null) {
				_i.remove();
				_i.add(_str.trim());
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2004/07/02 09:00:08  venku
   - added support to serialize/deserialize slice criteria. (feature #397)
   - used the above support in SliceXMLizerCLI.
   - used Jakarta Commons IO library.
 */