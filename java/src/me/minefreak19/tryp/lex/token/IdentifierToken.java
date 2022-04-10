package me.minefreak19.tryp.lex.token;

import me.minefreak19.tryp.lex.FileLocation;

public class IdentifierToken extends Token {
	public IdentifierToken(FileLocation loc, String text) {
		super(loc, text);
	}
}
