package me.minefreak19.tryp.parse;

import me.minefreak19.tryp.lex.token.Operator;
import me.minefreak19.tryp.lex.token.Token;

public abstract class Expr {
	public interface Visitor<R> {
		R visitBinaryExpr(Binary binary);

		R visitGroupingExpr(Grouping grouping);

		R visitLiteralExpr(Literal literal);

		R visitUnaryExpr(Unary unary);
	}

	public static class Binary extends Expr {
		public Binary(Expr left, Token operator, Expr right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitBinaryExpr(this);
		}

		public final Expr left;
		public final Token operator;
		public final Expr right;
	}

	public static class Grouping extends Expr {
		public Grouping(Expr expression) {
			this.expression = expression;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitGroupingExpr(this);
		}

		public final Expr expression;
	}

	public static class Literal extends Expr {
		public Literal(Object value) {
			this.value = value;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitLiteralExpr(this);
		}

		public final Object value;
	}

	public static class Unary extends Expr {
		public Unary(Operator operator, Expr right) {
			this.operator = operator;
			this.right = right;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitUnaryExpr(this);
		}

		public final Operator operator;
		public final Expr right;
	}

	public abstract <R> R accept(Visitor<R> visitor);
}
