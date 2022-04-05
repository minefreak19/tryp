package me.minefreak19.tryp.parse;

import me.minefreak19.tryp.SyntaxException;
import me.minefreak19.tryp.lex.token.*;
import me.minefreak19.tryp.tree.Expr;
import me.minefreak19.tryp.tree.Stmt;
import me.minefreak19.tryp.util.CompilerError;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static me.minefreak19.tryp.lex.token.Keyword.*;
import static me.minefreak19.tryp.lex.token.Operator.*;

@SuppressWarnings("SameParameterValue")
public final class Parser {
	private final List<Token> tokens;
	private int current;
	private boolean hadError = false;

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

	public List<Stmt> parse() {
		var statements = new ArrayList<Stmt>();
		while (!atEnd()) {
			statements.add(declaration());
		}

		return statements;
	}

	private Stmt declaration() {
		try {
			if (match(VAR)) return varDecl();
			if (match(PROC)) return procDecl();

			return statement();
		} catch (SyntaxException exception) {
			hadError = true;
			sync();
			return null;
		}
	}

	/**
	 * Expects 'var' keyword to be already consumed.
	 */
	private Stmt varDecl() {
		Token varName = expect(IdentifierToken.class);

		Expr initializer = null;
		if (match(EQUAL)) {
			initializer = expression();
		}

		expect(SEMICOLON);

		return new Stmt.Var(varName, initializer);
	}

	/**
	 * expects proc keyword to be already consumed
	 */
	private Stmt procDecl() {
		var name = expect(IdentifierToken.class);
		expect(OPEN_PAREN);

		List<Token> params;
		if (!check(CLOSE_PAREN)) {
			// TODO: rename funParams to procParams, funArgs to procArgs
			params = funParams();
		} else {
			params = new ArrayList<>(0);
		}
		expect(CLOSE_PAREN);

		expect(OPEN_CURLY);
		List<Stmt> body = blockStatement();

		return new Stmt.ProcDecl(name, params, body);
	}

	/**
	 * Expects opening paren to be consumed. Does not consume closing paren.
	 * Expects at least one parameter to be present.
	 */
	private List<Token> funParams() {
		var ret = new ArrayList<Token>();

		do {
			ret.add(expect(IdentifierToken.class));
		} while (match(COMMA));

		return ret;
	}

	private Stmt statement() {
		if (match(PRINT)) return printStatement();
		if (match(OPEN_CURLY)) return new Stmt.Block(blockStatement());
		if (match(IF)) return ifStatement();
		if (match(WHILE)) return whileStatement();
		if (match(FOR)) return forStatement();

		return expressionStatement();
	}

	private Stmt printStatement() {
		assert previous() instanceof KeywordToken kwTok && kwTok.getValue() == PRINT;

		Expr value = expression();
		expect(SEMICOLON);
		return new Stmt.Print(value);
	}

	/**
	 * Expects the opening curly to be already consumed.
	 * Consumes the closing curly.
	 */
	private List<Stmt> blockStatement() {
		var statements = new ArrayList<Stmt>();

		while (!check(CLOSE_CURLY) && !atEnd()) {
			statements.add(declaration());
		}

		expect(CLOSE_CURLY);

		return statements;
	}

	private Stmt ifStatement() {
		expect(OPEN_PAREN);
		Expr condition = expression();
		expect(CLOSE_PAREN);

		Stmt thenBranch = statement();

		Stmt elseBranch = null;
		// NOTE: `else` binds to the nearest `if`, because
		//  we check for it here
		if (match(ELSE)) {
			elseBranch = statement();
		}

		return new Stmt.If(condition, thenBranch, elseBranch);
	}

	private Stmt whileStatement() {
		expect(OPEN_PAREN);
		Expr condition = expression();
		expect(CLOSE_PAREN);

		Stmt body = statement();

		return new Stmt.While(condition, body);
	}

