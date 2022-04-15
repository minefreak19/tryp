package me.minefreak19.tryp.lex.token;

import java.util.HashMap;
import java.util.Map;

public enum Keyword {
	CLASS("class"),
	ELSE("else"),
	EXTENDS("extends"),
	FALSE("false"),
	FOR("for"),
	IF("if"),
	INCLUDE("include"),
	NIL("nil"),
	PROC("proc"),
	RETURN("return"),
	STATIC("static"),
	SUPER("super"),
	THIS("this"),
	TRUE("true"),
	VAR("var"),
	WHILE("while"),
	;

	static final Map<String, Keyword> byText;

	static {
		byText = new HashMap<>();

		for (var kw : values()) {
			byText.put(kw.text, kw);
		}
	}

	private final String text;

	Keyword(String text) {
		this.text = text;
	}

	public static Keyword fromText(String text) {
		return byText.get(text);
	}


	@Override
	public String toString() {
		return text;
	}
}
