package com.kebab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;               
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    private Stmt declaration() {
        try {
            if (match(TokenType.CLASS)) return classDeclaration();
            if (match(TokenType.FUNC)) return function("function");
            if (match(TokenType.VAR)) return varDeclaration();
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt classDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expect class name");

        Expr.Variable superclass = null;
        if (match(TokenType.EXTENDS)) {
            consume(TokenType.IDENTIFIER, "Expect superclass name");
            superclass = new Expr.Variable(previous());
        }

        consume (TokenType.LEFT_BRACE, "Expect '{' after class name");

        List<Stmt.Function> methods = new ArrayList<>();
        List<Stmt.Function> staticMethods = new ArrayList<>();
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            if (match(TokenType.STATIC)) {
                staticMethods.add(function("static method"));
            } else {
                methods.add(function("method"));
            }
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after class body.");

        return new Stmt.Class(name, methods, staticMethods, superclass);
    }

    private Stmt.Function function(String kind) {
        Token name = consume(TokenType.IDENTIFIER, "Expect " + kind + " name.");
        consume(TokenType.LEFT_PARENTHESIS, "Expected '(' after " + kind + " name.");
        List<Token> parameters = new ArrayList<>();
        if (!check(TokenType.RIGHT_PARANTHESIS)) {
            do {
                if (parameters.size() >= 255) {
                    error(peek(), "Can't have more then 255 parameters.");
                }
                parameters.add(consume(TokenType.IDENTIFIER, "Expect parameter name"));
            } while (match(TokenType.COMMA));
        }
        consume(TokenType.RIGHT_PARANTHESIS, "Expected ')' after parameters");

        consume(TokenType.LEFT_BRACE, "Expect '{' before " + kind + " body.");
        List<Stmt> body = block();
        return new Stmt.Function(name, parameters, body);
    }

    private Stmt varDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(TokenType.EQUAL)) {
            initializer = expression();
        }

        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration");
        if (initializer instanceof Expr.Lambda) {
            Expr.Lambda lambda = (Expr.Lambda) initializer;
            return new Stmt.Function(name, lambda.params, lambda.body);
        }
            
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement() {
        if (match(TokenType.FOR)) return forStatement();
        if (match(TokenType.IF)) return ifStatement();
        if (match(TokenType.PRINT)) return printStatement();
        if (match(TokenType.RETURN)) return returnStatement();
        if (match(TokenType.LEFT_BRACE)) return new Stmt.Block(block());
        if (match(TokenType.WHILE)) return whileStatement();
        if (match(TokenType.BREAK)) return breakStatement();
        return expressionStatement();
    }

    private Stmt forStatement() {
        consume(TokenType.LEFT_PARENTHESIS, "Expected '(' after 'for'");
        Stmt initializer;
        if (match(TokenType.SEMICOLON)) {
            initializer = null;
        } else if (match(TokenType.VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expr condition = null;
        if (!check(TokenType.SEMICOLON)) {
            condition = expression();
        }
        consume(TokenType.SEMICOLON, "Expected ';' after 'for' condition");

        Expr increment = null;
        if (!check(TokenType.RIGHT_PARANTHESIS)) {
            increment = expression();
        }
        consume(TokenType.RIGHT_PARANTHESIS, "Expected closing ')' at 'for'");

        Stmt body = statement();

        if (increment != null) {
            body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));
        }

        if (condition == null) {
            condition = new Expr.Literal(true);
        }
        body = new Stmt.While(condition, body);

        if (initializer != null) {
            body = new Stmt.Block(Arrays.asList(initializer, body));
        }

        return body;
    }

    private Stmt ifStatement() {
        consume(TokenType.LEFT_PARENTHESIS, "Expect '(' after 'if'");
        Expr condition = expression();
        consume(TokenType.RIGHT_PARANTHESIS, "Expected closing ')' at 'if'");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(TokenType.ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(TokenType.SEMICOLON)) {
            value = ternary();
        }

        consume(TokenType.SEMICOLON, "Expected ';' after a return statement.");
        return new Stmt.Return(keyword, value);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd())  {
            statements.add(declaration());
        }

        consume(TokenType.RIGHT_BRACE, "Expected '}' after block.");

        return statements;
    }

    private Stmt whileStatement() {
        consume(TokenType.LEFT_PARENTHESIS, "Expect '(' after 'while'");
        Expr condition = expression();
        consume(TokenType.RIGHT_PARANTHESIS, "expected closing ')' at 'while'");

        Stmt body = statement();
        return new Stmt.While(condition, body);
    }

    private Stmt breakStatement() {
        Token token = previous();
        consume(TokenType.SEMICOLON, "Expect ';' after 'break'");
        return new Stmt.Break(token);
    }

    private Stmt expressionStatement() {
        Expr value = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after value.");
        return new Stmt.Expression(value);
    }

    private Expr expression() {
        Expr expr = assignment();

        while (match(TokenType.COMMA)) {
            Token operator = previous();
            Expr right = assignment();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr assignment() {
        if (match(TokenType.LEFT_BRACKET)) {
            return lambda();
        }
        Expr expr = ternary();
        
        if (match (TokenType.EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            } else if (expr instanceof Expr.Get) {
                Expr.Get get = (Expr.Get) expr;
                return new Expr.Set(get.object, get.name, value);
            }

            error(equals, "Invalid assignment");
        }

        return expr;
    }

    private Expr lambda() {
        List<Token> parameters = new ArrayList<>();
        if (!check(TokenType.RIGHT_BRACKET)) {
            do {
                if (parameters.size() >= 255) {
                    error(peek(), "Can't have more then 255 parameters");
                }
                parameters.add(consume(TokenType.IDENTIFIER, "Expect parameter name"));
            } while (match(TokenType.COMMA));
        }
        consume(TokenType.RIGHT_BRACKET, "Expected ']' after parameters");

        consume(TokenType.LEFT_BRACE, "Expect '{' before lambda body");
        List<Stmt> body = block();
        return new Expr.Lambda(parameters, body);
    }

    private Expr ternary() {
        Expr expr = or();
        
        if (match(TokenType.QUESTION)) {
            Expr left = or();
            if (!match(TokenType.COLON)) {
                throw error(peek(), "Expected ':' to complete ternary operator '?:'. Usage <condition> ? <expression 1> : <expression 2>");
            }
            Expr right = or();
            expr = new Expr.Ternary(expr, left, right);
        }

        return expr;
    }

    private Expr or() {
        Expr expr = and();

        while (match(TokenType.OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        while (match(TokenType.AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(TokenType.LESS_EQUAL, TokenType.GREATER_EQUAL, TokenType.GREATER, TokenType.LESS)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(TokenType.STAR, TokenType.SLASH)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return call();
    }

    private Expr call() {
        Expr expr = primary();
        
        while (true) {
            if (match(TokenType.LEFT_PARENTHESIS)) {
                expr = finishCall(expr);
            } else if (match(TokenType.DOT)) {
                Token name = consume(TokenType.IDENTIFIER, "Expect property name after '.'");
                expr = new Expr.Get(expr, name);
            }
            else {
                break;
            }
        }

        return expr;
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(TokenType.RIGHT_PARANTHESIS)) {
            if (arguments.size() >= 255) {
                error(peek(), "Can't have more then 255 arguments");
            }
            do {
                arguments.add(assignment());
            } while (match(TokenType.COMMA));
        }

        Token paren = consume(TokenType.RIGHT_PARANTHESIS, "Expect ')' after arguments");

        return new Expr.Call(callee, paren, arguments);
    }

    private Expr primary() {
        if (match(TokenType.FALSE)) return new Expr.Literal(false);
        if (match(TokenType.TRUE)) return new Expr.Literal(true);
        if (match(TokenType.NIL)) return new Expr.Literal(null);

        if (match(TokenType.NUMBER, TokenType.STRING)) return new Expr.Literal(previous().literal);
        if (match(TokenType.THIS)) return new Expr.This(previous());
        if (match(TokenType.IDENTIFIER)) return new Expr.Variable(previous());
        if (match(TokenType.SUPER)) {
            Token keyword = previous();
            consume(TokenType.DOT, "Expect '.' after 'super'.");
            Token method = consume(TokenType.IDENTIFIER, "Expect superclass method name");
            return new Expr.Super(keyword, method);
        }
        if (match(TokenType.LEFT_PARENTHESIS)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PARANTHESIS, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Incomplete syntax");
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        App.error(token, message);
        return new ParseError();
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private void synchronize() {
        advance();
        
        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return;

            switch (peek().type) {
				case AND:
				case CLASS:
				case FOR:
				case FUNC:
				case IF:
				case PRINT:
				case RETURN:
				case VAR:
				case WHILE:
                    return;
				default:
					break;
            }
            advance();
        }
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }
}
