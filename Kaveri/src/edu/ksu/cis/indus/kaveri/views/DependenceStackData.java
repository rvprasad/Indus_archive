/*
 * Created on Jan 3, 2005
 *
 */
package edu.ksu.cis.indus.kaveri.views;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.graphics.RGB;

/**
 * 
 * Holds the information for each action in the stack.
 * @author ganeshan
 *
 * 
 */
public class DependenceStackData {
	private String statement;
	private IFile file;
	private int lineNo;
	private RGB depColor;
	
	public  DependenceStackData() {		
	}
	
	
	
	/**
	 * @return Returns the file.
	 */
	public IFile getFile() {
		return file;
	}
	/**
	 * @param file The file to set.
	 */
	public void setFile(final IFile file) {
		this.file = file;
	}
	/**
	 * @return Returns the lineNo.
	 */
	public int getLineNo() {
		return lineNo;
	}
	/**
	 * @param lineNo The lineNo to set.
	 */
	public void setLineNo(final int lineNo) {
		this.lineNo = lineNo;
	}
	/**
	 * @return Returns the statement.
	 */
	public String getStatement() {
		return statement;
	}
	/**
	 * @param statement The statement to set.
	 */
	public void setStatement(final String statement) {
		this.statement = statement;
	}
	/**
	 * @return Returns the depColor.
	 */
	public RGB getDepColor() {
		return depColor;
	}
	/**
	 * @param depColor The depColor to set.
	 */
	public void setDepColor(RGB depColor) {
		this.depColor = depColor;
	}
}
