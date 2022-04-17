package me.minefreak19.tryp.lex.token;

import java.util.HashMap;
import java.util.Map;

public enum Operator {
	// NOTE: These must be declared in decreasing order of
	//  string length, so that the longer operators are checked
	//  first during lexing. (i.e. check >= before >)
	RIGHT_ARROW("->"),
	GREATER_EQUAL(">="),
	LESS_EQUAL("<="),
	EQUAL_EQUAL("=="),
	BANG_EQUAL("!="),
	AND_AND("&&"),
	OR_OR("||"),
	PLUS_EQUALS("+="),
	MINUS_EQUALS("-="),
	STAR_EQUALS("*="),
	SLASH_EQUALS("/="),
	PERCENT_EQUALS("%="),
	GREATER_THAN(">"),
	LESS_THAN("<"),
	EQUAL("="),
	BANG("!"),
	MINUS("-"),
	PLUS("+"),
	SLASH("/"),
	STAR("*"),
	PERCENT("%"),
	OPEN_CURLY("{"),
	CLOSE_CURLY("}"),
	OPEN_PAREN("("),
	CLOSE_PAREN(")"),
	SEMICOLON(";"),
	COMMA(","),
	BACKSLASH("\\"),
	DOT("."),
	QUESTION("?"),
	COLON(":"),
	;

	private static final Map<String, Operator> byText;

	static {
		initShorthands();

		byText = new HashMap<>();

		for (var op : values()) {
			byText.put(op.text, op);
		}
	}

	private static void initShorthands() {
		PLUS_EQUALS.shorthandAssignmentFor = PLUS;
		MINUS_EQUALS.shorthandAssignmentFor = MINUS;
		STAR_EQUALS.shorthandAssignmentFor = STAR;
		SLASH_EQUALS.shorthandAssignmentFor = SLASH;
		PERCENT_EQUALS.shorthandAssignmentFor = PERCENT;
	}

	public final String text;
	public Operator shorthandAssignmentFor;

	Operator(String text) {
		this.text = text;
		this.shorthandAssignmentFor = null;
	}

	public static Operator fromText(String text) {
		return byText.get(text);
	}
}