	/**
	 * Note: A for loop compiles down to a while loop.
	 * <p>
	 * <code>
	 * for (var i = 0; i < 10; i <- i + 1) {
	 * print i;
	 * }
	 * </code>
	 * <p>
	 * is equivalent to
	 * <p>
	 * <code>
	 * {
	 * var i = 0;
	 * while (i < 10) {
	 * print i;
	 * i <- i + 1;
	 * }
	 * }
	 * </code>
	 * <p>
	 * See <a href="https://en.wikipedia.org/wiki/Syntactic_sugar">desugaring</a>.
	 */
	private Stmt forStatement() {
		expect(OPEN_PAREN);
		Stmt initializer;
		if (match(VAR)) {
			initializer = varDecl();
		} else if (match(SEMICOLON)) {
			initializer = null;
		} else {
			initializer = expressionStatement();
		}

		Expr condition;
		if (!match(SEMICOLON)) {
			condition = expression();
			expect(SEMICOLON);
		} else condition = null;

		Expr updation;
		if (check(CLOSE_PAREN)) {
			updation = null;
		} else {
			updation = expression();
		}

		expect(CLOSE_PAREN);

		Stmt forBody = statement();

		{
			Stmt whileBody;
			if (updation != null) {
				var bodyList = new ArrayList<Stmt>(2);
				bodyList.add(forBody);
				bodyList.add(new Stmt.Expression(updation));
				whileBody = new Stmt.Block(bodyList);
			} else {
				whileBody = forBody;
			}

			if (condition == null) condition = new Expr.Literal(true);

			var outerStmts = new ArrayList<Stmt>(2);
			if (initializer != null) {
				outerStmts.add(initializer);
			}
			outerStmts.add(new Stmt.While(condition, whileBody));

			return new Stmt.Block(outerStmts);
		}
	}

	private Stmt expressionStatement() {
		Expr value = expression();
		expect(SEMICOLON);
		return new Stmt.Expression(value);
	}

	private Expr expression() {
		return compound();
	}

	private Expr compound() {
		Expr expr = assignment();
		while (match(COMMA)) {
			var opTok = (OpToken) previous();
			Expr right = assignment();
			expr = new Expr.Binary(expr, opTok, right);
		}

		return expr;
	}

	// https://craftinginterpreters.com/statements-and-state.html#assignment-syntax
	private Expr assignment() {
		// delegate to equality() if there's no `<-` after that
		Expr expr = logicalOr();

		if (match(LEFT_ARROW)) {
			Token arrow = previous();
			Expr value = assignment();
			if (expr instanceof Expr.Variable varExpr) {
				return new Expr.Assign(varExpr.name, value);
			}

			throw new CompilerError()
					.error(arrow.getLoc(), "Invalid target for assignment")
					.report();
		}

		return expr;
	}

	private Expr logicalOr() {
		Expr left = logicalAnd();
		while (match(OR_OR)) {
			var opTok = (OpToken) previous();
			Expr right = logicalAnd();
			left = new Expr.Logical(left, opTok, right);
		}

		return left;
	}

	private Expr logicalAnd() {
		Expr left = equality();
		while (match(AND_AND)) {
			var opTok = (OpToken) previous();
			Expr right = equality();
			left = new Expr.Logical(left, opTok, right);
		}

		return left;
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

		return call();
	}

	private Expr call() {
		Expr name = primary();
		if (!check(OPEN_PAREN)) {
			return name;
		}
		var paren = (OpToken) expect(OPEN_PAREN);
		List<Expr> args;
		if (!check(CLOSE_PAREN)) {
			args = funArgs();
		} else {
			args = new ArrayList<>(0);
		}

		expect(CLOSE_PAREN);

		return new Expr.Call(name, paren, args);
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

			case IdentifierToken iTok -> new Expr.Variable(iTok);

			default -> throw new CompilerError()
					.badToken(token, "Expected expression")
					.report();

		};
	}

	/**
	 * Expects at least one expression.
	 */
	@SuppressWarnings("ThrowableNotThrown")
	private List<Expr> funArgs() {
		var ret = new ArrayList<Expr>();
		do {
			if (ret.size() >= 255) {
				new CompilerError()
						.error(peek().getLoc(), "Function can't have more than 255 arguments")
						.report();
			}
			ret.add(expression());
		} while (match(COMMA));

		return ret;
	}

	@SuppressWarnings("UnusedReturnValue")
	private OpToken expect(Operator op) {
		if (check(op))
			return (OpToken) advance();

		throw new CompilerError()
				.expectedButFound(op.text, peek())
				.report();
	}

	@SuppressWarnings("unchecked")
	private <T extends Token> T expect(Class<T> tokenType) {
		if (check(tokenType))
			return (T) advance();

		throw new CompilerError()
				.expectedButFound(Token.humanTokType(tokenType), peek())
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
		// TODO: handle this case properly (by throwing an error)
		if (!atEnd()) current++;
		return previous();
	}

	private boolean check(Operator op) {
		if (atEnd()) return false;

		return peek() instanceof OpToken opTok
				       && opTok.getValue() == op;
	}

	private boolean check(Keyword kw) {
		if (atEnd()) return false;

		return peek() instanceof KeywordToken kwTOk
				       && kwTOk.getValue() == kw;
	}

	private <T extends Token> boolean check(Class<T> tokenType) {
		return peek().getClass() == tokenType;
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

	private boolean match(Keyword... kws) {
		for (var kw : kws) {
			if (check(kw)) {
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

	public boolean hadError() {
		return this.hadError;
	}

}
