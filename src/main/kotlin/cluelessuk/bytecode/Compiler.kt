package cluelessuk.bytecode

import cluelessuk.language.ExpressionStatement
import cluelessuk.language.InfixExpression
import cluelessuk.language.IntegerLiteral
import cluelessuk.language.Node
import cluelessuk.language.Program
import cluelessuk.vm.MInteger
import cluelessuk.vm.MObject


sealed class Result<T>
data class Success<T>(val value: T) : Result<T>()
data class Failure(val reasons: List<String>) : Result<Unit>()

val Successful = Success(Unit)

class Bytecode(val instructions: Array<ByteArray>, val constants: Array<MObject>)

class Compiler {

    private val byteEncoder = ByteEncoder()
    private val output = CompiledInstructions()
    private val constants = ConstantPool()

    fun bytecode(): Bytecode {
        return Bytecode(output.get(), constants.get())
    }

    fun compile(node: Node): Result<*> {
        return when (node) {
            is Program -> compileProgram(node)
            is ExpressionStatement -> compile(node.expression)
            is InfixExpression -> compileInfixExpression(node)
            is IntegerLiteral -> compileIntegerLiteral(node)
            else -> Failure(listOf("Node not supported ${node.tokenLiteral()}"))
        }
    }

    private fun compileProgram(node: Program): Result<*> {
        return node.statements
            .map(::compile)
            .firstOrNull { it is Failure }
            ?: Successful
    }

    private fun compileInfixExpression(node: InfixExpression): Result<*> {
        val left = compile(node.left)
        if (left is Failure) {
            return left
        }
        val right = compile(node.right)
        if (right is Failure) {
            return right
        }

        return when (node.operator) {
            "+" -> Success(emit(OpCode.ADD))
            else -> Successful
        }
    }

    private fun compileIntegerLiteral(node: IntegerLiteral): Result<*> {
        val pointerToConstant = constants.addConstantForIndex(MInteger(node.value.toUShort()))
        emit(OpCode.CONSTANT, pointerToConstant)
        return Successful
    }

    private fun emit(opcode: OpCode, vararg operands: UShort): UShort {
        val instruction = byteEncoder.make(opcode, operands.toList())
        return output.addInstructionForIndex(instruction)
    }
}

class ConstantPool {
    private val objects = mutableListOf<MObject>()

    // returns last index of constant pool as an ID for the item added
    fun addConstantForIndex(obj: MObject): UShort {
        objects += obj
        return objects.lastIndex.toUShort()
    }

    fun get(): Array<MObject> = objects.toTypedArray()
}

class CompiledInstructions {
    private val instructions = mutableListOf<ByteArray>()

    fun addInstructionForIndex(instruction: ByteArray): UShort {
        instructions += instruction
        return instructions.lastIndex.toUShort()
    }

    fun get(): Array<ByteArray> = instructions.toTypedArray()
}