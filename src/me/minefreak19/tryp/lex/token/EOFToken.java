package me.minefreak19.tryp.lex.token;

import me.minefreak19.tryp.lex.FileLocation;

/**
 * Marker token indicating end of source.
 */
public class EOFToken extends Token {
	public EOFToken(FileLocation loc) {
		super(loc, "<EOF>");
	}
}
