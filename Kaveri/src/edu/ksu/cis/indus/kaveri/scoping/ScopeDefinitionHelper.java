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
 
package edu.ksu.cis.indus.kaveri.scoping;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.ui.search.PrettySignature;

/**
 * @author ganeshan
 * This class has a set of static methods that generate the xml
 * scope specifications for the corresponding JDT elements. The xml
 * scope specifications for the elements can then be combined with the 
 * global scope specification.
 * 
 */
public class ScopeDefinitionHelper {

    /**
     * Returns the xml scope definition for an IMethod.
     * @param method The JDT method whose xml scope is needed
     * @param specName The name for this specification
     * @return String The xml representation of the scope
     */
    public static final String getScopeDefinition(final IMethod method, final String specName )
    	throws JavaModelException {
        String _scopeDefinition;
        _scopeDefinition = "<indus:methodSpec indus:specName=\"" +  specName  + "\" indus:methodNameSpec=\"" +
        		method.getElementName() + "\">\n";
        _scopeDefinition += "<indus:declaringClassSpec indus:scopeExtension=\"IDENTITY\"  indus:nameSpec=\"" +
        		PrettySignature.getSignature(method.getParent()) + "\" />\n";
        _scopeDefinition += "<indus:returnTypeSpec indus:scopeExtension=";
        final String _retTypeString = Signature.getReturnType(method.getSignature());        
        final int _retType = Signature.getTypeSignatureKind(_retTypeString);
        if (_retType == Signature.ARRAY_TYPE_SIGNATURE
                || _retType == Signature.BASE_TYPE_SIGNATURE) {
          _scopeDefinition += "\"PRIMITIVE\"";    
        } else {
          _scopeDefinition += "\"IDENTITY\"";
        }
        _scopeDefinition += " indus:nameSpec=\"" + JavaModelUtil.getResolvedTypeName(_retTypeString, method.getDeclaringType()) 
        	+ "\" />\n";
        _scopeDefinition += " <indus:parameterSpecs>";
        final String _paramSig[] = method.getParameterTypes();
        
        for (int i = 0; i < _paramSig.length; i++) {
            _scopeDefinition += "<indus:typeSpec indus:scopeExtension=";
            final int _sigKind = Signature.getTypeSignatureKind(_paramSig[i]);
            if (_sigKind == Signature.ARRAY_TYPE_SIGNATURE ||
                    _sigKind == Signature.BASE_TYPE_SIGNATURE) {
                _scopeDefinition +="\"PRIMITIVE\"";
            } else {
                _scopeDefinition += "\"IDENTITY\""; 
            }
            _scopeDefinition += " indus:nameSpec=\"" + JavaModelUtil.getResolvedTypeName(_paramSig[i], method.getDeclaringType()) + "\" />\n";
        }                
                
        
        _scopeDefinition += " </indus:parameterSpecs>\n";
        _scopeDefinition += "  </indus:methodSpec>";        
        return _scopeDefinition;
    }
    
    /**
     * Returns the scope xml representation for the given class
     * @param classType The JDT class.
     * @param specName The name for the specification
     * @param classProp The scope around the specified type
     * @return String The xml representation for the class
     */
    public static final  String getScopeDefinition(final IType classType, final String specName, final String classProp)  {
        String _scopeDefinition;
        _scopeDefinition = "<indus:classSpec indus:specName=\"" +  specName  + "\">\n";
        _scopeDefinition += "<indus:typeSpec indus:scopeExtension=\"" + classProp + "\" indus:nameSpec=\"";
        _scopeDefinition += classType.getFullyQualifiedName() + "\" />\n";       
        _scopeDefinition += "</indus:classSpec>";        	
        return _scopeDefinition;
    }
    
    /**
     * Returns the scope xml representation for the given class
     * @param classType The JDT class.
     * @param specName The name for the specification
     * @param classProp The scope around the specified type
     * @return String The xml representation for the class
     */
    public static final  String getScopeDefinition(final String classType, final String specName, final String classProp)  {
        String _scopeDefinition;
        _scopeDefinition = "<indus:classSpec indus:specName=\"" +  specName  + "\">\n";
        _scopeDefinition += "<indus:typeSpec indus:scopeExtension=\"" + classProp + "\" indus:nameSpec=\"";
        _scopeDefinition += classType + "\" />\n";       
        _scopeDefinition += "</indus:classSpec>";
        return _scopeDefinition;
    }
    
    /**
     * Returns the scope xml representation for the given Field
     * @param field The JDT Field.
     * @param specName The name for the specification
     * @return String The xml representation for the field
     */
    public static final  String getScopeDefinition(final IField field, final String specName) throws JavaModelException {
        String _scopeDefinition;
        _scopeDefinition = "<indus:fieldSpec indus:specName=\"" +  specName  + "\" indus:fieldNameSpec=\"" +
		field.getElementName() + "\">\n";
        _scopeDefinition += "<indus:declaringClassSpec indus:scopeExtension=\"IDENTITY\"  indus:nameSpec=\"" +
			PrettySignature.getSignature(field.getParent()) + "\" />\n";
        _scopeDefinition += "<indus:fieldTypeSpec indus:scopeExtension=";
        final int _retType = Signature.getTypeSignatureKind(field.getTypeSignature());
        if (_retType == Signature.ARRAY_TYPE_SIGNATURE ||
                _retType == Signature.BASE_TYPE_SIGNATURE) {
            _scopeDefinition += "\"PRIMITIVE\"";
        } else {
            _scopeDefinition += "\"IDENTITY\"";   
        }
        _scopeDefinition += " indus:nameSpec=\"" + JavaModelUtil.getResolvedTypeName(Signature.toString(field.getTypeSignature()),field.getDeclaringType()) + "\" />\n";               
        _scopeDefinition += "</indus:fieldSpec>";
        return _scopeDefinition;
    }
    
    
    
}
