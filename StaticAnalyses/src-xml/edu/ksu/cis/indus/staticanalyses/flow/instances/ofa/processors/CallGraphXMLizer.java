
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;

import edu.ksu.cis.indus.processing.IProcessingFilter;

import edu.ksu.cis.indus.xmlizer.AbstractXMLizer;
import edu.ksu.cis.indus.xmlizer.XMLizingProcessingFilter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;


/**
 * This class xmlizes call graphs.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
final class CallGraphXMLizer
  extends AbstractXMLizer {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(CallGraphXMLizer.class);

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param name DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public String getFileName(final String name) {
		return "callgraph_" + xmlizeString(name) + ".xml";
	}

	/**
	 * Writes the call graph in XML.
	 *
	 * @param info is a map of id's to implementation that satisfies the interface associated with the id.
	 *
	 * @pre rootname != null and info != null
	 * @pre info.oclIsKindOf(Map(Object, Object))
	 * @pre info.get(ICallGraphInfo.ID) != null and info.get(ICallGraphInfo.ID).oclIsKindOf(ICallGraphInfo)
	 */
	public void writeXML(final Map info) {
		final File _f = new File(getXmlOutputDir() + File.separator + getFileName((String) info.get(FILE_NAME_ID)));
		final FileWriter _writer;

		try {
			_writer = new FileWriter(_f);

			final ICallGraphInfo _cgi = (ICallGraphInfo) info.get(ICallGraphInfo.ID);

			_writer.write("<callgraph>\n");

			// Control the order in which methods are processed. 
			final IProcessingFilter _filter = new XMLizingProcessingFilter();
			final Collection _temp = new HashSet();

			for (final Iterator _i = _filter.filterMethods(_cgi.getReachableMethods()).iterator(); _i.hasNext();) {
				final SootMethod _method = (SootMethod) _i.next();
				_writer.write("\t<method id=\"" + getIdGenerator().getIdForMethod(_method) + "\">\n");
				_temp.clear();

				for (final Iterator _j = _cgi.getCallees(_method).iterator(); _j.hasNext();) {
					final CallTriple _ctrp = (CallTriple) _j.next();
					_temp.add(_ctrp.getMethod());
				}

				for (final Iterator _j = _filter.filterMethods(_temp).iterator(); _j.hasNext();) {
					final SootMethod _callee = (SootMethod) _j.next();
					_writer.write("\t\t<callee id=\"" + getIdGenerator().getIdForMethod(_callee) + "\"/>\n");
				}

				_temp.clear();

				for (final Iterator _j = _cgi.getCallers(_method).iterator(); _j.hasNext();) {
					final CallTriple _ctrp = (CallTriple) _j.next();
					_temp.add(_ctrp.getMethod());
				}

				for (final Iterator _j = _filter.filterMethods(_temp).iterator(); _j.hasNext();) {
					final SootMethod _caller = (SootMethod) _j.next();
					_writer.write("\t\t<caller id=\"" + getIdGenerator().getIdForMethod(_caller) + "\"/>\n");
				}

				_writer.write("\t</method>\n");
			}
			_writer.write("</callgraph>\n");
			_writer.flush();
			_writer.close();
		} catch (final IOException _e) {
			LOGGER.error("Error while xmlizing call graph", _e);
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.8  2004/02/09 17:40:53  venku
   - dependence and call graph info serialization is done both ways.
   - refactored the xmlization framework.
     - Each information type has a xmlizer (XMLizer)
     - Each information type has a xmlizer driver (XMLizerCLI)
     - Tests use the XMLizer.
   Revision 1.7  2004/02/09 07:32:38  venku
   - added support to differentiate test method name and test name.
   - added logic to change name of AbstractXMLBasedTest tests as well.
   Revision 1.6  2004/02/09 06:49:02  venku
   - deleted dependency xmlization and test classes.
   Revision 1.5  2004/02/09 04:39:36  venku
   - refactoring test classes still..
   - need to make xmlizer classes independent of their purpose.
     Hence, they need to be highly configurable.
   - For each concept, test setup should be in TestSetup
     rather than in the XMLizer.
   Revision 1.4  2004/02/09 02:19:05  venku
    - first stab at refactoring xmlizer framework to be amenable
     to testing and standalone execution.
   Revision 1.3  2004/02/09 02:00:14  venku
   - changed AbstractXMLizer.
   - ripple effect.
   Revision 1.2  2004/02/09 01:21:03  venku
   - publicized execute() to be used in regression testing.
   Revision 1.1  2004/02/08 03:05:46  venku
   - renamed xmlizer packages to be in par with the packages
     that contain the classes whose data is being xmlized.
   Revision 1.5  2003/12/27 20:07:40  venku
   - fixed xmlizers/driver to not throw exception
     when -h is specified
   Revision 1.4  2003/12/13 02:29:08  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.3  2003/12/08 12:20:44  venku
   - moved some classes from staticanalyses interface to indus interface package
   - ripple effect.
   Revision 1.2  2003/12/08 12:15:59  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.1  2003/12/08 11:59:47  venku
   - added a new class AbstractXMLizer which will host
     primary logic to xmlize analyses information.
   - DependencyXMLizerDriver inherits from this new class.
   - added a new class CallGraphXMLizer to xmlize
     call graph information.  The logic to write out the call
     graph is empty.
 */
