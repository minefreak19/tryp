package me.minefreak19.tryp.eval;

import me.minefreak19.tryp.lex.token.IdentifierToken;
import me.minefreak19.tryp.tree.Stmt;

import java.util.List;

public record TrypProc(Stmt.ProcDecl declaration, Environment closure) implements TrypCallable {
	public TrypProc bind(TrypInstance instance) {
		var env = new Environment(closure);
		env.define("this", instance);
		return new TrypProc(declaration, env);
	}

	@Override
	public int arity() {
		return declaration.params.size();
	}

	@Override
	public Object call(Interpreter interpreter, List<Object> args) {
		// We're assuming the closure always leads back through some path to the global scope.
		boolean isConstructor = declaration.name.getText().equals("$init");

		var env = new Environment(closure);
		for (int i = 0; i < args.size(); i++) {
			env.define(declaration.params.get(i).getText(), args.get(i));
		}
		try {
			interpreter.executeBlock(declaration.body, env);
		} catch (Return ret) {
			// return instance itself from constructor
			if (isConstructor) return closure.getAt(0, new IdentifierToken(null, "this"));

			return ret.value;
		}
		if (isConstructor) return closure.getAt(0, new IdentifierToken(null, "this"));

		return null;
	}

	@Override
	public String toString() {
		return "<proc " + declaration.name.getText() + ">";
	}
}
