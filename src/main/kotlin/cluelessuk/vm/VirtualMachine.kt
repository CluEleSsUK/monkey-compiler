package cluelessuk.vm

import cluelessuk.bytecode.Bytecode
import cluelessuk.bytecode.Instruction
import cluelessuk.bytecode.OpCode
import java.lang.RuntimeException

data class VirtualMachine(
    private val bytecode: Bytecode
) {
    private val stack = CallStack<MObject>()
    fun result(): MObject? = stack.lastPoppedValue

    fun run(): VirtualMachine {
        bytecode.instructions.forEach {
            when (val opcode = opcodeFrom(it)) {
                OpCode.CONSTANT -> runConstant(it)
                OpCode.ADD,
                OpCode.SUBTRACT,
                OpCode.MULTIPLY,
                OpCode.DIVIDE -> runBinaryOperation(opcode)
                OpCode.POP -> stack.pop()
                OpCode.TRUE -> stack.push(MBoolean.TRUE)
                OpCode.FALSE -> stack.push(MBoolean.FALSE)
            }
        }

        return this
    }

    private fun runConstant(instruction: Instruction) {
        val pointer = constantPoolMemoryAddress(instruction)
        stack.push(dereference(pointer))
    }

    private fun runBinaryOperation(opcode: OpCode) {
        if (stack.size() < 2) {
            return
        }

        val right = stack.pop()
        val left = stack.pop()
        if (left !is MInteger || right !is MInteger) {
            throw RuntimeException("Addition only supports MIntegers")
        }

        val expressionResult = when (opcode) {
            OpCode.ADD -> MInteger.from(left.value + right.value)
            OpCode.SUBTRACT -> MInteger.from(left.value - right.value)
            OpCode.MULTIPLY -> MInteger.from(left.value * right.value)
            OpCode.DIVIDE -> MInteger.from(left.value / right.value)
            else -> throw RuntimeException("Infix opcode $opcode not supported")
        }

        stack.push(expressionResult)
    }

    private fun dereference(pointer: MInteger): MObject? {
        if (bytecode.constants.size < pointer.value.toInt()) {
            return null
        }

        return bytecode.constants[pointer.value.toInt()]
    }
}


private fun opcodeFrom(instruction: Instruction): OpCode {
    if (instruction.isEmpty()) {
        throw RuntimeException("Instruction was empty, and thus did not have an opcode")
    }

    return OpCode.from(instruction[0])
}

private fun constantPoolMemoryAddress(instruction: Instruction): MInteger {
    if (instruction.size != 3) {
        throw RuntimeException("Instruction expected a 3 byte instruction, but got ${instruction.size}")
    }
    return MInteger.from(instruction[1], instruction[2])
}