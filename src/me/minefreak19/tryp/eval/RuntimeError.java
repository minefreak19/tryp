package me.minefreak19.tryp.eval;

import me.minefreak19.tryp.lex.token.Token;

public class RuntimeError extends RuntimeException {
	public RuntimeError(Token cause, String message) {
		super(cause.getLoc() + ": RUNTIME ERROR: " + message);
	}
}
