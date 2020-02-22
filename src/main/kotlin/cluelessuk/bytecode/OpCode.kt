package cluelessuk.bytecode

enum class OpCode {
    CONSTANT,
    POP,
    ADD,
    SUBTRACT,
    MULTIPLY,
    DIVIDE,
    TRUE,
    FALSE,
    NULL,
    EQUAL,
    NOT_EQUAL,
    GREATER_THAN,
    MINUS,
    BANG,
    JUMP,
    JUMP_IF_NOT_TRUE,
    SET_GLOBAL,
    GET_GLOBAL;

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

const val pointerSize = UShort.SIZE_BYTES

val opcodeDefinitions = mapOf(
    OpCode.CONSTANT to OpCodeDefinition("OpConstant", listOf(pointerSize)),
    OpCode.POP to OpCodeDefinition("OpPop"),
    OpCode.ADD to OpCodeDefinition("OpAdd"),
    OpCode.SUBTRACT to OpCodeDefinition("OpSubstract"),
    OpCode.MULTIPLY to OpCodeDefinition("OpMultiply"),
    OpCode.DIVIDE to OpCodeDefinition("OpDivide"),
    OpCode.TRUE to OpCodeDefinition("OpTrue"),
    OpCode.FALSE to OpCodeDefinition("OpFalse"),
    OpCode.NULL to OpCodeDefinition("OpNull"),
    OpCode.EQUAL to OpCodeDefinition("OpEqual"),
    OpCode.NOT_EQUAL to OpCodeDefinition("OpNotEqual"),
    OpCode.GREATER_THAN to OpCodeDefinition("OpGreaterThan"),
    OpCode.MINUS to OpCodeDefinition("OpMinus"),
    OpCode.BANG to OpCodeDefinition("OpBang"),
    OpCode.JUMP to OpCodeDefinition("OpJump", listOf(pointerSize)),
    OpCode.JUMP_IF_NOT_TRUE to OpCodeDefinition("OpJumpIfNotTrue", listOf(pointerSize)),
    OpCode.SET_GLOBAL to OpCodeDefinition("OpJumpIfNotTrue", listOf(pointerSize)),
    OpCode.GET_GLOBAL to OpCodeDefinition("OpJumpIfNotTrue", listOf(pointerSize))
)

// this will happily blow up if you pass in an empty array
fun opcodeFrom(instruction: ByteArray): OpCode {
    return OpCode.from(instruction[0])
}
