package me.minefreak19.tryp.tree;

import me.minefreak19.tryp.lex.token.IdentifierToken;
import me.minefreak19.tryp.lex.token.Token;

import java.util.List;

public abstract class Stmt {
	@SuppressWarnings("SameReturnValue")
	public interface Visitor<R> {
		R visitBlockStmt(Block stmt);

		R visitClassStmt(Class stmt);

		R visitExpressionStmt(Expression stmt);

		R visitIfStmt(If stmt);

		R visitProcDeclStmt(ProcDecl stmt);

		R visitReturnStmt(Return stmt);

		R visitVarStmt(Var stmt);

		R visitWhileStmt(While stmt);
	}

	public static class Block extends Stmt {
		public Block(List<Stmt> statements) {
			this.statements = statements;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitBlockStmt(this);
		}

		public final List<Stmt> statements;
	}

	public static class Class extends Stmt {
		public Class(IdentifierToken name, Expr.Variable superclass, List<Stmt.ProcDecl> methods) {
			this.name = name;
			this.superclass = superclass;
			this.methods = methods;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitClassStmt(this);
		}

		public final IdentifierToken name;
		public final Expr.Variable superclass;
		public final List<Stmt.ProcDecl> methods;
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

	public static class If extends Stmt {
		public If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
			this.condition = condition;
			this.thenBranch = thenBranch;
			this.elseBranch = elseBranch;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitIfStmt(this);
		}

		public final Expr condition;
		public final Stmt thenBranch;
		public final Stmt elseBranch;
	}

	public static class ProcDecl extends Stmt {
		public ProcDecl(Token name, List<Token> params, List<Stmt> body, boolean isStatic) {
			this.name = name;
			this.params = params;
			this.body = body;
			this.isStatic = isStatic;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitProcDeclStmt(this);
		}

		public final Token name;
		public final List<Token> params;
		public final List<Stmt> body;
		public final boolean isStatic;
	}

	public static class Return extends Stmt {
		public Return(Token kw, Expr value) {
			this.kw = kw;
			this.value = value;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitReturnStmt(this);
		}

		public final Token kw;
		public final Expr value;
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

	public static class While extends Stmt {
		public While(Expr condition, Stmt body) {
			this.condition = condition;
			this.body = body;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitWhileStmt(this);
		}

		public final Expr condition;
		public final Stmt body;
	}

	@SuppressWarnings("UnusedReturnValue")
	public abstract <R> R accept(Visitor<R> visitor);
}
