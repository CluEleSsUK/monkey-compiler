package cluelessuk.bytecode

fun ByteArray.prettyPrint(): String {
    var offset = 0
    var output = ""
    var currentInstruction = this

    while (offset < this.size) {
        val (instructionString, bytesRead) = nextInstructionAsString(currentInstruction)

        output += "${offset.asAddress()} $instructionString\n"
        offset += bytesRead
        currentInstruction = this.sliceArray(offset until this.size)
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

fun Int.asAddress() = "%0${4}d".format(this)
