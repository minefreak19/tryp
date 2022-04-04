package me.minefreak19.tryp.lex.token;

import java.util.HashMap;
import java.util.Map;

public enum Operator {
	// NOTE: These must be declared in decreasing order of
	//  string length, so that the longer operators are checked
	//  first during lexing. (i.e. check >= before >)
	LEFT_ARROW("<-"),
	GREATER_EQUAL(">="),
	LESS_EQUAL("<="),
	EQUAL_EQUAL("=="),
	BANG_EQUAL("!="),
	AND_AND("&&"),
	OR_OR("||"),
	GREATER_THAN(">"),
	LESS_THAN("<"),
	EQUAL("="),
	BANG("!"),
	MINUS("-"),
	PLUS("+"),
	SLASH("/"),
	STAR("*"),
	OPEN_CURLY("{"),
	CLOSE_CURLY("}"),
	OPEN_PAREN("("),
	CLOSE_PAREN(")"),
	SEMICOLON(";"),
	COMMA(","),
	;

	private static final Map<String, Operator> byText;

	static {
		byText = new HashMap<>();

		for (var op : values()) {
			byText.put(op.text, op);
		}
	}

	public final String text;

	Operator(String text) {
		this.text = text;
	}

	public static Operator fromText(String text) {
		return byText.get(text);
	}
}
