class Scanner(private val source: String) {
	private val keywords = mapOf(
		"and" to TokenType.And,
		"class" to TokenType.Class,
		"else" to TokenType.Else,
		"false" to TokenType.False,
		"for" to TokenType.For,
		"fun" to TokenType.Fun,
		"if" to TokenType.If,
		"nil" to TokenType.Nil,
		"or" to TokenType.Or,
		"print" to TokenType.Print,
		"return" to TokenType.Return,
		"super" to TokenType.Super,
		"this" to TokenType.This,
		"true" to TokenType.True,
		"var" to TokenType.Var,
		"while" to TokenType.While,
	)

	private val tokens = ArrayList<Token>()

	private var start = 0
	private var current = 0
	private var line = 1

	private val isAtEnd
		get() = current >= source.length

	fun scanTokens(): List<Token> {
		while (!isAtEnd) {
			start = current
			scanToken()
		}

		tokens.add(Token(TokenType.Eof, "", null, line))
		return tokens
	}

	private fun scanToken() {
		when (val c = advance()) {
			'(' -> addToken(TokenType.LeftParen)
			')' -> addToken(TokenType.RightParen)
			'{' -> addToken(TokenType.LeftBrace)
			'}' -> addToken(TokenType.RightBrace)
			',' -> addToken(TokenType.Comma)
			'.' -> addToken(TokenType.Dot)
			'-' -> addToken(TokenType.Minus)
			'+' -> addToken(TokenType.Plus)
			';' -> addToken(TokenType.Semicolon)
			'*' -> addToken(TokenType.Star)

			'!' -> addToken(if (match('=')) TokenType.BangEqual else TokenType.Bang)
			'=' -> addToken(if (match('=')) TokenType.EqualEqual else TokenType.Equal)
			'<' -> addToken(if (match('=')) TokenType.LessEqual else TokenType.Less)
			'>' -> addToken(if (match('=')) TokenType.GreaterEqual else TokenType.Greater)

			'/' -> if (match('/')) {
					while (peek() != '\n' && !isAtEnd) advance()
				} else {
					addToken(TokenType.Slash)
				}

			// Ignore whitespaces
			' ', '\r', '\t' -> { }

			'\n' -> line++

			'"' -> string()

			// Reserved words
			'o' -> if (match('r')) addToken(TokenType.Or)

			else -> if (isDigit(c)) {
				number()
			} else if (isAlpha(c)) {
				identifier()
			} else {
				error(line, "Unexpected character.")
			}
		}
	}

	private fun advance() = source[current++]

	private fun addToken(type: TokenType, literal: Any? = null) {
		val text = source.substring(start, current)
		tokens.add(Token(type, text, literal, line))
	}

	private fun match(expected: Char): Boolean {
		if (isAtEnd) return false
		if (source[current] != expected) return false

		current++
		return true
	}

	private fun peek(): Char {
		if (isAtEnd) return 0.toChar()
		return source[current]
	}

	private fun string() {
		while (peek() != '"' && !isAtEnd) {
			if (peek() == '\n') line++
			advance()
		}

		if (isAtEnd) {
			error(line, "Unterminated string.")
		}

		// Trailing "
		advance()

		val value = source.substring(start + 1, current - 1)
		addToken(TokenType.String, value)
	}

	private fun number() {
		while (isDigit(peek())) advance()

		if (peek() == '.' && isDigit(peekNext())) {
			advance()
			while (isDigit(peek())) advance()
		}
		addToken(TokenType.Number, source.substring(start, current))
	}

	private fun identifier() {
		while (isAlphaNumeric(peek())) advance()

		val text = source.substring(start, current)
		var type = keywords[text]
		if (type == null) type = TokenType.Identifier
		addToken(type)
	}

	private fun peekNext(): Char {
		if (current + 1 >= source.length) return 0.toChar()
		return source[current + 1]
	}

	private fun isDigit(c: Char) = c in '0'..'9'

	private fun isAlpha(c: Char) = (c in 'a'..'z') || (c in 'A'..'Z') || (c == '_')

	private fun isAlphaNumeric(c: Char) = isAlpha(c) || isDigit(c)
}
