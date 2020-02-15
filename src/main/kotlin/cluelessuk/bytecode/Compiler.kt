package cluelessuk.bytecode

import cluelessuk.language.BlockStatement
import cluelessuk.language.BooleanLiteral
import cluelessuk.language.ExpressionStatement
import cluelessuk.language.IfExpression
import cluelessuk.language.InfixExpression
import cluelessuk.language.IntegerLiteral
import cluelessuk.language.Node
import cluelessuk.language.PrefixExpression
import cluelessuk.language.Program
import cluelessuk.vm.MInteger
import cluelessuk.vm.MObject

class Bytecode(val instructions: Array<ByteArray>, val constants: Array<MObject>)

class Compiler {

    private val byteEncoder = ByteEncoder()
    private val output = CompiledInstructions()
    private val constants = ConstantPool()

    fun compile(node: Node): CompilationResult<Bytecode> {
        return when (node) {
            is Program -> compile(node.statements)
            is ExpressionStatement -> compileExpressionStatement(node)
            is InfixExpression -> compileInfixExpression(node)
            is PrefixExpression -> compilePrefixExpression(node)
            is IfExpression -> compileIfExpression(node)
            is BlockStatement -> compile(node.statements)
            is IntegerLiteral -> compileIntegerLiteral(node)
            is BooleanLiteral -> compileBooleanLiteral(node)
            else -> Failure(listOf("Node not supported ${node.tokenLiteral()}"))
        }
    }

    private fun compile(nodes: List<Node>): CompilationResult<Bytecode> = compile(*nodes.toTypedArray())

    // not using `flatMap` makes tailrec possible here
    private tailrec fun compile(vararg nodes: Node): CompilationResult<Bytecode> {
        if (nodes.isEmpty()) {
            return success()
        }

        val result = compile(nodes.first())
        if (result is Failure) {
            return result
        }

        val theRest = nodes.drop(1).toTypedArray()
        return compile(*theRest)
    }

    private fun compileExpressionStatement(node: ExpressionStatement): CompilationResult<Bytecode> {
        return compile(node.expression)
            .then { emit(OpCode.POP) }
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

        return result.flatMap { success() }
    }

    private fun compilePrefixExpression(node: PrefixExpression): CompilationResult<Bytecode> {
        return compile(node.right).then {
            when (node.operator) {
                "!" -> emit(OpCode.BANG)
                "-" -> emit(OpCode.MINUS)
                else -> Failure<Bytecode>(listOf("Prefix operator ${node.operator} not supported"))
            }
        }
    }

    private fun compileIfExpression(node: IfExpression): CompilationResult<Bytecode> {
        return compile(node.condition).then {
            // 9999 is bogus result that will be rewritten
            val jumpPosition = emit(OpCode.JUMP_IF_NOT_TRUE, 9999.toMemoryAddress())

            compile(node.consequence)
                .then(::removeLastIfPop)
                .then {
                    // rewrite the 9999 to the real address
                    val instructionAfterConsequence = output.get().map { it.size }.sum().toMemoryAddress()
                    output.replaceOperand(jumpPosition, instructionAfterConsequence)
                }
        }
    }

    private fun removeLastIfPop() {
        if (output.get().last().contentEquals(byteEncoder.make(OpCode.POP))) {
            output.pop()
        }
    }

    private fun compileIntegerLiteral(node: IntegerLiteral): CompilationResult<Bytecode> {
        val pointerToConstant = constants.addConstantForIndex(MInteger.from(node.value))
        emit(OpCode.CONSTANT, pointerToConstant)
        return success()
    }

    private fun compileBooleanLiteral(node: BooleanLiteral): CompilationResult<Bytecode> {
        if (node.value) emit(OpCode.TRUE) else emit(OpCode.FALSE)
        return success()
    }

    private fun emit(opcode: OpCode, vararg operands: MemoryAddress): MemoryAddress {
        val instruction = byteEncoder.make(opcode, operands.toList())
        return output.addInstructionForIndex(instruction)
    }

    private fun success(): Success<Bytecode> {
        return Success(Bytecode(output.get(), constants.get()))
    }
}

class ConstantPool {
    private val objects = mutableListOf<MObject>()

    // returns last index of constant pool as an ID for the item added
    fun addConstantForIndex(obj: MObject): MemoryAddress {
        objects += obj
        return objects.lastIndex.toMemoryAddress()
    }

    fun get(): Array<MObject> = objects.toTypedArray()
}

class CompiledInstructions {
    private val instructions = mutableListOf<ByteArray>()
    // 0 . . . . n
    private var lastInstruction: ByteArray? = null
    // 0 . . . m, n
    private var secondLastInstruction: ByteArray? = null

    fun pop(): ByteArray? {
        if (instructions.isEmpty()) {
            return null
        }

        lastInstruction = secondLastInstruction
        return instructions.removeAt(instructions.lastIndex)
    }

    fun addInstructionForIndex(instruction: ByteArray): MemoryAddress {
        instructions += instruction
        lastInstruction = instructions.lastOrNull()
        secondLastInstruction = instructions.drop(1).lastOrNull()
        return instructions.lastIndex.toMemoryAddress()
    }

    fun get(): Array<ByteArray> = instructions.toTypedArray()


    private val byteEncoder = ByteEncoder()
    fun replaceOperand(position: MemoryAddress, operand: MemoryAddress) {
        if (position >= instructions.size.toMemoryAddress()) {
            return
        }

        val opcode = OpCode.from(instructions[position.toInt()][0])
        val updatedInstruction = byteEncoder.make(opcode, operand)
        replaceInstruction(position, updatedInstruction)
    }

    private fun replaceInstruction(position: MemoryAddress, updatedInstruction: ByteArray) {
        if (position >= instructions.size.toMemoryAddress()) {
            return
        }

        instructions[position.toInt()] = updatedInstruction
    }
}