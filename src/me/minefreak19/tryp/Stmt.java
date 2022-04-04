package me.minefreak19.tryp;

public abstract class Stmt {
	public interface Visitor<R> {
		R visitExpressionStmt(Expression expression);

		R visitPrintStmt(Print print);
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

	public abstract <R> R accept(Visitor<R> visitor);
}
