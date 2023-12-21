package com.interpreter.lox.parser;

import com.interpreter.lox.Lox;
import com.interpreter.lox.lexer.Token;
import com.interpreter.lox.lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

import static com.interpreter.lox.lexer.TokenType.*;

public class Parser {
    private static class ParseError extends RuntimeException{}
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // TODO: Add support for ternary operator (?:)

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();

        while(!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    private Stmt declaration() {
        try {
            if(match(VAR)) {
                return varDeclaration();
            }

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expected variable name.");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expected ';' after statement");
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement() {
        if(match(PRINT)) return printStatement();

        if(match(LEFT_BRACE)) return new Stmt.Block(block());

        return expressionStatement();
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expected ';' after statement");

        return new Stmt.Print(value);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();
        while(!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expected ';' after statement");

        return new Stmt.Expression(expr);
    }

    private Expr expression() { return assignment(); }

    private Expr assignment() {
        // try to parse as equality
        Expr expr = equality();

        // if match EQUALITY means the above expr is an IDENTIFIER (Variable)
        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            // try casting as IDENTIFIER
            if(expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid assignment target.");
        }
        // Just return the equality expression
        return expr;
    }

    // Rule: equality -> comparison (("==" | "!=") comparison)*
    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL ,EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();

            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // Rule: comparison -> term ((">" | ">=" | "<" | "<=") term)*
    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();

            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // Rule: factor (("-" | "+") factor)*
    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // Rule: unary (("/" | "*") unary)*
    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // Rule: ("!" | "-") unary | primary
    private Expr unary() {
        if(match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return  new Expr.Unary(operator, right);
        }
        else if(match(STAR, SLASH, PLUS)) {
            throw error(previous(), "Expected left Hand side of the Binary operator");
        }

        return primary();
    }

    private Expr primary() {
        if(match(FALSE)) return  new Expr.Literal(FALSE);
        if(match(TRUE)) return new Expr.Literal(TRUE);
        if(match(NIL)) return new Expr.Literal(NIL);
        if(match(NUMBER, STRING)) {
            return  new Expr.Literal(previous().literal);
        }
        if(match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expected ')' after expression.");
            return new Expr.Grouping(expr);
        }
        if(match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        throw error(peek(), "Expect expression.");
    }

    private Token consume(TokenType type, String message) {
        if(check(type)) return advance();

        throw  error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }
            advance();
        }
    }

    private boolean match(TokenType...types) {
        for(TokenType type: types) {
            if(check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token advance() {
        if(!isAtEnd()) current++;
        return previous();
    }

    private Token previous() {
        return tokens.get(current - 1);
    }


    private boolean check(TokenType type) {
        if(isAtEnd()) return false;
        return peek().type == type;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }
}
