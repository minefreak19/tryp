package me.minefreak19.tryp.util;

import me.minefreak19.tryp.SyntaxException;
import me.minefreak19.tryp.Tryp;
import me.minefreak19.tryp.lex.FileLocation;
import me.minefreak19.tryp.lex.token.Token;

import java.io.PrintStream;

public class CompilerError {
	private final StringBuilder message = new StringBuilder();

	public CompilerError() {
	}

	/**
	 * Message header for an error message.
	 * <p>
	 * For example, {@code messageHeader(FileLocation("hello.txt", 1, 4), "ERROR")}
	 * appends {@code "hello.txt:1:4: ERROR: "} to the internal buffer.
	 *
	 * @param loc    Location of the problem.
	 * @param prefix Nature of the problem (e.g. ERROR, WARN, etc.)
	 */
	private void messageHeader(FileLocation loc, String prefix) {
		this.message
				.append(loc)
				.append(": ")
				.append(prefix)
				.append(": ");
	}

	public CompilerError error(FileLocation loc, String error) {
		messageHeader(loc, "ERROR");
		this.message
				.append(error)
				.append('\n');
		return this;
	}

	public CompilerError expectedButFound(String expected, Token found) {
		messageHeader(found.getLoc(), "ERROR");
		this.message
				.append("Expected ")
				.append(expected)
				.append(" but found ")
				.append(Token.humanTokType(found.getClass()))
				.append(": `")
				.append(found.getText())
				.append('`')
				.append('\n');
		return this;
	}

	/**
	 * @param cause   The token that caused the error.
	 * @param problem The String describing the problem. e.g. "Illegal start of statement"
	 *
	 * @return Itself.
	 */
	public CompilerError badToken(Token cause, String problem) {
		messageHeader(cause.getLoc(), "ERROR");
		this.message
				.append(problem)
				.append(": `")
				.append(cause.getText())
				.append('`')
				.append('\n');
		return this;
	}

	public SyntaxException report() {
		return report(System.err);
	}

	public SyntaxException report(PrintStream err) {
		Tryp.hadError = true;
		String msg = this.message.toString();
		err.print(msg);
		message.setLength(0);
		return new SyntaxException(msg);
	}
}
