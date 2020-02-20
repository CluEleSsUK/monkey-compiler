package cluelessuk.bytecode

class CompiledInstructions {
    private val byteEncoder = ByteEncoder()
    private var instructions = byteArrayOf()
    // 0 . . . . n
    private var lastInstruction: ByteArray? = null
    // 0 . . . m, n
    private var secondLastInstruction: ByteArray? = null

    fun get() = instructions
    fun last() = lastInstruction

    fun pop(): ByteArray? {
        if (instructions.isEmpty() || lastInstruction == null) {
            return null
        }

        val lastInstructionLength = lastInstruction?.size ?: 0
        val startOfLastInstruction = instructions.size - lastInstructionLength

        instructions = instructions.sliceArray(0 until startOfLastInstruction)
        return lastInstruction.also {
            lastInstruction = secondLastInstruction
        }
    }

    fun addInstructionForIndex(instruction: ByteArray): MemoryAddress {
        val pointer = nextAvailableMemoryAddress()
        instructions += instruction
        secondLastInstruction = lastInstruction
        lastInstruction = instruction
        return pointer
    }

    fun nextAvailableMemoryAddress(): MemoryAddress = instructions.size.toMemoryAddress()
    fun lastMemoryAddress(): MemoryAddress = instructions.lastIndex.toMemoryAddress()

    fun replaceOperand(position: MemoryAddress, operand: MemoryAddress) {
        if (position > lastMemoryAddress()) {
            return
        }

        val opcode = OpCode.from(instructions[position.toInt()])
        val updatedInstruction = byteEncoder.make(opcode, operand)
        replaceInstruction(position, updatedInstruction)
    }

    private fun replaceInstruction(position: MemoryAddress, updatedInstruction: ByteArray) {
        if (position + updatedInstruction.size.toMemoryAddress() > lastMemoryAddress()) {
            return
        }

        updatedInstruction.forEachIndexed { index, value ->
            val updatingPosition = position.toInt() + index
            instructions[updatingPosition] = value
        }
    }
}
