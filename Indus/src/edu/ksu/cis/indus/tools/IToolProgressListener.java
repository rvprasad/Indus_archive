
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

import java.util.EventListener;
import java.util.EventObject;


/**
 * This interface is used to communicate the progress of a tool.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IToolProgressListener
  extends EventListener {
	/**
	 * This event contains information about the progress of a tool.  The source of the event is the tool.  So,
	 * <code>getSource()</code> will return a <code>ITool</code> object that caused the event.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	public class ToolProgressEvent
	  extends EventObject {
		/** 
		 * This can be any information that the tool may want to provide to the application.
		 */
		private final Object info;

		/** 
		 * This is the message indicating the progress.
		 */
		private final String msg;

		/**
		 * Creates an instance of this class.
		 *
		 * @param tool about whose progress is being reported.
		 * @param message indicating the progress.
		 * @param information can be any information provided by the tool.
		 *
		 * @pre tool != null
		 */
		public ToolProgressEvent(final Object tool, final String message, final Object information) {
			super(tool);
			msg = message;
			info = information;
		}

		/**
		 * Retrieves the information provided by the tool.
		 *
		 * @return the information provided by the tool.
		 */
		public Object getInfo() {
			return info;
		}

		/**
		 * Retrieves the message provided by the tool.
		 *
		 * @return the message provided by the tool.
		 */
		public String getMsg() {
			return msg;
		}
	}

	/**
	 * This method is called when information about the progress of the tool needs to reported.
	 *
	 * @param evt contains the progress information.
	 *
	 * @pre evt != null and evt.getSource() != null
	 */
	void toolProgess(ToolProgressEvent evt);
}

/*
   ChangeLog:
   $Log$
 */
