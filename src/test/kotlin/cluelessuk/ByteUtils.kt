package cluelessuk

import cluelessuk.bytecode.ByteEncoder
import cluelessuk.bytecode.Bytecode
import cluelessuk.bytecode.BytesRead
import cluelessuk.bytecode.CompilationResult
import cluelessuk.bytecode.Failure
import cluelessuk.bytecode.OpCode
import cluelessuk.bytecode.Success
import cluelessuk.vm.MObject
import java.lang.RuntimeException

class ByteUtils {
    companion object {
        @JvmStatic
        fun assertExpected(expected: Array<ByteArray>, result: CompilationResult<Bytecode>): Boolean = when (result) {
            is Failure -> throw RuntimeException("Compilation failed:\n${result.reasons.joinToString("\n")}")
            is Success -> {
                val actual = result.value.instructions
                if (!expected.contentDeepEquals(actual)) {
                    val message = bytecodeAssertionFailedMessage(expected, actual)
                    println(message)
                    throw RuntimeException(message)
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

fun prettyPrinter(instruction: Array<ByteArray>): String {
    val flattenedArray = instruction.foldRight(ByteArray(0)) { acc, next -> acc.plus(next) }
    return prettyPrinter(flattenedArray)
}

fun prettyPrinter(instructions: ByteArray): String {
    var offset = 0
    var output = ""

    while (offset < instructions.size) {
        val (instructionString, bytesRead) = nextInstructionAsString(instructions.sliceArray(offset..instructions.lastIndex))
        val asAddress = offset.asAddress()

        output += "$asAddress $instructionString\n"
        offset += bytesRead
    }

    return output
}

val errorResponse = "" to 0
fun nextInstructionAsString(instruction: ByteArray): Pair<String, BytesRead> {
    if (instruction.isEmpty()) {
        return errorResponse
    }

    val opcode = OpCode.from(instruction[0])
    if (ByteEncoder.lookup(opcode) == null) {
        return errorResponse
    }

    val (operands, operandBytes) = ByteEncoder().readOperands(instruction)
    val spaceIfHasOperands = if (operands.isEmpty()) "" else " "
    val output = "$opcode${operands.joinToString(prefix = spaceIfHasOperands, separator = ",")}"
    val bytesRead = OpCode.width() + operandBytes

    return output to bytesRead
}

fun bytecodeAssertionFailedMessage(expected: Array<ByteArray>, actual: Array<ByteArray>): String {
    return "Bytecode assertion failed:\n" +
        "EXPECTED:\n" +
        prettyPrinter(expected) + "\n" +
        "RECEIVED:\n" +
        prettyPrinter(actual)
}

fun Int.asAddress() = "%04d".format(this)
