/*******************************************************************************
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2007 SAnToS Laboratory, Kansas State University
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 *******************************************************************************/
package edu.ksu.cis.indus.kaveri.common;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

public class PrettySignature {

	public static String getSignature(IJavaElement element) {
		if (element == null)
			return null;
		else
			switch (element.getElementType()) {
				case IJavaElement.METHOD:
					return getMethodSignature((IMethod)element);
				case IJavaElement.TYPE:
					return JavaModelUtil.getFullyQualifiedName((IType)element);
				default:
					return element.getElementName();
			}
	}
	
	public static String getMethodSignature(IMethod method) {
		StringBuffer buffer= new StringBuffer();
		buffer.append(JavaModelUtil.getFullyQualifiedName(method.getDeclaringType()));
		boolean isConstructor= method.getElementName().equals(method.getDeclaringType().getElementName());
		if (!isConstructor) {
			buffer.append('.');
		}
		buffer.append(getUnqualifiedMethodSignature(method, !isConstructor));
		
		return buffer.toString();
	}

	public static String getUnqualifiedTypeSignature(IType type) {
		return type.getElementName();
	}
	
	public static String getUnqualifiedMethodSignature(IMethod method, boolean includeName) {
		StringBuffer buffer= new StringBuffer();
		if (includeName) {
			buffer.append(method.getElementName());
		}
		buffer.append('(');
		
		String[] types= method.getParameterTypes();
		if (types.length > 0)
			buffer.append(Signature.toString(types[0]));
		for (int i= 1; i < types.length; i++) {
			buffer.append(", "); //$NON-NLS-1$
			buffer.append(Signature.toString(types[i]));
		}
		
		buffer.append(')');
		
		return buffer.toString();
	}

	public static String getUnqualifiedMethodSignature(IMethod method) {
		return getUnqualifiedMethodSignature(method, true);
	}	
}
