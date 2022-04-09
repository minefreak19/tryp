package me.minefreak19.tryp.eval;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class TrypClass extends TrypInstance implements TrypCallable {
	private final String name;
	private final Map<String, TrypProc> methods;
	private final TrypClass superclass;

	public TrypClass(String name, TrypClass superclass, Map<String, TrypProc> methods) {
		{
			String mName = "$static$" + name;
//			var mMethods
//					= methods.entrySet().stream()
//					.filter(entry -> entry.getValue().isStatic())
//					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			var mMethods = new HashMap<String, TrypProc>();
			for (var entry : methods.entrySet()) {
				if (entry.getValue().isStatic()) {
					methods.remove(entry.getKey());
					mMethods.put(entry.getKey(), entry.getValue());
				}
			}

			this.klass = new TrypClass(mName, superclass, mMethods, 0);
		}

		this.name = name;
		this.superclass = superclass;
		this.methods = methods;
	}

	// Avoids the metaclass logic of the public constructor.
	@SuppressWarnings("unused")
	private TrypClass(String name, TrypClass superclass, Map<String, TrypProc> methods, int dummy) {
		this.name = name;
		this.superclass = superclass;
		this.methods = methods;
	}

	public TrypProc findMethod(String name) {
		if (methods.containsKey(name)) {
			return methods.get(name);
		}

		if (superclass != null) {
			return superclass.findMethod(name);
		}

		return null;
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
