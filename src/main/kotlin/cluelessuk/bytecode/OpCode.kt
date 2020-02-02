package cluelessuk.bytecode

enum class OpCode {
    CONSTANT,
    POP,
    ADD,
    SUBTRACT,
    MULTIPLY,
    DIVIDE;

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

data class OpCodeDefinition(val name: String, val operandWidthBytes: List<Int> = emptyList())

val opcodeDefinitions = mapOf(
    OpCode.CONSTANT to OpCodeDefinition("OpConstant", listOf(UShort.SIZE_BYTES)),
    OpCode.POP to OpCodeDefinition("OpPop"),
    OpCode.ADD to OpCodeDefinition("OpAdd"),
    OpCode.SUBTRACT to OpCodeDefinition("OpSubstract"),
    OpCode.MULTIPLY to OpCodeDefinition("OpMultiply"),
    OpCode.DIVIDE to OpCodeDefinition("OpDivide")
)