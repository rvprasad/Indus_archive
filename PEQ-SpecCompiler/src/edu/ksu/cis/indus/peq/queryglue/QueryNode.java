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

package edu.ksu.cis.indus.peq.queryglue;

import antlr.Token;
import antlr.collections.AST;
import antlr.BaseAST;

/**
 * @author ganeshan
 *
 * This represents the query ast node.
 */
public class QueryNode extends BaseAST {
    
    private String queryName;
    
    private ConstructorNode headNode;
    
    private boolean isExistentialQuery;
    
    
    public void setQueryName(final String queryName) {
        this.queryName = queryName;
    }
    
    public void setHeadConstructor(final ConstructorNode cnode) {
        this.headNode = cnode;
    }
    
    

    /* (non-Javadoc)
     * @see antlr.collections.AST#initialize(int, java.lang.String)
     */
    public void initialize(int t, String txt) {        
        
    }

    /* (non-Javadoc)
     * @see antlr.collections.AST#initialize(antlr.collections.AST)
     */
    public void initialize(AST t) {

    }

    /* (non-Javadoc)
     * @see antlr.collections.AST#initialize(antlr.Token)
     */
    public void initialize(Token t) {
    }

    /**
     * @return Returns the constructorList.
     */
    public ConstructorNode getHeadConstructor() {
        return headNode;
    }
    /**
     * @return Returns the queryName.
     */
    public String getQueryName() {
        return queryName;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
       return "query " + queryName;
    }
    /**
     * @return Returns the isExistentialQuery.
     */
    public boolean isExistentialQuery() {
        return isExistentialQuery;
    }
    /**
     * @param isExistentialQuery The isExistentialQuery to set.
     */
    public void setExistentialQuery(boolean isExistentialQuery) {
        this.isExistentialQuery = isExistentialQuery;
    }
}
