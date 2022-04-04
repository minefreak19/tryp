package me.minefreak19.tryp.tree;

import me.minefreak19.tryp.lex.token.*;

import java.util.*;

public abstract class Stmt {
	public interface Visitor<R> {
		R visitExpressionStmt(Expression expression);
		R visitPrintStmt(Print print);
		R visitVarStmt(Var var);
	}

	public static class Expression extends Stmt {
		public Expression(Expr expr) {
			this.expr = expr;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitExpressionStmt(this);
		}

		public final Expr expr;
	}

	public static class Print extends Stmt {
		public Print(Expr expr) {
			this.expr = expr;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitPrintStmt(this);
		}

		public final Expr expr;
	}

	public static class Var extends Stmt {
		public Var(Token name, Expr initializer) {
			this.name = name;
			this.initializer = initializer;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitVarStmt(this);
		}

		public final Token name;
		public final Expr initializer;
	}

	public abstract <R> R accept(Visitor<R> visitor);
}
