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

import edu.ksu.cis.indus.common.collections.MapUtils;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

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

/**
 * This processor collects allocation sites.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public final class AllocationSiteCollectingProcessor
		extends AbstractProcessor {

	/**
	 * This maps classes to allocation sites at which their instances are created.
	 */
	private Map<SootClass, Collection<Value>> class2allocationSite = new HashMap<SootClass, Collection<Value>>();

	/**
	 * The application/environment in which the allocation sites occur.
	 */
	private IEnvironment env;

	/**
	 * {@inheritDoc}
	 */
	@Override public void callback(final ValueBox vBox, @SuppressWarnings("unused") final Context context) {
		final Value _v = vBox.getValue();
		if (_v instanceof StringConstant) {
			MapUtils.putIntoCollectionInMap(class2allocationSite, env.getClass("java.lang.String"), _v);
		} else if (_v instanceof NewExpr) {
			MapUtils.putIntoCollectionInMap(class2allocationSite, ((RefType) _v.getType()).getSootClass(), _v);
		}
	}

	/**
	 * Retrieves the allocation sites for the given class.
	 * 
	 * @param clazz of interest.
	 * @return the collection of allocation sites.
	 */
	public Collection<Value> getAllocationSitesFor(final SootClass clazz) {
		if (class2allocationSite.containsKey(clazz)) {
			return class2allocationSite.get(clazz);
		}
		return Collections.singleton(null);
	}

	/**
	 * {@inheritDoc}
	 */
	public void hookup(final ProcessingController ppc) {
		ppc.register(NewExpr.class, this);
		ppc.register(StringConstant.class, this);
		env = ppc.getEnvironment();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void reset() {
		super.reset();
		class2allocationSite.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregister(NewExpr.class, this);
		ppc.unregister(StringConstant.class, this);
		env = null;
	}
}

// End of File
