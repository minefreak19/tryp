package me.minefreak19.tryp;

import me.minefreak19.tryp.lex.FileLocation;

public class SyntaxException extends RuntimeException {

	private final FileLocation loc;

	public SyntaxException(FileLocation loc, String message) {
		super(message);
		this.loc = loc;
	}

	public SyntaxException() {
		super();
		this.loc = null;
	}

	public SyntaxException(String message) {
		super(message);
		this.loc = null;
	}

	@Override
	public String toString() {
		return loc + ": " + super.toString();
	}
}
