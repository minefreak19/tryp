package me.minefreak19.tryp.eval;

import me.minefreak19.tryp.lex.token.OpToken;
import me.minefreak19.tryp.tree.Expr;
import me.minefreak19.tryp.tree.Stmt;

import java.util.List;

public class Interpreter
		implements Expr.Visitor<Object>,
				           Stmt.Visitor<Void> {
	private boolean hadError = false;

	private Environment environment = new Environment();

	public static boolean isTruthy(Object o) {
		return switch (o) {
			case null -> false;
			case Boolean b -> b;
			default -> true;
		};
	}

	public static boolean areEqual(Object a, Object b) {
		if (a == null && b == null) return true;
		if (a == null) return false; // b == null is handled in a.equals(b)

		return a.equals(b);
	}

	public static void checkNumber(OpToken opTok, Object... objs) {
		for (var obj : objs) {
			if (!(obj instanceof Double)) {
				throw new RuntimeError(opTok,
						"Operand for " + opTok.getText() + " must be a number");
			}
		}
	}

	public static String stringify(Object value) {
		return switch (value) {

			case null -> "nil";

			case Double d -> {
				String text = d.toString();
				if (text.endsWith(".0")) {
					text = text.substring(0, text.length() - 2);
				}
				yield text;
			}

			// TODO: unescape the string
			case String s -> '"' + s + '"';

			default -> value.toString();

		};
	}

	/**
	 * Version of {@link #stringify(Object)} that doesn't stringify strings.
	 *
	 * @param value The value to stringify.
	 *
	 * @return {@code value} if {@code value instanceof String}, else {@code stringify(value)}
	 */
	public static String toString(Object value) {
		if (value instanceof String s) return s;

		return stringify(value);
	}

	public void interpret(List<Stmt> program) {
		try {
			for (var stmt : program) {
				execute(stmt);
			}
		} catch (RuntimeError err) {
			System.err.println(err.getLocalizedMessage());
			hadError = true;
		}
	}

	private void execute(Stmt stmt) {
		stmt.accept(this);
	}

	private void executeBlock(List<Stmt> statements, Environment environment) {
		Environment previous = this.environment;
		try {
			this.environment = environment;

			for (var stmt : statements) {
				execute(stmt);
			}
		} finally {
			this.environment = previous;
		}
	}

	public Object evaluate(Expr expr) {
		return expr.accept(this);
	}

	@Override
	public Object visitAssignExpr(Expr.Assign assign) {
		Object value = evaluate(assign.value);
		environment.assign(assign.name, value);
		return value;
	}

	@Override
	public Object visitBinaryExpr(Expr.Binary binary) {
		Object left = evaluate(binary.left);
		Object right = evaluate(binary.right);

		return switch (binary.operator.getValue()) {

			case BANG_EQUAL -> !areEqual(left, right);
			case EQUAL_EQUAL -> areEqual(left, right);

			case PLUS -> {
				if (left instanceof String strLeft && right instanceof String strRight)
					yield strLeft + strRight;
				else if (left instanceof String strLeft)
					yield strLeft + stringify(right);
				else if (right instanceof String strRight)
					yield stringify(left) + strRight;
				else if (left instanceof Double dLeft && right instanceof Double dRight)
					yield dLeft + dRight;
				else
					throw new RuntimeError(binary.operator,
							"Can't apply operator `" + binary.operator.getText()
									+ "` to operands "
									+ left + " and " + right);
			}

			case MINUS -> {
				checkNumber(binary.operator, left, right);
				yield (double) left - (double) right;
			}

			case SLASH -> {
				checkNumber(binary.operator, left, right);
				yield (double) left / (double) right;
			}

			case STAR -> {
				checkNumber(binary.operator, left, right);
				yield (double) left * (double) right;
			}

			case GREATER_THAN -> {
				checkNumber(binary.operator, left, right);
				yield (double) left > (double) right;
			}

			case LESS_THAN -> {
				checkNumber(binary.operator, left, right);
				yield (double) left < (double) right;
			}

			case GREATER_EQUAL -> {
				checkNumber(binary.operator, left, right);
				yield (double) left >= (double) right;
			}

			case LESS_EQUAL -> {
				checkNumber(binary.operator, left, right);
				yield (double) left <= (double) right;
			}

			case COMMA -> right;

			default -> throw new AssertionError("unreachable");

		};
	}

	@Override
	public Object visitGroupingExpr(Expr.Grouping grouping) {
		return evaluate(grouping.expression);
	}

	@Override
	public Object visitLiteralExpr(Expr.Literal literal) {
		return literal.value;
	}

	@Override
	public Object visitUnaryExpr(Expr.Unary unary) {
		Object right = evaluate(unary.right);
		return switch (unary.operator.getValue()) {

			case MINUS -> -(double) right;
			case BANG -> !isTruthy(right);

			default -> throw new AssertionError("unreachable");

		};
	}

	@Override
	public Object visitVariableExpr(Expr.Variable variable) {
		return environment.get(variable.name);
	}

	@Override
	public Void visitBlockStmt(Stmt.Block block) {
		executeBlock(block.statements, new Environment(environment));
		return null;
	}

	@Override
	public Void visitExpressionStmt(Stmt.Expression expression) {
		evaluate(expression.expr);
		return null;
	}

	@Override
	public Void visitIfStmt(Stmt.If ifStmt) {
		if (isTruthy(evaluate(ifStmt.condition))) {
			execute(ifStmt.thenBranch);
		} else if (ifStmt.elseBranch != null) {
			execute(ifStmt.elseBranch);
		}
		return null;
	}

	@Override
	public Void visitPrintStmt(Stmt.Print print) {
		Object value = evaluate(print.expr);
		System.out.println(toString(value));
		return null;
	}

	@Override
	public Void visitVarStmt(Stmt.Var var) {
		Object value = null;
		if (var.initializer != null) {
			value = evaluate(var.initializer);
		}

		environment.define(var.name.getText(), value);
		return null;
	}

	public boolean hadError() {
		return hadError;
	}
}
