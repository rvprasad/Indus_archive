// $ANTLR : "L1LanguageSpecification.g" -> "Level1Parser.java"$


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
import edu.ksu.cis.indus.peq.queryglue.QueryNode;
import edu.ksu.cis.indus.peq.queryglue.IPEQRegexTypes;
import edu.ksu.cis.indus.peq.queryglue.OrNode;
import edu.ksu.cis.indus.peq.queryglue.AndNode;

public class Level1Parser extends antlr.LLkParser       implements Level1LexerTokenTypes
 {

    private AbstractLevel2Parser level2Parser;

    public void setLevel2Parser(AbstractLevel2Parser p) {
        level2Parser = p;
    }


protected Level1Parser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public Level1Parser(TokenBuffer tokenBuf) {
  this(tokenBuf,1);
}

protected Level1Parser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public Level1Parser(TokenStream lexer) {
  this(lexer,1);
}

public Level1Parser(ParserSharedInputState state) {
  super(state,1);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

	public final void firstRule() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST firstRule_AST = null;
		AST u_AST = null;
		AST ul_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_Uquery:
			{
				match(LITERAL_Uquery);
				AST tmp2_AST = null;
				tmp2_AST = astFactory.create(LT(1));
				match(NAME);
				match(LBRACE);
				{
				union();
				u_AST = returnAST;
				}
				match(RBRACE);
				match(SEMICOLON);
				firstRule_AST = currentAST.root;
				
					final QueryNode _qn = new QueryNode();
					_qn.setQueryName((tmp2_AST).toString());
					_qn.setHeadConstructor((ConstructorNode) u_AST);
					_qn.setExistentialQuery(false);
					firstRule_AST =_qn;
				
				currentAST.root = firstRule_AST;
				currentAST.child = firstRule_AST!=null &&firstRule_AST.getFirstChild()!=null ?
					firstRule_AST.getFirstChild() : firstRule_AST;
				currentAST.advanceChildToEnd();
				break;
			}
			case LITERAL_Equery:
			{
				match(LITERAL_Equery);
				AST tmp7_AST = null;
				tmp7_AST = astFactory.create(LT(1));
				match(NAME);
				match(LBRACE);
				{
				union();
				ul_AST = returnAST;
				}
				match(RBRACE);
				match(SEMICOLON);
				firstRule_AST = currentAST.root;
				
					final QueryNode _qn = new QueryNode();
					_qn.setQueryName((tmp7_AST).toString());
					_qn.setHeadConstructor((ConstructorNode) ul_AST);
					_qn.setExistentialQuery(true);
					firstRule_AST =_qn;
				
				currentAST.root = firstRule_AST;
				currentAST.child = firstRule_AST!=null &&firstRule_AST.getFirstChild()!=null ?
					firstRule_AST.getFirstChild() : firstRule_AST;
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
			consumeUntil(_tokenSet_0);
		}
		returnAST = firstRule_AST;
	}
	
	public final void union() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST union_AST = null;
		AST cx_AST = null;
		AST cy_AST = null;
		
		try {      // for error handling
			{
			concatenation();
			cx_AST = returnAST;
			}
			{
			_loop64:
			do {
				if ((LA(1)==ALTERNATION)) {
					match(ALTERNATION);
					concatenation();
					cy_AST = returnAST;
				}
				else {
					break _loop64;
				}
				
			} while (true);
			}
			union_AST = currentAST.root;
			
				if (cy_AST != null) {
					final OrNode _orNode = new OrNode();
					_orNode.setLeftNode((ConstructorNode) cx_AST);
					_orNode.setRightNode((ConstructorNode) cy_AST);		
					union_AST =  _orNode;	
				} else {
					union_AST = cx_AST;
				}
			
			currentAST.root = union_AST;
			currentAST.child = union_AST!=null &&union_AST.getFirstChild()!=null ?
				union_AST.getFirstChild() : union_AST;
			currentAST.advanceChildToEnd();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_1);
		}
		returnAST = union_AST;
	}
	
	public final void concatenation() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST concatenation_AST = null;
		AST r1_AST = null;
		AST r2_AST = null;
		
		try {      // for error handling
			repterm();
			r1_AST = returnAST;
			{
			_loop67:
			do {
				if ((LA(1)==LPAREN||LA(1)==LANGLE)) {
					repterm();
					r2_AST = returnAST;
				}
				else {
					break _loop67;
				}
				
			} while (true);
			}
			concatenation_AST = currentAST.root;
			
				if(r2_AST != null) {
					final AndNode _aNode = new AndNode();
					_aNode.setLeftNode((ConstructorNode) r1_AST);
					_aNode.setRightNode((ConstructorNode) r2_AST);
					concatenation_AST = _aNode;
				} else {
					concatenation_AST = r1_AST;
				}
			
			currentAST.root = concatenation_AST;
			currentAST.child = concatenation_AST!=null &&concatenation_AST.getFirstChild()!=null ?
				concatenation_AST.getFirstChild() : concatenation_AST;
			currentAST.advanceChildToEnd();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_2);
		}
		returnAST = concatenation_AST;
	}
	
	public final void repterm() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST repterm_AST = null;
		AST t_AST = null;
		AST r_AST = null;
		
		try {      // for error handling
			term();
			t_AST = returnAST;
			{
			switch ( LA(1)) {
			case ONE_OR_MORE:
			case ZERO_OR_MORE:
			case ZERO_OR_ONE:
			{
				repetition();
				r_AST = returnAST;
				break;
			}
			case LPAREN:
			case RPAREN:
			case RBRACE:
			case ALTERNATION:
			case LANGLE:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			repterm_AST = currentAST.root;
			
				final ConstructorNode _n = (ConstructorNode) t_AST;
					if (r_AST != null) {
						final String _regexOp = r_AST.toString();
						if (_regexOp.equals("?")) {
							_n.setRegexOperator(IPEQRegexTypes.ZERO_OR_ONE);
						} else if (_regexOp.equals("*")) {
							_n.setRegexOperator(IPEQRegexTypes.ZERO_OR_MORE);
						} else if (_regexOp.equals("+")) {
							_n.setRegexOperator(IPEQRegexTypes.ONE_OR_MORE);
						}
					} else {
						_n.setRegexOperator(IPEQRegexTypes.NO_REGEXTYPE);
					}
				repterm_AST = _n;
			
			currentAST.root = repterm_AST;
			currentAST.child = repterm_AST!=null &&repterm_AST.getFirstChild()!=null ?
				repterm_AST.getFirstChild() : repterm_AST;
			currentAST.advanceChildToEnd();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_3);
		}
		returnAST = repterm_AST;
	}
	
	public final void term() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST term_AST = null;
		AST g_AST = null;
		AST a_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LPAREN:
			{
				group();
				g_AST = returnAST;
				term_AST = currentAST.root;
				
					term_AST = g_AST;
				
				currentAST.root = term_AST;
				currentAST.child = term_AST!=null &&term_AST.getFirstChild()!=null ?
					term_AST.getFirstChild() : term_AST;
				currentAST.advanceChildToEnd();
				break;
			}
			case LANGLE:
			{
				atom();
				a_AST = returnAST;
				term_AST = currentAST.root;
				
					term_AST = a_AST;
				
				currentAST.root = term_AST;
				currentAST.child = term_AST!=null &&term_AST.getFirstChild()!=null ?
					term_AST.getFirstChild() : term_AST;
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
			consumeUntil(_tokenSet_4);
		}
		returnAST = term_AST;
	}
	
	public final void repetition() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST repetition_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case ZERO_OR_ONE:
			{
				AST tmp12_AST = null;
				tmp12_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp12_AST);
				match(ZERO_OR_ONE);
				repetition_AST = currentAST.root;
				break;
			}
			case ZERO_OR_MORE:
			{
				AST tmp13_AST = null;
				tmp13_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp13_AST);
				match(ZERO_OR_MORE);
				repetition_AST = currentAST.root;
				break;
			}
			case ONE_OR_MORE:
			{
				AST tmp14_AST = null;
				tmp14_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp14_AST);
				match(ONE_OR_MORE);
				repetition_AST = currentAST.root;
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
			consumeUntil(_tokenSet_3);
		}
		returnAST = repetition_AST;
	}
	
	public final void group() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST group_AST = null;
		AST u_AST = null;
		
		try {      // for error handling
			match(LPAREN);
			union();
			u_AST = returnAST;
			match(RPAREN);
			group_AST = currentAST.root;
			
				 group_AST = u_AST;
			
			currentAST.root = group_AST;
			currentAST.child = group_AST!=null &&group_AST.getFirstChild()!=null ?
				group_AST.getFirstChild() : group_AST;
			currentAST.advanceChildToEnd();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_4);
		}
		returnAST = group_AST;
	}
	
	public final void atom() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST atom_AST = null;
		
		try {      // for error handling
			match(LANGLE);
			atom_AST = currentAST.root;
			
			level2Parser.setInputState(getInputState());
			level2Parser.firstRule(); // go parse the content
			atom_AST = level2Parser.getAST();
			
			currentAST.root = atom_AST;
			currentAST.child = atom_AST!=null &&atom_AST.getFirstChild()!=null ?
				atom_AST.getFirstChild() : atom_AST;
			currentAST.advanceChildToEnd();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_4);
		}
		returnAST = atom_AST;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"LPAREN",
		"RPAREN",
		"LBRACKET",
		"RBRACKET",
		"LBRACE",
		"RBRACE",
		"ONE_OR_MORE",
		"ZERO_OR_MORE",
		"ZERO_OR_ONE",
		"NEG",
		"ALTERNATION",
		"ANY",
		"SEMICOLON",
		"NAME",
		"WS",
		"LANGLE",
		"\"Uquery\"",
		"\"Equery\""
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
		long[] data = { 544L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { 16928L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = { 541232L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = { 548400L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	
	}
