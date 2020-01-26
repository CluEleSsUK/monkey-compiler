package cluelessuk.bytecode

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
        val buffer = allocate(definition.operandWidthBytes.sum())

        var offset = 1 // start at one to ignore the opcode
        definition.operandWidthBytes.forEach { width ->
            val operand = instruction.sliceArray(offset until offset + width)
            buffer.put(operand)
            offset += width
        }

        return buffer.toUShortArray() to offset - 1
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

class Instructions(val value: ByteArray) {
    override fun toString(): String {
        var offset = 0
        var output = ""
        var currentInstruction = value

        while (offset < value.size) {
            val (instructionString, bytesRead) = asString(currentInstruction)

            output += "${offset.asAddress()} $instructionString\n"
            offset += bytesRead
            currentInstruction = value.sliceArray(offset until value.size)
        }

        return output
    }
}

val errorResponse = "" to 0
fun asString(instruction: Instruction): Pair<String, BytesRead> {
    if (instruction.isEmpty()) {
        return errorResponse
    }

    val opcode = OpCode.from(instruction[0])
    if (ByteEncoder.lookup(opcode) == null) {
        return errorResponse
    }

    val (operands, operandBytes) = ByteEncoder().readOperands(instruction)
    val output = "$opcode ${operands.joinToString(separator = ",")}"
    val bytesRead = OpCode.width() + operandBytes

    return output to bytesRead
}

fun Int.asAddress() = "%0${4}d".format(this)
