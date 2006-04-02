// $ANTLR : "IndusPEQLanguageSpecification.g" -> "IndusPeqParser.java"$

	
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


import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;
import antlr.collections.AST;
import antlr.ASTFactory;
import antlr.ASTPair;

import edu.ksu.cis.indus.peq.queryglue.ConstructorNode;
import edu.ksu.cis.indus.peq.queryglue.IIndusConstructorTypes;

public class IndusPeqParser extends AbstractLevel2Parser       implements IndusPeqLexerTokenTypes
 {

protected IndusPeqParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public IndusPeqParser(TokenBuffer tokenBuf) {
  this(tokenBuf,1);
}

protected IndusPeqParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public IndusPeqParser(TokenStream lexer) {
  this(lexer,1);
}

public IndusPeqParser(ParserSharedInputState state) {
  super(state,1);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

	public final void firstRule() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST firstRule_AST = null;
		AST c_AST = null;
		
		try {      // for error handling
			constructor();
			c_AST = returnAST;
			match(RANGLE);
			firstRule_AST = currentAST.root;
			
					firstRule_AST = c_AST;
				
			currentAST.root = firstRule_AST;
			currentAST.child = firstRule_AST!=null &&firstRule_AST.getFirstChild()!=null ?
				firstRule_AST.getFirstChild() : firstRule_AST;
			currentAST.advanceChildToEnd();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_0);
		}
		returnAST = firstRule_AST;
	}
	
	public final void constructor() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST constructor_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case RANGLE:
			{
				break;
			}
			case LITERAL_cdepd:
			{
				match(LITERAL_cdepd);
				match(LPAREN);
				AST tmp4_AST = null;
				tmp4_AST = astFactory.create(LT(1));
				match(IDENT);
				match(RPAREN);
				constructor_AST = currentAST.root;
				
					  	ConstructorNode _n = new ConstructorNode();
					  	_n.setConstructorType(IIndusConstructorTypes.CDEPD);
					  	_n.setVariableName((tmp4_AST).toString());
					  	constructor_AST = _n;
					
				currentAST.root = constructor_AST;
				currentAST.child = constructor_AST!=null &&constructor_AST.getFirstChild()!=null ?
					constructor_AST.getFirstChild() : constructor_AST;
				currentAST.advanceChildToEnd();
				break;
			}
			case LITERAL_cdept:
			{
				match(LITERAL_cdept);
				match(LPAREN);
				AST tmp8_AST = null;
				tmp8_AST = astFactory.create(LT(1));
				match(IDENT);
				match(RPAREN);
				constructor_AST = currentAST.root;
				
					  	ConstructorNode _n = new ConstructorNode();
					  	_n.setConstructorType(IIndusConstructorTypes.CDEPT);
					  	_n.setVariableName((tmp8_AST).toString());
					  	constructor_AST = _n;
					
				currentAST.root = constructor_AST;
				currentAST.child = constructor_AST!=null &&constructor_AST.getFirstChild()!=null ?
					constructor_AST.getFirstChild() : constructor_AST;
				currentAST.advanceChildToEnd();
				break;
			}
			case LITERAL_dvgd:
			{
				match(LITERAL_dvgd);
				match(LPAREN);
				AST tmp12_AST = null;
				tmp12_AST = astFactory.create(LT(1));
				match(IDENT);
				match(RPAREN);
				constructor_AST = currentAST.root;
				
					  	ConstructorNode _n = new ConstructorNode();
					  	_n.setConstructorType(IIndusConstructorTypes.DDEPD);
					  	_n.setVariableName((tmp12_AST).toString());
					  	constructor_AST = _n;
					
				currentAST.root = constructor_AST;
				currentAST.child = constructor_AST!=null &&constructor_AST.getFirstChild()!=null ?
					constructor_AST.getFirstChild() : constructor_AST;
				currentAST.advanceChildToEnd();
				break;
			}
			case LITERAL_dvgdt:
			{
				match(LITERAL_dvgdt);
				match(LPAREN);
				AST tmp16_AST = null;
				tmp16_AST = astFactory.create(LT(1));
				match(IDENT);
				match(RPAREN);
				constructor_AST = currentAST.root;
				
					  	ConstructorNode _n = new ConstructorNode();
					  	_n.setConstructorType(IIndusConstructorTypes.DDEPT);
					  	_n.setVariableName((tmp16_AST).toString());
					  	constructor_AST = _n;
					
				currentAST.root = constructor_AST;
				currentAST.child = constructor_AST!=null &&constructor_AST.getFirstChild()!=null ?
					constructor_AST.getFirstChild() : constructor_AST;
				currentAST.advanceChildToEnd();
				break;
			}
			case LITERAL_readydd:
			{
				match(LITERAL_readydd);
				match(LPAREN);
				AST tmp20_AST = null;
				tmp20_AST = astFactory.create(LT(1));
				match(IDENT);
				match(RPAREN);
				constructor_AST = currentAST.root;
				
					  	ConstructorNode _n = new ConstructorNode();
					  	_n.setConstructorType(IIndusConstructorTypes.RDEPD);
					  	_n.setVariableName((tmp20_AST).toString());
					  	constructor_AST = _n;
					
				currentAST.root = constructor_AST;
				currentAST.child = constructor_AST!=null &&constructor_AST.getFirstChild()!=null ?
					constructor_AST.getFirstChild() : constructor_AST;
				currentAST.advanceChildToEnd();
				break;
			}
			case LITERAL_readydt:
			{
				match(LITERAL_readydt);
				match(LPAREN);
				AST tmp24_AST = null;
				tmp24_AST = astFactory.create(LT(1));
				match(IDENT);
				match(RPAREN);
				constructor_AST = currentAST.root;
				
					  	ConstructorNode _n = new ConstructorNode();
					  	_n.setConstructorType(IIndusConstructorTypes.RDEPT);
					  	_n.setVariableName((tmp24_AST).toString());
					  	constructor_AST = _n;
					
				currentAST.root = constructor_AST;
				currentAST.child = constructor_AST!=null &&constructor_AST.getFirstChild()!=null ?
					constructor_AST.getFirstChild() : constructor_AST;
				currentAST.advanceChildToEnd();
				break;
			}
			case LITERAL_syncdd:
			{
				match(LITERAL_syncdd);
				match(LPAREN);
				AST tmp28_AST = null;
				tmp28_AST = astFactory.create(LT(1));
				match(IDENT);
				match(RPAREN);
				constructor_AST = currentAST.root;
				
					  	ConstructorNode _n = new ConstructorNode();
					  	_n.setConstructorType(IIndusConstructorTypes.SDEPD);
					  	_n.setVariableName((tmp28_AST).toString());
					  	constructor_AST = _n;
					
				currentAST.root = constructor_AST;
				currentAST.child = constructor_AST!=null &&constructor_AST.getFirstChild()!=null ?
					constructor_AST.getFirstChild() : constructor_AST;
				currentAST.advanceChildToEnd();
				break;
			}
			case LITERAL_syncdt:
			{
				match(LITERAL_syncdt);
				match(LPAREN);
				AST tmp32_AST = null;
				tmp32_AST = astFactory.create(LT(1));
				match(IDENT);
				match(RPAREN);
				constructor_AST = currentAST.root;
				
					  	ConstructorNode _n = new ConstructorNode();
					  	_n.setConstructorType(IIndusConstructorTypes.SDEPT);
					  	_n.setVariableName((tmp32_AST).toString());
					  	constructor_AST = _n;
					
				currentAST.root = constructor_AST;
				currentAST.child = constructor_AST!=null &&constructor_AST.getFirstChild()!=null ?
					constructor_AST.getFirstChild() : constructor_AST;
				currentAST.advanceChildToEnd();
				break;
			}
			case LITERAL_datadef:
			{
				match(LITERAL_datadef);
				match(LPAREN);
				AST tmp36_AST = null;
				tmp36_AST = astFactory.create(LT(1));
				match(IDENT);
				match(RPAREN);
				constructor_AST = currentAST.root;
				
					  	ConstructorNode _n = new ConstructorNode();
					  	_n.setConstructorType(IIndusConstructorTypes.DDEF);
					  	_n.setVariableName((tmp36_AST).toString());
					  	constructor_AST = _n;
					
				currentAST.root = constructor_AST;
				currentAST.child = constructor_AST!=null &&constructor_AST.getFirstChild()!=null ?
					constructor_AST.getFirstChild() : constructor_AST;
				currentAST.advanceChildToEnd();
				break;
			}
			case LITERAL_datause:
			{
				match(LITERAL_datause);
				match(LPAREN);
				AST tmp40_AST = null;
				tmp40_AST = astFactory.create(LT(1));
				match(IDENT);
				match(RPAREN);
				constructor_AST = currentAST.root;
				
					  	ConstructorNode _n = new ConstructorNode();
					  	_n.setConstructorType(IIndusConstructorTypes.DUSE);
					  	_n.setVariableName((tmp40_AST).toString());
					  	constructor_AST = _n;
					
				currentAST.root = constructor_AST;
				currentAST.child = constructor_AST!=null &&constructor_AST.getFirstChild()!=null ?
					constructor_AST.getFirstChild() : constructor_AST;
				currentAST.advanceChildToEnd();
				break;
			}
			case LITERAL_intfdd:
			{
				match(LITERAL_intfdd);
				match(LPAREN);
				AST tmp44_AST = null;
				tmp44_AST = astFactory.create(LT(1));
				match(IDENT);
				match(RPAREN);
				constructor_AST = currentAST.root;
				
					  	ConstructorNode _n = new ConstructorNode();
					  	_n.setConstructorType(IIndusConstructorTypes.IDEPD);
					  	_n.setVariableName((tmp44_AST).toString());
					  	constructor_AST = _n;
					
				currentAST.root = constructor_AST;
				currentAST.child = constructor_AST!=null &&constructor_AST.getFirstChild()!=null ?
					constructor_AST.getFirstChild() : constructor_AST;
				currentAST.advanceChildToEnd();
				break;
			}
			case LITERAL_intfdt:
			{
				match(LITERAL_intfdt);
				match(LPAREN);
				AST tmp48_AST = null;
				tmp48_AST = astFactory.create(LT(1));
				match(IDENT);
				match(RPAREN);
				constructor_AST = currentAST.root;
				
					  	ConstructorNode _n = new ConstructorNode();
					  	_n.setConstructorType(IIndusConstructorTypes.IDEPT);
					  	_n.setVariableName((tmp48_AST).toString());
					  	constructor_AST = _n;
					
				currentAST.root = constructor_AST;
				currentAST.child = constructor_AST!=null &&constructor_AST.getFirstChild()!=null ?
					constructor_AST.getFirstChild() : constructor_AST;
				currentAST.advanceChildToEnd();
				break;
			}
			case LITERAL_wc:
			{
				match(LITERAL_wc);
				match(LPAREN);
				match(RPAREN);
				constructor_AST = currentAST.root;
				
					  	ConstructorNode _n = new ConstructorNode();
					  	_n.setConstructorType(IIndusConstructorTypes.WC);
					  	_n.setVariableName("default");
					  	constructor_AST = _n;
					
				currentAST.root = constructor_AST;
				currentAST.child = constructor_AST!=null &&constructor_AST.getFirstChild()!=null ?
					constructor_AST.getFirstChild() : constructor_AST;
				currentAST.advanceChildToEnd();
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_1);
		}
		returnAST = constructor_AST;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"LPAREN",
		"RPAREN",
		"IDENT",
		"WS",
		"RANGLE",
		"\"cdepd\"",
		"\"cdept\"",
		"\"dvgd\"",
		"\"dvgdt\"",
		"\"readydd\"",
		"\"readydt\"",
		"\"syncdd\"",
		"\"syncdt\"",
		"\"datadef\"",
		"\"datause\"",
		"\"intfdd\"",
		"\"intfdt\"",
		"\"wc\""
	};
	
	protected void buildTokenTypeASTClassMap() {
		tokenTypeToASTClassMap=null;
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 2L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 256L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	
	}
