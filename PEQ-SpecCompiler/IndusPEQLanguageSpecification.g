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
class IndusPeqLexer extends Lexer;

options {
    k=2; // needed for newline junk
    charVocabulary='\u0000'..'\u007F'; // allow ascii
}

{
    private TokenStreamSelector selector;

    public final void setSelector(TokenStreamSelector s) {
        selector = s;
    }
}


LPAREN:         '(' ;
RPAREN:         ')' ;
IDENT: 
    ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'_'|'0'..'9')* 
    ;
WS: (   ' ' 
      | '\r' '\n' 
      | '\n' 
      | '\t'
    )
    {
        $setType(Token.SKIP);
    }
    ;
RANGLE:
    '>' { selector.pop(); }
    ;

{
import antlr.Parser;
import edu.ksu.cis.indus.peq.queryglue.ConstructorNode;
import edu.ksu.cis.indus.peq.queryglue.QueryNode;
import edu.ksu.cis.indus.peq.queryglue.IIndusConstructorTypes;
import edu.ksu.cis.indus.peq.queryglue.IPEQRegexTypes;
}
class IndusPeqParser extends Parser("AbstractLevel2Parser");
options {
    buildAST=true;      
}

firstRule!: c:constructor  RANGLE! 
	{
		#firstRule = #c;
	}
    ;

constructor!:
/*
  "idef"! LPAREN! IDENT RPAREN!
 	  {
 	  	ConstructorNode _n = new ConstructorNode();
 	  	_n.setConstructorType(IIndusConstructorTypes.IDEF);
 	  	_n.setVariableName((#IDENT).toString());
 	  	#constructor = _n;
 	  }
    | "iuse"! LPAREN! IDENT RPAREN!
     	  {
 	  	ConstructorNode _n = new ConstructorNode();
 	  	_n.setConstructorType(IIndusConstructorTypes.IUSE);
 	  	_n.setVariableName((#IDENT).toString());
 	  	#constructor = _n;
 	  } */
    | "cdepd"! LPAREN! IDENT RPAREN!
     	  {
 	  	ConstructorNode _n = new ConstructorNode();
 	  	_n.setConstructorType(IIndusConstructorTypes.CDEPD);
 	  	_n.setVariableName((#IDENT).toString());
 	  	#constructor = _n;
 	  }
 	   | "cdept"! LPAREN! IDENT RPAREN!
     	  {
 	  	ConstructorNode _n = new ConstructorNode();
 	  	_n.setConstructorType(IIndusConstructorTypes.CDEPT);
 	  	_n.setVariableName((#IDENT).toString());
 	  	#constructor = _n;
 	  }
 	   | "dvgd"! LPAREN! IDENT RPAREN!
     	  {
 	  	ConstructorNode _n = new ConstructorNode();
 	  	_n.setConstructorType(IIndusConstructorTypes.DDEPD);
 	  	_n.setVariableName((#IDENT).toString());
 	  	#constructor = _n;
 	  }
 	   | "dvgdt"! LPAREN! IDENT RPAREN!
     	  {
 	  	ConstructorNode _n = new ConstructorNode();
 	  	_n.setConstructorType(IIndusConstructorTypes.DDEPT);
 	  	_n.setVariableName((#IDENT).toString());
 	  	#constructor = _n;
 	  }
 	  | "readydd"! LPAREN! IDENT RPAREN!
     	  {
 	  	ConstructorNode _n = new ConstructorNode();
 	  	_n.setConstructorType(IIndusConstructorTypes.RDEPD);
 	  	_n.setVariableName((#IDENT).toString());
 	  	#constructor = _n;
 	  }
 	  | "readydt"! LPAREN! IDENT RPAREN!
     	  {
 	  	ConstructorNode _n = new ConstructorNode();
 	  	_n.setConstructorType(IIndusConstructorTypes.RDEPT);
 	  	_n.setVariableName((#IDENT).toString());
 	  	#constructor = _n;
 	  }
 	   | "syncdd"! LPAREN! IDENT RPAREN!
     	  {
 	  	ConstructorNode _n = new ConstructorNode();
 	  	_n.setConstructorType(IIndusConstructorTypes.SDEPD);
 	  	_n.setVariableName((#IDENT).toString());
 	  	#constructor = _n;
 	  }
 	   | "syncdt"! LPAREN! IDENT RPAREN!
     	  {
 	  	ConstructorNode _n = new ConstructorNode();
 	  	_n.setConstructorType(IIndusConstructorTypes.SDEPT);
 	  	_n.setVariableName((#IDENT).toString());
 	  	#constructor = _n;
 	  }
 	   | "datadef"! LPAREN! IDENT RPAREN!
     	  {
 	  	ConstructorNode _n = new ConstructorNode();
 	  	_n.setConstructorType(IIndusConstructorTypes.DDEF);
 	  	_n.setVariableName((#IDENT).toString());
 	  	#constructor = _n;
 	  }
 	  
 	   | "datause"! LPAREN! IDENT RPAREN!
     	  {
 	  	ConstructorNode _n = new ConstructorNode();
 	  	_n.setConstructorType(IIndusConstructorTypes.DUSE);
 	  	_n.setVariableName((#IDENT).toString());
 	  	#constructor = _n;
 	  } 
 	   | "intfdd"! LPAREN! IDENT RPAREN!
     	  {
 	  	ConstructorNode _n = new ConstructorNode();
 	  	_n.setConstructorType(IIndusConstructorTypes.IDEPD);
 	  	_n.setVariableName((#IDENT).toString());
 	  	#constructor = _n;
 	  }
 	   | "intfdt"! LPAREN! IDENT RPAREN!
     	  {
 	  	ConstructorNode _n = new ConstructorNode();
 	  	_n.setConstructorType(IIndusConstructorTypes.IDEPT);
 	  	_n.setVariableName((#IDENT).toString());
 	  	#constructor = _n;
 	  }
 	  /*
 	   | "ruse"! LPAREN! IDENT RPAREN!
     	  {
 	  	ConstructorNode _n = new ConstructorNode();
 	  	_n.setConstructorType(IIndusConstructorTypes.RUSE);
 	  	_n.setVariableName((#IDENT).toString());
 	  	#constructor = _n;
 	  }
 	   | "rdef"! LPAREN! IDENT RPAREN!
     	  {
 	  	ConstructorNode _n = new ConstructorNode();
 	  	_n.setConstructorType(IIndusConstructorTypes.RDEF);
 	  	_n.setVariableName((#IDENT).toString());
 	  	#constructor = _n;
 	  }*/
 	  | "wc"! LPAREN! RPAREN! 
 	  {
 	  	ConstructorNode _n = new ConstructorNode();
 	  	_n.setConstructorType(IIndusConstructorTypes.WC);
 	  	_n.setVariableName("default");
 	  	#constructor = _n;
 	  }
    ;

