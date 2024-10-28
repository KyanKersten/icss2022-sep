package nl.han.ica.icss.transforms;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import org.checkerframework.checker.units.qual.A;

import javax.swing.text.Style;
import java.util.HashMap;

public class Evaluator implements Transform {

    private IHANLinkedList<HashMap<String, Literal>> variableValues;

    public Evaluator() {
        variableValues = new HANLinkedList<>();
        variableValues.addFirst(new HashMap<>());
    }

    @Override
    public void apply(AST ast) {
        applyStyleSheet(ast.root);
    }

    private void applyStyleSheet(Stylesheet node) {
        for (ASTNode child : node.getChildren()) {
            if (child instanceof Stylerule) {
                applyStyleRule((Stylerule) child);
            } else if (child instanceof VariableAssignment) {
                applyVariableAssignment((VariableAssignment) child);
            }
        }
    }

    private void applyVariableAssignment(VariableAssignment node) {
        Literal value = (Literal) evalExpression(node.expression);
        variableValues.getFirst().put(node.name.name, value);
    }

    private void applyStyleRule(Stylerule node) {
        for (ASTNode child: node.getChildren()){
            if (child instanceof Declaration){
                applyDeclaration((Declaration) child);
            }
        }
    }

    private void applyDeclaration(Declaration node) {
        node.expression = evalExpression(node.expression);
    }

    private Expression evalExpression(Expression expression) {
        if (expression instanceof VariableReference){
            VariableReference ref = (VariableReference) expression;
            Literal value = lookupVariableValue(ref.name);

            if (value != null){
                return value;
            } else {
                expression.setError("Variable " + ref.name + " not defined.");
            }
        } else if (expression instanceof Operation){
            return evalOperation(expression);
        }
        return expression;
    }

    private Expression evalOperation(Expression operation) {
        if (operation instanceof MultiplyOperation){
            return evalMultiplyOperation((MultiplyOperation) operation);
        } else if (operation instanceof AddOperation){
            return evalAddOperation((AddOperation) operation);
        } else if (operation instanceof SubtractOperation){
            return evalSubtractOperation((SubtractOperation) operation);
        }
        operation.setError("Unknown operation");
        return null;
    }

    private Literal evalMultiplyOperation(MultiplyOperation operation) {
        Literal lhsValue = (Literal) evalExpression(operation.lhs);
        Literal rhsValue = (Literal) evalExpression(operation.rhs);

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

    private Literal evalAddOperation(AddOperation operation) {
        Literal left = (Literal) evalExpression(operation.lhs);
        Literal right = (Literal) evalExpression(operation.rhs);

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

    private Literal evalSubtractOperation(SubtractOperation operation) {
        Literal lhsValue = (Literal) evalExpression(operation.lhs);
        Literal rhsValue = (Literal) evalExpression(operation.rhs);

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
