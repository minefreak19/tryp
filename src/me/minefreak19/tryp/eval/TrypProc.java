package me.minefreak19.tryp.eval;

import me.minefreak19.tryp.tree.Stmt;

import java.util.List;

public record TrypProc(Stmt.ProcDecl declaration) implements TrypCallable {
	@Override
	public int arity() {
		return declaration.params.size();
	}

	@Override
	public Object call(Interpreter interpreter, List<Object> args) {
		var env = new Environment(interpreter.getEnvironment());
		for (int i = 0; i < args.size(); i++) {
			env.define(declaration.params.get(i).getText(), args.get(i));
		}
		interpreter.executeBlock(declaration.body, env);

		return null;
	}

	@Override
	public String toString() {
		return "<proc " + declaration.name.getText() + ">";
	}
}
