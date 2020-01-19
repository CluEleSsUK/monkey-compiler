package cluelessuk

import java.nio.ByteBuffer
import java.nio.ByteOrder


typealias Instruction = ByteArray
typealias BytesRead = Int

enum class OpCode {
    CONSTANT;

    fun byte(): Byte = this.ordinal.toByte()

    companion object {
        fun from(byte: Byte): OpCode {
            return values()[byte.toInt()]
        }

        fun width(): Int {
            return 1
        }
    }
}

data class OpCodeDefinition(val name: String, val operandWidthBytes: List<Int>)

val opcodeDefinitions = mapOf(
    OpCode.CONSTANT to OpCodeDefinition("OpConstant", listOf(2))
)

class ByteEncoder {

    fun make(opcode: OpCode, operand: UShort): Instruction = make(opcode, listOf(operand))
    fun make(opcode: OpCode, operands: List<UShort>): Instruction {
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

    fun readOperands(instruction: Instruction): Pair<UShortArray, BytesRead> {
        val errorValue = { ushortArrayOf() to 0 }
        val definition = lookup(instruction) ?: return errorValue()

        val totalBytes = definition.operandWidthBytes.sum()
        val buffer = ByteBuffer
            .allocate(totalBytes)
            .order(ByteOrder.BIG_ENDIAN)
            .putOperands(instruction)

        return buffer.toUShortArray() to totalBytes
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
    }
}

