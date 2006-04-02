// $ANTLR : "IndusPEQLanguageSpecification.g" -> "IndusPeqLexer.java"$

	
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
 
package edu.ksu.cis.indus.peq.queryparser;


public interface IndusPeqLexerTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int LPAREN = 4;
	int RPAREN = 5;
	int IDENT = 6;
	int WS = 7;
	int RANGLE = 8;
	int LITERAL_cdepd = 9;
	int LITERAL_cdept = 10;
	int LITERAL_dvgd = 11;
	int LITERAL_dvgdt = 12;
	int LITERAL_readydd = 13;
	int LITERAL_readydt = 14;
	int LITERAL_syncdd = 15;
	int LITERAL_syncdt = 16;
	int LITERAL_datadef = 17;
	int LITERAL_datause = 18;
	int LITERAL_intfdd = 19;
	int LITERAL_intfdt = 20;
	int LITERAL_wc = 21;
}
