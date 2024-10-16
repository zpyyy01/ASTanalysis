import com.github.weisj.jsvg.S;
import com.intellij.psi.PsiAssignmentExpression;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AssignExpList {
    private final List<PsiAssignmentExpression> assignments;

    public AssignExpList() {
        this.assignments = new ArrayList<>();
    }

    public void addAssignment(PsiAssignmentExpression assignment) {
        assignments.add(assignment);
    }

    public List<PsiAssignmentExpression> getAssignments() {
        return assignments;
    }

    public void printAssignments() {
        for (PsiAssignmentExpression assignment : assignments) {
            System.out.println(assignment.getText());
        }
    }



    public void substituteAssignments() {

        AssignExpList newAssignments = new AssignExpList();
        //copy to newAssignments
        List<PsiAssignmentExpression> assignmentsCopy = new ArrayList<>(assignments);
        for (PsiAssignmentExpression assignment : assignmentsCopy) {
            newAssignments.addAssignment(assignment);
        }

        for (PsiAssignmentExpression assignment : newAssignments.getAssignments()) {
            System.out.println("substituteAssignments: " + assignment.getText());
            System.out.println("re= " + assignment.getRExpression().getText());
            PsiExpression rExpression = assignment.getRExpression();
            rExpression.accept(new JavaRecursiveElementVisitor() {
                @Override
                public void visitReferenceExpression(@NotNull PsiReferenceExpression expression) {

                    System.out.println("visitReferenceExpression: " + expression.getText());
                    for(PsiAssignmentExpression findLExpression: assignments){
                        if(findLExpression == assignment){
                            break;
                        }
                        System.out.println("findLExpression: " + findLExpression.getText());
                        //System.out.println("findLExpression.getLExpression(): " + findLExpression.getLExpression());
                        if(findLExpression.getLExpression().getText().equals(expression.getText())){
                            System.out.println("found: " + findLExpression.getText());
                            System.out.println("expression: " + expression.getText());
                            //add bracket to the expression
                            PsiElementFactory factory = JavaPsiFacade.getElementFactory(expression.getProject());
                            PsiExpression newExpression = factory.createExpressionFromText("(" + findLExpression.getRExpression().getText() + ")", expression);
                            //replace the expression
                            expression.replace(newExpression);
                            System.out.println("assignment: " + assignment.getText());
                            //print the whole tree structure of the assignment
                            PsiElement parent = expression.getParent();

                            break;
                        }
                    }
                    //super.visitReferenceExpression(expression);
                }
            });

        }
        System.out.println("New Assignments:");
        newAssignments.printAssignments();
    }
}