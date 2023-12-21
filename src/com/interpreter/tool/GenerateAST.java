package com.interpreter.tool;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class GenerateAST {
    public static void main(String[] args) throws IOException {
        if(args.length != 1) {
            System.err.println("Usage: generate_ast <output_dir>");
            System.exit(64);
        }
        String outputDir = args[0];

        defineAST(outputDir, "Expr", Arrays.asList(
                "Binary : Expr left, Token operator, Expr right",
                "Grouping : Expr expression",
                "Literal : Object value",
                "Unary : Token operator, Expr right"
        ));

        defineAST(outputDir, "Stmt", Arrays.asList(
                "Expression : Expr expression",
                "Print : Expr expression"
        ));
    }

    private static void defineAST(String outputDir, String baseName, List<String> types) throws IOException {
        Path p = Path.of(outputDir, baseName + ".java");
        OutputStream file = Files.newOutputStream(p);
        PrintWriter writer = new PrintWriter(file);

        writer.println("package com.interpreter.lox.parser;");
        writer.println();
        writer.println("import com.interpreter.lox.lexer.Token;");
        writer.println();
//        writer.println("import java.util.List;");
//        writer.println();

        writer.println("public abstract class " + baseName + " {");

        defineVisitor(writer, baseName, types);

        for(String type: types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();

            defineType(writer, baseName, className, fields);
        }

        writer.println();
        writer.println("    public abstract <R> R accept(Visitor<R> visitor);");

        writer.println(" }");

        writer.flush();
        writer.close();
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("    public interface Visitor<R> {");
        for(String type:types) {
            String typeName = type.split(":")[0].trim();
            writer.println("        R visit" + typeName + baseName + "(" +
                            typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("    }");
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldsList) {
        writer.println("    public static class " + className + " extends " + baseName + " {");
        writer.println("        " + className + "(" + fieldsList + ") {");

        String[] fields = fieldsList.split(",");
        for(String field: fields) {
            field = field.trim();
            String name = field.split(" ")[1];
            writer.println("            this." + name + " = " + name + " ; ");
        }

        writer.println("        }");

        writer.println();
        for(String field: fields) {
            writer.println("        public final " + field + ";");
        }

        writer.println();
        writer.println("        @Override");
        writer.println("        public <R> R accept(Visitor<R> visitor) {");
        writer.println("            return visitor.visit" + className + baseName + "(this);");
        writer.println("        }");

        writer.println("    }");
    }
}
