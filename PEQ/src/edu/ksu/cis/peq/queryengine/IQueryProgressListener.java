/*
 * PEQ, a parameteric regular path query library
 * Copyright (c) 2005 SAnToS Laboratory, Kansas State University
 *
 * This software is licensed under the KSU Open Academic License.
 * You should have received a copy of the license with the distribution.
 * A copy can be found at
 *     http://www.cis.ksu.edu/santos/license.html
 * or you can contact the lab at:
 *     SAnToS Laboratory
 *     234 Nichols Hall
 *     Manhattan, KS 66506, USA
 *
 * Created on March 8, 2005, 8:55 PM
 */

package edu.ksu.cis.peq.queryengine;

import java.util.EventObject;

/**
 * Reports the progress of the query engine.
 * @author ganeshan
 */
public interface IQueryProgressListener {
    
    /**
     * This is the event that is reported when there is progress from the query engine.
     * @author Ganeshan
     */
    public final  class QueryProgressEvent extends EventObject {
        
        private Object source;
        private String message;
        private Object information;
        
        /**
         * Constructor
         * @param source The source of the event
         * @param message The message associated with the event
         * @param information The information object associated with the event
         */
        public QueryProgressEvent(final Object source, final String message, final Object information) {
            super(source);
            this.source = source;
            this.message = message;
            this.information = information;
        }        

        /**
         * Returns the message associated with the event.
         * @return String The message 
         */
        public String getMessage() {
            return message;
        }

        /**
         * Returns the information object associated withe the event.
         * @return Object The object associated with the event
         */
        public Object getInformation() {
            return information;
        }
        
    }
    
    /**
     * Progress report message.
     * @param qpe The progress event
     */
    public void queryProgress(final QueryProgressEvent qpe);
}
