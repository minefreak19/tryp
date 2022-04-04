package me.minefreak19.tryp.lex.token;

import me.minefreak19.tryp.lex.FileLocation;

public class StringToken extends Token {
	private final String value;

	public StringToken(FileLocation loc, String text, String value) {
		super(loc, text);
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
