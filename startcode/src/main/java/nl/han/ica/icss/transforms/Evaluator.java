package nl.han.ica.icss.transforms;

import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import org.checkerframework.checker.units.qual.A;

import java.util.HashMap;

public class Evaluator implements Transform {

    private IHANLinkedList<HashMap<String, Literal>> variableValues;

    public Evaluator() {
        //variableValues = new HANLinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        //variableValues = new HANLinkedList<>();
        applyStyleSheet((Stylesheet)ast.root);
    }

    private void applyStyleSheet(Stylesheet node) {
            applyStyleRule((Stylerule) node.getChildren().get(0));
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
        return expression;
    }
}
