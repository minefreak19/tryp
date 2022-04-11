package me.minefreak19.tryp.tree;

import me.minefreak19.tryp.lex.token.IdentifierToken;
import me.minefreak19.tryp.lex.token.KeywordToken;
import me.minefreak19.tryp.lex.token.OpToken;
import me.minefreak19.tryp.lex.token.Token;

import java.util.List;

public abstract class Expr {
	public interface Visitor<R> {
		R visitAssignExpr(Assign expr);

		R visitBinaryExpr(Binary expr);

		R visitCallExpr(Call expr);

		R visitGetExpr(Get expr);

		R visitGroupingExpr(Grouping expr);

		R visitLambdaExpr(Lambda expr);

		R visitLiteralExpr(Literal expr);

		R visitLogicalExpr(Logical expr);

		R visitSetExpr(Set expr);

		R visitSuperExpr(Super expr);

		R visitThisExpr(This expr);

		R visitUnaryExpr(Unary expr);

		R visitVariableExpr(Variable expr);
	}

	public static class Assign extends Expr {
		public Assign(Token name, Expr value) {
			this.name = name;
			this.value = value;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitAssignExpr(this);
		}

		public final Token name;
		public final Expr value;
	}

	public static class Binary extends Expr {
		public Binary(Expr left, OpToken operator, Expr right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitBinaryExpr(this);
		}

		public final Expr left;
		public final OpToken operator;
		public final Expr right;
	}

	public static class Call extends Expr {
		public Call(Expr callee, OpToken paren, List<Expr> args) {
			this.callee = callee;
			this.paren = paren;
			this.args = args;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitCallExpr(this);
		}

		public final Expr callee;
		public final OpToken paren;
		public final List<Expr> args;
	}

	public static class Get extends Expr {
		public Get(Expr object, IdentifierToken name) {
			this.object = object;
			this.name = name;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitGetExpr(this);
		}

		public final Expr object;
		public final IdentifierToken name;
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

	public static class Lambda extends Expr {
		public Lambda(OpToken lambda, List<Token> params, List<Stmt> body) {
			this.lambda = lambda;
			this.params = params;
			this.body = body;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitLambdaExpr(this);
		}

		public final OpToken lambda;
		public final List<Token> params;
		public final List<Stmt> body;
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

	public static class Logical extends Expr {
		public Logical(Expr left, OpToken operator, Expr right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitLogicalExpr(this);
		}

		public final Expr left;
		public final OpToken operator;
		public final Expr right;
	}

	public static class Set extends Expr {
		public Set(Expr object, IdentifierToken name, Expr value) {
			this.object = object;
			this.name = name;
			this.value = value;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitSetExpr(this);
		}

		public final Expr object;
		public final IdentifierToken name;
		public final Expr value;
	}

	public static class Super extends Expr {
		public Super(KeywordToken kw, IdentifierToken method) {
			this.kw = kw;
			this.method = method;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitSuperExpr(this);
		}

		public final KeywordToken kw;
		public final IdentifierToken method;
	}

	public static class This extends Expr {
		public This(KeywordToken kw) {
			this.kw = kw;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitThisExpr(this);
		}

		public final KeywordToken kw;
	}

	public static class Unary extends Expr {
		public Unary(OpToken operator, Expr right) {
			this.operator = operator;
			this.right = right;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitUnaryExpr(this);
		}

		public final OpToken operator;
		public final Expr right;
	}

	public static class Variable extends Expr {
		public Variable(Token name) {
			this.name = name;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitVariableExpr(this);
		}

		public final Token name;
	}

	public abstract <R> R accept(Visitor<R> visitor);
}
