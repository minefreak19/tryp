package me.minefreak19.tryp.lex.token;

import me.minefreak19.tryp.lex.FileLocation;

public class OpToken extends Token {
	private final Operator value;

	public OpToken(FileLocation loc, String text) {
		super(loc, text);
		this.value = Operator.fromText(text);

		if (this.value == null) {
			throw new IllegalArgumentException("`" + text + "` is not an operator");
		}
	}

	public OpToken(FileLocation loc, String text, Operator value) {
		super(loc, text);
		this.value = value;
	}

	public Operator getValue() {
		return value;
	}
}
