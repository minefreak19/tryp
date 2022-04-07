package me.minefreak19.tryp.parse;

import me.minefreak19.tryp.SyntaxException;
import me.minefreak19.tryp.eval.Interpreter;
import me.minefreak19.tryp.lex.token.Token;
import me.minefreak19.tryp.tree.Expr;
import me.minefreak19.tryp.tree.Stmt;
import me.minefreak19.tryp.util.CompilerError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

@SuppressWarnings("ThrowableNotThrown")
public final class Resolver
		implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
	private final Interpreter interpreter;
	// stack<map<string name, boolean defined>>
	private final Stack<Map<String, Boolean>> scopes = new Stack<>();
	private ProcType currentProc = ProcType.NONE;
	private boolean hadError = false;

	private enum ProcType {
		NONE,
		PROC,
		LAMBDA,
	}

	public Resolver(Interpreter interpreter) {
		this.interpreter = interpreter;
	}

	public void resolve(List<Stmt> statements) {
		try {
			statements.forEach(this::resolve);
		} catch (SyntaxException e) {
			this.hadError = true;
			throw e;
		}
	}

	private void resolve(Stmt stmt) {
		stmt.accept(this);
	}

	private void resolve(Expr expr) {
		expr.accept(this);
	}

	private void beginScope() {
		scopes.push(new HashMap<>());
	}

	private void endScope() {
		scopes.pop();
	}

	private void declare(Token name) {
		if (scopes.isEmpty()) return;

		scopes.peek().put(name.getText(), false);
	}

	private void define(Token name) {
		if (scopes.isEmpty()) return;

		scopes.peek().put(name.getText(), true);
	}

	private void resolveLocal(Expr expr, Token name) {
		for (int i = 0; i < scopes.size(); i++) {
			if (scopes.get(i).containsKey(name.getText())) {
				interpreter.resolve(expr, scopes.size() - i - 1);
				return;
			}
		}
	}

	private void resolveFunction(Stmt.ProcDecl proc) {
		var prevProc = currentProc;
		currentProc = ProcType.PROC;
		beginScope();
		for (Token param : proc.params) {
			declare(param);
			define(param);
		}

		resolve(proc.body);
		endScope();
		currentProc = prevProc;
	}

	@Override
	public Void visitAssignExpr(Expr.Assign expr) {
		resolve(expr.value);
		return null;
	}

	@Override
	public Void visitBinaryExpr(Expr.Binary expr) {
		resolve(expr.left);
		resolve(expr.right);
		return null;
	}

	@Override
	public Void visitCallExpr(Expr.Call expr) {
		resolve(expr.callee);
		for (Expr arg : expr.args) {
			resolve(arg);
		}
		return null;
	}

	@Override
	public Void visitGroupingExpr(Expr.Grouping expr) {
		resolve(expr.expression);
		return null;
	}

	@Override
	public Void visitLambdaExpr(Expr.Lambda expr) {
		var prevProc = currentProc;
		currentProc = ProcType.LAMBDA;
		beginScope();
		for (Token param : expr.params) {
			declare(param);
			define(param);
		}

		resolve(expr.body);

		currentProc = prevProc;
		return null;
	}

	@Override
	public Void visitLiteralExpr(Expr.Literal expr) {
		// Do nothing.
		return null;
	}

	@Override
	public Void visitLogicalExpr(Expr.Logical expr) {
		resolve(expr.left);
		resolve(expr.right);
		return null;
	}

	@Override
	public Void visitUnaryExpr(Expr.Unary expr) {
		resolve(expr.right);
		return null;
	}

	@Override
	public Void visitVariableExpr(Expr.Variable expr) {
		if (!scopes.empty()
				    && !scopes.peek().get(expr.name.getText())) {
			new CompilerError()
					.error(expr.name.getLoc(), "Can't read local variable in its own initializer.")
					.report();
		}

		resolveLocal(expr, expr.name);
		return null;
	}

	@Override
	public Void visitBlockStmt(Stmt.Block stmt) {
		beginScope();
		resolve(stmt.statements);
		endScope();
		return null;
	}

	@Override
	public Void visitExpressionStmt(Stmt.Expression stmt) {
		resolve(stmt.expr);
		return null;
	}

	@Override
	public Void visitIfStmt(Stmt.If stmt) {
		resolve(stmt.condition);
		resolve(stmt.thenBranch);
		if (stmt.elseBranch != null) resolve(stmt.elseBranch);
		return null;
	}

	@Override
	public Void visitPrintStmt(Stmt.Print stmt) {
		resolve(stmt.expr);
		return null;
	}

	@Override
	public Void visitProcDeclStmt(Stmt.ProcDecl stmt) {
		declare(stmt.name);
		define(stmt.name);

		resolveFunction(stmt);
		return null;
	}

	@Override
	public Void visitReturnStmt(Stmt.Return stmt) {
		if (stmt.value != null) resolve(stmt.value);
		if (currentProc == ProcType.NONE) {
			new CompilerError()
					.error(stmt.kw.getLoc(), "Can't return from outside a proc.")
					.report();
		}
		// we don't call endScope() here because resolution
		// has nothing to do with the control flow
		// we only care about resolving every variable referred to
		return null;
	}

	@Override
	public Void visitVarStmt(Stmt.Var stmt) {
		declare(stmt.name);
		if (stmt.initializer != null) {
			resolve(stmt.initializer);
		}
		define(stmt.name);
		return null;
	}

	@Override
	public Void visitWhileStmt(Stmt.While stmt) {
		resolve(stmt.condition);
		resolve(stmt.body);
		return null;
	}

	public boolean hadError() {
		return this.hadError;
	}
}
