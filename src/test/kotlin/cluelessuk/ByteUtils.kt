package cluelessuk

import cluelessuk.bytecode.Bytecode
import cluelessuk.bytecode.CompilationResult
import cluelessuk.bytecode.Failure
import cluelessuk.bytecode.Success
import cluelessuk.bytecode.flatten
import cluelessuk.bytecode.prettyPrinter
import cluelessuk.vm.MObject
import java.lang.RuntimeException
import kotlin.test.expect

class ByteUtils {
    companion object {
        @JvmStatic
        fun assertExpected(expected: List<ByteArray>, result: CompilationResult<Bytecode>): Boolean = when (result) {
            is Failure -> throw RuntimeException("Compilation failed:\n${result.reasons.joinToString("\n")}")
            is Success -> {
                val flatExpected = flatten(expected)
                val actual = result.value.instructions

                if (!flatExpected.contentEquals(actual)) {
                    expect(prettyPrinter(flatExpected)) { prettyPrinter(actual) }
                }

                true
            }
        }

        @JvmStatic
        fun assertExpectedConstants(expected: List<MObject>, result: CompilationResult<Bytecode>): Boolean = when (result) {
            is Failure -> throw RuntimeException("Compilation failed:\n${result.reasons.joinToString("\n")}")
            is Success -> {
                val actual = listOf(*result.value.constants)
                if (expected != actual) {
                    val message = "Constants assertion failed:\n" +
                        "Expected:\n" +
                        expected + "\n" +
                        "Actual:\n" +
                        actual
                    println(message)
                    throw RuntimeException(message)
                }
                true
            }
        }
    }
}
