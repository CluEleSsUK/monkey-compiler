package cluelessuk

import java.nio.ByteBuffer
import java.nio.ByteOrder


enum class OpCode {
    CONSTANT;

    fun byte(): Byte = this.ordinal.toByte()
}

data class OpCodeDefinition(val name: String, val operandWidths: List<Short>)

val lookup = mapOf(
    OpCode.CONSTANT to OpCodeDefinition("OpConstant", listOf(2))
)

typealias Instruction = ByteBuffer

class ByteCode {

    fun make(opcode: OpCode, operands: List<Short>): Instruction {
        val definition = lookup[opcode] ?: return ByteBuffer.allocate(0)
        val instructionLength = 1 + definition.operandWidths.sum() // Opcode byte + operand width bytes
        val buffer = ByteBuffer
            .allocate(instructionLength)
            .order(ByteOrder.BIG_ENDIAN)
            .put(opcode.byte())

        operands.forEachIndexed { index, operand ->
            when (definition.operandWidths[index]) {
                2.toShort() -> buffer.putShort(operand)
            }
        }

        return buffer
    }
}
