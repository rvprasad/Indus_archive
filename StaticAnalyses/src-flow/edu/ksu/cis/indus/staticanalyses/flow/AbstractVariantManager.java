
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (C) 2003, 2004, 2005
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://indus.projects.cis.ksu.edu/).
 * It is understood that any modification not identified as such is
 * not covered by the preceding statement.
 *
 * This work is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this toolkit; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * Java is a trademark of Sun Microsystems, Inc.
 *
 * To submit a bug report, send a comment, or get the latest news on
 * this project and other SAnToS projects, please visit the web-site
 *                http://indus.projects.cis.ksu.edu/
 */

package edu.ksu.cis.indus.staticanalyses.flow;

import edu.ksu.cis.indus.staticanalyses.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;


/**
 * This class manages variants.  An variant manager classes should extend this class.  This class embodies the logic to
 * manage the variants.
 * 
 * <p>
 * Created: Tue Jan 22 05:21:42 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public abstract class AbstractVariantManager {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(AbstractVariantManager.class);

	/**
	 * The instance of the framework in which this object is used.
	 *
	 * @invariant fa != null
	 */
	protected final FA fa;

	/**
	 * A manager of indices that map entities to variants.
	 *
	 * @invariant indexManager != null
	 */
	private final AbstractIndexManager idxManager;

	/**
	 * A map from indices to variants.
	 *
	 * @invariant index2variant != null
	 */
	private final Map index2variant = new HashMap();

	/**
	 * Creates a new <code>AbstractVariantManager</code> instance.
	 *
	 * @param theAnalysis the instance of the framework in which this object is used.
	 * @param indexManager the manager of indices that map the entities to variants.
	 *
	 * @pre theAnalysis != null and indexManager != null
	 */
	AbstractVariantManager(final FA theAnalysis, final AbstractIndexManager indexManager) {
		this.fa = theAnalysis;
		this.idxManager = indexManager;
	}

	/**
	 * Returns the variant corresponding to the given entity in the given context, if one exists.
	 *
	 * @param o the entity whose variant is to be returned.
	 * @param context the context corresponding to which the variant is requested.
	 *
	 * @return the variant correponding to the entity in the given context, if one exists.  <code>null</code> if none exist.
	 *
	 * @pre o != null and context != null
	 */
	public final IVariant query(final Object o, final Context context) {
		return (IVariant) index2variant.get(idxManager.getIndex(o, context));
	}

	/**
	 * Returns the variant corresponding to the given entity in the given context.  If a variant does not exist, a new one is
	 * created.  If one exists, it shall be returned.
	 *
	 * @param o the entity whose variant is to be returned.
	 * @param context the context corresponding to which the variant is requested.
	 *
	 * @return the variant correponding to the entity in the given context.
	 *
	 * @pre o != null and context != null
	 * @post result != null
	 */
	public final IVariant select(final Object o, final Context context) {
		IIndex index = idxManager.getIndex(o, context);
		IVariant temp = null;

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Entering - IIndex: " + index + "\n" + o + "\n" + context + "\n" + fa._analyzer.active + "\n"
				+ index.hashCode());
		}

		if (index2variant.containsKey(index)) {
			temp = (IVariant) index2variant.get(index);
		} else if (fa._analyzer.active) {
			temp = getNewVariant(o);
			index2variant.put(index, temp);
			temp.process();
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Exiting - IIndex: " + index + "\n" + o + "\n" + context + "\n" + fa._analyzer.active);
		}

		return temp;
	}

	/**
	 * Returns the new variant correponding to the given object. This is a template method to be provided by concrete
	 * implementations.
	 *
	 * @param o the object whose corresponding variant is to be returned.
	 *
	 * @return the new variant corresponding to the given object.
	 *
	 * @pre o != null
	 * @post result != null
	 */
	protected abstract IVariant getNewVariant(final Object o);

	/**
	 * Resets the manager.  All internal data structures are reset to enable a new session of usage.
	 */
	void reset() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("IVariant manager being reset.");
		}
		index2variant.clear();
		idxManager.reset();
	}
}

/*
   ChangeLog:
   
   $Log$
   Revision 1.2  2003/08/17 09:59:03  venku
   Spruced up documentation and specification.
   Documentation changes to FieldVariant.

   
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
    
   Revision 0.13  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
