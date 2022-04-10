package me.minefreak19.tryp.lex.token;

import me.minefreak19.tryp.lex.FileLocation;

public class NumberToken extends Token {
	private final double value;

	public NumberToken(FileLocation loc, String text) {
		this(loc, text, Double.parseDouble(text));
	}

	public NumberToken(FileLocation loc, String text, double value) {
		super(loc, text);
		this.value = value;
	}

	public double getValue() {
		return value;
	}
}
