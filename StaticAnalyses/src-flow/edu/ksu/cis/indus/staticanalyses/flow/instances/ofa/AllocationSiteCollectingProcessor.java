
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import soot.RefType;
import soot.SootClass;
import soot.Value;
import soot.ValueBox;
import soot.jimple.NewExpr;
import soot.jimple.StringConstant;
import edu.ksu.cis.indus.common.collections.MapUtils;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

/**
 * DOCUMENT ME!
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public final class AllocationSiteCollectingProcessor
		extends AbstractProcessor {

	/**
	 * DOCUMENT ME!
	 */
	private IEnvironment env;

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#callback(soot.ValueBox, edu.ksu.cis.indus.processing.Context)
	 */
	@Override public void callback(final ValueBox vBox, final Context context) {
		final Value _v = vBox.getValue();
		if (_v instanceof StringConstant) {
			MapUtils.putIntoCollectionInMap(class2allocationSite, env.getClass("java.lang.String"), _v);
		} else if (_v instanceof NewExpr) {
			MapUtils.putIntoCollectionInMap(class2allocationSite, ((RefType) _v.getType()).getSootClass(), _v);
		}
	}

	/**
	 * DOCUMENT ME!
	 */
	private Map<SootClass, Collection<Value>> class2allocationSite = new HashMap<SootClass, Collection<Value>>();

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void hookup(final ProcessingController ppc) {
		ppc.register(NewExpr.class, this);
		ppc.register(StringConstant.class, this);
		env = ppc.getEnvironment();
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregister(NewExpr.class, this);
		ppc.unregister(StringConstant.class, this);
		env = null;
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#reset()
	 */
	@Override public void reset() {
		super.reset();
		class2allocationSite.clear();
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param declaringClass DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public Collection<Value> getAllocationSitesFor(SootClass declaringClass) {
		if (class2allocationSite.containsKey(declaringClass)) {
			return class2allocationSite.get(declaringClass);
		}
		return Collections.singleton(null);
	}
}

// End of File
