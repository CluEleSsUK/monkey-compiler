package cluelessuk.bytecode

import java.nio.ByteBuffer
import java.nio.ByteOrder

class ByteEncoder {

    fun make(opcode: OpCode): Instruction = make(opcode, emptyList())
    fun make(opcode: OpCode, operand: MemoryAddress): Instruction = make(opcode, listOf(operand))
    fun make(opcode: OpCode, operands: List<MemoryAddress>): Instruction {
        val definition = opcodeDefinitions[opcode] ?: return ByteBuffer.allocate(0).array()
        val instructionLength = 1 + definition.operandWidthBytes.sum() // Opcode byte + operand width bytes
        val buffer = ByteBuffer
            .allocate(instructionLength)
            .order(ByteOrder.BIG_ENDIAN)
            .put(opcode.byte())

        operands.forEachIndexed { index, operand ->
            when (definition.operandWidthBytes[index]) {
                2 -> buffer.putUShort(operand)
            }
        }

        return buffer.array()
    }

    fun readOperands(instruction: Instruction): Pair<MemoryAddressArray, BytesRead> {
        val errorValue = { ushortArrayOf() to 0 }
        val definition = lookup(instruction) ?: return errorValue()
        val buffer = allocate(definition.operandWidthBytes.sum())

        var offset = 1 // start at one to ignore the opcode
        definition.operandWidthBytes.forEach { width ->
            val operand = instruction.sliceArray(offset until offset + width)
            buffer.put(operand)
            offset += width
        }

        return buffer.toAddressArray() to offset - 1
    }

    companion object {

        fun lookup(opcode: OpCode): OpCodeDefinition? {
            return opcodeDefinitions[opcode]
        }

        private fun lookup(instruction: Instruction): OpCodeDefinition? {
            if (instruction.isEmpty()) {
                return null
            }
            return lookup(OpCode.from(instruction.first()))
        }

        private fun allocate(sizeInBytes: Int): ByteBuffer {
            return ByteBuffer
                .allocate(sizeInBytes)
                .order(ByteOrder.BIG_ENDIAN)
        }
    }
}
