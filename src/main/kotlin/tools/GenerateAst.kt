@file:JvmName("GenerateAst")
package tools

import java.io.PrintWriter
import kotlin.system.exitProcess

fun main(vararg args: String) {
    if (args.size != 1) {
        System.err.println("Usage: generate_ast <output directory>")
        exitProcess(64)
    }

    val outputDir = args.first()
    defineAst(outputDir, "Expr", listOf(
        "Binary   : Expr left, Token operator, Expr right",
        "Grouping : Expr expression",
        "Literal  : Any value",
        "Unary    : Token operator, Expr right"
    ))
}

private fun defineAst(outputDir: String, baseName: String, types: List<String>) {
    val path = "$outputDir/$baseName.kt"

    val writer = PrintWriter(path, "UTF-8")

    writer.println("sealed class $baseName {")
    defineVisitor(writer, baseName, types)
    writer.println("    abstract fun <R> accept(visitor: Visitor<R>): R")
    writer.println("}")

    types.forEach {
        val line = it.split(":")
        val className = line.first().trim()
        val fields = line.last().trim()
        defineType(writer, baseName, className, fields)
    }


    writer.close()
}

private fun defineType(writer: PrintWriter, baseName: String, className: String, fieldList: String) {
     val fields = fieldList.split(",")
        .map {
            val parts = it.trim().split(" ")
            "val ${parts.last()}: ${parts.first()}"
        }.joinToString(", ").trim()

    writer.println()
    writer.println("data class $className($fields) : $baseName() {")
    writer.println("    override fun <R> accept(visitor: Visitor<R>): R {")
    writer.println("        return visitor.visit$className$baseName(this)")
    writer.println("    }")
    writer.println("}")
}

private fun defineVisitor(writer: PrintWriter, baseName: String, types: List<String>) {
    writer.println("    interface Visitor<R> {")

    types.forEach {
        val typeName = it.split(":").first().trim()
        writer.println("        fun visit$typeName$baseName(${baseName.lowercase()}: $typeName): R")
    }

    writer.println("    }")
}