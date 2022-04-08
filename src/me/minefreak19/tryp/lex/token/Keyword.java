package me.minefreak19.tryp.lex.token;

import java.util.HashMap;
import java.util.Map;

public enum Keyword {
	PROC("proc"),
	IF("if"),
	ELSE("else"),
	VAR("var"),
	TRUE("true"),
	FALSE("false"),
	NIL("nil"),
	WHILE("while"),
	RETURN("return"),
	FOR("for"),
	PRINT("print"),
	CLASS("class"),
	THIS("this"),
	;

	static Map<String, Keyword> byText;

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
