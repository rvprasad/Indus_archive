
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

package edu.ksu.cis.indus.staticanalyses.flow;

import java.util.Collection;


/**
 * This is the interface of filter implementations that will be used to filter values during flow analysis.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IValueFilter {
	/**
	 * Filters the given values.
	 *
	 * @param values to be filtered.
	 *
	 * @return a collection of values without the values that were filtered.
	 */
	Collection filter(final Collection values);

	/**
	 * Checks if the given value should be filtered out.
	 *
	 * @param value to be filtered.
	 *
	 * @return <code>true</code> indicates the given value should be filtered out; <code>false</code>, otherwise.
	 */
	boolean filter(final Object value);
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.2  2003/08/16 02:55:12  venku
   Removed redundant keywords.
   Formatting.
   Revision 1.1  2003/08/16 02:33:50  venku
   Renamed AValueFilter to IValueFilter.
   Revision 1.2  2003/08/15 04:07:56  venku
   Spruced up documentation and specification.
   - Important change is that previously all types of retype and nullconstant were let through.
     This is incorrect as there is not type filtering happening.  This has been fixed.  We now
     only let those that are not of the monitored type.
   Revision 1.1  2003/08/15 03:41:35  venku
   Renamed ValueFilter to IValueFilter as it is an abstract class.
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
   Revision 1.2  2003/05/22 22:18:32  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
