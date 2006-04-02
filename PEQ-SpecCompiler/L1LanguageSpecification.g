header {

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
}

{
	import antlr.TokenStreamSelector; 
}
class Level1Lexer extends Lexer; 

options {
    k=2; // needed for newline junk
    charVocabulary='\u0000'..'\u007F'; // allow ascii
}

{
    private TokenStreamSelector selector;
    
    public final void setSelector(TokenStreamSelector s) {
        selector = s;
    }

    private String level2LexerName;

    public final void setLevel2LexerName(String s) {
        level2LexerName = s;
    }
}


LPAREN:         '(' ;
RPAREN:         ')' ;
LBRACKET:       '[' ;
RBRACKET:       ']' ;
LBRACE:         '{' ;
RBRACE:         '}' ;
ONE_OR_MORE:    '+' ;
ZERO_OR_MORE:   '*' ;
ZERO_OR_ONE:    '?' ;
NEG:            '^' ;
ALTERNATION:    '|' ;
ANY:            '.' ;
SEMICOLON:      ';' ;
NAME: 
    ('a'..'z'|'A'..'Z') ('a'..'z'|'A'..'Z'|'_'|'0'..'9')* 
    ;
WS: (   ' ' 
      | '\r' '\n' 
      | '\n' 
      | '\t'
    )
    {
        $setType(Token.SKIP);
    };
LANGLE:         
    '<' { selector.push(level2LexerName); };
 

{
import antlr.Parser;
import edu.ksu.cis.indus.peq.queryglue.ConstructorNode;
import edu.ksu.cis.indus.peq.queryglue.QueryNode;
import edu.ksu.cis.indus.peq.queryglue.IIndusConstructorTypes;
import edu.ksu.cis.indus.peq.queryglue.IPEQRegexTypes;
import edu.ksu.cis.indus.peq.queryglue.OrNode;
import edu.ksu.cis.indus.peq.queryglue.AndNode;
}
class Level1Parser extends Parser;
options {
    buildAST=true;         
}


{
    private AbstractLevel2Parser level2Parser;

    public void setLevel2Parser(AbstractLevel2Parser p) {
        level2Parser = p;
    }

}
firstRule!: "Uquery"! NAME   LBRACE! (u : union)  RBRACE! SEMICOLON!
{
	final QueryNode _qn = new QueryNode();
	_qn.setQueryName((#NAME).toString());
	_qn.setHeadConstructor((ConstructorNode) #u);
	_qn.setExistentialQuery(false);
	#firstRule =_qn;
}
| "Equery"! NAME  LBRACE! (ul : union)  RBRACE! SEMICOLON!
{
	final QueryNode _qn = new QueryNode();
	_qn.setQueryName((#NAME).toString());
	_qn.setHeadConstructor((ConstructorNode) #ul);
	_qn.setExistentialQuery(true);
	#firstRule =_qn;
}
    ;

union!:  (cx:concatenation) ( ALTERNATION! cy:concatenation )*
{
	if (#cy != null) {
		final OrNode _orNode = new OrNode();
		_orNode.setLeftNode((ConstructorNode) #cx);
		_orNode.setRightNode((ConstructorNode) #cy);		
		#union =  _orNode;	
	} else {
		#union = #cx;
	}
}
    ;


concatenation!: r1:repterm ( options {greedy=true;}: r2:repterm )*
{
	if(#r2 != null) {
		final AndNode _aNode = new AndNode();
		_aNode.setLeftNode((ConstructorNode) #r1);
		_aNode.setRightNode((ConstructorNode) #r2);
		#concatenation = _aNode;
	} else {
		#concatenation = #r1;
	}
}
    ;

repterm!: t: term  (r : repetition)?!
    {
    	final ConstructorNode _n = (ConstructorNode) #t;
		if (#r != null) {
			final String _regexOp = #r.toString();
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
    	## = _n;
    }
    ;

repetition: ZERO_OR_ONE
    | ZERO_OR_MORE
    | ONE_OR_MORE
    ;
    
term!: g : group
      {
      	## = #g;
      }
/*    | choice */
    | a : atom 
    {
    	## = #a;
    }
    ;
    
group!: LPAREN! u : union RPAREN!
       {
       	 ## = #u;
       }
    ;

/*
choice: LBRACKET! ( NEG )? ( atom )+ RBRACKET!
    ;
*/

atom!: LANGLE! 
        {   
            level2Parser.setInputState(getInputState());
            level2Parser.firstRule(); // go parse the content
            ## = level2Parser.getAST();
        }
/*    | ANY */
    ;

    

