package cluelessuk.bytecode


fun prettyPrinter(instructions: ByteArray): String {
    var offset = 0
    var output = ""

    while (offset < instructions.size) {
        val (instructionString, bytesRead) = nextInstructionAsString(instructions.sliceArray(offset..instructions.lastIndex))
        val asAddress = offset.asAddress()

        output += "$asAddress $instructionString\n"
        offset += bytesRead
    }

    return output
}

val errorResponse = "" to 0
fun nextInstructionAsString(instruction: ByteArray): Pair<String, BytesRead> {
    if (instruction.isEmpty()) {
        return errorResponse
    }

    val opcode = OpCode.from(instruction[0])
    if (ByteEncoder.lookup(opcode) == null) {
        return errorResponse
    }

    val (operands, operandBytes) = ByteEncoder().readOperands(instruction)
    val spaceIfHasOperands = if (operands.isEmpty()) "" else " "
    val output = "$opcode${operands.joinToString(prefix = spaceIfHasOperands, separator = ",")}"
    val bytesRead = OpCode.width() + operandBytes

    return output to bytesRead
}

fun Int.asAddress() = "%04d".format(this)
