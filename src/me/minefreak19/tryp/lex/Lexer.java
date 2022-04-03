package me.minefreak19.tryp.lex;

import me.minefreak19.tryp.CompilerError;
import me.minefreak19.tryp.SyntaxException;
import me.minefreak19.tryp.lex.token.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Character.isDigit;

public class Lexer {
	private String source;
	private final FileLocation loc;

	public static boolean isIdentifier(char ch) {
		return 'a' <= ch && ch <= 'z'
				       || 'A' <= ch && ch <= 'Z'
				       || ch == '!'
				       || ch == '_';
	}

	public static boolean isNum(char ch) {
		// TODO: this may need to account for different radixes in the future
		return Character.isDigit(ch)
				       || ch == '.'
				       || ch == '_';
	}

	public Lexer(String source, FileLocation loc) {
		this.source = source;
		this.loc = loc;
	}

	public Lexer(File file) throws IOException {
		this(Files.readString(file.toPath()), FileLocation.from(file));
	}

	private static class InvalidEscapeException extends SyntaxException {
		public static InvalidEscapeException forChar(Lexer ctx, char ch) {
			return new InvalidEscapeException(ctx.loc, "invalid escape sequence `\\" + ch + "`");
		}

		public InvalidEscapeException(FileLocation loc, String message) {
			super(loc, message);
		}
	}

	private void trimWhitespace() {
		int i;
		for (i = 0; i < source.length(); i++) {
			char ch = source.charAt(i);

			if (ch == '\n') {
				loc.advanceLine();
				continue;
			}

			if (!Character.isWhitespace(ch)) break;

			loc.advanceCol();
		}

		this.source = source.substring(i);
	}

	private Token finish(Token token) {
		// for string literals and the like
		String[] lines = token.getText().split("\n");
		if (lines.length > 1) {
			loc.advanceLine(lines.length - 1);
		}

		loc.advanceCol(lines[lines.length - 1].length());

		return token;
	}

	private void skipLineComment() {
		if (!this.source.startsWith("//")) {
			throw new IllegalStateException("skipLineComment() called but no single line comment");
		}

		this.source = this.source.split("\n", 2)[1];
		loc.advanceLine();
	}

	private void skipMultilineComment() {
		if (!this.source.startsWith("/*")) {
			throw new IllegalStateException("skipMultilineComment() called but no multiline comment");
		}

		String[] parts = this.source.split("\\*/", 2);
		loc.advanceLine((int) parts[0].lines().count() - 1);

		this.source = parts[1];
	}

	private Token lexNum() {
		StringBuilder sb = new StringBuilder();
		int i;
		for (i = 0; i < source.length(); i++) {
			char ch = this.source.charAt(i);

			if (!isNum(ch)) break;

			if (ch != '_') {
				sb.append(ch);
			}
		}

		this.source = this.source.substring(i);

		var tokText = sb.toString();
		try {
			return finish(new NumberToken(this.loc, tokText));
		} catch (NumberFormatException e) {
			// this probably shouldn't happen normally because of the !isNum checks
			//  still, it could happen if the number somehow can't be represented
			//  in a double.
			throw new CompilerError()
					.error(this.loc, "Invalid number: `" + tokText + "`")
					.report();
		}
	}

	private Token lexWord() {
		var sb = new StringBuilder();

		int i;
		for (i = 0; i < this.source.length(); i++) {
			char ch = this.source.charAt(i);

			if (!isIdentifier(ch)) break;

			sb.append(ch);
		}

		this.source = this.source.substring(i);

		var tokText = sb.toString();
		try {
			return finish(new KeywordToken(this.loc, tokText));
		} catch (IllegalArgumentException ignored) {
			return finish(new IdentifierToken(this.loc, tokText));
		}
	}

	private Token lexString() {
		if (source.charAt(0) != '"') {
			throw new IllegalStateException("lexString() called without a String to lex");
		}

		var sbStr = new StringBuilder();

		boolean escape = false;
		int i;
		loop:
		for (i = 1 /* skip initial quote */; i < this.source.length(); i++) {
			char ch = this.source.charAt(i);

			if (escape) {
				escape = false;

				sbStr.append(switch (ch) {
					case 'n' -> '\n';
					case 't' -> '\t';
					case '"' -> '"';
					case '\\' -> '\\';
					default -> throw InvalidEscapeException.forChar(this, ch);
				});
			} else {
				switch (ch) {
				case '"' -> {
					// increment it, because we're breaking the loop
					//  this ensures that the ending quote is included in
					//  the string literal and not left in `this.source`
					i++;
					break loop;
				}
				case '\\' -> escape = true;
				default -> sbStr.append(ch);
				}
			}
		}

		String str = sbStr.toString();
		String text = this.source.substring(0, i);
		this.source = this.source.substring(i);
		if (i == 1 || !text.endsWith("\"")) {
			throw new SyntaxException(this.loc,
					String.format("`%s` (%s)", text, str) + " " +
							"unclosed string literal");
		}

		return finish(new StringToken(this.loc, text, str));
	}

	// TODO: expose this interface. it's probably simpler for the parsing
	private Token nextToken() {
		trimWhitespace();
		if (this.source.isEmpty()) return null;

		if (this.source.startsWith("//")) {
			skipLineComment();
			return nextToken();
		}
		if (this.source.startsWith("/*")) {
			skipMultilineComment();
			return nextToken();
		}

		for (var op : Operator.values()) {
			if (source.startsWith(op.text)) {
				this.source = this.source.substring(op.text.length());
				return finish(new OpToken(this.loc, op.text, op));
			}
		}

		char ch = this.source.charAt(0);
		if (isDigit(ch))
			return lexNum();
		else if (ch == '"')
			return lexString();
		else
			return lexWord();
	}

	public List<Token> tokens() {
		var ret = new LinkedList<Token>();
		while (!this.source.isEmpty()) {
//            System.out.println(this.source);
			var token = nextToken();
			if (token != null) {
				ret.add(token);
			}
		}

		ret.add(new EOFToken());

		return ret;
	}
}
