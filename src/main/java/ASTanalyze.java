import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
            Messages.showInfoMessage("Substitution done!", "AST Analyze Tool");
        } else {
            Messages.showErrorDialog("Please select a Java file.", "AST Analyze Tool");
        }
    }

    private void assignAnalyze(PsiClass cls, PsiFile psiFile, AssignExpList assignExpList) {
        Map<String, String> variableAssignments = new HashMap<>();
        cls.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitAssignmentExpression(@NotNull PsiAssignmentExpression expression) {
                super.visitAssignmentExpression(expression);
                PsiExpression left = expression.getLExpression();
                PsiExpression right = expression.getRExpression();

                if (left instanceof PsiReferenceExpression && right != null) {
                    String variableName = ((PsiReferenceExpression) left).getReferenceName();
                    variableAssignments.put(variableName, right.getText());
                }
            }
        });
        cls.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitReferenceExpression(@NotNull PsiReferenceExpression expression) {
                super.visitReferenceExpression(expression);
                String newText = replaceVariables(expression.getText(), variableAssignments);
                if (!newText.equals(expression.getText())) {
                    WriteAction.run(() -> {
                        PsiElement newElement = PsiElementFactory.getInstance(psiFile.getProject())
                                .createExpressionFromText(newText, null);
                        expression.replace(newElement);
                    });
                }5
            }
        });

    }
    private String replaceVariables(String expressionText, Map<String, String> variableAssignments) {
        for (Map.Entry<String, String> entry : variableAssignments.entrySet()) {
            expressionText = expressionText.replace(entry.getKey(), entry.getValue());
        }
        return expressionText;
    }
}

