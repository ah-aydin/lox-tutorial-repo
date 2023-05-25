package com.kebab;

import com.kebab.Expr.Binary;
import com.kebab.Expr.Grouping;
import com.kebab.Expr.Literal;
import com.kebab.Expr.Ternary;
import com.kebab.Expr.Unary;

public class Interpreter implements Expr.Visitor<Object> {

    public void interpret(Expr expression) {
        try {
            Object value = evaluate(expression);
            System.out.println(stringify(value));
        } catch (RuntimeError error) {
            App.runtimeError(error);
        }
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
	public Object visitLiteralExpr(Literal expr) {
        return expr.value;
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
