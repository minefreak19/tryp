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

	public Object getAt(int depth, Token name) {
		return this.ancestor(depth).get(name);
	}

	private Environment ancestor(int depth) {
		var env = this;
		for (int i = 0; i < depth; i++) {
			if (env == null) {
				throw new IllegalArgumentException("depth " + depth + " too high! max " + i);
			}
			env = env.parent;
		}

		return env;
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

	public void assignAt(int depth, Token name, Object value) {
		this.ancestor(depth).values.put(name.getText(), value);
	}

	public Environment getParent() {
		return parent;
	}
}
