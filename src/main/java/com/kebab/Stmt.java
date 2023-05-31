package com.kebab;

import java.util.List;

abstract class Stmt {
	interface Visitor<R> {
		R visitBlockStmt(Block stmt);
		R visitBreakStmt(Break stmt);
		R visitClassStmt(Class stmt);
		R visitExpressionStmt(Expression stmt);
		R visitFunctionStmt(Function stmt);
		R visitIfStmt(If stmt);
		R visitPrintStmt(Print stmt);
		R visitReturnStmt(Return stmt);
		R visitVarStmt(Var stmt);
		R visitWhileStmt(While stmt);
	}

	static class Block extends Stmt {
		final List<Stmt> statements;
		Block (List<Stmt> statements) {
			this.statements = statements;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBlockStmt(this);
		}
		@Override
		public String toString() {
			return "(Block: " + statements.toString() + ")";
		}
	}

	static class Break extends Stmt {
		final Token token;
		Break (Token token) {
			this.token = token;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBreakStmt(this);
		}
		@Override
		public String toString() {
			return "(Break: " + token.toString() + ")";
		}
	}

	static class Class extends Stmt {
		final Token name;
		final List<Stmt.Function> methods;
		final List<Stmt.Function> staticMethods;
		Class (Token name, List<Stmt.Function> methods, List<Stmt.Function> staticMethods) {
			this.name = name;
			this.methods = methods;
			this.staticMethods = staticMethods;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitClassStmt(this);
		}
		@Override
		public String toString() {
			return "(Class: " + name.toString() + " | " + methods.toString() + " | " + staticMethods.toString() + ")";
		}
	}

	static class Expression extends Stmt {
		final Expr expression;
		Expression (Expr expression) {
			this.expression = expression;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitExpressionStmt(this);
		}
		@Override
		public String toString() {
			return "(Expression: " + expression.toString() + ")";
		}
	}

	static class Function extends Stmt {
		final Token name;
		final List<Token> params;
		final List<Stmt> body;
		Function (Token name, List<Token> params, List<Stmt> body) {
			this.name = name;
			this.params = params;
			this.body = body;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitFunctionStmt(this);
		}
		@Override
		public String toString() {
			return "(Function: " + name.toString() + " | " + params.toString() + " | " + body.toString() + ")";
		}
	}

	static class If extends Stmt {
		final Expr condition;
		final Stmt thenBranch;
		final Stmt elseBranch;
		If (Expr condition, Stmt thenBranch, Stmt elseBranch) {
			this.condition = condition;
			this.thenBranch = thenBranch;
			this.elseBranch = elseBranch;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitIfStmt(this);
		}
		@Override
		public String toString() {
			return "(If: " + condition.toString() + " | " + thenBranch.toString() + " | " + elseBranch.toString() + ")";
		}
	}

	static class Print extends Stmt {
		final Expr expression;
		Print (Expr expression) {
			this.expression = expression;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitPrintStmt(this);
		}
		@Override
		public String toString() {
			return "(Print: " + expression.toString() + ")";
		}
	}

	static class Return extends Stmt {
		final Token keyword;
		final Expr value;
		Return (Token keyword, Expr value) {
			this.keyword = keyword;
			this.value = value;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitReturnStmt(this);
		}
		@Override
		public String toString() {
			return "(Return: " + keyword.toString() + " | " + value.toString() + ")";
		}
	}

	static class Var extends Stmt {
		final Token name;
		final Expr initializer;
		Var (Token name, Expr initializer) {
			this.name = name;
			this.initializer = initializer;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitVarStmt(this);
		}
		@Override
		public String toString() {
			return "(Var: " + name.toString() + " | " + initializer.toString() + ")";
		}
	}

	static class While extends Stmt {
		final Expr condition;
		final Stmt body;
		While (Expr condition, Stmt body) {
			this.condition = condition;
			this.body = body;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitWhileStmt(this);
		}
		@Override
		public String toString() {
			return "(While: " + condition.toString() + " | " + body.toString() + ")";
		}
	}


	abstract <R> R accept(Visitor<R> visitor);

	public String toString() { return ""; }
}
