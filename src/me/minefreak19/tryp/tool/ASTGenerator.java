package me.minefreak19.tryp.tool;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("SameParameterValue")
public class ASTGenerator {
	public static void main(String[] args) {
//		defineAST("Expr", Arrays.asList(
//				"Binary   : Expr left, OpToken operator, Expr right",
//				"Grouping : Expr expression",
//				"Literal  : Object value",
//				"Unary    : OpToken operator, Expr right"
//		));

		defineAST("Stmt", Arrays.asList(
				"Expression : Expr expr",
				"Print      : Expr expr"
		));
	}

	private static void defineAST(String baseName,
	                              List<String> types) {
		PrintStream w = System.out;

		w.println("package me.minefreak19.tryp.parse;");
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

	private static void defineVisitor(PrintStream w,
	                                  String baseName,
	                                  List<String> types) {
		w.println("\tpublic interface Visitor<R> {");

		for (String type : types) {
			String typeName = type.split(":")[0].trim();
			w.println("\t\tR visit" + typeName + baseName
					          + "(" + typeName + " " + typeName.toLowerCase() + ");");
		}

		w.println("\t}");
	}

	private static void defineType(PrintStream w, String baseName, String className, String fieldList) {
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