package com.kebab;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

    private enum ClassType {
        NONE,
        CLASS,
        SUBCLASS
    }

    private enum FunctionType {
        NONE,
        FUNCTION,
        INITIALIAER,
        METHOD,
        STATIC_METHOD
    }

    private enum WhileType {
        NONE,
        WHILE
    }

    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private final Map<String, Boolean> globalScope = new HashMap<String, Boolean>();

    private ClassType currentClass = ClassType.NONE;
    private FunctionType currentFunction = FunctionType.NONE;
    private WhileType currentWhile = WhileType.NONE;

    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    public void resolve(List<Stmt> statements) {
        for (Stmt statement : statements) {
            resolve(statement);
        }
    }
    
    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.callee);
        for (Expr argument : expr.arguments) {
            resolve(argument);
        }
        return null;
    }

    @Override
    public Void visitGetExpr(Expr.Get expr) {
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLambdaExpr(Expr.Lambda expr) {
        beginScope();
        for (Token param : expr.params) {
            declare(param);
            define(param);
        }
        resolve(expr.body);
        endScope();
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitSetExpr(Expr.Set expr) {
        resolve(expr.value);
        resolve(expr.object);
        return null;
    }

	@Override
	public Void visitSuperExpr(Expr.Super expr) {
        if (currentClass == ClassType.NONE) {
            App.error(expr.keyword, "Cannot use 'super' outisde of a class");
        } else if (currentClass == ClassType.CLASS) {
            App.error(expr.keyword, "Cannot user 'super' without inheriting from a superclass");
        }
        resolveLocal(expr, expr.keyword);
        return null;
	}

    @Override
    public Void visitThisExpr(Expr.This expr) {
        if (currentClass == ClassType.NONE) {
            App.error(expr.keyword, "Can't use 'this' outside of a class.");
            return null;
        }
        resolveLocal(expr, expr.keyword);
        return null;
    }

	@Override
	public Void visitTernaryExpr(Expr.Ternary expr) {
        resolve(expr.condition);
        resolve(expr.left);
        resolve(expr.right);
        return null;
	}

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        if (!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
            App.error(expr.name, "Can't read local variable in its own initializer.");
        }

        resolveLocal(expr, expr.name);
        return null;
    }


    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        if (currentWhile == WhileType.NONE) {
            App.error(stmt.token, "Can't break from outside a loop");
        }
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        ClassType enclosingClass = currentClass;
        currentClass = ClassType.CLASS;

        declare(stmt.name);
        define(stmt.name);
        
        if (stmt.superclass != null) {
            if (stmt.name.lexeme.equals(stmt.superclass.name.lexeme)) {
                App.error(stmt.superclass.name, "A class cannot inherit from itself");
            } else {
                currentClass = ClassType.SUBCLASS;
                resolve(stmt.superclass);
            }
            beginScope();
            scopes.peek().put("super", true);
        }

        beginScope();
        scopes.peek().put("this", true);

        for (Stmt.Function method : stmt.methods) {
            resolveFunction(method, FunctionType.METHOD);
        }
        for (Stmt.Function staticMethod : stmt.methods) {
            resolveFunction(staticMethod, FunctionType.STATIC_METHOD);
        }
        endScope();

        if (stmt.superclass != null) {
            endScope();
        }

        currentClass = enclosingClass;
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        declare(stmt.name);
        define(stmt.name);
        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null) {
            resolve(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (currentFunction == FunctionType.NONE) {
            App.error(stmt.keyword, "Can't return from outside a function");
        }
        if (stmt.value != null) {
            if (currentFunction == FunctionType.INITIALIAER) {
                App.error(stmt.keyword, "Can't return a value from a constructor");
            }
            resolve(stmt.value);
        }
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        declare(stmt.name);
        if (stmt.initializer != null) {
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        WhileType enclosingWhile = currentWhile;
        currentWhile = WhileType.WHILE;
        resolve(stmt.condition);
        resolve(stmt.body);
        currentWhile = enclosingWhile;
        return null;
    }

    private void resolveFunction(Stmt.Function function, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        beginScope();
        for (Token param : function.params) {
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();

        currentFunction = enclosingFunction;
    }

    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; --i) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size() - 1 - i);
            }
        }
    }

    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }

    private void beginScope() {
        scopes.push(new HashMap<String, Boolean>());
    }

    private void endScope() {
        scopes.pop();
    }

    private void declare(Token name) {
        Map<String, Boolean> scope;
        if (scopes.isEmpty()) {
            scope = globalScope;
        } else {
            scope = scopes.peek();
        }

        if (scope.containsKey(name.lexeme)) {
            App.error(name, "A variable with this name allready exists");
        }
        scope.put(name.lexeme, false);
    }

    private void define(Token name) {
        Map<String, Boolean> scope;
        if (scopes.isEmpty()) {
            scope = globalScope;
        } else {
            scope = scopes.peek();
        }
        scope.put(name.lexeme, true);
    }
}
