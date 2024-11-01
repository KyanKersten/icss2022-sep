package nl.han.ica.icss.transforms;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Evaluator implements Transform {

    private IHANLinkedList<HashMap<String, Literal>> variableValues;

    public Evaluator() {
        variableValues = new HANLinkedList<>();
        variableValues.addFirst(new HashMap<>()); // Add a global scope
    }

    @Override
    public void apply(AST ast) {
        applyStyleSheet(ast.root);
    }

    private void applyStyleSheet(Stylesheet node) {
        List<ASTNode> toRemove = new ArrayList<>(); // List of nodes to remove, we don't want to see variable assignments in the final stylesheet

        this.variableValues.addFirst(new HashMap<>()); // Add a new scope for the stylesheet

        for (ASTNode child : node.getChildren()) {
            if (child instanceof Stylerule) {
                applyStyleRule((Stylerule) child);
            } else if (child instanceof VariableAssignment) {
                this.applyVariableAssignment((VariableAssignment) child);
                toRemove.add(child); // Add to remove list
            }
        }

        this.variableValues.removeFirst(); // Remove the scope for the stylesheet
        toRemove.forEach(node::removeChild); // Remove all variable assignments from the stylesheet
    }

    private void applyVariableAssignment(VariableAssignment node) {
        Literal value = (Literal) applyExpression(node.expression); // Evaluate the expression
        variableValues.getFirst().put(node.name.name, value); // Add the variable to the current scope
    }

    private void applyStyleRule(Stylerule node) {
        ArrayList<ASTNode> toAdd = new ArrayList<>(); // List of nodes to add, we don't want to see if clauses in the final stylesheet

        variableValues.addFirst(new HashMap<>()); // Add a new scope for the style rule

        for (ASTNode child: node.body){
            applyRuleBody(child, toAdd); // Apply the rule body
        }

        this.variableValues.removeFirst(); // Remove the scope for the style rule
        node.body = toAdd; // Replace the body with the new body
    }

    private void applyRuleBody(ASTNode node, ArrayList<ASTNode> parentBody) {
        if (node instanceof Declaration) {
            applyDeclaration((Declaration) node);
            parentBody.add(node); // Add the declaration to the parent body
        }

        if (node instanceof VariableAssignment) {
            applyVariableAssignment((VariableAssignment) node);
        }

        // TR02: Evalueer if/else expressies. Schrijf een transformatie in Evaluator die alle IfClauses uit de AST verwijdert. Wanneer de conditie van de IfClause TRUE is wordt deze vervangen door de body van het if-statement. Als de conditie FALSE is dan vervang je de IfClause door de body van de ElseClause. Als er geen ElseClause is bij een negatieve conditie dan verwijder je de IfClause volledig uit de AST.
        if (node instanceof IfClause) {
            IfClause ifClause = (IfClause) node;
            ifClause.conditionalExpression = applyExpression(ifClause.conditionalExpression);

            BoolLiteral condition = (BoolLiteral) ifClause.conditionalExpression;

            if (condition.value){
                if (ifClause.elseClause != null){
                    ifClause.elseClause.body = new ArrayList<>(); // Clear else clause if condition is true
                }
            } else {
                if (ifClause.elseClause == null){
                    ifClause.body = new ArrayList<>(); // Clear body if condition is false and there is no else clause
                } else {
                    ifClause.body = ifClause.elseClause.body;
                    ifClause.elseClause.body = new ArrayList<>(); // Clear else clause if condition is false
                }
            }

            this.applyIfClause(node, parentBody);
        }
    }

    private void applyIfClause(ASTNode node, ArrayList<ASTNode> parentBody) {
        for (ASTNode child : node.getChildren()) {
            applyRuleBody(child, parentBody);
        }
    }

    private void applyDeclaration(Declaration node) {
        node.expression = applyExpression(node.expression);
    }

    private Expression applyExpression(Expression expression) {
        if (expression instanceof VariableReference){
            VariableReference ref = (VariableReference) expression;
            Literal value = lookupVariableValue(ref.name);

            if (value != null){
                return value;
            } else {
                expression.setError("Variable " + ref.name + " not defined.");
            }
        } else if (expression instanceof Operation){
            return applyOperation(expression);
        }
        return expression;
    }

    // TR01: Evalueer expressies. Schrijf een transformatie in Evaluator die alle Expression knopen in de AST door een Literal knoop met de berekende waarde vervangt.
    private Expression applyOperation(Expression operation) {
        if (operation instanceof MultiplyOperation){
            return applyMultiplyOperation((MultiplyOperation) operation);
        } else if (operation instanceof AddOperation){
            return applyAddOperation((AddOperation) operation);
        } else if (operation instanceof SubtractOperation){
            return applySubtractOperation((SubtractOperation) operation);
        }
        operation.setError("Unknown operation");

        return null;
    }

    private Literal applyMultiplyOperation(MultiplyOperation operation) {
        Literal lhsValue = (Literal) applyExpression(operation.lhs);
        Literal rhsValue = (Literal) applyExpression(operation.rhs);

        if (lhsValue instanceof ScalarLiteral && rhsValue instanceof PixelLiteral) {
            int result = ((ScalarLiteral) lhsValue).value * ((PixelLiteral) rhsValue).value;
            return new PixelLiteral(result + "px");
        } else if (lhsValue instanceof PixelLiteral && rhsValue instanceof ScalarLiteral) {
            int result = ((PixelLiteral) lhsValue).value * ((ScalarLiteral) rhsValue).value;
            return new PixelLiteral(result + "px");
        } else if (lhsValue instanceof ScalarLiteral && rhsValue instanceof PercentageLiteral) {
            int result = ((ScalarLiteral) lhsValue).value * ((PercentageLiteral) rhsValue).value;
            return new PercentageLiteral(result + "%");
        } else if (lhsValue instanceof PercentageLiteral && rhsValue instanceof ScalarLiteral) {
            int result = ((PercentageLiteral) lhsValue).value * ((ScalarLiteral) rhsValue).value;
            return new PercentageLiteral(result + "%");
        }

        return null;
    }

    private Literal applyAddOperation(AddOperation operation) {
        Literal left = (Literal) applyExpression(operation.lhs);
        Literal right = (Literal) applyExpression(operation.rhs);

        if (left instanceof PixelLiteral && right instanceof PixelLiteral){
            int result = ((PixelLiteral) left).value + ((PixelLiteral) right).value;
            return new PixelLiteral(result + "px");
        } else if (left instanceof ScalarLiteral && right instanceof ScalarLiteral){
            int result = ((ScalarLiteral) left).value + ((ScalarLiteral) right).value;
            return new ScalarLiteral(result);
        } else if (left instanceof PercentageLiteral && right instanceof PercentageLiteral){
            int result = ((PercentageLiteral) left).value + ((PercentageLiteral) right).value;
            return new PercentageLiteral(result + "%");
        }

        return null;
    }

    private Literal applySubtractOperation(SubtractOperation operation) {
        Literal lhsValue = (Literal) applyExpression(operation.lhs);
        Literal rhsValue = (Literal) applyExpression(operation.rhs);

        if (lhsValue instanceof PixelLiteral && rhsValue instanceof PixelLiteral) {
            int result = ((PixelLiteral) lhsValue).value - ((PixelLiteral) rhsValue).value;
            return new PixelLiteral(result + "px");
        } else if (lhsValue instanceof ScalarLiteral && rhsValue instanceof ScalarLiteral) {
            int result = ((ScalarLiteral) lhsValue).value - ((ScalarLiteral) rhsValue).value;
            return new ScalarLiteral(result);
        } else if (lhsValue instanceof PercentageLiteral && rhsValue instanceof PercentageLiteral) {
            int result = ((PercentageLiteral) lhsValue).value - ((PercentageLiteral) rhsValue).value;
            return new PercentageLiteral(result + "%");
        }

        return null;
    }

    private Literal lookupVariableValue(String variableName) {
        for (int i = 0; i < variableValues.getSize(); i++) {
            HashMap<String, Literal> currentScope = variableValues.get(i);
            if (currentScope.containsKey(variableName)) {
                return currentScope.get(variableName);
            }
        }
        return null;
    }
}
