package me.minefreak19.tryp.eval;

import java.util.List;

public interface TrypCallable {
	int arity();

	Object call(Interpreter interpreter, List<Object> args);
}
