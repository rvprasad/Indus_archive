package edu.ksu.cis.indus.staticanalyses.dependency;

import edu.ksu.cis.indus.xmlizer.AbstractXMLizer;

import java.util.Map;

public class DependencyXMLizer extends AbstractXMLizer {

    /**
     * @see edu.ksu.cis.indus.xmlizer.AbstractXMLizer#writeXML(java.util.Map)
     */
    public void writeXML(Map info) {
        /*
         * requires environment, call graph, list of DAs, 
         * create call graph based processing controller
         * Create a stmt xmlizer for each DA, and drive the bunch via the controller.
         */ 
        

    }

}


/*
ChangeLog:

$Log$
Revision 1.5  2004/02/09 06:49:02  venku
- deleted dependency xmlization and test classes.

*/