package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.ArrayList;
import java.util.HashMap;

public class Checker {
    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;

    public Checker() {
        variableTypes = new HANLinkedList<>();
        variableTypes.addFirst(new HashMap<>()); // Add global scope
    }

    public void check(AST ast) {
        checkStyleSheet(ast.root);
    }

    private void checkStyleSheet(Stylesheet node) {
        for (ASTNode child : node.getChildren()) {
            if (child instanceof Stylerule) {
                enterScope();
                checkStylerule((Stylerule) child);
                exitScope();
            } else if (child instanceof VariableAssignment) {
                checkVariableAssignment((VariableAssignment) child);
            }
        }
    }

    private void checkVariableAssignment(VariableAssignment node) {
        String variableName = node.name.name;
        ExpressionType expressionType = determineExpressionType(node.expression);

        variableTypes.getFirst().put(variableName, expressionType);

        checkExpression(node.expression);
    }

    private void checkExpression(Expression expression) {
        if (expression instanceof VariableReference) {
            checkVariableReference((VariableReference) expression);
        } else if (expression instanceof Operation) {
            checkOperation((Operation) expression);
        }
    }

    private void checkVariableReference(VariableReference node) {
        String variableName = node.name;

        // CH01: Controleer of er geen variabelen worden gebruikt die niet gedefinieerd zijn.
        // CH06: Controleer of variabelen enkel binnen hun scope gebruikt worden
        if (!variableExistsInScope(variableName)) {
            node.setError("Variable '" + variableName + "' is not defined in the current scope");
        }
    }

    private void checkStylerule(Stylerule node) {
        this.checkRuleBody(node.body);
    }

    private void checkIfClause(IfClause child) {
        ExpressionType expressionType = determineExpressionType(child.conditionalExpression);

        // CH05: Controleer of de conditie van een if-clause een boolean is.
        if (expressionType != ExpressionType.BOOL) {
            child.conditionalExpression.setError("If clause condition must be of type bool");
        }

        for (ASTNode bodyChild : child.body) {
            if (bodyChild instanceof Declaration) {
                checkDeclaration((Declaration) bodyChild);
            } else if (bodyChild instanceof VariableAssignment) {
                checkVariableAssignment((VariableAssignment) bodyChild);
            } else if (bodyChild instanceof IfClause) {
                checkIfClause((IfClause) bodyChild);
            }
        }

        if (child.elseClause != null){
            checkElseClause(child.elseClause);
        }
    }

    private void checkElseClause(ElseClause elseClause) {
        for (ASTNode child : elseClause.getChildren()) {
            if (child instanceof Declaration) {
                checkDeclaration((Declaration) child);
            } else if (child instanceof VariableAssignment) {
                checkVariableAssignment((VariableAssignment) child);
            }
        }
    }

    private void checkDeclaration(Declaration node) {
        ExpressionType expressionType = determineExpressionType(node.expression);

        if (node.expression instanceof VariableReference){
            checkVariableReference((VariableReference) node.expression);
            if (node.expression.hasError()) { // If variable reference has error return to not display multiple errors
                return;
            }
        }

        // CH04 - Controleer of bij declaraties het type van de value klopt met de property.
        switch (node.property.name){
        case "width":
        case "height":
        if (expressionType != ExpressionType.PIXEL && expressionType != ExpressionType.PERCENTAGE) {
            node.setError("Property '" + node.property.name + "' can only be of type pixel or percentage literal");
        }
        break;

        case "color":
        case "background-color":
        if (expressionType != ExpressionType.COLOR) {
            node.setError("Property '" + node.property.name + "' can only be of type color literal");
        }
        break;

        default:
        node.setError("Property " + node.property.name + " is not a valid property");
        break;
        }
    }

    private void checkRuleBody(ArrayList<ASTNode> body){
        for (ASTNode node : body){
            if (node instanceof Declaration){
                checkDeclaration((Declaration) node);
            } else if (node instanceof VariableAssignment){
                checkVariableAssignment((VariableAssignment) node);
            } else if (node instanceof IfClause){
                enterScope();
                checkIfClause((IfClause) node);
                exitScope();
            }
        }
    }

    private void checkOperation(Operation operation) {
        if (operation instanceof MultiplyOperation) {
            checkMultiplyOperation((MultiplyOperation) operation);
        } else if (operation instanceof AddOperation) {
            checkAddOperation((AddOperation) operation);
        } else if (operation instanceof SubtractOperation) {
            checkSubtractOperation((SubtractOperation) operation);
        }
    }

