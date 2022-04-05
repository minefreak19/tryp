package me.minefreak19.tryp.eval;

import me.minefreak19.tryp.tree.Stmt;

import java.util.List;

public record TrypProc(Stmt.ProcDecl declaration, Environment closure) implements TrypCallable {
	@Override
	public int arity() {
		return declaration.params.size();
	}

	@Override
	public Object call(Interpreter interpreter, List<Object> args) {
		// We're assuming the closure always leads back through some path to the global scope.
		var env = new Environment(closure);
		for (int i = 0; i < args.size(); i++) {
			env.define(declaration.params.get(i).getText(), args.get(i));
		}
		try {
			interpreter.executeBlock(declaration.body, env);
		} catch (Return ret) {
			return ret.value;
		}

		return null;
	}

	@Override
	public String toString() {
		return "<proc " + declaration.name.getText() + ">";
	}
}
