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

package edu.ksu.cis.indus.peq.querytest;

import java.io.DataInputStream;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.TokenStreamSelector;
import edu.ksu.cis.indus.peq.queryglue.ConstructorNode;
import edu.ksu.cis.indus.peq.queryglue.IIndusConstructorTypes;
import edu.ksu.cis.indus.peq.queryglue.QueryNode;
import edu.ksu.cis.indus.peq.queryparser.IndusPeqLexer;
import edu.ksu.cis.indus.peq.queryparser.IndusPeqParser;
import edu.ksu.cis.indus.peq.queryparser.Level1Lexer;
import edu.ksu.cis.indus.peq.queryparser.Level1Parser;



/**
 * @author ganeshan
 *
 * Test the AST.
 */
public class MainClass {

    public static void main(String[] args)  {        
            
        	TokenStreamSelector _tss = new TokenStreamSelector();
            Level1Lexer _tl1 = new Level1Lexer(new DataInputStream(System.in));
            Level1Parser _tp1 = new Level1Parser(_tss);
            IndusPeqLexer _tl2 = new IndusPeqLexer(_tl1.getInputState());
            IndusPeqParser _tp2 = new IndusPeqParser(_tss);
            
            _tss.addInputStream(_tl1, "level1lexer");
            _tss.addInputStream(_tl2, "level2lexer");
            _tss.select("level1lexer");
            _tl1.setSelector(_tss);
            _tl1.setLevel2LexerName("level2lexer");
            _tl2.setSelector(_tss);
            _tp1.setLevel2Parser(_tp2);
            
            try {
                _tp1.firstRule();
                final QueryNode _qn = (QueryNode) _tp1.getAST();
                if (_qn != null) {
                    System.out.println("Query Name: " + _qn.getQueryName());
                    ConstructorNode _cn = _qn.getHeadConstructor();
                    while (_cn != null) {
                        final int _cons = _cn.getConstructorType();
                        String _cMsg = "";
                        if (_cons == IIndusConstructorTypes.IDEF) {
                            _cMsg = "Def";
                        }
                        if (_cons == IIndusConstructorTypes.IUSE) {
                            _cMsg = "Use";
                        }
                        System.out.println("ConstructorAST type: " + _cMsg + " variable : " + _cn.getVariableName());
                        _cn = _cn.getNextConstructor();
                    }
                }
            } catch (RecognitionException e) {               
                e.printStackTrace();
            } catch (TokenStreamException e) {
                e.printStackTrace();
            }
            
    }
}
