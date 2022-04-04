package me.minefreak19.tryp.eval;

import me.minefreak19.tryp.Expr;
import me.minefreak19.tryp.lex.token.OpToken;

public class Interpreter implements Expr.Visitor<Object> {
	private boolean hadError = false;

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

	public void interpret(Expr expr) {
		try {
			Object value = evaluate(expr);
			System.out.println(stringify(value));
		} catch (RuntimeError err) {
			System.err.println(err.getLocalizedMessage());
			hadError = true;
		}
	}

	public Object evaluate(Expr expr) {
		return expr.accept(this);
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

	public boolean hadError() {
		return hadError;
	}
}
