package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
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
        for (ASTNode child : node.getChildren()) {
            if (child instanceof Stylerule) {
                checkStylerule((Stylerule) child);
            } else if (child instanceof VariableAssignment) {
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
        if (!variableExists(node.name)) {
            node.setError("Variable " + node.name + " is not defined");
        }
    }

    private void checkStylerule(Stylerule node) {
        for (ASTNode child : node.getChildren()) {
            if (child instanceof Declaration) {
                checkDeclaration((Declaration) child);
            }
        }
    }

    private void checkDeclaration(Declaration node) {
        if (node.expression instanceof VariableReference) {
            checkVariableReference((VariableReference) node.expression);
        } else if (node.expression instanceof Operation) {
            checkOperation((Operation) node.expression);
        } else if (node.property.name.equals("width")) {
            if (!(node.expression instanceof PixelLiteral)) {
                node.property.setError("Property 'width' has invalid type");
            }
        } else if (node.property.name.equals("color") | node.property.name.equals("background-color")) {
            if (!(node.expression instanceof ColorLiteral)) {
                node.setError("Property '" + node.property.name + "' has invalid type");
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
        if (operation.lhs == null || operation.rhs == null) {
            operation.setError("Multiply operation has invalid operands");
        } else {
            ExpressionType lhsType = determineExpressionType(operation.lhs);
            ExpressionType rhsType = determineExpressionType(operation.rhs);

            if (lhsType != ExpressionType.SCALAR && rhsType != ExpressionType.SCALAR) {
                operation.setError("Multiply operation has invalid operands");
            }
        }
    }

    private void checkSubtractOperation(SubtractOperation operation) {
        if (operation.lhs == null || operation.rhs == null) {
            operation.setError("Subtract operation has invalid operands");
        } else {
            ExpressionType lhsType = determineExpressionType(operation.lhs);
            ExpressionType rhsType = determineExpressionType(operation.rhs);

            if (lhsType != rhsType) {
                operation.setError("Subtract operation has invalid operands");
            }
        }
    }

    private void checkAddOperation(AddOperation operation) {
        if (operation.lhs == null || operation.rhs == null) {
            System.out.println(operation.lhs + " " + operation.rhs);
        } else {
            if (operation.lhs instanceof Operation) {
                checkOperation((Operation) operation.lhs);
            } else if (operation.rhs instanceof Operation) {
                checkOperation((Operation) operation.rhs);
            }

            ExpressionType lhsType = determineExpressionType(operation.lhs);
            ExpressionType rhsType = determineExpressionType(operation.rhs);

            if (lhsType != rhsType) {
                operation.setError("Add operation has invalid operands");
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
        } else {
            return null;
        }
    }

    private ExpressionType determineOperationType(Operation operation){
        ExpressionType lhsType = determineExpressionType(operation.lhs);
        ExpressionType rhsType = determineExpressionType(operation.rhs);

        if (operation instanceof MultiplyOperation) {
            if (lhsType == ExpressionType.SCALAR) {
                return rhsType;
            } else if (rhsType == ExpressionType.SCALAR) {
                return lhsType;
            }
        } else if (operation instanceof AddOperation || operation instanceof SubtractOperation) {
            if (lhsType == rhsType) {
                return lhsType;
            }
        }
        return null;
    }

    private ExpressionType getVariableType(String variableName) {
        for (int i = 0; i < variableTypes.getSize(); i++) {
            HashMap<String, ExpressionType> scope = variableTypes.get(i);
            if (scope.containsKey(variableName)) {
                return scope.get(variableName);
            }
        }
        return null;
    }
}
