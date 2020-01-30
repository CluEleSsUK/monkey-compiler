package cluelessuk.bytecode

enum class OpCode {
    CONSTANT,
    ADD;

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
    OpCode.CONSTANT to OpCodeDefinition("OpConstant", listOf(2)),
    OpCode.ADD to OpCodeDefinition("OpAdd", emptyList())
)