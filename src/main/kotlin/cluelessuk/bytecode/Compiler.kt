package cluelessuk.bytecode

import cluelessuk.language.BooleanLiteral
import cluelessuk.language.ExpressionStatement
import cluelessuk.language.InfixExpression
import cluelessuk.language.IntegerLiteral
import cluelessuk.language.Node
import cluelessuk.language.Program
import cluelessuk.vm.MInteger
import cluelessuk.vm.MObject
import java.lang.Error


sealed class Result<T> {
    fun <U> flatMap(fn: (Result<T>) -> Result<U>): Result<U> {
        if (this is Failure) {
            return Failure(this.reasons)
        }
        return fn(this)
    }

    fun then(block: () -> Unit): Result<T> {
        if (this is Failure) {
            return Failure(this.reasons)
        }

        block()
        return this
    }
}

data class Success<T>(val value: T) : Result<T>()
data class Failure<T>(val reasons: List<String>) : Result<T>()

class Bytecode(val instructions: Array<ByteArray>, val constants: Array<MObject>)

class Compiler {

    private val byteEncoder = ByteEncoder()
    private val output = CompiledInstructions()
    private val constants = ConstantPool()

    fun compile(node: Node): Result<Bytecode> {
        return when (node) {
            is Program -> compileProgram(node)
            is ExpressionStatement -> compileExpressionStatement(node)
            is InfixExpression -> compileInfixExpression(node)
            is IntegerLiteral -> compileIntegerLiteral(node)
            is BooleanLiteral -> compileBooleanLiteral(node)
            else -> Failure(listOf("Node not supported ${node.tokenLiteral()}"))
        }
    }

    private fun compileProgram(node: Program): Result<Bytecode> {
        return node.statements
            .map(::compile)
            .firstOrNull { it is Failure }
            ?: Success(bytecode())
    }

    private fun compileExpressionStatement(node: ExpressionStatement): Result<Bytecode> {
        val expressionCompilation = compile(node.expression)
        if (expressionCompilation is Error) {
            return expressionCompilation
        }

        return success { emit(OpCode.POP) }
    }

    private fun compileInfixExpression(node: InfixExpression): Result<Bytecode> {
        val result = when (node.operator) {
            "<" -> compile(node.right)
                .flatMap { compile(node.left) }
                .then { emit(OpCode.GREATER_THAN) }
            else -> compile(node.left)
                .flatMap { compile(node.right) }
                .then {
                    when (node.operator) {
                        "+" -> emit(OpCode.ADD)
                        "-" -> emit(OpCode.SUBTRACT)
                        "*" -> emit(OpCode.MULTIPLY)
                        "/" -> emit(OpCode.DIVIDE)
                        "==" -> emit(OpCode.EQUAL)
                        "!=" -> emit(OpCode.NOT_EQUAL)
                        ">" -> emit(OpCode.GREATER_THAN)
                        else -> Failure<Bytecode>(listOf("Operator not supported: ${node.operator}"))
                    }
                }
        }

        return result.flatMap { Success(bytecode()) }
    }

    private fun compileIntegerLiteral(node: IntegerLiteral): Result<Bytecode> {
        return success {
            val pointerToConstant = constants.addConstantForIndex(MInteger(node.value.toUShort()))
            emit(OpCode.CONSTANT, pointerToConstant)
        }
    }

    private fun compileBooleanLiteral(node: BooleanLiteral): Result<Bytecode> {
        return success {
            if (node.value) emit(OpCode.TRUE) else emit(OpCode.FALSE)
        }
    }

    private fun emit(opcode: OpCode, vararg operands: UShort): UShort {
        val instruction = byteEncoder.make(opcode, operands.toList())
        return output.addInstructionForIndex(instruction)
    }

    private fun bytecode(): Bytecode {
        return Bytecode(output.get(), constants.get())
    }

    private fun success(fn: () -> Unit): Success<Bytecode> {
        fn()
        return Success(bytecode())
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