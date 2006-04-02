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
 * This defines the type of the regex elements implemented in Indus-PEQ
 */
public interface IPEQRegexTypes {

    int NO_REGEXTYPE = 0;
    int ZERO_OR_MORE = 1;
    int ZERO_OR_ONE = 2;
    int ONE_OR_MORE = 3;
}
