import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.system.exitProcess

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
}

private fun rumPrompt() {
	val input = InputStreamReader(System.`in`)
	val reader = BufferedReader(input)

	while (true) {
		print("> ")
		reader.readLine()?.let {
			run(it)
		} ?: break
	}
}

private fun run(source: String) {
	val scanner = Scanner(source)
	scanner.tokens().forEach {
		println(it)
	}
}
