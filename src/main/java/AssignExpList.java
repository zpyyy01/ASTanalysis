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
        for(PsiAssignmentExpression assignment: assignments){
            newAssignments.addAssignment(assignment);
        }
        for (PsiAssignmentExpression assignment : newAssignments.getAssignments()) {
            System.out.println("substituteAssignments: " + assignment.getText());
            assignment.getRExpression().accept(new JavaRecursiveElementVisitor() {
                @Override
                public void visitReferenceExpression(@NotNull PsiReferenceExpression expression) {
                    super.visitReferenceExpression(expression);
                    System.out.println("visitReferenceExpression: " + expression.getText());
                    for(PsiAssignmentExpression findLExpression: assignments){
                        if(findLExpression == assignment){
                            continue;
                        }
                        System.out.println("findLExpression: " + findLExpression.getText());
                        System.out.println("findLExpression.getLExpression(): " + findLExpression.getLExpression());
                        if(findLExpression.getLExpression().getText().equals(expression.getText())){
                            System.out.println("found: " + findLExpression.getText());
                            expression.replace(Objects.requireNonNull(findLExpression.getRExpression()));
                        }
                    }
                }
            });

        }
        System.out.println("New Assignments:");
        newAssignments.printAssignments();
    }
}