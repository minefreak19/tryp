package me.minefreak19.tryp;

import me.minefreak19.tryp.eval.Interpreter;
import me.minefreak19.tryp.lex.Lexer;
import me.minefreak19.tryp.parse.Parser;

import java.io.File;
import java.io.IOException;

public class Main {
	public static void main(String[] args) throws IOException {
		var lexer = new Lexer(new File("./hello.tryp"));

		var tokens = lexer.tokens();
//        tokens.forEach(System.out::println);

		var parser = new Parser(tokens);
		Expr expr = parser.parse();

		if (expr == null) {
			return;
		}

//		System.out.println(new ASTPrinter().print(expr));

		new Interpreter().interpret(expr);
	}
}
