/*
 * Created on Apr 9, 2006
 */
package com.python.pydev.refactoring.refactorer.refactorings.renamelocal;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.python.pydev.core.docutils.PySelection;
import org.python.pydev.core.docutils.StringUtils;
import org.python.pydev.editor.codecompletion.revisited.CodeCompletionTestsBase;
import org.python.pydev.editor.codecompletion.revisited.modules.CompiledModule;
import org.python.pydev.editor.refactoring.AbstractPyRefactoring;
import org.python.pydev.editor.refactoring.RefactoringRequest;

import com.python.pydev.refactoring.refactorer.AstEntryRefactorerRequestConstants;
import com.python.pydev.refactoring.refactorer.Refactorer;
import com.python.pydev.refactoring.wizards.rename.PyRenameEntryPoint;

public class RefactoringLocalTestBase extends CodeCompletionTestsBase {
    
    protected static boolean DEBUG = false;


    public void setUp() throws Exception {
        super.setUp();
        CompiledModule.COMPILED_MODULES_ENABLED = getCompiledModulesEnabled();
        this.restorePythonPath(getForceRestorePythonPath());
        AbstractPyRefactoring.setPyRefactoring(new Refactorer());
        
    }

    protected boolean getForceRestorePythonPath() {
        return false;
    }

    protected boolean getCompiledModulesEnabled() {
        return false;
    }
    
    public void tearDown() throws Exception {
        CompiledModule.COMPILED_MODULES_ENABLED = true;
        super.tearDown();
    }

    protected void applyRenameRefactoring(RefactoringRequest request) throws CoreException {
        applyRenameRefactoring(request, false);
    }

    /** Applies a rename refactoring 
     */
    protected void applyRenameRefactoring(RefactoringRequest request, boolean expectError) throws CoreException {
        PyRenameEntryPoint processor = new PyRenameEntryPoint(request);
        NullProgressMonitor nullProgressMonitor = new NullProgressMonitor();
        checkStatus(processor.checkInitialConditions(nullProgressMonitor), expectError);
        checkStatus(processor.checkFinalConditions(nullProgressMonitor, null), expectError);
        Change change = processor.createChange(nullProgressMonitor);
        if(!expectError){
            //otherwise, if there is an error, the change may be null
            change.perform(nullProgressMonitor);
        }
    }

//    /** Applies an extract method refactoring 
//     */
//    private void applyExtractMethodRefactoring(RefactoringRequest request, boolean expectError) throws OperationCanceledException, CoreException {
//        PyExtractMethodProcessor processor = new PyExtractMethodProcessor(request);
//        NullProgressMonitor monitor = new NullProgressMonitor();
//        checkStatus(processor.checkInitialConditions(monitor), expectError);
//        checkStatus(processor.checkFinalConditions(monitor, null), expectError);
//        Change change = processor.createChange(monitor);
//        change.perform(monitor);
//    }
    
    protected void checkStatus(RefactoringStatus status, boolean expectError) {
        RefactoringStatusEntry err = status.getEntryMatchingSeverity(RefactoringStatus.ERROR);
        RefactoringStatusEntry fatal = status.getEntryMatchingSeverity(RefactoringStatus.FATAL);
        
        if(!expectError){
            assertNull(err != null? err.getMessage() : "", err);
            assertNull(fatal != null? fatal.getMessage() : "", fatal);
        }else{
            assertNotNull(err);
            assertNotNull(fatal);
        }
    }
    
    protected void checkRename(String strDoc, int line, int col, String initialName, boolean expectError) throws CoreException {
        checkRename(strDoc, line, col, initialName, expectError, false);
    }
    
    /**
     * @param line 0-based
     * @param col 0-based
     */
    protected void checkRename(String strDoc, int line, int col, String initialName, boolean expectError, boolean onlyOnLocalScope) throws CoreException {
        checkRename(strDoc, line, col, initialName, expectError, onlyOnLocalScope, "bb");
    }
    
//    protected void checkExtract(String initialDoc, String expectedDoc, int line, int col, int len, boolean expectError, String newName) throws CoreException {
//        Document doc = new Document(initialDoc);
//        PySelection ps = new PySelection(doc, line, col, len);
//        
//        RefactoringRequest request = new RefactoringRequest(null, ps, nature);
//        request.moduleName = "foo";
//        request.inputName = newName;
//        request.fillInitialNameAndOffset();
//        
//        applyExtractMethodRefactoring(request, expectError);
//        String refactored = doc.get();
//        if(DEBUG){
//            System.out.println(refactored);
//        }
//        if(!expectError){
//            assertEquals(expectedDoc,  refactored);
//        }else{
//            //cannot have changed
//            assertEquals(initialDoc, doc.get());
//        }
//        
//    }

    protected void checkRename(String strDoc, int line, int col, String initialName, boolean expectError, boolean onlyOnLocalScope, String newName) throws CoreException {
        Document doc = new Document(StringUtils.format(strDoc, getSame(initialName)));
        PySelection ps = new PySelection(doc, line, col);
        
        RefactoringRequest request = new RefactoringRequest(null, ps, nature);
        request.setAdditionalInfo(AstEntryRefactorerRequestConstants.FIND_REFERENCES_ONLY_IN_LOCAL_SCOPE, onlyOnLocalScope);
        request.moduleName = "foo";
        request.inputName = newName;
        request.fillInitialNameAndOffset();
        
        applyRenameRefactoring(request, expectError);
        String refactored = doc.get();
        if(DEBUG){
            System.out.println(refactored);
        }
        if(!expectError){
            assertEquals(initialName, request.initialName); 
            assertEquals(StringUtils.format(strDoc, getSame("bb")),  refactored);
        }else{
            //cannot have changed
            assertEquals(StringUtils.format(strDoc, getSame(initialName)),  refactored);
        }
    }

    /**
     * Always checks for the 'default' test, which is:
     * - get the document and change all the occurrences of %s to 'aa'
     * - apply the refactor process
     * - check if all the %s occurrences are now 'bb'
     */
    protected void checkRename(String strDoc, int line, int col) throws CoreException {
        checkRename(strDoc, line, col, "aa", false);
    }


    protected Object[] getSame(String string) {
        ArrayList<Object> list = new ArrayList<Object>();
        for (int i = 0; i < 10; i++) {
            list.add(string);
        }
        return list.toArray();
    }



}