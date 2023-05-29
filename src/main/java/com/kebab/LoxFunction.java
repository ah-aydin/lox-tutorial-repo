package com.kebab;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;
    private final Environment closure;
    
    LoxFunction(Stmt.Function declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
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
                System.out.println("Found lambda expr as argument");
                Expr.Lambda lambda = (Expr.Lambda) argument;
                Stmt.Function functionStmt = new Stmt.Function(declaration.params.get(i), lambda.params, lambda.body);
                LoxFunction function = new LoxFunction(functionStmt, closure);
                environment.define(declaration.params.get(i).lexeme, function);
                continue;
            }
            environment.define(declaration.params.get(i).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (ReturnException returnValue) {
            return returnValue.value;
        }
        return null;
	}

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }
}
