package me.minefreak19.tryp.lex.token;

import me.minefreak19.tryp.lex.FileLocation;

public class KeywordToken extends Token {
	private final Keyword value;

	public KeywordToken(FileLocation loc, String text) {
		super(loc, text);
		this.value = Keyword.fromText(text);

		if (this.value == null) {
			throw new IllegalArgumentException("`" + text + "` is not a keyword");
		}
	}

	public Keyword getValue() {
		return value;
	}
}
