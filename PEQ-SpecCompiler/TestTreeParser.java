// $ANTLR : "L1LanguageSpecification.g" -> "TestTreeParser.java"$

import antlr.TreeParser;
import antlr.Token;
import antlr.collections.AST;
import antlr.RecognitionException;
import antlr.ANTLRException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.collections.impl.BitSet;
import antlr.ASTPair;
import antlr.collections.impl.ASTArray;


public class TestTreeParser extends antlr.TreeParser       implements TestLevel1LexerTokenTypes
 {
public TestTreeParser() {
	tokenNames = _tokenNames;
}

	public final void firstRule(AST _t) throws RecognitionException {
		
		AST firstRule_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST firstRule_AST = null;
		
		try {      // for error handling
			AST __t845 = _t;
			AST tmp1_AST = null;
			AST tmp1_AST_in = null;
			tmp1_AST = astFactory.create((AST)_t);
			tmp1_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp1_AST);
			ASTPair __currentAST845 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,NAME);
			_t = _t.getFirstChild();
			currentAST = __currentAST845;
			_t = __t845;
			_t = _t.getNextSibling();
			firstRule_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = firstRule_AST;
		_retTree = _t;
	}
	
