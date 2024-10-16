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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
                    Map<String, String> variableAssignments = new HashMap<>();
                    dataCollection(cls,psiFile, assignExpList, variableAssignments);
                    assignSubstitution(cls,psiFile, assignExpList, variableAssignments);
                }
            });
            Messages.showInfoMessage("Substitution done!", "AST Analyze Tool");
        } else {
            Messages.showErrorDialog("Please select a Java file.", "AST Analyze Tool");
        }
    }

    private void dataCollection(PsiClass cls, PsiFile psiFile, AssignExpList assignExpList, Map<String, String> variableAssignments) {

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
    }
    private void assignSubstitution(PsiClass cls, PsiFile psiFile, AssignExpList assignExpList, Map<String, String> variableAssignments) {
        //add a string list to store the Substituted expression
        List<String> SubstitutedExpression = new ArrayList<>();
        cls.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitAssignmentExpression(@NotNull PsiAssignmentExpression expression) {
                super.visitAssignmentExpression(expression);
                PsiExpression left = expression.getLExpression();
                PsiExpression right = expression.getRExpression();

                if (left instanceof PsiReferenceExpression && right != null) {
                    String variableName = ((PsiReferenceExpression) left).getReferenceName();
                    String expressionText = right.getText();
                    expressionText = replaceVariables(expressionText, variableAssignments);
                    SubstitutedExpression.add(variableName + " = " + expressionText);
                }
            }
        });
        Messages.showInfoMessage("Substituted Expression: " + SubstitutedExpression, "AST Analyze Tool");
    }
    private String replaceVariables(String expressionText, Map<String, String> variableAssignments) {
        for (Map.Entry<String, String> entry : variableAssignments.entrySet()) {
            expressionText = expressionText.replace(entry.getKey(), entry.getValue());
        }
        return expressionText;
    }
}

