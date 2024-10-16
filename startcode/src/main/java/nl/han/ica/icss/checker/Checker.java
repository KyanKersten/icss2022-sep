package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.HashMap;



public class Checker {

    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;

    public void check(AST ast) {
        checkStyleSheet(ast.root);
        // variableTypes = new HANLinkedList<>();

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
        for (ASTNode child : node.getChildren()){
            if (child instanceof VariableReference){
                checkVariableReference((VariableReference) child);
            }

        }
    }

    private void checkVariableReference(VariableReference node) {
    }

    private void checkStylerule(Stylerule node) {
        for (ASTNode child : node.getChildren()) {
            if (child instanceof Declaration){
                checkDeclaration((Declaration) child);
            }
        }
    }

    private void checkDeclaration(Declaration node) {
        if (node.property.name.equals("width")){
            if (!(node.expression instanceof PixelLiteral)){
                node.setError("Property 'width' has invalid type");
            }
        } else if (node.property.name.equals("color")){
            if (!(node.expression instanceof ColorLiteral)){
                node.setError("Property 'color' has invalid type");
            }
        } else if (node.property.name.equals("background-color")){
            if (!(node.expression instanceof ColorLiteral)){
                node.setError("Property 'background-color' has invalid type");
            }
        }
    }
}
