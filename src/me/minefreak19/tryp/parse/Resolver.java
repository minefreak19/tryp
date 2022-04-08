package me.minefreak19.tryp.parse;

import me.minefreak19.tryp.eval.Interpreter;
import me.minefreak19.tryp.lex.token.KeywordToken;
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
	private final Stack<Map<String, Var>> scopes = new Stack<>();
	private ProcType currentProc = ProcType.NONE;
	private ClassType currentClass = ClassType.NONE;

	private static class Var {
		public boolean defined = false;
		public boolean used = false;
		public Token name;

		public Var(boolean defined, boolean used, Token name) {
			this.defined = defined;
			this.used = used;
			this.name = name;
		}

		public Var(Token name) {
			this.name = name;
		}
	}

	private enum ProcType {
		NONE,
		PROC,
		LAMBDA,
		METHOD,
	}

	private enum ClassType {
		NONE, CLASS,
	}

	public Resolver(Interpreter interpreter) {
		this.interpreter = interpreter;
	}

	public void resolve(List<Stmt> statements) {
		statements.forEach(this::resolve);
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
		var scope = scopes.pop();
		checkUnused(scope);
	}

	private void checkUnused(Map<String, Var> scope) {
		for (var entry : scope.entrySet()) {
			if (!entry.getValue().used) {
				new CompilerError()
						.error(entry.getValue().name.getLoc(),
								"Unused variable " + entry.getKey())
						.report();
			}
		}
	}

	private void declare(Token name) {
		if (scopes.isEmpty()) return;

		scopes.peek().put(name.getText(), new Var(name));
	}

	private void define(Token name) {
		if (scopes.isEmpty()) return;

		try {
			scopes.peek().get(name.getText()).defined = true;
		} catch (NullPointerException e) {
			throw new IllegalStateException("`define` called before `declare` for var "
					                                + name.getText(), e);
		}
	}

	private void resolveLocal(Expr expr, Token name) {
		for (int i = 0; i < scopes.size(); i++) {
			if (scopes.get(i).containsKey(name.getText())) {
				scopes.get(i).get(name.getText()).used = true;
				interpreter.resolve(expr, scopes.size() - i - 1);
				return;
			}
		}
	}

	private void resolveFunction(Stmt.ProcDecl proc, ProcType type) {
		var prevProc = currentProc;
		currentProc = type;
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
	public Void visitGetExpr(Expr.Get expr) {
		resolve(expr.object);
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
	public Void visitSetExpr(Expr.Set expr) {
		// resolution of fields is done dynamically.
		resolve(expr.object);
		resolve(expr.value);
		return null;
	}

	@Override
	public Void visitThisExpr(Expr.This expr) {
		if (this.currentClass == ClassType.NONE) {
			new CompilerError()
					.badToken(expr.kw, "Can't use `this` outside a class")
					.report();
		}
		resolveLocal(expr, expr.kw);
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
				    && scopes.peek().containsKey(expr.name.getText())
				    && !scopes.peek().get(expr.name.getText()).defined) {
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
	public Void visitClassStmt(Stmt.Class stmt) {
		declare(stmt.name);
		define(stmt.name);

		var prevClassType = this.currentClass;
		this.currentClass = ClassType.CLASS;
		beginScope();
		scopes.peek().put("this", new Var(true, true, new KeywordToken(null, "this")));

		for (Stmt.ProcDecl method : stmt.methods) {
			ProcType type = ProcType.METHOD;
			resolveFunction(method, type);
		}

		endScope();
		this.currentClass = prevClassType;

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

		resolveFunction(stmt, ProcType.PROC);
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
}
