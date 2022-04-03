package me.minefreak19.tryp.lex.token;

import me.minefreak19.tryp.lex.FileLocation;

public abstract class Token {
	private FileLocation loc;
	private String text;

	public Token(FileLocation loc, String text) {
		if (loc != null)
			this.loc = new FileLocation(loc);
		this.text = text;
	}

	public static String humanTokType(Class<? extends Token> tokType) {
		if (tokType == StringToken.class) return "string literal";
		else if (tokType == OpToken.class) return "operator";
		else if (tokType == KeywordToken.class) return "keyword";
		else if (tokType == NumberToken.class) return "integer";
		else if (tokType == IdentifierToken.class) return "identifier";
		else {
			assert false : "unreachable";
			return null;
		}
	}

	public Token() {
		this(null, null);
	}

	public FileLocation getLoc() {
		return loc;
	}

	public void setLoc(FileLocation loc) {
		this.loc = loc;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return this.loc.toString() + ": "
				       + "(" + this.getClass().getSimpleName() + ") "
				       + this.text;
	}
}
