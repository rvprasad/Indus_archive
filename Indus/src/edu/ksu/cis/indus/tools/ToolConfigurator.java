
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

package edu.ksu.cis.indus.tools;

/**
 * This is API exposed by the tool for configuration via GUI.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface ToolConfigurator {
	/**
	 * Displays the editor widget.  The widget can be hidden by calling <code>hide()</code>.
	 */
	public void display();

	/**
	 * Disposes the editor widget. If the widget is displayed, it will be hidden and the widget will not respond to any
	 * subsequent method calls.
	 */
	public void dispose();

	/**
	 * Hides the editor widget.  The widget can be redisplayed by calling <code>display()</code>.
	 */
	public void hide();
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/09/24 07:03:02  venku
   - Renamed ToolConfigurationEditor to ToolConfigurator.
   - Added property id creation support, via factory method, to ToolConfiguration.
   - Changed the interface in Tool.
   Revision 1.1  2003/09/24 02:38:55  venku
   - Added Interfaces to expose the components of Indus as a
     tool and configure it.
 */
