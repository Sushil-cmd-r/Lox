package com.interpreter.lox.parser;

import com.interpreter.lox.lexer.Token;

import java.util.List;

public abstract class Stmt {
    public interface Visitor<R> {
        R visitBlockStmt(Block stmt);
        R visitExpressionStmt(Expression stmt);
        R visitPrintStmt(Print stmt);
        R visitVarStmt(Var stmt);
    }
    public static class Block extends Stmt {
        Block(List<Stmt> statements) {
            this.statements = statements ; 
        }

        public final List<Stmt> statements;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }
    public static class Expression extends Stmt {
        Expression(Expr expression) {
            this.expression = expression ; 
        }

        public final Expr expression;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }
    public static class Print extends Stmt {
        Print(Expr expression) {
            this.expression = expression ; 
        }

        public final Expr expression;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }
    }
    public static class Var extends Stmt {
        Var(Token name, Expr initializer) {
            this.name = name ; 
            this.initializer = initializer ; 
        }

        public final Token name;
        public final  Expr initializer;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarStmt(this);
        }
    }

    public abstract <R> R accept(Visitor<R> visitor);
 }
