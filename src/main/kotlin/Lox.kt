@file:JvmName("Lox")

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

private var hadError = false
private var hadRuntimeError = false

private val interpreter = Interpreter()

fun main(vararg args: String) {
	when (args.size) {
		0 -> rumPrompt()
		1 -> runFile(args.first())
		else -> {
			println("Usage: jlox [script]")
			exitProcess(64)
		}
	}
}

private fun runFile(path: String) {
	val bytes = Files.readAllBytes(Paths.get(path))
	run(String(bytes, Charset.defaultCharset()))

	if (hadError) exitProcess(65)
	if (hadRuntimeError) exitProcess(70)
}

private fun rumPrompt() {
	val input = InputStreamReader(System.`in`)
	val reader = BufferedReader(input)

	while (true) {
		print("> ")
		reader.readLine()?.let {
			run(it)
			hadError = false
		} ?: break
	}
}

private fun run(source: String) {
	val scanner = Scanner(source)
	val tokens = scanner.scanTokens()
	val parser = Parser(tokens)
	val expression = parser.parse()

	if (hadError) return
	interpreter.interpret(expression)
}

/* Error handling */

fun error(line: Int, message: String) {
	report(line, "", message)
}

fun error(token: Token, message: String) {
	if (token.type == TokenType.Eof) {
		report(token.line, " at end ", message)
	} else {
		report(token.line, " at '${token.lexeme}'", message)
	}
}

fun runtimeError(error: RuntimeError) {
	System.err.println("${error.message}\n[line ${error.token.line}]")
	hadRuntimeError = true
}

private fun report(line: Int, where: String, message: String) {
	System.err.println("[line $line] Error $where: $message")
	hadError = true
}
