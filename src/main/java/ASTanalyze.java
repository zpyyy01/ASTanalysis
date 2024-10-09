import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.io.FileReader;
import java.io.IOException;

public class ASTanalyze extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);

        if (psiFile instanceof PsiJavaFile javaFile) {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                PsiClass[] classes = javaFile.getClasses();
                //System.out.println("Loaded configuration data: " + configData);
                for (PsiClass cls : classes) {
                    //System.out.println("Processing class: " + cls.getName());
                    AssignExpList assignExpList = new AssignExpList();
                    assignAnalyze(cls,psiFile, assignExpList);
                }
            });
            Messages.showInfoMessage("API call replacements complete!", "API Migration Tool");
        } else {
            Messages.showErrorDialog("Please select a Java file.", "API Migration Tool");
        }
    }

    private void assignAnalyze(PsiClass cls, PsiFile psiFile, AssignExpList assignExpList) {
        cls.accept(new JavaRecursiveElementVisitor() {
            //visit all assignment expressions
            @Override
            public void visitAssignmentExpression(@NotNull PsiAssignmentExpression expression) {
                super.visitAssignmentExpression(expression);
                assignExpList.addAssignment(expression);
                PsiExpression left = expression.getLExpression();
                PsiExpression right = expression.getRExpression();
                System.out.println(expression.getText());
                System.out.println("left: " + left.getText() + (right == null ? "" : ", right: " + right.getText()));
            }
        });

        assignExpList.substituteAssignments();
    }

}

