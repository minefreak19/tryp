package me.minefreak19.tryp.eval;

import me.minefreak19.tryp.lex.token.Token;

import java.util.HashMap;
import java.util.Map;

public class TrypInstance {
	protected TrypClass klass;
	private final Map<String, Object> fields = new HashMap<>();

	protected TrypInstance() {
		this(null);
	}

	public TrypInstance(TrypClass klass) {
		this.klass = klass;
	}

	public Object get(Token name) {
		if (fields.containsKey(name.getText())) {
			return fields.get(name.getText());
		}

		TrypProc method = klass.findMethod(name.getText());
		if (method != null) return method.bind(this);

		throw new RuntimeError(name, "Undefined property `" + name.getText() + "`");
	}

	public void set(Token name, Object value) {
		fields.put(name.getText(), value);
	}

	@Override
	public String toString() {
		return "<" + klass.name() + " instance>";
	}
}
