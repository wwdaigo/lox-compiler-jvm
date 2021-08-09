@file:JvmName("Lox")

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

private var hadError = false

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
	scanner.scanTokens().forEach() {
		println(it)
	}
}

/* Error handling */

fun error(line: Int, message: String) {
	report(line, "", message)
}

private fun report(line: Int, where: String, message: String) {
	System.err.println("[line $line] Error $where: $message")
	hadError = true
}