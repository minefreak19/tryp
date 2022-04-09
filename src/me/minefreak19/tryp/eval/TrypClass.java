package me.minefreak19.tryp.eval;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class TrypClass implements TrypCallable {
	private final String name;
	private final Map<String, TrypProc> methods;

	public TrypClass(String name, Map<String, TrypProc> methods) {
		this.name = name;
		this.methods = methods;
	}

	public TrypProc findMethod(String name) {
		return methods.get(name);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int arity() {
		TrypProc constructor = findMethod("$init");
		if (constructor != null) {
			return constructor.arity();
		}

		return 0;
	}

	@Override
	public Object call(Interpreter interpreter, List<Object> args) {
		var instance = new TrypInstance(this);

		TrypProc constructor = findMethod("$init");
		if (constructor != null) {
			constructor.bind(instance).call(interpreter, args);
		}

		return instance;
	}

	public String name() {
		return name;
	}

	public Map<String, TrypProc> methods() {
		return methods;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		var that = (TrypClass) obj;
		return Objects.equals(this.name, that.name) &&
				       Objects.equals(this.methods, that.methods);
	}
}
