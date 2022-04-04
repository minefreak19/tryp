package me.minefreak19.tryp.parse;

import me.minefreak19.tryp.Expr;
import me.minefreak19.tryp.SyntaxException;
import me.minefreak19.tryp.lex.token.*;
import me.minefreak19.tryp.util.CompilerError;

import java.util.List;
import java.util.Objects;

import static me.minefreak19.tryp.lex.token.Operator.*;

@SuppressWarnings("SameParameterValue")
public final class Parser {
	private final List<Token> tokens;
	private int current;

	public Parser(List<Token> tokens) {
		this.tokens = tokens;
		this.current = 0;
	}

	private void sync() {
		advance();

		while (!atEnd()) {
			if (previous() instanceof OpToken opTok
					    && opTok.getValue() == SEMICOLON) return;

			if (peek() instanceof KeywordToken kwTok) {
				switch (kwTok.getValue()) {
				case PROC, VAR, FOR, IF, WHILE, RETURN -> {
					return;
				}
				}
			}

			advance();
		}
	}

	public Expr parse() {
		try {
			return expression();
		} catch (SyntaxException ignored) {
			return null;
		}
	}

	private Expr expression() {
		return compound();
	}

	private Expr compound() {
		Expr expr = equality();
		while (match(COMMA)) {
			var opTok = (OpToken) previous();
			Expr right = equality();
			expr = new Expr.Binary(expr, opTok, right);
		}

		return expr;
	}

	private Expr equality() {
		Expr expr = comparison();
		while (match(BANG_EQUAL, EQUAL_EQUAL)) {
			var opTok = (OpToken) previous();
			Expr right = comparison();
			expr = new Expr.Binary(expr, opTok, right);
		}

		return expr;
	}

	private Expr comparison() {
		Expr expr = term();
		while (match(GREATER_THAN, LESS_THAN, GREATER_EQUAL, LESS_EQUAL)) {
			var opTok = (OpToken) previous();
			Expr right = term();
			expr = new Expr.Binary(expr, opTok, right);
		}

		return expr;
	}

	private Expr term() {
		Expr expr = factor();
		while (match(MINUS, PLUS)) {
			var opTok = (OpToken) previous();
			Expr right = factor();
			expr = new Expr.Binary(expr, opTok, right);
		}

		return expr;
	}

	private Expr factor() {
		Expr expr = unary();
		while (match(SLASH, STAR)) {
			var opTok = (OpToken) previous();
			Expr right = unary();
			expr = new Expr.Binary(expr, opTok, right);
		}

		return expr;
	}

	private Expr unary() {
		if (match(BANG, MINUS)) {
			var opTok = (OpToken) previous();
			return new Expr.Unary(opTok, unary());
		}

		return primary();
	}

	private Expr primary() {
		var token = advance();
		return switch (token) {

			case KeywordToken kwTok -> new Expr.Literal(switch (kwTok.getValue()) {
				case NIL -> null;
				case TRUE -> true;
				case FALSE -> false;
				default -> throw new CompilerError()
						.badToken(token, "Unexpected keyword here")
						.report();
			});

			case NumberToken intTok -> new Expr.Literal(intTok.getValue());
			case StringToken strTok -> new Expr.Literal(strTok.getValue());

			case OpToken opTok -> {
				if (opTok.getValue() == OPEN_PAREN) {
					Expr expr = expression();
					expect(CLOSE_PAREN);
					yield expr;
				} else {
					throw new CompilerError()
							.badToken(opTok, "Illegal start of expression")
							.report();
				}
			}

			default -> throw new CompilerError()
					.badToken(token, "Expected expression")
					.report();

		};
	}

	@SuppressWarnings("UnusedReturnValue")
	private OpToken expect(Operator op) {
		if (check(op))
			return (OpToken) advance();

		throw new CompilerError()
				.expectedButFound(op.text, peek())
				.report();
	}

	private Token previous() {
		return tokens.get(current - 1);
	}

	private Token peek() {
		return tokens.get(current);
	}

	private boolean atEnd() {
		return peek() instanceof EOFToken;
	}

	private Token advance() {
		if (!atEnd()) current++;
		return previous();
	}

	private boolean check(Operator op) {
		if (atEnd()) return false;

		return peek() instanceof OpToken opTok
				       && opTok.getValue() == op;
	}

	private boolean match(Operator... ops) {
		for (var op : ops) {
			if (check(op)) {
				advance();
				return true;
			}
		}

		return false;
	}

	public List<Token> tokens() {
		return tokens;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		var that = (Parser) obj;
		return Objects.equals(this.tokens, that.tokens);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tokens);
	}

	@Override
	public String toString() {
		return "Parser[" +
				       "tokens=" + tokens + ']';
	}

}
