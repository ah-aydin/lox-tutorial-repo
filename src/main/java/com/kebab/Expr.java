package com.kebab;

import java.util.List;

abstract class Expr {
	interface Visitor<R> {
		R visitAssignExpr(Assign expr);
		R visitBinaryExpr(Binary expr);
		R visitTernaryExpr(Ternary expr);
		R visitCallExpr(Call expr);
		R visitGetExpr(Get expr);
		R visitGroupingExpr(Grouping expr);
		R visitLambdaExpr(Lambda expr);
		R visitLiteralExpr(Literal expr);
		R visitLogicalExpr(Logical expr);
		R visitSetExpr(Set expr);
		R visitSuperExpr(Super expr);
		R visitThisExpr(This expr);
		R visitUnaryExpr(Unary expr);
		R visitVariableExpr(Variable expr);
	}

	static class Assign extends Expr {
		final Token name;
		final Expr value;
		Assign (Token name, Expr value) {
			this.name = name;
			this.value = value;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitAssignExpr(this);
		}
		@Override
		public String toString() {
			return "(Assign: " + name.toString() + " | " + value.toString() + ")";
		}
	}

	static class Binary extends Expr {
		final Expr left;
		final Token operator;
		final Expr right;
		Binary (Expr left, Token operator, Expr right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBinaryExpr(this);
		}
		@Override
		public String toString() {
			return "(Binary: " + left.toString() + " | " + operator.toString() + " | " + right.toString() + ")";
		}
	}

	static class Ternary extends Expr {
		final Expr condition;
		final Expr left;
		final Expr right;
		Ternary (Expr condition, Expr left, Expr right) {
			this.condition = condition;
			this.left = left;
			this.right = right;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitTernaryExpr(this);
		}
		@Override
		public String toString() {
			return "(Ternary: " + condition.toString() + " | " + left.toString() + " | " + right.toString() + ")";
		}
	}

	static class Call extends Expr {
		final Expr callee;
		final Token paren;
		final List<Expr> arguments;
		Call (Expr callee, Token paren, List<Expr> arguments) {
			this.callee = callee;
			this.paren = paren;
			this.arguments = arguments;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitCallExpr(this);
		}
		@Override
		public String toString() {
			return "(Call: " + callee.toString() + " | " + paren.toString() + " | " + arguments.toString() + ")";
		}
	}

	static class Get extends Expr {
		final Expr object;
		final Token name;
		Get (Expr object, Token name) {
			this.object = object;
			this.name = name;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitGetExpr(this);
		}
		@Override
		public String toString() {
			return "(Get: " + object.toString() + " | " + name.toString() + ")";
		}
	}

	static class Grouping extends Expr {
		final Expr expression;
		Grouping (Expr expression) {
			this.expression = expression;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitGroupingExpr(this);
		}
		@Override
		public String toString() {
			return "(Grouping: " + expression.toString() + ")";
		}
	}

	static class Lambda extends Expr {
		final List<Token> params;
		final List<Stmt> body;
		Lambda (List<Token> params, List<Stmt> body) {
			this.params = params;
			this.body = body;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitLambdaExpr(this);
		}
		@Override
		public String toString() {
			return "(Lambda: " + params.toString() + " | " + body.toString() + ")";
		}
	}

	static class Literal extends Expr {
		final Object value;
		Literal (Object value) {
			this.value = value;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitLiteralExpr(this);
		}
		@Override
		public String toString() {
			return "(Literal: " + value.toString() + ")";
		}
	}

	static class Logical extends Expr {
		final Expr left;
		final Token operator;
		final Expr right;
		Logical (Expr left, Token operator, Expr right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitLogicalExpr(this);
		}
		@Override
		public String toString() {
			return "(Logical: " + left.toString() + " | " + operator.toString() + " | " + right.toString() + ")";
		}
	}

	static class Set extends Expr {
		final Expr object;
		final Token name;
		final Expr value;
		Set (Expr object, Token name, Expr value) {
			this.object = object;
			this.name = name;
			this.value = value;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitSetExpr(this);
		}
		@Override
		public String toString() {
			return "(Set: " + object.toString() + " | " + name.toString() + " | " + value.toString() + ")";
		}
	}

	static class Super extends Expr {
		final Token keyword;
		final Token method;
		Super (Token keyword, Token method) {
			this.keyword = keyword;
			this.method = method;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitSuperExpr(this);
		}
		@Override
		public String toString() {
			return "(Super: " + keyword.toString() + " | " + method.toString() + ")";
		}
	}

	static class This extends Expr {
		final Token keyword;
		This (Token keyword) {
			this.keyword = keyword;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitThisExpr(this);
		}
		@Override
		public String toString() {
			return "(This: " + keyword.toString() + ")";
		}
	}

	static class Unary extends Expr {
		final Token operator;
		final Expr right;
		Unary (Token operator, Expr right) {
			this.operator = operator;
			this.right = right;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitUnaryExpr(this);
		}
		@Override
		public String toString() {
			return "(Unary: " + operator.toString() + " | " + right.toString() + ")";
		}
	}

	static class Variable extends Expr {
		final Token name;
		Variable (Token name) {
			this.name = name;
		}
		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitVariableExpr(this);
		}
		@Override
		public String toString() {
			return "(Variable: " + name.toString() + ")";
		}
	}


	abstract <R> R accept(Visitor<R> visitor);

	public String toString() { return ""; }
}