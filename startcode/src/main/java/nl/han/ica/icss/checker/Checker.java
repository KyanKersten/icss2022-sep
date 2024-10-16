package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.HashMap;



public class Checker {

    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;

    public Checker() {
        variableTypes = new HANLinkedList<>();
        variableTypes.addFirst(new HashMap<>());
    }

    public void check(AST ast) {
        checkStyleSheet(ast.root);
    }

    private void checkStyleSheet(Stylesheet node) {
        for (ASTNode child : node.getChildren()){
            if (child instanceof Stylerule) {
                checkStylerule((Stylerule) child);
            } else if (child instanceof VariableAssignment){
                checkVariableAssignment((VariableAssignment) child);
            }
        }
    }

    private void checkVariableAssignment(VariableAssignment node) {
        ExpressionType expressionType = determineExpressionType(node.expression);

        if (expressionType != null) {
            variableTypes.getFirst().put(node.name.name, expressionType);
        } else {
            node.setError("Cannot assign an unknown type to variable " + node.name.name);
        }
    }

    private void checkVariableReference(VariableReference node) {
        if (!variableExists(node.name)){
            node.setError("Variable " + node.name + " is not defined");
        }
    }

    private void checkStylerule(Stylerule node) {
        for (ASTNode child : node.getChildren()) {
            if (child instanceof Declaration){
                checkDeclaration((Declaration) child);
            }
        }
    }

    private void checkDeclaration(Declaration node) {
        if (node.expression instanceof VariableReference){
            checkVariableReference((VariableReference) node.expression);
        } else if (node.property.name.equals("width")){
            if (!(node.expression instanceof PixelLiteral)){
                node.property.setError("Property 'width' has invalid type");
            }
        } else if (node.property.name.equals("color") | node.property.name.equals("background-color")){
            if (!(node.expression instanceof ColorLiteral)){
                node.setError("Property '" + node.property.name + "' has invalid type");
            }
        }
    }

    private boolean variableExists(String variableName) {
        for (int i = 0; i < variableTypes.getSize(); i++) {
            HashMap<String, ExpressionType> scope = variableTypes.get(i);
            if (scope.containsKey(variableName)) {
                return true;
            }
        }
        return false;
    }

    private ExpressionType determineExpressionType(Expression expression) {
        if (expression instanceof PixelLiteral) {
            return ExpressionType.PIXEL;
        } else if (expression instanceof ColorLiteral) {
            return ExpressionType.COLOR;
        } else if (expression instanceof BoolLiteral){
            return ExpressionType.BOOL;
        } else if (expression instanceof PercentageLiteral){
            return ExpressionType.PERCENTAGE;
        } else {
            return null;
        }
    }
}
