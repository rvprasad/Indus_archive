
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

package edu.ksu.cis.indus.staticanalyses.dependency;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator;
import edu.ksu.cis.indus.xmlizer.UniqueIDGenerator;

import org.apache.commons.logging.LogFactory;

import soot.G;


/**
 * DOCUMENT ME!
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class DependencyTest
  extends TestCase {
    
    /**
     * <p>
     * DOCUMENT ME!
     * </p>
     */
    private final String xmlInDir;

    

    private DependencyXMLizer daXMLizer;

    private DependencyTest(final DependencyXMLizer xmlizer) {
        daXMLizer = xmlizer;
        daXMLizer.initialize();
    }
    
    /**
     * @see TestCase#setUp()
     */
     protected void setUp() {
         daXMLizer.populateDAs()
     execute();
     }
     
     /**
      * DOCUMENT ME!
      * 
      * <p></p>
      *
      * @return DOCUMENT ME!
      */
      public static Test suite() {
      TestSuite suite = new TestSuite("Test for edu.ksu.cis.indus.staticanalyses.dependency");
      String propFileName = System.getProperty("indus.dependencytest.properties.file");

      if (propFileName == null) {
      propFileName =
      ClassLoader.getSystemResource("edu.ksu.cis.indus.staticanalyses.dependency.DependencyXMLizer.properties")
      .getFile();
      }
      setupTests(propFileName, suite);
      return suite;
      }
      
      /*
       * DOCUMENT ME!
       * 
       * <p></p>
       *
       * @param propFileName DOCUMENT ME!
       * @param suite DOCUMENT ME!
       *
       * @throws IllegalArgumentException DOCUMENT ME!
       */
       private static final void setupTests(final String propFileName, final TestSuite suite) {
       Properties props = new Properties();
       IJimpleIDGenerator generator = new UniqueIDGenerator();

       try {
       props.load(new FileInputStream(new File(propFileName)));

       String[] configs = props.getProperty("configs").split(" ");

       for (int i = 0; i <= configs.length; i++) {
       String config = configs[i];
       String[] temp = props.getProperty(config + ".classNames").split(" ");
       String xmlOutputDir = props.getProperty(config + ".xmlOutputDir");
       String xmlInputDir = props.getProperty(config + ".xmlInputDir");
       Test test;

       try {
       test = new DependencyTest(new DependencyXMLizer(temp, xmlOutputDir, generator, props));
       } catch (IllegalArgumentException e) {
       test = null;
       }

       if (test != null) {
       suite.addTest(test);
       }
       }
       } catch (IOException e) {
       throw new IllegalArgumentException("Specified property file does not exist.");
       }
       }
       
       /**
        * @see junit.framework.TestCase#tearDown()
        */
        protected void teardown()
        throws Exception {
        G.reset();
        }       
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/11/11 10:11:27  venku
   - in the process of making XMLization a user
     application and at the same time a tester application.

 */
