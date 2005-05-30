/*
 * Created on Jan 6, 2005
 *
 * 
 */
package edu.ksu.cis.indus.kaveri.scoping;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.jibx.runtime.JiBXException;

import edu.ksu.cis.indus.common.scoping.ClassSpecification;
import edu.ksu.cis.indus.common.scoping.FieldSpecification;
import edu.ksu.cis.indus.common.scoping.MethodSpecification;
import edu.ksu.cis.indus.common.scoping.SpecificationBasedScopeDefinition;
import edu.ksu.cis.indus.common.scoping.TypeSpecification;
import edu.ksu.cis.indus.kaveri.KaveriErrorLog;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.PrettySignature;
import edu.ksu.cis.indus.kaveri.common.SECommons;

/**
 * @author ganeshan
 *  
 */
public class ScopePopupAction implements IObjectActionDelegate {

    private String scopePreferenceId = "edu.ksu.cis.indus.kaveri.scope";

    private ISelection selection;
    
    private IWorkbenchPart targetPart;
    
    /**
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction,
     *      org.eclipse.ui.IWorkbenchPart)
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        this.targetPart = targetPart;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        if (selection != null && selection instanceof IStructuredSelection) {
            IStructuredSelection _ssl = (IStructuredSelection) selection;
            final Object _retObj = _ssl.getFirstElement();

            String _scopeSpec = KaveriPlugin.getDefault().getPreferenceStore()
                    .getString(scopePreferenceId);
            final Shell _parentShell = targetPart.getSite().getShell();

            if (_scopeSpec.equals("")) {
                _scopeSpec = "<indus:scopeSpec xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                        + "xmlns:indus=\"http://indus.projects.cis.ksu.edu/indus\""
                        + "indus:specName=\"scope_spec\">";
                _scopeSpec += "\n</indus:scopeSpec>";
            }

            try {
                SpecificationBasedScopeDefinition _sbsd = SpecificationBasedScopeDefinition
                        .deserialize(_scopeSpec);
                if (_retObj instanceof IMethod) {
                    final IMethod _method = (IMethod) _retObj;
                    final ScopePropertiesSelectionDialog _spsd = new ScopePropertiesSelectionDialog(
                            _parentShell,
                            IScopeDialogMorphConstants.SCOPE_NAME_ONLY,
                            "Method_" + _method.getElementName());
                    if (_spsd.open() == IDialogConstants.OK_ID) {
                        final MethodSpecification _ms = addMethodSpecification(
                                _method, _spsd.getStrScopeName());
                        if (_ms != null) {
                            _sbsd.getMethodSpecs().add(_ms);
                            _scopeSpec = SpecificationBasedScopeDefinition
                                    .serialize(_sbsd);
                        }
                    }

                }
                if (_retObj instanceof IField) {
                    final IField _field = (IField) _retObj;
                    final ScopePropertiesSelectionDialog _spsd = new ScopePropertiesSelectionDialog(
                            _parentShell,
                            IScopeDialogMorphConstants.SCOPE_NAME_ONLY,
                            "Field_" + _field.getElementName());
                    if (_spsd.open() == IDialogConstants.OK_ID) {
                        final FieldSpecification _fs = getFieldSpecification(
                                _field, _spsd.getStrScopeName());
                        if (_fs != null) {
                            _sbsd.getFieldSpecs().add(_fs);
                            _scopeSpec = SpecificationBasedScopeDefinition
                                    .serialize(_sbsd);
                        }
                    }
                }

                if (_retObj instanceof IType) {
                    final IType _type = (IType) _retObj;
                    final ScopePropertiesSelectionDialog _spsd = new ScopePropertiesSelectionDialog(
                            _parentShell,
                            IScopeDialogMorphConstants.SCOPE_NAME_REGEX,
                            "Class_" + _type.getElementName());
                    _spsd.setStrDefaultClassName(_type.getFullyQualifiedName());
                    if (_spsd.open() == IDialogConstants.OK_ID) {
                        final ClassSpecification _cs = getClassSpecification(
                                _type, _spsd.getStrScopeName(), _spsd
                                        .getStrChoice(), _spsd
                                        .getStrClassRegex());
                        if (_cs != null) {
                            _sbsd.getClassSpecs().add(_cs);
                            _scopeSpec = SpecificationBasedScopeDefinition
                                    .serialize(_sbsd);
                        }
                    }

                }
                KaveriPlugin.getDefault().getPreferenceStore().setValue(
                        scopePreferenceId, _scopeSpec);
                KaveriPlugin.getDefault().savePluginPreferences();
            } catch (JiBXException _jbe) {
                SECommons.handleException(_jbe);
                KaveriErrorLog.logException(
                        "Unable to desrialize scope specification", _jbe);
            }
        }
    }

    /**
     * Get the class specification for the given class.
     * 
     * @param type
     *            The JDT class.
     * @param strScopeName
     *            The scope name
     * @param strChoice
     *            The scope extension
     * @param strClassName
     *            The new class name
     * @return ClassSpecification The class scope specification for the given
     *         class.
     */
    private ClassSpecification getClassSpecification(IType type,
            String strScopeName, String strChoice, String strClassName) {
        final ClassSpecification _cs = new ClassSpecification();
        _cs.setInclusion(true);
        _cs.setName(strScopeName);
        final TypeSpecification _ts = new TypeSpecification();
        if (type.getFullyQualifiedName().equals(strClassName)) {
            _ts.setNamePattern(type.getFullyQualifiedName());
        } else {
            _ts.setNamePattern(strClassName);
        }
        _ts.setScopeExtension(strChoice);
        _cs.setTypeSpec(_ts);
        return _cs;
    }

