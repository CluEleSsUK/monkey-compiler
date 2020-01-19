package cluelessuk

import cluelessuk.bytecode.ByteEncoder
import cluelessuk.language.Node

val byteEncoder = ByteEncoder()

sealed class Result<T>
data class Success<T>(val value: T) : Result<T>()
data class Error<T>(val reasons: List<String>) : Result<T>()


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

