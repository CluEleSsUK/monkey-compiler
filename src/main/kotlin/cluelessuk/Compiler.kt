package cluelessuk

import cluelessuk.language.Node

val byteEncoder = ByteEncoder()

sealed class Result<T>
data class Success<T>(val value: T) : Result<T>()
data class Error<T>(val reasons: List<String>) : Result<T>()
class Instructions(val value: ByteArray) {
    override fun toString(): String {
        var offset = 0
        var output = ""

        while (offset < value.size) {
            val opcodeByte = value[offset]
            val opcode = OpCode.from(opcodeByte)
            val definition = ByteEncoder.lookup(opcode)
            if (definition == null) {
                println("Invalid OpCode $opcode")
                continue
            }

            val operands = byteEncoder.readOperands(value)
            output += "${offset.format(4)} $opcode ${operands.first.joinToString(separator = ",")}\n"
            offset += OpCode.width() + operands.second
        }

        return output
    }
}

class Bytecode(val instructions: Array<ByteArray>, val constants: Array<Any>)

class Compiler {

    private val instructions = arrayOf(byteArrayOf())
    private val constants = arrayOf<Any>()

    fun compile(node: Node): Result<Any?> {
        return Error(emptyList())
    }

    fun bytecode(): Bytecode {
        return Bytecode(instructions, constants)
    }
}

fun Int.format(dp: Int) = "%0${dp}d".format(this)
