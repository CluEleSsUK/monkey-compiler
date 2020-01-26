package cluelessuk

import cluelessuk.bytecode.ByteEncoder
import cluelessuk.bytecode.OpCode
import cluelessuk.language.ExpressionStatement
import cluelessuk.language.InfixExpression
import cluelessuk.language.IntegerLiteral
import cluelessuk.language.Node
import cluelessuk.language.Program


sealed class Result<T>
data class Success<T>(val value: T) : Result<T>()
data class Error(val reasons: List<String>) : Result<Unit>()

val Successful = Success(Unit)

class Bytecode(val instructions: Array<ByteArray>, val constants: Array<Any>)

class Compiler {

    private val byteEncoder = ByteEncoder()
    private val instructions = mutableListOf<ByteArray>()
    private val constants = mutableListOf<Any>()

    fun bytecode(): Bytecode {
        return Bytecode(instructions.toTypedArray(), constants.toTypedArray())
    }

    fun compile(node: Node): Result<*> {
        return when (node) {
            is Program -> compileProgram(node)
            is ExpressionStatement -> compile(node.expression)
            is InfixExpression -> compileInfixExpression(node)
            is IntegerLiteral -> compileIntegerLiteral(node)
            else -> Error(listOf("Node not supported ${node.tokenLiteral()}"))
        }
    }

    private fun compileProgram(node: Program): Result<*> {
        return node.statements
            .map(::compile)
            .firstOrNull { it is Error }
            ?: Successful
    }

    private fun compileInfixExpression(node: InfixExpression): Result<*> {
        val left = compile(node.left)
        if (left is Error) {
            return left
        }
        val right = compile(node.right)
        if (right is Error) {
            return right
        }

        return Successful
    }

    private fun compileIntegerLiteral(node: IntegerLiteral): Result<*> {
        emit(OpCode.CONSTANT, addConstant(MInteger(node.value)))
        return Successful
    }

    // returns last index of constant pool as an ID for the item added
    private fun addConstant(obj: MObject): UShort {
        constants += obj
        return (constants.size - 1).toUShort()
    }

    private fun emit(opcode: OpCode, vararg operands: UShort): UShort {
        val instruction = byteEncoder.make(opcode, operands.toList())
        return addInstructionForPosition(instruction)
    }

    // returns starting position of this instruction
    private fun addInstructionForPosition(instruction: ByteArray): UShort {
        val positionForNext = instructions.size
        instructions += instruction
        return positionForNext.toUShort()
    }
}

