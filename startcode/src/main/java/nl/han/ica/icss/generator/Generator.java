package nl.han.ica.icss.generator;


import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;

public class Generator {
	// GE01: Implementeer de generator in nl.han.ica.icss.generator.Generator die de AST naar een CSS2-compliant string omzet
	public String generate(AST ast) {
        return generateStylesheet(ast.root);
	}

	private String generateStylesheet(Stylesheet node) {
		StringBuilder result = new StringBuilder();
		for (ASTNode child : node.getChildren()) {
			if (child instanceof Stylerule) {
				result.append(generateStylerule((Stylerule) child)).append("\n"); // Voor iedere stylerule een nieuwe regel
			}
		}
		return result.toString();
	}

	private String generateStylerule(Stylerule node) {
		StringBuilder result = new StringBuilder();

		for (ASTNode child : node.selectors){
			result.append(child.toString()).append(" {\n"); // Voor iedere selector een nieuwe regel
		}
		for (ASTNode declaration : node.body){
			result.append(generateDeclaration((Declaration) declaration)).append("\n"); // Voor iedere declaration een nieuwe regel
		}

		result.append("}\n"); // Sluit de stylerule af
		return result.toString();
	}

	private String generateDeclaration(Declaration node) {
		// GE02: Zorg dat de CSS met twee spaties inspringing per scopeniveau gegenereerd wordt.
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
