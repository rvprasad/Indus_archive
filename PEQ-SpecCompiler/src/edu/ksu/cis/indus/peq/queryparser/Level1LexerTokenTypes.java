// $ANTLR : "L1LanguageSpecification.g" -> "Level1Lexer.java"$


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

public interface Level1LexerTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int LPAREN = 4;
	int RPAREN = 5;
	int LBRACKET = 6;
	int RBRACKET = 7;
	int LBRACE = 8;
	int RBRACE = 9;
	int ONE_OR_MORE = 10;
	int ZERO_OR_MORE = 11;
	int ZERO_OR_ONE = 12;
	int NEG = 13;
	int ALTERNATION = 14;
	int ANY = 15;
	int SEMICOLON = 16;
	int NAME = 17;
	int WS = 18;
	int LANGLE = 19;
	int LITERAL_Uquery = 20;
	int LITERAL_Equery = 21;
}
