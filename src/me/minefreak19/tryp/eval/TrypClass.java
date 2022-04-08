package me.minefreak19.tryp.eval;

import java.util.List;
import java.util.Map;

public record TrypClass(String name, Map<String, TrypProc> methods) implements TrypCallable {
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
}
