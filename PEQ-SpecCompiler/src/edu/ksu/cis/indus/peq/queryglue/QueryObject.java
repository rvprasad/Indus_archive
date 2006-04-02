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

import edu.ksu.cis.indus.peq.queryast.BaseAST;

/**
 * @author ganeshan
 *
 * This represents a query entity.
 */
public class QueryObject {

    /**
     * The name of the query.
     */
    private String queryName;
    
    /**
     * Type of query.
     */
    private boolean isExistential;
    
    /**
     * The initial ast node.
     * @author ganeshan         
     */
    private BaseAST startNode;
    
    
    public QueryObject() {        
    }
    
    public void setQueryName(final String queryName) {
        this.queryName = queryName;
    }
    
    public void setStartNode(final BaseAST node) {
        startNode = node;
    }
    
    /**
     * @return Returns the starting node.
     */
    public BaseAST getStartNode() {
        return startNode;
    }
    /**
     * @return Returns the queryName.
     */
    public String getQueryName() {
        return queryName;
    }
    /**
     * @return Returns the isExistential.
     */
    public boolean isExistential() {
        return isExistential;
    }
    /**
     * Set the query to be existential.
     * @param isExistential The isExistential to set.
     */
    public void setExistential(boolean isExistential) {
        this.isExistential = isExistential;
    }
}
