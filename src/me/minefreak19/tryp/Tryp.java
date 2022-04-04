package me.minefreak19.tryp;

import me.minefreak19.tryp.eval.Interpreter;
import me.minefreak19.tryp.lex.FileLocation;
import me.minefreak19.tryp.lex.Lexer;
import me.minefreak19.tryp.parse.Parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

public class Tryp {
	private final Interpreter interpreter = new Interpreter();

	public static void main(String[] args) throws IOException {
		if (args.length > 1) {
			System.err.println("Usage: tryp [sourcefile]");
			System.exit(64);
		} else if (args.length == 1) {
			new Tryp(new File(args[0]));
		} else {
			new Tryp();
		}
	}

	/**
	 * Runs tryp code from source file.
	 *
	 * @param file Sourcefile.
	 */
	private Tryp(File file) throws IOException {
		runFile(file);
	}

	private Tryp() throws IOException {
		runREPL();
	}

	private void runFile(File file) throws IOException {
		String source = Files.readString(file.toPath());
		run(source, file.getName());
	}

	private void runREPL() throws IOException {
		var br = new BufferedReader(new InputStreamReader(System.in));

		while (true) {
			System.out.print("> ");
			String line = br.readLine();

			if (line == null) return;
			run(line, "<stdin>");
		}
	}

	private void run(String source, String fileName) {
		Lexer lexer = new Lexer(source, new FileLocation(fileName, 1, 1));
		var tokens = lexer.tokens();

		Parser parser = new Parser(tokens);
		var program = parser.parse();
		if (parser.hadError()) return;

		interpreter.interpret(program);
	}
}
