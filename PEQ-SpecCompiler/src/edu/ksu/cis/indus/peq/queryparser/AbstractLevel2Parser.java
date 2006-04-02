
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

import antlr.ParserSharedInputState;
import antlr.TokenStream;
import antlr.TokenBuffer;
import antlr.RecognitionException;
import antlr.TokenStreamException;

public abstract class AbstractLevel2Parser extends antlr.LLkParser {

    public AbstractLevel2Parser(int k_) {
        super(k_);
    }
               
    public AbstractLevel2Parser(ParserSharedInputState state, int k_) {
        super(state, k_);
    }
               
    public AbstractLevel2Parser(TokenBuffer tokenBuf, int k_) {
        super(tokenBuf, k_);
    }
               
    public AbstractLevel2Parser(TokenStream lexer, int k_) {
        super(lexer, k_);
    }
    
    public abstract void firstRule() throws RecognitionException, TokenStreamException;
}
