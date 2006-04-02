/*
 *
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
 *
 * This software is licensed under the KSU Open Academic License.
 * You should have received a copy of the license with the distribution.
 * A copy can be found at
 *     http://www.cis.ksu.edu/santos/license.html
 * or you can contact the lab at:
 *     SAnToS Laboratory
 *     234 Nichols Hall
 *     Manhattan, KS 66506, USA
 */
 
package edu.ksu.cis.indus.peq.queryglue;

import java.io.StringReader;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.TokenStreamSelector;

import edu.ksu.cis.indus.peq.queryast.AndAST;
import edu.ksu.cis.indus.peq.queryast.BaseAST;
import edu.ksu.cis.indus.peq.queryast.ConstructorAST;
import edu.ksu.cis.indus.peq.queryast.OrAST;
import edu.ksu.cis.indus.peq.queryparser.IndusPeqLexer;
import edu.ksu.cis.indus.peq.queryparser.IndusPeqParser;
import edu.ksu.cis.indus.peq.queryparser.Level1Lexer;
import edu.ksu.cis.indus.peq.queryparser.Level1Parser;

/**
 * @author ganeshan
 *
 * Converts the given query and returns the ast.
 */
public class QueryConvertor {
    private String errorString;
    
    public QueryConvertor() {
        
    }
    
    /**
     * Parse the query and return the query object.
     * @param query The query string
     * @return QueryObject The query abstraction of the query string.
     * @post Result != null implies Parse was successful.
     */
    public QueryObject getQueryObject(final String query) {
        QueryObject _retObj = null;
        TokenStreamSelector _tss = new TokenStreamSelector();
        Level1Lexer _tl1 = new Level1Lexer(new StringReader(query));
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
                _retObj = new QueryObject();
                _retObj.setQueryName(_qn.getQueryName());
                _retObj.setExistential(_qn.isExistentialQuery());
                ConstructorNode _cn = _qn.getHeadConstructor();
                final BaseAST _startNode = processConstructors(_cn);                
                
                /*while (_cn != null) {
                    
                    final int _cons = _cn.getConstructorType();
                    final ConstructorAST _c =  new ConstructorAST();
                    _c.setConstructType(_cons);
                    _c.setRegexType(_cn.getRegexOperator());
                    _c.setVariableName(_cn.getVariableName());
                    _retObj.addConstructor(_c);
                    _cn = _cn.getNextConstructor();
                }*/
                _retObj.setStartNode(_startNode);
            }
        } catch (RecognitionException e) {
            errorString =  e.getMessage();
        } catch (TokenStreamException e) {            
            errorString =  e.getMessage();
        }        
        return _retObj;
    }

    /**
     * Process constructors.
     * @param cn The constructor to process.
     * @return The processed constructor.
     */
    private BaseAST processConstructors(ConstructorNode cn) {        
        BaseAST _result = null;
        
       if (cn != null) { 
       if (cn instanceof OrNode) {
           final OrAST _orAst = new OrAST();
           final BaseAST leftNode = processConstructors(((OrNode) cn).getLeftNode());
           final BaseAST rightNode = processConstructors(((OrNode) cn).getRightNode());
           _orAst.setLeftNode(leftNode);
           _orAst.setRightNode(rightNode);
           if (cn.getNextConstructor() != null) {
               _orAst.setNextNode(processConstructors(cn.getNextConstructor()));
           }
           _result = _orAst;
           _orAst.setRegexType(cn.getRegexOperator());
       } else if (cn instanceof AndNode) {
           final AndAST _aAst = new AndAST();
           final BaseAST leftNode = processConstructors(((AndNode) cn).getLeftNode());
           final BaseAST rightNode = processConstructors(((AndNode) cn).getRightNode());
           _aAst.setLeftNode(leftNode);
           _aAst.setRightNode(rightNode);
           if (cn.getNextConstructor() != null) {
               _aAst.setNextNode(processConstructors(cn.getNextConstructor()));
           }
           _aAst.setRegexType(cn.getRegexOperator());
           _result = _aAst;
       }       
       else  {
           final ConstructorAST _cast = new ConstructorAST();
           _cast.setConstructType(cn.getConstructorType());
           _cast.setRegexType(cn.getRegexOperator());
           _cast.setVariableName(cn.getVariableName());
           _cast.setNextNode(processConstructors(cn.getNextConstructor()));
           _result = _cast;
       }       
       }
       return _result;
    }

    /**
     * @return Returns the errorString.
     */
    public String getErrorString() {
        return errorString;
    }
}
