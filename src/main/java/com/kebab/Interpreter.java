package com.kebab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kebab.Expr.Assign;
import com.kebab.Expr.Binary;
import com.kebab.Expr.Call;
import com.kebab.Expr.Get;
import com.kebab.Expr.Grouping;
import com.kebab.Expr.Lambda;
import com.kebab.Expr.Literal;
import com.kebab.Expr.Logical;
import com.kebab.Expr.Set;
import com.kebab.Expr.Super;
import com.kebab.Expr.Ternary;
import com.kebab.Expr.This;
import com.kebab.Expr.Unary;
import com.kebab.Expr.Variable;
import com.kebab.Stmt.Block;
import com.kebab.Stmt.Break;
import com.kebab.Stmt.Expression;
import com.kebab.Stmt.Function;
import com.kebab.Stmt.If;
import com.kebab.Stmt.Print;
import com.kebab.Stmt.Return;
import com.kebab.Stmt.Var;
import com.kebab.Stmt.While;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    private class BreakException extends RuntimeException {
        final Token token;
        public BreakException(Token token) {
            this.token = token;
        }

        public int getLine() {
            return token.getLine();
        }
    }

    final Environment globals = new Environment();
    private Environment environment = globals;
    private Map<Expr, Integer> locals = new HashMap<>();

    public Interpreter() {
        globals.define("clock", new LoxCallable() {
            @Override
            public int arity() { return 0; }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double) System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        globals.define("print", new LoxCallable() {
            @Override
            public int arity() { return 1; }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                System.out.println(arguments.get(0));
                return null;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        globals.define("exit", new LoxCallable() {
            @Override
            public int arity() { return 0; }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                System.exit(0);
                return null;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });
    }

	public void resolve(Expr expr, int i) {
        locals.put(expr, i);
	}

    public void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            App.runtimeError(error);
        } catch (BreakException error) {
            App.report(error.getLine(), "", "'break' must be used inside a loop.");
        }
    }

    @Override
    public Object visitAssignExpr(Assign expr) {
        Object value = evaluate(expr.value);
        Integer distance = locals.get(expr);
        if (distance != null) {
            environment.assignAt(distance, expr.name, value);
        } else {
            globals.assign(expr.name, value);
        }
        //environment.assign(expr.name, value);
        return value;
    }

	@Override
	public Object visitBinaryExpr(Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case COMMA:
                return right;
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                if (((double) right) == 0) {
                    throw new RuntimeError(expr.operator, "Division by 0 is not allowed");
                }
                return (double) left / (double) right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }
                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }
                throw new RuntimeError(expr.operator, "Operands must be two 2 numbers or 2 strings");
            default:
                break;
        }
        // Unreachable
        return null;
	}

	@Override
	public Object visitCallExpr(Call expr) {
        Object callee = evaluate(expr.callee);

        if (!(callee instanceof LoxCallable)) {
            System.out.println(callee);
            throw new RuntimeError(expr.paren, "Can only call functions and classes");
        }

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }
        
        LoxCallable function = (LoxCallable) callee;
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected " + function.arity() + " arguments but got " + arguments.size() + ".");
        }
        return function.call(this, arguments);
	}

    @Override
    public Object visitGetExpr(Get expr) {
        Object object = evaluate(expr.object);
        if (object instanceof LoxInstance) {
            return ((LoxInstance) object).get(expr.name);
        } else if (object instanceof LoxClass) {
            return ((LoxClass) object).getStaticMethod(expr.name.lexeme);
        }

        throw new RuntimeError(
                expr.name,
                "Only properties of an instance and static methods of a class can be accessed this way"
        );
    }

	@Override
	public Object visitTernaryExpr(Ternary expr) {
        Object condition = evaluate(expr.condition);

        if (isTruthy(condition)) {
            return evaluate(expr.left);
        }
        return evaluate(expr.right);
	}

	@Override
	public Object visitGroupingExpr(Grouping expr) {
        return evaluate(expr.expression);
	}

    @Override
    public Object visitLambdaExpr(Lambda expr) {
        return expr;
    }

	@Override
	public Object visitLiteralExpr(Literal expr) {
        return expr.value;
	}

    @Override
    public Object visitLogicalExpr(Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR && isTruthy(left)) {
            return left;
        }
        if (expr.operator.type == TokenType.AND && !isTruthy(left)) {
            return left;
        }
        
        return evaluate(expr.right);
    }

    @Override
    public Object visitSetExpr(Set expr) {
        Object object = evaluate(expr.object);

        if (!(object instanceof LoxInstance)) {
            throw new RuntimeError(expr.name, "Only instances have fields");
        }

        Object value = evaluate(expr.value);
        ((LoxInstance) object).set(expr.name, value);
        return value;
    }

    @Override
    public Object visitSuperExpr(Super expr) {
        int distance = locals.get(expr);
        LoxClass superclass = (LoxClass) environment.getAt(distance, "super");
        LoxInstance object = (LoxInstance) environment.getAt(distance - 1, "this");

        LoxFunction method = superclass.findMethod(expr.method.lexeme);
        if (method == null) {
            throw new RuntimeError(expr.method, "Undefined property '" + expr.method.lexeme + "'.");
        }
        return method.bind(object);
    }

    @Override
    public Object visitThisExpr(This expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return environment.getAt(distance, expr.keyword.lexeme);
        }
        return globals.get(expr.keyword);
    }

	@Override
	public Object visitUnaryExpr(Unary expr) {
        Object right = evaluate(expr.right);
        
        switch (expr.operator.type) {
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double) right;
            case BANG:
                return !isTruthy(right);
            default:
                // Unreachable
                return null;
        }
	}

	@Override
	public Object visitVariableExpr(Variable expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return environment.getAt(distance, expr.name.lexeme);
        }
        return globals.get(expr.name);
        //return environment.get(expr.name);
	}

    @Override
    public Void visitBlockStmt(Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        Object superclass = null;
        if (stmt.superclass != null) {
            superclass = evaluate(stmt.superclass);
            if (!(superclass instanceof LoxClass)) {
                throw new RuntimeError(stmt.superclass.name, "A class can only inherit from another class.");
            }
        }

        environment.define(stmt.name.lexeme, null);

        if (stmt.superclass != null) {
            environment = new Environment(environment);
            environment.define("super", superclass);
        }

        Map<String, LoxFunction> methods = new HashMap<>();
        Map<String, LoxFunction> staticMethods = new HashMap<>();
        for (Stmt.Function method : stmt.methods) {
            LoxFunction function = new LoxFunction(method, environment, method.name.lexeme.equals("init"));
            methods.put(method.name.lexeme, function);
        }
        
        for (Stmt.Function staticMethod : stmt.staticMethods) {
            LoxFunction function = new LoxFunction(staticMethod, environment, false);
            staticMethods.put(staticMethod.name.lexeme, function);
            System.out.println("Adding static method " + staticMethod.name.lexeme + " to " + stmt.name.lexeme);
        }

        LoxClass klass = new LoxClass(stmt.name.lexeme, methods, staticMethods, (LoxClass) superclass);
        if (superclass != null) {
            environment = environment.enclosing;
        }
        environment.assign(stmt.name, klass);
        return null;
    }

	@Override
	public Void visitExpressionStmt(Expression stmt) {
        evaluate(stmt.expression);
        return null;
	}

    @Override
    public Void visitFunctionStmt(Function stmt) {
        LoxFunction function = new LoxFunction(stmt, environment, false);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

	@Override
	public Void visitPrintStmt(Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
	}

    @Override
    public Void visitReturnStmt(Return stmt) {
        Object value = null;
        if (stmt.value != null) value = evaluate(stmt.value);
        throw new ReturnException(value);
    }

	@Override
	public Void visitVarStmt(Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value);
        return null;
	}

    @Override
    public Void visitIfStmt(If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            try {
                execute(stmt.body);
            } catch (BreakException e) {
                break;
            }
        }
        return null;
    }

	@Override
	public Void visitBreakStmt(Break stmt) {
        throw new BreakException(stmt.token);
	}

    public void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

	private String stringify(Object value) {
        if (value == null) return "nil";
        
        if (value instanceof Double) {
            String text = value.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return value.toString();
	}

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;
        return true;
    }

    private boolean isEqual(Object left, Object right) {
        if (left == null && right == null) return true;
        if (left == null) return false;
        return left.equals(right);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be a number");
    }
}
