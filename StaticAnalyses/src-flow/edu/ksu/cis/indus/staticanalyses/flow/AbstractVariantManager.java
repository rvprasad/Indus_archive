
/*
 * Bandera, a Java(TM) analysis and transformation toolkit
 * Copyright (C) 2002, 2003, 2004.
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://www.cis.ksu.edu/santos/bandera).
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
 *                http://www.cis.ksu.edu/santos/bandera
 */

package edu.ksu.cis.indus.staticanalyses.flow;

import edu.ksu.cis.indus.staticanalyses.*;
import edu.ksu.cis.indus.staticanalyses.*;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;


//AbstractVariantManager.java

/**
 * <p>
 * This class manages variants.  An variant manager classes should extend this class.  This class embodies the logic to
 * manage the variants.
 * </p>
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
	 * <p>
	 * An instance of <code>Logger</code> used for logging purpose.
	 * </p>
	 */
	private static final Logger LOGGER = LogManager.getLogger(AbstractVariantManager.class);

	/**
	 * <p>
	 * The instance of the framework in which this object is used.
	 * </p>
	 */
	protected final BFA bfa;

	/**
	 * <p>
	 * A manager of indices which map entities to variants.
	 * </p>
	 */
	private final AbstractIndexManager indexManager;

	/**
	 * <p>
	 * A map from indices to variants.
	 * </p>
	 */
	private final Map index2variant = new HashMap();

	/**
	 * <p>
	 * Creates a new <code>AbstractVariantManager</code> instance.
	 * </p>
	 *
	 * @param bfa the instance of the framework in which this object is used.  This cannot be <code>null</code>.
	 * @param indexManager the manager of indices which map the entities to variants.  This cannot be <code>null</code>.
	 */
	AbstractVariantManager(BFA bfa, AbstractIndexManager indexManager) {
		this.bfa = bfa;
		this.indexManager = indexManager;
	}

	/**
	 * <p>
	 * Returns the variant corresponding to the given entity in the given context, if one exists.
	 * </p>
	 *
	 * @param o the entity whose variant is to be returned.
	 * @param context the context corresponding to which the variant is requested.
	 *
	 * @return the variant correponding to the entity in the given context, if one exists.  <code>null</code> if none exist.
	 */
	public final IVariant query(Object o, Context context) {
		return (IVariant) index2variant.get(indexManager.getIndex(o, context));
	}

	/**
	 * <p>
	 * Returns the variant corresponding to the given entity in the given context.  If a variant does not exist, a new one is
	 * created.  If one exists, it shall be returned.
	 * </p>
	 *
	 * @param o the entity whose variant is to be returned.
	 * @param context the context corresponding to which the variant is requested.
	 *
	 * @return the variant correponding to the entity in the given context.
	 */
	public final IVariant select(Object o, Context context) {
		IIndex index = indexManager.getIndex(o, context);
		IVariant temp = null;

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Entering - IIndex: " + index + "\n" + o + "\n" + context + "\n" + bfa._ANALYZER.active + "\n"
				+ index.hashCode());
		}

		if (index2variant.containsKey(index)) {
			temp = (IVariant) index2variant.get(index);
		} else if (bfa._ANALYZER.active) {
			temp = getNewVariant(o);
			index2variant.put(index, temp);
			temp.process();
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Exiting - IIndex: " + index + "\n" + o + "\n" + context + "\n" + bfa._ANALYZER.active);
		}

		return temp;
	}

	/**
	 * <p>
	 * Returns the new variant correponding to the given object.
	 * </p>
	 *
	 * @param o the object whose corresponding variant is to be returned.
	 *
	 * @return the new variant corresponding to the given object.
	 */
	protected abstract IVariant getNewVariant(Object o);

	/**
	 * <p>
	 * Resets the manager.  All internal data structures are reset to enable a new session of usage.
	 * </p>
	 */
	void reset() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("IVariant manager being reset.");
		}
		index2variant.clear();
		indexManager.reset();
	}
}

/*****
 ChangeLog:

$Log$
Revision 0.13  2003/05/22 22:18:31  venku
All the interfaces were renamed to start with an "I".
Optimizing changes related Strings were made.


*****/
