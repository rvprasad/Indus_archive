
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;

import edu.ksu.cis.indus.processing.IProcessingFilter;

import edu.ksu.cis.indus.xmlizer.AbstractXMLizer;
import edu.ksu.cis.indus.xmlizer.CustomXMLOutputter;
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

import org.znerd.xmlenc.XMLOutputter;

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
	 * @see edu.ksu.cis.indus.xmlizer.IXMLizer#getFileName(String)
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
	 * @pre info.get(FILE_NAME_ID) != null and info.get(FILE_NAME_ID).oclIsKindOf(String)
	 */
	public void writeXML(final Map info) {
		final File _f = new File(getXmlOutputDir() + File.separator + getFileName((String) info.get(FILE_NAME_ID)));
		final FileWriter _writer;

		try {
			_writer = new FileWriter(_f);

			final XMLOutputter _xmlWriter = new CustomXMLOutputter(_writer);
			final ICallGraphInfo _cgi = (ICallGraphInfo) info.get(ICallGraphInfo.ID);
			_xmlWriter.declaration();
			_xmlWriter.startTag("callgraph");

			// Control the order in which methods are processed. 
			final IProcessingFilter _filter = new XMLizingProcessingFilter();
			final Collection _temp = new HashSet();

			for (final Iterator _i = _filter.filterMethods(_cgi.getReachableMethods()).iterator(); _i.hasNext();) {
				final SootMethod _method = (SootMethod) _i.next();
				_xmlWriter.startTag("method");
				_xmlWriter.attribute("id", getIdGenerator().getIdForMethod(_method));
				_temp.clear();

				for (final Iterator _j = _cgi.getCallees(_method).iterator(); _j.hasNext();) {
					final CallTriple _ctrp = (CallTriple) _j.next();
					_temp.add(_ctrp.getMethod());
				}

				for (final Iterator _j = _filter.filterMethods(_temp).iterator(); _j.hasNext();) {
					final SootMethod _callee = (SootMethod) _j.next();
					_xmlWriter.startTag("callee");
					_xmlWriter.attribute("calleeId", getIdGenerator().getIdForMethod(_callee));
					_xmlWriter.endTag();
				}

				_temp.clear();

				for (final Iterator _j = _cgi.getCallers(_method).iterator(); _j.hasNext();) {
					final CallTriple _ctrp = (CallTriple) _j.next();
					_temp.add(_ctrp.getMethod());
				}

				for (final Iterator _j = _filter.filterMethods(_temp).iterator(); _j.hasNext();) {
					final SootMethod _caller = (SootMethod) _j.next();
					_xmlWriter.startTag("caller");
					_xmlWriter.attribute("callerId", getIdGenerator().getIdForMethod(_caller));
					_xmlWriter.endTag();
				}

				_xmlWriter.endTag();
			}
			_xmlWriter.startTag("reachables");

			for (final Iterator _i = _cgi.getReachableMethods().iterator(); _i.hasNext();) {
				final SootMethod _sm = (SootMethod) _i.next();
				_xmlWriter.startTag("method");
				_xmlWriter.attribute("methodId", getIdGenerator().getIdForMethod(_sm));
				_xmlWriter.endTag();
			}
			_xmlWriter.endDocument();
			_writer.flush();
			_writer.close();
		} catch (final IOException _e) {
			LOGGER.error("Error while xmlizing call graph", _e);
		}
	}
}

// End of File
