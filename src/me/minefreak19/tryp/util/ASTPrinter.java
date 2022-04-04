package me.minefreak19.tryp.util;

import me.minefreak19.tryp.tree.Expr;

public class ASTPrinter implements Expr.Visitor<String> {

	public String print(Expr expr) {
		return expr.accept(this);
	}

	@Override
	public String visitBinaryExpr(Expr.Binary binary) {
		return parenthesize(binary.operator.getText(), binary.left, binary.right);
	}

	@Override
	public String visitGroupingExpr(Expr.Grouping grouping) {
		return parenthesize("group", grouping.expression);
	}

	@Override
	public String visitLiteralExpr(Expr.Literal literal) {
		return literal.value.toString();
	}

	@Override
	public String visitUnaryExpr(Expr.Unary unary) {
		return parenthesize(unary.operator.getText(), unary.right);
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
