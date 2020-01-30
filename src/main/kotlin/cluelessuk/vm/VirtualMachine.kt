package cluelessuk.vm

import cluelessuk.bytecode.Bytecode
import cluelessuk.bytecode.OpCode
import java.util.ArrayDeque
import java.util.Deque

data class VirtualMachine(
    private val stack: Deque<MObject> = ArrayDeque(),
    private var stackPointer: Int = 0
) {

    fun peek(): MObject? {
        if (stackPointer == 0) {
            return null
        }
        return stack.peek()
    }

    fun run(bytecode: Bytecode): VirtualMachine {
        bytecode.instructions.forEach {
            val opcode = OpCode.from(it[0])
            val constantPoolMemoryAddress = MInteger.from(it[1], it[2])
            when (opcode) {
                OpCode.CONSTANT -> {
                    stack.push(constantPoolMemoryAddress)
                    stackPointer += 2
                }
            }
        }

        return this
    }
}

