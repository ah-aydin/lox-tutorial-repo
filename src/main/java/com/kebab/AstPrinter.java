package com.kebab;

import com.kebab.Expr.Assign;
import com.kebab.Expr.Ternary;
import com.kebab.Expr.Variable;
import com.kebab.Stmt.Block;
import com.kebab.Stmt.Break;
import com.kebab.Stmt.Expression;
import com.kebab.Stmt.If;
import com.kebab.Stmt.Print;
import com.kebab.Stmt.Var;
import com.kebab.Stmt.While;

public class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {

    public String print(Stmt stmt) {
        return stmt.accept(this);
    }

    public String print(Expr expr) {
        return expr.accept(this);
    }

	@Override
	public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
	}

	@Override
	public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
	}

	@Override
	public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
	}

    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

	@Override
	public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
	}

    @Override
    public String visitTernaryExpr(Ternary expr) {
        return parenthesize("?", expr.condition, expr.left, expr.right);
    }

	@Override
	public String visitVariableExpr(Variable expr) {
        return parenthesize("access " + expr.name.lexeme);
	}
    
    @Override
	public String visitAssignExpr(Assign expr) {
        return parenthesize("assign " + expr.name.lexeme + " ", expr.value);
	}

	@Override
	public String visitBlockStmt(Block stmt) {
        StringBuilder builder = new StringBuilder();

        builder.append("{");
        for (Stmt s : stmt.statements) {
            builder.append(";\n");
            builder.append(s.accept(this));
        }
        builder.append("}");

        return builder.toString();
	}

    @Override
    public String visitExpressionStmt(Expression stmt) {
        return parenthesize("Expression", stmt.expression);
    }

    @Override
    public String visitPrintStmt(Print stmt) {
        return parenthesize("Print", stmt.expression);
    }

	@Override
	public String visitVarStmt(Var stmt) {
        return parenthesize("var ", stmt.initializer);
	}

    @Override
    public String visitIfStmt(If stmt) {
        StringBuilder builder = new StringBuilder();

        builder.append("(if (").append(stmt.condition.accept(this)).append(")");
        builder.append("{").append(stmt.thenBranch.accept(this));
        builder.append("}");
        if (stmt.elseBranch != null) {
            builder.append(" else {").append(stmt.elseBranch.accept(this)).append("}");
        }
        builder.append(")");

        return builder.toString();
    }

    @Override
    public String visitWhileStmt(While stmt) {
        StringBuilder builder = new StringBuilder();

        builder.append("(while (").append(stmt.condition.accept(this)).append(")");
        builder.append("{").append(stmt.body.accept(this));
        builder.append("}");
        builder.append(")");

        return builder.toString();
    }

	@Override
	public String visitBreakStmt(Break stmt) {
        StringBuilder builder = new StringBuilder();

        builder.append("(break)");

        return builder.toString();
	}

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }
}
