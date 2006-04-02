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
 * Created on March 8, 2005, 6:45 PM
 */

package edu.ksu.cis.indus.peq.queryglue;

/**
 * @author ganeshan
 *
 * This defines the type of the constructos implemented in Indus-PEQ
 */
public interface IIndusConstructorTypes {
    /* Identifier Def() */
    int IDEF = 1;
    /* Identifier Use() */
    int IUSE = IDEF + 1;
    /* Cd-depd() */
    int CDEPD = IDEF + 2;
    /* Cd-dept() */
    int CDEPT = IDEF + 3;        
    /* Divergence depedenee */
    int DDEPD = IDEF + 4;
    /* Divergence dependent */
    int DDEPT = IDEF + 5;
    /* Ready dependee */
    int RDEPD = IDEF + 6;
    /* Ready dependent */
    int RDEPT = IDEF + 7;
    /* Synchronization dependee */
    int SDEPD = IDEF + 8;
    /* Synchronization dependent */
    int SDEPT = IDEF + 9;
    /* Interference dependee */
    int IDEPD = IDEF + 10;
    /* Interference dependent */
    int IDEPT = IDEF + 11;
    /* Reference use */
    int RUSE = IDEF + 12;
    /* Reference DEF */
    int RDEF = IDEF + 13;   
    /* Wildcard */
    int WC = RDEF + 14;    
    /* Data def */
    int DDEF = WC + 15;
    /* Data use */
    int DUSE = DDEF + 16;
}