    /**
     * Get the field specification for the given IField.
     * 
     * @param field
     *            The JDT field.
     * @param strScopeName
     *            The name of the scope.
     * @return FieldSpecification The field specification for the given field.
     */
    private FieldSpecification getFieldSpecification(IField field,
            String strScopeName) {
        FieldSpecification _fs = new FieldSpecification();
        _fs.setInclusion(true);
        _fs.setName(strScopeName);
        _fs.setFieldNameSpec(field.getElementName());
        
        final TypeSpecification _declClassSpec = new TypeSpecification();
        _declClassSpec.setNamePattern(PrettySignature.getSignature(field
                .getParent()));
        _declClassSpec.setScopeExtension("IDENTITY");
        _fs.setDeclaringClassSpec(_declClassSpec);

        final TypeSpecification _fieldTypeSpec = new TypeSpecification();
        final String _retTypeString;
        try {
            _retTypeString = field.getTypeSignature();
        
        _fieldTypeSpec.setNamePattern(JavaModelUtil.getResolvedTypeName(
                _retTypeString, field.getDeclaringType()));        
            final int _retType = Signature.getTypeSignatureKind(field
                    .getTypeSignature());
            String _scopeDefinition = "";
            if (_retType == Signature.ARRAY_TYPE_SIGNATURE
                    || _retType == Signature.BASE_TYPE_SIGNATURE) {
                _scopeDefinition = "PRIMITIVE";
            } else {
                _scopeDefinition = "IDENTITY";
            }
            _fieldTypeSpec.setScopeExtension(_scopeDefinition);
            
            _fs.setFieldTypeSpec(_fieldTypeSpec);           
        } catch (JavaModelException _jme) {
            _fs = null;
        }
         catch (IllegalArgumentException e) {
           _fs = null;
            SECommons.handleException(e);
            KaveriErrorLog.logException("Illegal Argument Exception", e);
        }
        return _fs;
    }

    /**
     * Get the method specification for the given method
     * 
     * @param method
     *            The JDT method
     * @param strScopeName
     *            The scope name
     * @return MethodSpecification The scope specification for the given method.
     */
    private MethodSpecification addMethodSpecification(IMethod method,
            String strScopeName) {
        MethodSpecification _ms = new MethodSpecification();
        _ms.setInclusion(true);
        _ms.setMethodNameSpec(method.getElementName());
        _ms.setName(strScopeName);
        try {

            // Return type specification.
            final TypeSpecification _ts = new TypeSpecification();
            
            final String _retTypeString = Signature.getReturnType(method
                    .getSignature());
            String _scopeDefinition = "";
            final int _retType = Signature.getTypeSignatureKind(_retTypeString);
            if (_retType == Signature.ARRAY_TYPE_SIGNATURE
                    || _retType == Signature.BASE_TYPE_SIGNATURE) {
                _scopeDefinition = "PRIMITIVE";
            } else {
                _scopeDefinition = "IDENTITY";
            }

            _ts.setNamePattern(JavaModelUtil.getResolvedTypeName(
                    _retTypeString, method.getDeclaringType()));
            _ts.setScopeExtension(_scopeDefinition);
            _ms.setReturnTypeSpec(_ts);

            // Declaring class specification.
            final TypeSpecification _declSpecType = new TypeSpecification();
            _declSpecType.setNamePattern(method.getDeclaringType()
                    .getFullyQualifiedName());
            _declSpecType.setScopeExtension("IDENTITY");
            _ms.setDeclaringClassSpec(_declSpecType);

            final List _lstParamTypes = new LinkedList();
            final String _paramSig[] = method.getParameterTypes();

            for (int i = 0; i < _paramSig.length; i++) {
                final TypeSpecification _paramType = new TypeSpecification();
                final int _sigKind = Signature
                        .getTypeSignatureKind(_paramSig[i]);
                if (_sigKind == Signature.ARRAY_TYPE_SIGNATURE
                        || _sigKind == Signature.BASE_TYPE_SIGNATURE) {
                    _scopeDefinition = "PRIMITIVE";
                } else {
                    _scopeDefinition = "IDENTITY";
                }
                _paramType.setScopeExtension(_scopeDefinition);
                _paramType.setNamePattern(JavaModelUtil.getResolvedTypeName(
                        _paramSig[i], method.getDeclaringType()));
                _lstParamTypes.add(_paramType);
            }
            _ms.setParameterTypeSpecs(_lstParamTypes);

        } catch (JavaModelException _jme) {
            _ms = null;
            SECommons.handleException(_jme);
            KaveriErrorLog.logException("Java Model Exception", _jme);
        }
        return _ms;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        this.selection = selection;
    }

}