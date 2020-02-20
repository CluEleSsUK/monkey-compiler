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

class Bytecode(val instructions: ByteArray, val constants: Array<MObject>)


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
        return when (node.operator) {
            "<" -> compile(node.right, node.left)
                .then { emit(OpCode.GREATER_THAN) }

            else -> compile(node.left, node.right).flatMap {
                when (node.operator) {
                    "+" -> emit(OpCode.ADD)
                    "-" -> emit(OpCode.SUBTRACT)
                    "*" -> emit(OpCode.MULTIPLY)
                    "/" -> emit(OpCode.DIVIDE)
                    "==" -> emit(OpCode.EQUAL)
                    "!=" -> emit(OpCode.NOT_EQUAL)
                    ">" -> emit(OpCode.GREATER_THAN)
                    else -> Failure<MemoryAddress>(listOf("Operator not supported: ${node.operator}"))
                }
            }
        }.flatMap {
            success()
        }
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
        val placeholderAddress = 9999.toMemoryAddress()

        val compileCondition = {
            compile(node.condition)
                .flatMap { emit(OpCode.JUMP_IF_NOT_TRUE, placeholderAddress) }
        }

        val compileTruthyBranch = {
            compile(node.consequence)
                .then(::removeLastIfPop)
                .flatMap { emit(OpCode.JUMP, placeholderAddress) }
        }

        val compileFalsyBranch = {
            if (node.alternative == null) {
                emit(OpCode.NULL).flatMap { success() }
            } else {
                compile(node.alternative)
            }.then { removeLastIfPop() }
        }

        val rewriteToNextInstructionPointer = { pointer: MemoryAddress -> output.replaceOperand(pointer, output.nextAvailableMemoryAddress()) }

        return compileCondition().map { jumpIfNotTruePointer ->
            compileTruthyBranch()
                .then { rewriteToNextInstructionPointer(jumpIfNotTruePointer) }
                .map { postTruthyJumpPointer ->
                    compileFalsyBranch().then { rewriteToNextInstructionPointer(postTruthyJumpPointer) }
                }
        }.flatMap { success() }
    }

    private fun removeLastIfPop() {
        val lastInstruction = output.last()
        if (lastInstruction != null && opcodeFrom(lastInstruction) == OpCode.POP) {
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

    private fun emit(opcode: OpCode, vararg operands: MemoryAddress): CompilationResult<MemoryAddress> {
        val instruction = byteEncoder.make(opcode, operands.toList())
        return Success(output.addInstructionForIndex(instruction))
    }

    private fun success(): Success<Bytecode> {
        return Success(Bytecode(output.get(), constants.get()))
    }
}
