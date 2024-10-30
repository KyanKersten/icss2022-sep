package nl.han.ica.icss.generator;


import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;

public class Generator {

	public String generate(AST ast) {
        return generateStylesheet((Stylesheet)ast.root);
	}

	private String generateStylesheet(Stylesheet node) {
		String result = "";
		for (ASTNode child : node.getChildren()) {
			if (child instanceof Stylerule) {
				result += generateStylerule((Stylerule) child) + "\n";
			}
		}
		return result.toString();
	}

	private String generateStylerule(Stylerule node) {
		String result = "";

		for (ASTNode child : node.selectors){
			result += child.toString() + " {\n";
		}
		for (ASTNode declaration : node.body){
			result += generateDeclaration((Declaration) declaration) + "\n";
		}

		result += "}\n";
		return result;
	}

	private String generateDeclaration(Declaration node) {
		return "  " + node.property.name + ": " + generateExpression(node.expression);
	}

	private String generateExpression(Expression node) {
		String result = "";

		if (node instanceof ColorLiteral) {
			result = ((ColorLiteral) node).value;
		} else if (node instanceof PixelLiteral) {
			result = ((PixelLiteral) node).value + "px";
		} else if (node instanceof PercentageLiteral) {
			result = ((PercentageLiteral) node).value + "%";
		} else if (node instanceof BoolLiteral) {
			result = String.valueOf(((BoolLiteral) node).value);
		}
		return result;
	}
}
