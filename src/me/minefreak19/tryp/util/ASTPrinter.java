package me.minefreak19.tryp.util;

import me.minefreak19.tryp.tree.Expr;

import java.util.Arrays;

public class ASTPrinter implements Expr.Visitor<String> {

	public String print(Expr expr) {
		return expr.accept(this);
	}

	@Override
	public String visitAssignExpr(Expr.Assign assign) {
		return parenthesize("= " + assign.name.getText(), assign.value);
	}

	@Override
	public String visitBinaryExpr(Expr.Binary binary) {
		return parenthesize(binary.operator.getText(), binary.left, binary.right);
	}

	@Override
	public String visitCallExpr(Expr.Call expr) {
		return parenthesize("call", expr.callee,
				new Expr.Literal(Arrays.toString(expr.args.stream().map(this::print).toArray())));
	}

	@Override
	public String visitGroupingExpr(Expr.Grouping grouping) {
		return parenthesize("group", grouping.expression);
	}

	@Override
	public String visitLambdaExpr(Expr.Lambda expr) {
		return "(<lambda>)";
	}

	@Override
	public String visitLiteralExpr(Expr.Literal literal) {
		return literal.value.toString();
	}

	@Override
	public String visitLogicalExpr(Expr.Logical expr) {
		return parenthesize(expr.operator.getText(), expr.left, expr.right);
	}

	@Override
	public String visitUnaryExpr(Expr.Unary unary) {
		return parenthesize(unary.operator.getText(), unary.right);
	}

	@Override
	public String visitVariableExpr(Expr.Variable variable) {
		return parenthesize("var", new Expr.Literal(variable.name.getText()));
	}

	private String parenthesize(String name, Expr... exprs) {
		var sb = new StringBuilder();

		sb.append('(').append(name);
		for (var expr : exprs) {
			sb
					.append(" ")
					.append(expr.accept(this));
		}
		sb.append(')');

		return sb.toString();
	}
}
