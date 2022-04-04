package me.minefreak19.tryp.eval;

import me.minefreak19.tryp.lex.token.Token;

import java.util.HashMap;
import java.util.Map;

public class Environment {
	private final Environment parent;
	private final Map<String, Object> values = new HashMap<>();

	public Environment() {
		this.parent = null;
	}

	public Environment(Environment parent) {
		this.parent = parent;
	}

	public void define(String name, Object value) {
		// Maybe a redefinition check would be something to think about.
		values.put(name, value);
	}

	public Object get(Token name) {
		if (values.containsKey(name.getText())) {
			return values.get(name.getText());
		}

		if (this.parent != null) {
			return this.parent.get(name);
		}

		throw new RuntimeError(name, "Undefined variable `" + name.getText() + "`.");
	}

	public void assign(Token name, Object value) {
		if (values.containsKey(name.getText())) {
			values.put(name.getText(), value);
			return;
		}

		if (this.parent != null) {
			this.parent.assign(name, value);
			return;
		}

		throw new RuntimeError(name, "Undefined variable `" + name.getText() + "`.");
	}

	public Environment getParent() {
		return parent;
	}
}
