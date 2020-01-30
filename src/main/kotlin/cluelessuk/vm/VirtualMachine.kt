package cluelessuk.vm

import cluelessuk.bytecode.Bytecode
import cluelessuk.bytecode.Instruction
import cluelessuk.bytecode.OpCode
import java.lang.RuntimeException
import java.util.ArrayDeque
import java.util.Deque

data class VirtualMachine(
    private val bytecode: Bytecode
) {
    private val stack: Deque<MObject> = ArrayDeque()
    fun peek(): MObject? = stack.peek()

    fun run(): VirtualMachine {
        bytecode.instructions.forEach {
            when (opcodeFrom(it)) {
                OpCode.CONSTANT -> runConstant(it)
                OpCode.ADD -> runAdd()
            }
        }

        return this
    }

    private fun runConstant(instruction: Instruction) {
        val pointer = constantPoolMemoryAddress(instruction)
        stack.push(dereference(pointer))
    }

    private fun runAdd() {
        if (stack.size < 2) {
            return
        }

        val left = stack.pop() as MInteger
        val right = stack.pop() as MInteger
        stack.push(MInteger.from((left.value + right.value).toInt()))
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