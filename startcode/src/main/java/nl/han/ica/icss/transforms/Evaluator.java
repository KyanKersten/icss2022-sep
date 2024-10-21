package nl.han.ica.icss.transforms;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
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
        }
        return expression;
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
