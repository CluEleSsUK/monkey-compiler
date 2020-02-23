package cluelessuk.bytecode

import cluelessuk.language.ArrayLiteral
import cluelessuk.language.BlockStatement
import cluelessuk.language.BooleanLiteral
import cluelessuk.language.ExpressionStatement
import cluelessuk.language.Identifier
import cluelessuk.language.IfExpression
import cluelessuk.language.InfixExpression
import cluelessuk.language.IntegerLiteral
import cluelessuk.language.LetStatement
import cluelessuk.language.Node
import cluelessuk.language.PrefixExpression
import cluelessuk.language.Program
import cluelessuk.language.StringLiteral
import cluelessuk.vm.MInteger
import cluelessuk.vm.MObject
import cluelessuk.vm.MString

class Bytecode(val instructions: ByteArray, val constants: Array<MObject>)

class Compiler {

    private val byteEncoder = ByteEncoder()
    private val output = CompiledInstructions()
    private val constants = ConstantPool()
    private val symbolTable = SymbolTable()

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
            is StringLiteral -> compileStringLiteral(node)
            is ArrayLiteral -> compileArrayLiteral(node)
            is LetStatement -> compileLetStatement(node)
            is Identifier -> compileIdentifier(node)
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
            .then { emitForAddress(OpCode.POP) }
    }

    private fun compileInfixExpression(node: InfixExpression): CompilationResult<Bytecode> {
        return when (node.operator) {
            "<" -> compile(node.right, node.left)
                .flatMap { emit(OpCode.GREATER_THAN) }

            else -> compile(node.left, node.right).flatMap {
                when (node.operator) {
                    "+" -> emit(OpCode.ADD)
                    "-" -> emit(OpCode.SUBTRACT)
                    "*" -> emit(OpCode.MULTIPLY)
                    "/" -> emit(OpCode.DIVIDE)
                    "==" -> emit(OpCode.EQUAL)
                    "!=" -> emit(OpCode.NOT_EQUAL)
                    ">" -> emit(OpCode.GREATER_THAN)
                    else -> Failure(listOf("Operator not supported: ${node.operator}"))
                }
            }
        }
    }

    private fun compilePrefixExpression(node: PrefixExpression): CompilationResult<Bytecode> {
        return compile(node.right).flatMap {
            when (node.operator) {
                "!" -> emit(OpCode.BANG)
                "-" -> emit(OpCode.MINUS)
                else -> Failure(listOf("Prefix operator ${node.operator} not supported"))
            }
        }
    }

    private fun compileIfExpression(node: IfExpression): CompilationResult<Bytecode> {
        val placeholderAddress = 9999.toMemoryAddress()

        val compileCondition = {
            compile(node.condition)
                .flatMap { emitForAddress(OpCode.JUMP_IF_NOT_TRUE, placeholderAddress) }
        }

        val compileTruthyBranch = {
            compile(node.consequence)
                .then(::removeLastIfPop)
                .flatMap { emitForAddress(OpCode.JUMP, placeholderAddress) }
        }

        val compileFalsyBranch = {
            if (node.alternative == null) {
                emitForAddress(OpCode.NULL).flatMap { success() }
            } else {
                compile(node.alternative)
            }.then { removeLastIfPop() }
        }

        val rewriteToNextInstructionPointer = { pointer: MemoryAddress -> output.replaceOperand(pointer, output.nextAvailableMemoryAddress()) }

        return compileCondition()
            .flatMap { jumpIfNotTruePointer ->
                compileTruthyBranch().andWith { jumpIfNotTruePointer.map { rewriteToNextInstructionPointer(it) } }
            }
            .flatMap { postTruthyJumpPointer ->
                compileFalsyBranch().andWith { postTruthyJumpPointer.map { rewriteToNextInstructionPointer(it) } }
            }
    }

    private fun compileLetStatement(letStatement: LetStatement): CompilationResult<Bytecode> {
        return compile(letStatement.value).then {
            val symbol = symbolTable.define(letStatement.name.value)
            emitForAddress(OpCode.SET_GLOBAL, symbol.index.toMemoryAddress())
        }
    }

    private fun compileIdentifier(identifier: Identifier): CompilationResult<Bytecode> {
        val symbol = symbolTable.resolve(identifier.value) ?: return Failure.of("Identifier ${identifier.value} is not bound")

        return emitForAddress(OpCode.GET_GLOBAL, symbol.index.toMemoryAddress()).flatMap { success() }
    }

    private fun removeLastIfPop() {
        val lastInstruction = output.last()
        if (lastInstruction != null && opcodeFrom(lastInstruction) == OpCode.POP) {
            output.pop()
        }
    }

    private fun compileIntegerLiteral(node: IntegerLiteral): CompilationResult<Bytecode> {
        val pointerToConstant = constants.addConstantForIndex(MInteger.from(node.value))
        return emitForAddress(OpCode.CONSTANT, pointerToConstant).flatMap { success() }
    }

    private fun compileBooleanLiteral(node: BooleanLiteral): CompilationResult<Bytecode> {
        if (node.value) emitForAddress(OpCode.TRUE) else emitForAddress(OpCode.FALSE)
        return success()
    }

    private fun compileStringLiteral(node: StringLiteral): CompilationResult<Bytecode> {
        val pointerToConstant = constants.addConstantForIndex(MString(node.value))
        return emitForAddress(OpCode.CONSTANT, pointerToConstant).flatMap { success() }
    }

    private fun compileArrayLiteral(node: ArrayLiteral): CompilationResult<Bytecode> {
        return node.elements.fold(success() as CompilationResult<Bytecode>) { acc, next -> acc.flatMap { compile(next) } }
            .flatMap { emitForAddress(OpCode.ARRAY, node.elements.size.toMemoryAddress()) }
            .flatMap { success() }
    }

    private fun emitForAddress(opcode: OpCode, vararg operands: MemoryAddress): CompilationResult<MemoryAddress> {
        val instruction = byteEncoder.make(opcode, operands.toList())
        return Success(output.addInstructionForIndex(instruction))
    }

    private fun emit(opcode: OpCode, vararg operands: MemoryAddress): CompilationResult<Bytecode> {
        return emitForAddress(opcode, *operands).flatMap { success() }
    }

    private fun success(): Success<Bytecode> {
        return Success(Bytecode(output.get(), constants.get()))
    }
}
