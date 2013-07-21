package pl.micdev.intellij.fluentsetter;

import java.util.List;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;

public class GenerateAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiClass psiClass = getPsiClass(e);
        SelectFieldsDialog dialog = new SelectFieldsDialog(psiClass);
        dialog.show();
        if (dialog.isOK()) {
            generateSetters(psiClass, dialog.getSelectedFields());
        }
    }

    private void generateSetters(final PsiClass psiClass, final List<PsiField> fields) {
        new WriteCommandAction.Simple(psiClass.getProject(), psiClass.getContainingFile()) {
            @Override
            protected void run() throws Throwable {
                for (PsiField field : fields) {
                    PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(getProject());
                    PsiMethod setter = elementFactory.createMethodFromText(generateSetterText(field, psiClass), psiClass);
                    psiClass.add(setter);
                }
            }
        }.execute();
    }

    private String generateSetterText(PsiField field, PsiClass psiClass) {
        StringBuilder setterBuilder = new StringBuilder();
        setterBuilder.append("public ").append(psiClass.getName()).append(" ").append(field.getName())
                .append('(').append(field.getType().getPresentableText()).append(' ').append(field.getName()).append(") {\n");
        setterBuilder.append("this.").append(field.getName()).append(" = ").append(field.getName()).append(";\n");
        setterBuilder.append("return this;\n");
        setterBuilder.append("}\n");
        return setterBuilder.toString();
    }

    @Override
    public void update(AnActionEvent e) {
        PsiClass psiClass = getPsiClass(e);
        e.getPresentation().setEnabled(psiClass != null);
    }

    private PsiClass getPsiClass(AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null && psiFile == null) {
            return null;
        }
        int offset = editor.getCaretModel().getOffset();
        PsiElement psiElement = psiFile.findElementAt(offset);
        return PsiTreeUtil.getParentOfType(psiElement, PsiClass.class);
    }

}
