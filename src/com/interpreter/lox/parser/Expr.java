package com.interpreter.lox.parser;

import com.interpreter.lox.lexer.Token;

public abstract class Expr {
    public interface Visitor<R> {
        R visitBinaryExpr(Binary expr);
        R visitGroupingExpr(Grouping expr);
        R visitLiteralExpr(Literal expr);
        R visitUnaryExpr(Unary expr);
    }
    public static class Binary extends Expr {
        Binary(Expr left, Token operator, Expr right) {
            this.left = left ; 
            this.operator = operator ; 
            this.right = right ; 
        }

        public final Expr left;
        public final  Token operator;
        public final  Expr right;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }
    public static class Grouping extends Expr {
        Grouping(Expr expression) {
            this.expression = expression ; 
        }

        public final Expr expression;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }
    public static class Literal extends Expr {
        Literal(Object value) {
            this.value = value ; 
        }

        public final Object value;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }
    public static class Unary extends Expr {
        Unary(Token operator, Expr right) {
            this.operator = operator ; 
            this.right = right ; 
        }

        public final Token operator;
        public final  Expr right;

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    public abstract <R> R accept(Visitor<R> visitor);
 }