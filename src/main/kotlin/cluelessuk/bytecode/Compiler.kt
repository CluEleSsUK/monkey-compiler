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

class Bytecode(val instructions: Array<ByteArray>, val constants: Array<MObject>)

class Compiler {

    private val byteEncoder = ByteEncoder()
    private val output = CompiledInstructions()
    private val constants = ConstantPool()

    fun compile(node: Node): CompilationResult<Bytecode> {
        return when (node) {
            is Program -> compileProgram(node)
            is ExpressionStatement -> compileExpressionStatement(node)
            is InfixExpression -> compileInfixExpression(node)
            is IntegerLiteral -> compileIntegerLiteral(node)
            is BooleanLiteral -> compileBooleanLiteral(node)
            else -> Failure(listOf("Node not supported ${node.tokenLiteral()}"))
        }
    }

    private fun compileProgram(node: Program): CompilationResult<Bytecode> {
        return node.statements
            .map(::compile)
            .firstOrNull { it is Failure }
            ?: Success(bytecode())
    }

    private fun compileExpressionStatement(node: ExpressionStatement): CompilationResult<Bytecode> {
        return compile(node.expression)
            .then { emit(OpCode.POP) }
    }

    private fun compile(left: Node, right: Node): CompilationResult<Bytecode> {
        return compile(left).flatMap { compile(right) }
    }

    private fun compileInfixExpression(node: InfixExpression): CompilationResult<Bytecode> {
        val result = when (node.operator) {
            "<" -> compile(node.right, node.left)
                .then { emit(OpCode.GREATER_THAN) }
            else -> compile(node.left, node.right).then {
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

    private fun compileIntegerLiteral(node: IntegerLiteral): CompilationResult<Bytecode> {
        return success {
            val pointerToConstant = constants.addConstantForIndex(MInteger(node.value.toUShort()))
            emit(OpCode.CONSTANT, pointerToConstant)
        }
    }

    private fun compileBooleanLiteral(node: BooleanLiteral): CompilationResult<Bytecode> {
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