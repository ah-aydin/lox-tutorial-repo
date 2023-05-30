package com.kebab;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;
    private final Environment closure;
    private final boolean isInitializer;
    
    LoxFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
    }

	@Override
	public int arity() {
        return declaration.params.size();
	}

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < declaration.params.size(); ++i) {
            Object argument = arguments.get(i);
            if (argument instanceof Expr.Lambda) {
                Expr.Lambda lambda = (Expr.Lambda) argument;
                Stmt.Function functionStmt = new Stmt.Function(declaration.params.get(i), lambda.params, lambda.body);
                LoxFunction function = new LoxFunction(functionStmt, closure, false);
                environment.define(declaration.params.get(i).lexeme, function);
                continue;
            }
            environment.define(declaration.params.get(i).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (ReturnException returnValue) {
            if (isInitializer) {
                return closure.getAt(0, "this");
            }
            return returnValue.value;
        }

        if (isInitializer) return closure.getAt(0, "this");
        return null;
	}

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }

	public LoxFunction bind(LoxInstance loxInstance) {
        Environment environment = new Environment(closure);
        environment.define("this", loxInstance);
        return new LoxFunction(declaration, environment, isInitializer);
	}
}
