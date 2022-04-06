package me.minefreak19.tryp.tool;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("SameParameterValue")
public class ASTGenerator {
	public static void main(String[] args) throws FileNotFoundException {
		final String outDir = "src/me/minefreak19/tryp/tree";
		defineAST(outDir, "Expr", Arrays.asList(
				// This gets a separate AST node, because its lhs can't be any old expression.
				"Assign   : Token name, Expr value",
				"Binary   : Expr left, OpToken operator, Expr right",
				"Call     : Expr callee, OpToken paren, List<Expr> args",
				"Grouping : Expr expression",
				"Lambda   : OpToken lambda, List<Token> params, List<Stmt> body",
				"Literal  : Object value",
				"Logical  : Expr left, OpToken operator, Expr right",
				"Unary    : OpToken operator, Expr right",
				"Variable : Token name"
		));

		defineAST(outDir, "Stmt", Arrays.asList(
				"Block      : List<Stmt> statements",
				"Expression : Expr expr",
				"If         : Expr condition, Stmt thenBranch," +
						" Stmt elseBranch",
				"Print      : Expr expr",
				"ProcDecl   : Token name, List<Token> params, List<Stmt> body",
				"Return     : Token kw, Expr value",
				"Var        : Token name, Expr initializer",
				"While      : Expr condition, Stmt body"
		));
	}

	private static void defineAST(String outDir, String baseName,
	                              List<String> types) throws FileNotFoundException {
		PrintWriter w = new PrintWriter(outDir + "/" + baseName + ".java");

		w.println("package me.minefreak19.tryp.tree;");
		w.println();
		w.println("import me.minefreak19.tryp.lex.token.*;");
		w.println();
		w.println("import java.util.*;");
		w.println();
		w.println("public abstract class " + baseName + " {");

		defineVisitor(w, baseName, types);
		w.println();

		for (String type : types) {
			String className = type.split(":")[0].trim();
			String fields = type.split(":")[1].trim();
			defineType(w, baseName, className, fields);
			w.println();
		}

		w.println("\tpublic abstract <R> R accept(Visitor<R> visitor);");

		w.println("}");
		w.close();
	}

	private static void defineVisitor(PrintWriter w,
	                                  String baseName,
	                                  List<String> types) {
		w.println("\tpublic interface Visitor<R> {");

		for (String type : types) {
			String typeName = type.split(":")[0].trim();
			w.println("\t\tR visit" + typeName + baseName
					          + "(" + typeName + " " + baseName.toLowerCase() + ");");
		}

		w.println("\t}");
	}

	private static void defineType(PrintWriter w, String baseName, String className, String fieldList) {
		w.println("\tpublic static class " + className + " extends " + baseName + " {");

		String[] fields;

		// Constructor
		{
			w.println("\t\tpublic " + className + "(" + fieldList + ") {");

			fields = fieldList.split(", ");
			for (String field : fields) {
				String name = field.split(" ")[1];
				w.println("\t\t\tthis." + name + " = " + name + ";");
			}

			w.println("\t\t}");
		}

		w.println();

		// Visitor pattern
		{
			w.println("\t\t@Override");
			w.println("\t\tpublic <R> R accept(Visitor<R> visitor) {");
			w.println("\t\t\treturn visitor.visit" + className + baseName + "(this);");
			w.println("\t\t}");
		}

		w.println();

		// Fields
		{
			for (String field : fields) {
				w.println("\t\tpublic final " + field + ";");
			}
		}

		w.println("\t}");
	}
}
