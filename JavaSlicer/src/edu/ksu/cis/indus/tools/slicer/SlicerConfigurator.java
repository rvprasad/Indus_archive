
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

package edu.ksu.cis.indus.tools.slicer;

import edu.ksu.cis.indus.tools.ToolConfigurator;


/**
 * This provides the graphical user interface via which the user can configure the slicer. 
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SlicerConfigurator
  implements ToolConfigurator {
	/** 
	 * The configuration which can be edited via this editor. 
	 */
	private final SlicerConfiguration configuration;

	/**
	 * Creates a new SlicerConfigurator object.
	 *
	 * @param config is the configuration that can be edited by this editor.
	 */
	SlicerConfigurator(final SlicerConfiguration config) {
		configuration = config;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see edu.ksu.cis.indus.tools.ToolConfigurator#display()
	 */
	public void display() {
		// TODO: Auto-generated method stub
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see edu.ksu.cis.indus.tools.ToolConfigurator#hide()
	 */
	public void hide() {
		// TODO: Auto-generated method stub
	}
}

/*
   ChangeLog:
   $Log$
 */
