package pl.micdev.intellij.fluentsetter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.*;

import org.jetbrains.annotations.Nullable;

import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ToolbarDecorator;

public class SelectFieldsDialog extends DialogWrapper {

    private CollectionListModel<PsiField> fields;

    private LabeledComponent<JPanel> panel;

    public SelectFieldsDialog(PsiClass psiClass) {
        super(psiClass.getProject());
        setTitle("Select Fields");

        preparePanel(psiClass);

        init();
    }

    private void preparePanel(PsiClass psiClass) {
        fields = new CollectionListModel<PsiField>(findFieldsWithoutFluentSetters(psiClass));
        JList fieldList = new JList(fields);
        fieldList.setCellRenderer(new DefaultPsiElementCellRenderer());
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(fieldList);
        decorator.disableAddAction();
        JPanel panel = decorator.createPanel();
        this.panel = LabeledComponent.create(panel, "fields to generate setters for:");
    }

    private List<PsiField> findFieldsWithoutFluentSetters(PsiClass psiClass) {
        Set<String> methods = new HashSet<String>();
        for (PsiMethod psiMethod : psiClass.getMethods()) {
            methods.add(psiMethod.getName());
        }
        List<PsiField> fieldsWithoutSetters = new ArrayList<PsiField>();
        for (PsiField psiField : psiClass.getFields()) {
            if (!methods.contains(psiField.getName()))
                fieldsWithoutSetters.add(psiField);
        }
        return fieldsWithoutSetters;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return panel;
    }

    public List<PsiField> getSelectedFields() {
        return fields.getItems();
    }
}
