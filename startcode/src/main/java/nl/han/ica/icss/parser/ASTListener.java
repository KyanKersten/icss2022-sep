package nl.han.ica.icss.parser;


import com.sun.jdi.Value;
import nl.han.ica.datastructures.HANStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;
import org.antlr.v4.runtime.ParserRuleContext;

import javax.swing.text.html.HTML;
import java.awt.*;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends nl.han.ica.icss.parser.ICSSBaseListener {

	//Accumulator attributes:
	private AST ast;

	//Use this to keep track of the parent nodes when recursively traversing the ast
	private IHANStack<ASTNode> currentContainer;

	public ASTListener() {
		ast = new AST();
		currentContainer = new HANStack<>();
	}

	public AST getAST() {
		return ast;
	}

	@Override
	public void enterStylesheet(nl.han.ica.icss.parser.ICSSParser.StylesheetContext ctx) {
		Stylesheet stylesheet = new Stylesheet();
		currentContainer.push(stylesheet);
	}

	@Override
	public void exitStylesheet(nl.han.ica.icss.parser.ICSSParser.StylesheetContext ctx) {
		Stylesheet sheet = (Stylesheet) currentContainer.pop();
		ast.setRoot(sheet);
	}

	@Override
	public void enterStylerule(nl.han.ica.icss.parser.ICSSParser.StyleruleContext ctx) {
		Stylerule rule = new Stylerule();
		currentContainer.push(rule);
	}

	@Override
	public void exitStylerule(nl.han.ica.icss.parser.ICSSParser.StyleruleContext ctx) {
		Stylerule rule = (Stylerule) currentContainer.pop();
		currentContainer.peek().addChild(rule);
	}

	@Override
	public void enterTagSelector(nl.han.ica.icss.parser.ICSSParser.TagSelectorContext ctx) {
		TagSelector tagSelector = new TagSelector(ctx.getText());
		currentContainer.push(tagSelector);
	}

	@Override
	public void exitTagSelector(nl.han.ica.icss.parser.ICSSParser.TagSelectorContext ctx) {
		TagSelector tagSelector = (TagSelector) currentContainer.pop();
		currentContainer.peek().addChild(tagSelector);
	}

	@Override
	public void enterIdSelector(nl.han.ica.icss.parser.ICSSParser.IdSelectorContext ctx) {
		IdSelector idSelector = new IdSelector(ctx.getText());
		currentContainer.push(idSelector);
	}

	@Override
	public void exitIdSelector(nl.han.ica.icss.parser.ICSSParser.IdSelectorContext ctx) {
		IdSelector idSelector = (IdSelector) currentContainer.pop();
		currentContainer.peek().addChild(idSelector);
	}

	@Override
	public void enterClassSelector(nl.han.ica.icss.parser.ICSSParser.ClassSelectorContext ctx) {
		ClassSelector classSelector = new ClassSelector(ctx.getText());
		currentContainer.push(classSelector);
	}

	@Override
	public void exitClassSelector(nl.han.ica.icss.parser.ICSSParser.ClassSelectorContext ctx) {
		ClassSelector classSelector = (ClassSelector) currentContainer.pop();
		currentContainer.peek().addChild(classSelector);
	}

	@Override
	public void enterDeclaration(nl.han.ica.icss.parser.ICSSParser.DeclarationContext ctx) {
		Declaration declaration = new Declaration();
		currentContainer.push(declaration);
	}

	@Override
	public void exitDeclaration(nl.han.ica.icss.parser.ICSSParser.DeclarationContext ctx) {
		Declaration declaration = (Declaration) currentContainer.pop();
		currentContainer.peek().addChild(declaration);
	}

	@Override
	public void enterProperty(nl.han.ica.icss.parser.ICSSParser.PropertyContext ctx) {
		PropertyName propertyName = new PropertyName();
		propertyName.name = ctx.getText();
		currentContainer.push(propertyName);
	}

	@Override
	public void exitProperty(nl.han.ica.icss.parser.ICSSParser.PropertyContext ctx) {
		PropertyName propertyName = (PropertyName) currentContainer.pop();
		currentContainer.peek().addChild(propertyName);
	}

	@Override
	public void enterColor(nl.han.ica.icss.parser.ICSSParser.ColorContext ctx) {
		Expression expression = new ColorLiteral(ctx.getText());
		currentContainer.push(expression);
	}

	@Override
	public void exitColor(nl.han.ica.icss.parser.ICSSParser.ColorContext ctx) {
		Expression expression = (Expression) currentContainer.pop();
		currentContainer.peek().addChild(expression);
	}

	@Override
	public void enterPixelSize(nl.han.ica.icss.parser.ICSSParser.PixelSizeContext ctx) {
		Expression expression = new PixelLiteral(ctx.getText());
		currentContainer.push(expression);
	}

	@Override
	public void exitPixelSize(nl.han.ica.icss.parser.ICSSParser.PixelSizeContext ctx) {
		Expression expression = (Expression) currentContainer.pop();
		currentContainer.peek().addChild(expression);
	}

	@Override
	public void enterPercentage(nl.han.ica.icss.parser.ICSSParser.PercentageContext ctx) {
		Expression expression = new PercentageLiteral(ctx.getText());
		currentContainer.push(expression);
	}

	@Override
	public void exitPercentage(nl.han.ica.icss.parser.ICSSParser.PercentageContext ctx) {
		Expression expression = (Expression) currentContainer.pop();
		currentContainer.peek().addChild(expression);
	}

	@Override
	public void enterTrueBoolean(nl.han.ica.icss.parser.ICSSParser.TrueBooleanContext ctx) {
		Expression expression = new BoolLiteral(ctx.getText());
		currentContainer.push(expression);
	}

	@Override
	public void exitTrueBoolean(nl.han.ica.icss.parser.ICSSParser.TrueBooleanContext ctx) {
		Expression expression = (Expression) currentContainer.pop();
		currentContainer.peek().addChild(expression);
	}

	@Override
	public void enterFalseBoolean(nl.han.ica.icss.parser.ICSSParser.FalseBooleanContext ctx) {
		Expression expression = new BoolLiteral(ctx.getText());
		currentContainer.push(expression);
	}

	@Override
	public void exitFalseBoolean(nl.han.ica.icss.parser.ICSSParser.FalseBooleanContext ctx) {
		Expression expression = (Expression) currentContainer.pop();
		currentContainer.peek().addChild(expression);
	}

	@Override
	public void enterVariableAssignment(nl.han.ica.icss.parser.ICSSParser.VariableAssignmentContext ctx) {
		VariableAssignment variableAssignment = new VariableAssignment();
		currentContainer.push(variableAssignment);
	}

	@Override
	public void exitVariableAssignment(nl.han.ica.icss.parser.ICSSParser.VariableAssignmentContext ctx) {
		VariableAssignment variableAssignment = (VariableAssignment) currentContainer.pop();
		currentContainer.peek().addChild(variableAssignment);
	}

	@Override
	public void enterVariable(nl.han. ica.icss.parser.ICSSParser.VariableContext ctx) {
		VariableReference variableReference = new VariableReference(ctx.getText());
		currentContainer.push(variableReference);
	}

	@Override
	public void exitVariable(nl.han.ica.icss.parser.ICSSParser.VariableContext ctx) {
		VariableReference variableReference = (VariableReference) currentContainer.pop();
		currentContainer.peek().addChild(variableReference);
	}

	@Override
	public void enterScalar(nl.han.ica.icss.parser.ICSSParser.ScalarContext ctx) {
		ScalarLiteral scalarLiteral = new ScalarLiteral(ctx.getText());
		currentContainer.push(scalarLiteral);
	}

	@Override
	public void exitScalar(nl.han.ica.icss.parser.ICSSParser.ScalarContext ctx) {
		ScalarLiteral scalarLiteral = (ScalarLiteral) currentContainer.pop();
		currentContainer.peek().addChild(scalarLiteral);
	}

	@Override
	public void enterAddExpression(nl.han.ica.icss.parser.ICSSParser.AddExpressionContext ctx) {
		AddOperation addOperation = new AddOperation();
		currentContainer.push(addOperation);
	}

	@Override
	public void exitAddExpression(nl.han.ica.icss.parser.ICSSParser.AddExpressionContext ctx) {
		AddOperation addOperation = (AddOperation) currentContainer.pop();
		currentContainer.peek().addChild(addOperation);
	}

	@Override
	public void enterSubtractExpression(nl.han.ica.icss.parser.ICSSParser.SubtractExpressionContext ctx) {
		SubtractOperation subtractOperation = new SubtractOperation();
		currentContainer.push(subtractOperation);
	}

	@Override
	public void exitSubtractExpression(nl.han.ica.icss.parser.ICSSParser.SubtractExpressionContext ctx) {
		SubtractOperation subtractOperation = (SubtractOperation) currentContainer.pop();
		currentContainer.peek().addChild(subtractOperation);
	}

	@Override
	public void enterMultiplyExpression(nl.han.ica.icss.parser.ICSSParser.MultiplyExpressionContext ctx) {
		MultiplyOperation multiplyOperation = new MultiplyOperation();
		currentContainer.push(multiplyOperation);
	}

	@Override
	public void exitMultiplyExpression(nl.han.ica.icss.parser.ICSSParser.MultiplyExpressionContext ctx) {
		MultiplyOperation multiplyOperation = (MultiplyOperation) currentContainer.pop();
		currentContainer.peek().addChild(multiplyOperation);
	}
}