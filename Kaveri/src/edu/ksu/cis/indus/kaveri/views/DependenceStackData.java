/*
 * Created on Jan 3, 2005
 *
 */
package edu.ksu.cis.indus.kaveri.views;

import org.eclipse.core.resources.IFile;

/**
 * 
 * Holds the information for each action in the stack.
 * @author ganeshan
 *
 * 
 */
public class DependenceStackData {
	
	private PartialStmtData psd;
	private String nextElemDepType;
	
	/**
	 * The unique signature of the next element in the list.
	 */
	private String nextElementSignature;
			
	public  DependenceStackData(final PartialStmtData pd) {
	    psd = new PartialStmtData();	    
	    psd.setClassName(pd.getClassName());
	    psd.setMethodName(pd.getMethodName());
	    psd.setJavaFile(pd.getJavaFile());
	    psd.setLineNo(pd.getLineNo());
	    psd.setSelectedStatement(pd.getSelectedStatement());
	    psd.setStmtList(pd.getStmtList());	   
	}
	
    public boolean equals(Object obj) {
        if (obj instanceof DependenceStackData) {
         final DependenceStackData _cObj = (DependenceStackData) obj;
         final PartialStmtData _pd = _cObj.getPsd();
         
         boolean _result = false;
         if (_pd.getClassName().equals(psd.getClassName()) &&
                 _pd.getJavaFile().equals(psd.getJavaFile()) &&
                 _pd.getMethodName().equals(psd.getMethodName()) &&
                 _pd.getSelectedStatement().equals(psd.getSelectedStatement()) &&
                 _pd.getLineNo() == psd.getLineNo()) {
             _result = true;             
         }
         return _result;
         
        } else {
         return super.equals(obj);
        }
     }

	
	
	
	/**
	 * @return Returns the file.
	 */
	public IFile getFile() {
		return psd.getJavaFile();
	}
	
	/**
	 * @return Returns the lineNo.
	 */
	public int getLineNo() {
		return psd.getLineNo();
	}
	
	/**
	 * @return Returns the statement.
	 */
	public String getStatement() {
		return psd.getSelectedStatement();
	}
		
    /**
     * @return Returns the psd.
     */
    public PartialStmtData getPsd() {
        return psd;
    }
    /**
     * @return Returns the nextElementSignature.
     */
    public String getNextElementSignature() {
        return nextElementSignature;
    }
    /**
     * @param nextElementSignature The nextElementSignature to set.
     */
    public void setNextElementSignature(String nextElementSignature) {
        this.nextElementSignature = nextElementSignature;
    }



    /**
     * @param string
     */
    public void setNextElemDepType(String string) {
      nextElemDepType = string;
    }
    /**
     * @return Returns the nextElemDepType.
     */
    public String getNextElemDepType() {
        return nextElemDepType;
    }
}
