import TokenType.*
import java.lang.IllegalArgumentException
import java.lang.RuntimeException
import javax.swing.GroupLayout
import kotlin.math.exp

/*
expression     → equality ;
equality       → comparison ( ( "!=" | "==" ) comparison )* ;
comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term           → factor ( ( "-" | "+" ) factor )* ;
factor         → unary ( ( "/" | "*" ) unary )* ;
unary          → ( "!" | "-" ) unary | primary ;
primary        → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ;

( ... )* : While loop
| 		 : If
 */

class Parser(private val tokens: List<Token>) {
	private var current = 0

	private val isAtEnd get() = peek.type == Eof
	private val peek get() = tokens[current]
	private val previous get() = tokens[current - 1]

	fun parse(): Expr? = try {
		expression()
	} catch (e: ParseError) {
		null
	}

	private fun match(vararg types: TokenType): Boolean {
		types.forEach {
			if (check(it)) {
				advance()
				return true
			}
		}

		return false
	}

	// expression → equality ;
	private fun expression() = equality()

	// equality → comparison ( ( "!=" | "==" ) comparison )* ;
	private fun equality(): Expr {
		var expr = comparrison()

		// ( ( "!=" | "==" ) comparison )* ;
		while (match(BangEqual, EqualEqual)) {
			// as in match() it is advanced, the operator is in the previous position
			val operator = previous
			val right = comparrison()
			expr = Binary(expr, operator, right)
		}

		return expr
	}

	// comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
	private fun comparrison(): Expr {
		var expr = term()

		while (match(Greater, GreaterEqual, Less, LessEqual)) {
			val operator = previous
			val right = term()
			expr = Binary(expr, operator, right)
		}

		return expr
	}

	// term → factor ( ( "-" | "+" ) factor )* ;
	private fun term(): Expr {
		var expr = factor()

		while (match(Plus, Minus)) {
			val operator = previous
			val right = factor()
			expr = Binary(expr, operator, right)
		}

		return expr
	}

	// factor → unary ( ( "/" | "*" ) unary )* ;
	private fun factor(): Expr {
		var expr = unary()

		while (match(Slash, Star)) {
			val operator = previous
			val right = unary()
			expr = Binary(expr, operator, right)
		}

		return expr
	}

	// unary → ( "!" | "-" ) unary | primary ;
	private fun unary(): Expr {
		if (match(Bang, Minus)) {
			val operator = previous
			val right = unary()
			return Unary(operator, right)
		}

		return primary()
	}

	// primary → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ;
	private fun primary(): Expr {
		return when {
			match(False) -> Literal(false)
			match(True) -> Literal(true)
			match(Nil) -> Literal(null)
			match(Number, TokenType.String) -> Literal(previous.literal)
			match(LeftParen) -> {
				val expr = expression()
				consume(RightParen, "Expect ')' after expression")
				return Grouping(expr)
			}

			else -> throw reportError(peek, "Expect expression.")
		}
	}

	private fun advance(): Token {
		if (!isAtEnd) current++
		return previous
	}

	private fun check(type: TokenType): Boolean {
		if (isAtEnd) return false
		return peek.type == type
	}

	private fun consume(type: TokenType, message: String): Token {
		if (check(type)) return advance()
		throw reportError(peek, message)
	}

	private fun reportError(token: Token, message: String): ParseError {
		error(token, message)
		return ParseError()
	}

	private fun synchronize() {
		advance()

		while (!isAtEnd) {
			if (previous.type == Semicolon) return

			when (peek.type) {
				Class, Fun, Var, For, If, While, Print, Return -> return
				else -> {}
			}

			advance()
		}
	}

	private class ParseError : RuntimeException()
}
