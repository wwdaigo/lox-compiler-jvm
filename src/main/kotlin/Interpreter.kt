import java.lang.Exception

class Interpreter : Expr.Visitor<Any> {

	fun interpret(expression: Expr?) {
		try {
			val value = evaluate(expression)
			println(stringify(value))
		} catch (e: RuntimeError) {
			runtimeError(e)
		}
	}

	private fun stringify(obj: Any?): String {
		return obj?.let {
			if (obj is Double) {
				var text = obj.toString()
				if (text.endsWith(".0")) {
					text = text.substring(0, text.length - 2)
				}
				text
			} else {
				obj.toString()
			}
		} ?: "nil"
	}

	override fun visitLiteralExpr(expr: Literal): Any? {
		return expr.value
	}

	override fun visitGroupingExpr(expr: Grouping): Any? {
		return evaluate(expr.expression)
	}

	override fun visitUnaryExpr(expr: Unary): Any? {
		val right = evaluate(expr.right)

		return when (expr.operator.type) {
			TokenType.Bang -> !isTruthy(right)
			TokenType.Minus -> {
				checkNumberOperand(expr.operator, right)
				-(right.asDouble())
			}
			else -> null
		}
	}

	override fun visitBinaryExpr(expr: Binary): Any? {
		val left = evaluate(expr.left)
		val right = evaluate(expr.right)

		return when (expr.operator.type) {
			TokenType.Greater -> {
				checkNumberOperands(expr.operator, left, right)
				left.asDouble() > right.asDouble()
			}
			TokenType.GreaterEqual -> {
				checkNumberOperands(expr.operator, left, right)
				left.asDouble() >= (right.asDouble())
			}
			TokenType.Less -> {
				checkNumberOperands(expr.operator, left, right)
				(left.asDouble()) < right.asDouble()
			}
			TokenType.LessEqual -> {
				checkNumberOperands(expr.operator, left, right)
				(left.asDouble()) <= right.asDouble()
			}

			TokenType.BangEqual -> !isEqual(left, right)
			TokenType.EqualEqual -> isEqual(left, right)

			TokenType.Minus ->  {
				checkNumberOperands(expr.operator, left, right)
				left.asDouble() - right.asDouble()
			}
			TokenType.Slash -> {
				checkNumberOperands(expr.operator, left, right)
				left.asDouble() / right.asDouble()
			}
			TokenType.Star -> {
				checkNumberOperands(expr.operator, left, right)
				left.asDouble() * right.asDouble()
			}
			TokenType.Plus -> {
				try {
					left.asDouble() + right.asDouble()
				} catch (e: Exception) {
					if (left is String && right is String) {
						left + right
					} else throw RuntimeError(expr.operator, "Operands must be two numbers or two strings.")
				}
			}
			else -> null
		}
	}

	private fun evaluate(expr: Expr?) = expr?.accept(this)

	private fun isTruthy(obj: Any?): Boolean {
		return obj?.let {
			if (it is Boolean) it else true
		} ?: false
	}

	private fun isEqual(a: Any?, b: Any?): Boolean = a == b || (a == null && b == null)

	private fun checkNumberOperand(operator: Token, operand: Any?) {
		try {
			operand.toString().toDouble()
		} catch (e: Exception) {
			throw RuntimeError(operator, "Operand must be a number")
		}
	}

	private fun checkNumberOperands(operator: Token, left: Any?, right: Any?) {
		try {
			left.toString().toDouble()
			right.toString().toDouble()
		} catch (e: Exception) {
			throw RuntimeError(operator, "Operands must be numbers")
		}
	}
}

fun Any?.asDouble(): Double {
	return this.toString().toDouble()
}