    private void checkMultiplyOperation(MultiplyOperation operation) {
            ExpressionType lhsType = determineExpressionType(operation.lhs);
            ExpressionType rhsType = determineExpressionType(operation.rhs);

            // CH03: Controleer of er geen kleuren worden gebruikt in operaties (plus, min en keer).
            if (lhsType == ExpressionType.COLOR || rhsType == ExpressionType.COLOR){
                operation.setError("Color may not be used in an multiply operation");
                return;
            }

            // CH02: Controleer dat bij vermenigvuldigen minimaal een operand een scalaire waarde is.
            if (lhsType != ExpressionType.SCALAR && rhsType != ExpressionType.SCALAR) {
                operation.setError("Multiply operation has invalid operands");
            }
        }

    private void checkSubtractOperation(SubtractOperation operation) {
            ExpressionType lhsType = determineExpressionType(operation.lhs);
            ExpressionType rhsType = determineExpressionType(operation.rhs);

            // CH03: Controleer of er geen kleuren worden gebruikt in operaties (plus, min en keer).
            if (lhsType == ExpressionType.COLOR || rhsType == ExpressionType.COLOR){
                operation.setError("Color may not be used in an subtract operation");
                return;
            }

            // CH02: Controleer of de operanden van de operaties plus en min van gelijk type zijn.
            if (lhsType != rhsType) {
                operation.setError("Subtract operation has invalid operands");
            }
        }

    private void checkAddOperation(AddOperation operation) {
            if (operation.lhs instanceof Operation) {
                checkOperation((Operation) operation.lhs);
            } else if (operation.rhs instanceof Operation) {
                checkOperation((Operation) operation.rhs);
            }

            if (operation.lhs instanceof VariableReference) {
                checkVariableReference((VariableReference) operation.lhs);
            } else if (operation.rhs instanceof VariableReference) {
                checkVariableReference((VariableReference) operation.rhs);
            }

            ExpressionType lhsType = determineExpressionType(operation.lhs);
            ExpressionType rhsType = determineExpressionType(operation.rhs);

            // CH03: Controleer of er geen kleuren worden gebruikt in operaties (plus, min en keer).
            if (lhsType == ExpressionType.COLOR || rhsType == ExpressionType.COLOR){
                operation.setError("Color may not be used in an add operation");
                return;
            }

           // CH02: Controleer of de operanden van de operaties plus en min van gelijk type zijn.
            if (lhsType != rhsType) {
                operation.setError("Can not add " + lhsType + " to " + rhsType + "");
            }
        }

    private boolean variableExistsInScope(String variableName) {
        for (int i = 0; i < variableTypes.getSize(); i++) {
            HashMap<String, ExpressionType> scope = variableTypes.get(i); // Get scope
            if (scope.containsKey(variableName)) { // If variable is found in scope
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
        } else if (expression instanceof BoolLiteral) {
            return ExpressionType.BOOL;
        } else if (expression instanceof PercentageLiteral) {
            return ExpressionType.PERCENTAGE;
        } else if (expression instanceof ScalarLiteral) {
            return ExpressionType.SCALAR;
        } else if (expression instanceof VariableReference) {
            VariableReference ref = new VariableReference(((VariableReference) expression).name);
            return getVariableType(ref.name);
        } else if (expression instanceof Operation) {
            return determineOperationType((Operation) expression);
        }
        return null;
    }

    private ExpressionType determineOperationType(Operation operation){
        ExpressionType lhsType = determineExpressionType(operation.lhs);
        ExpressionType rhsType = determineExpressionType(operation.rhs);

        if (operation instanceof MultiplyOperation) {
            if (lhsType == ExpressionType.SCALAR) { // If left side is scalar
                return rhsType;
            } else if (rhsType == ExpressionType.SCALAR) { // If right side is scalar
                return lhsType;
            }
        } else if (operation instanceof AddOperation || operation instanceof SubtractOperation) {
            if (lhsType == rhsType) { // If left side is equal to right side
                return lhsType;
            }
        }
        return null;
    }

    private ExpressionType getVariableType(String variableName) {
        for (int i = 0; i < variableTypes.getSize(); i++) {
            HashMap<String, ExpressionType> scope = variableTypes.get(i); // Get scope
            if (scope.containsKey(variableName)) { // If variable is found in scope
                return scope.get(variableName);
            }
        }
        return null;
    }

    private void enterScope() {
        variableTypes.addFirst(new HashMap<>()); // Add new scope
    }

    private void exitScope() {
        variableTypes.removeFirst(); // Remove current scope
    }
}
