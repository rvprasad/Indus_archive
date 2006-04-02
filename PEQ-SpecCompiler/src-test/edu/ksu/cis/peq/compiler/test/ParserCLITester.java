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
 
package edu.ksu.cis.peq.compiler.test;

import edu.ksu.cis.indus.peq.queryast.AndAST;
import edu.ksu.cis.indus.peq.queryast.BaseAST;
import edu.ksu.cis.indus.peq.queryast.ConstructorAST;
import edu.ksu.cis.indus.peq.queryast.OrAST;
import edu.ksu.cis.indus.peq.queryglue.QueryConvertor;
import edu.ksu.cis.indus.peq.queryglue.QueryObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author ganeshan
 *
 * Test the parser ast generation.
 */
public class ParserCLITester {

    public static void main(String args[]) {
        System.out.println("Enter query: ");
        final BufferedReader _br = new BufferedReader(new InputStreamReader(
                System.in));
        try {
            final String _query = _br.readLine();
            final QueryConvertor _qc = new QueryConvertor();
            final QueryObject _qo = _qc.getQueryObject(_query);
            if (_qo != null) {
                System.out.println("Query name: " + _qo.getQueryName());
                System.out.println("Query Description: ");
                describeAST(_qo.getStartNode());
            }
        } catch (IOException _ie) {
            _ie.printStackTrace();
        }
    }

    /**
     * Describe the ast.
     * @param node
     */
    private static void describeAST(BaseAST node) {
        if (node != null) {
        if (node instanceof OrAST) {
            final OrAST _orAst = (OrAST) node;            
            System.out.println("Alternation :");
            System.out.println("left -> ");describeAST(_orAst.getLeftNode());
            System.out.println("right -> ");describeAST(_orAst.getRightNode());
            System.out.println("Regex: " + _orAst.getRegexType());
        } else if (node instanceof AndAST) {        
            final AndAST _aAst = (AndAST) node;
            System.out.println("Concatenation :");
            System.out.println("left -> ");describeAST(_aAst.getLeftNode());
            System.out.println("right -> ");describeAST(_aAst.getRightNode());
            System.out.println("Regex: " + _aAst.getRegexType());
        } else {
            final ConstructorAST _cast = (ConstructorAST) node;
            System.out.println("Constructor type: " +  _cast.getConstructType());
            System.out.println("Variable name: " + _cast.getVariableName());            
        }
        describeAST(node.getNextNode());
        }
    }
    
}
