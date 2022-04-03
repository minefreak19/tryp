package me.minefreak19.tryp.lex.token;

import me.minefreak19.tryp.lex.FileLocation;

public class IntegerToken extends Token {
	private final int value;

	public IntegerToken(FileLocation loc, String text) {
		this(loc, text, Integer.parseInt(text));
	}

	public IntegerToken(FileLocation loc, String text, int value) {
		super(loc, text);
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
