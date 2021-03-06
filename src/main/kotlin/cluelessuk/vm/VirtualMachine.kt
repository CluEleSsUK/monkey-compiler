package cluelessuk.vm

import cluelessuk.bytecode.Bytecode
import cluelessuk.bytecode.Instruction
import cluelessuk.bytecode.MemoryAddress
import cluelessuk.bytecode.OpCode
import cluelessuk.bytecode.UInt16
import cluelessuk.bytecode.from
import cluelessuk.bytecode.opcodeDefinitions


data class VirtualMachine(
    private val bytecode: Bytecode
) {
    private val stack = CallStack<MObject>()
    private val globalScope = Scope()
    fun result(): MObject? = stack.lastPoppedValue

    var instructionPointer = 0

    fun run(): VirtualMachine {
        while (bytecode.instructions.size > instructionPointer) {
            val currentInstruction = instructionPointerOnwards()

            when (val opcode = opcodeFrom(currentInstruction)) {
                OpCode.CONSTANT -> runConstant(currentInstruction)
                OpCode.ADD,
                OpCode.SUBTRACT,
                OpCode.MULTIPLY,
                OpCode.DIVIDE,
                OpCode.EQUAL,
                OpCode.NOT_EQUAL,
                OpCode.GREATER_THAN -> runBinaryOperation(opcode)
                OpCode.BANG -> runBangOperation()
                OpCode.MINUS -> negateTopOperation()
                OpCode.POP -> stack.pop()
                OpCode.TRUE -> stack.push(MBoolean.TRUE)
                OpCode.FALSE -> stack.push(MBoolean.FALSE)
                OpCode.JUMP -> runJumpOperation()
                OpCode.JUMP_IF_NOT_TRUE -> runJumpIfNotTrue()
                OpCode.NULL -> stack.push(Null)
                OpCode.SET_GLOBAL -> runSetGlobal()
                OpCode.GET_GLOBAL -> runGetGlobal()
                OpCode.ARRAY -> buildAndPushArray()
                OpCode.HASH_MAP -> buildAndPushHashmap()
                OpCode.INDEX -> runIndexOperation()
            }
            instructionPointer++
        }

        return this
    }

    private fun instructionPointerOnwards(): ByteArray {
        return bytecode.instructions.sliceArray(instructionPointer until bytecode.instructions.size)
    }

    private fun runConstant(instruction: Instruction) {
        val pointer = memoryAddressOperandOf(instruction)
        stack.push(dereference(pointer))
        instructionPointer += operandsWidth(OpCode.CONSTANT)
    }

    private fun runBinaryOperation(opcode: OpCode) {
        if (stack.size() < 2) {
            return
        }

        val right = stack.pop()
        val left = stack.pop()
        val expressionResult = when {
            left is MInteger && right is MInteger -> runIntegerBinaryOperation(opcode, left, right)
            left is MBoolean && right is MBoolean -> runBooleanBinaryOperation(opcode, left, right)
            left is MString && right is MString -> runStringBinaryOperation(opcode, left, right)
            else -> throw RuntimeException("Opcode not supported for the types provided (${left?.type})")
        }

        stack.push(expressionResult)
    }

    private fun runIntegerBinaryOperation(opcode: OpCode, left: MInteger, right: MInteger): MObject {
        return when (opcode) {
            OpCode.ADD -> left + right
            OpCode.SUBTRACT -> left - right
            OpCode.MULTIPLY -> left * right
            OpCode.DIVIDE -> left / right
            OpCode.GREATER_THAN -> MBoolean(left > right)
            else -> throw RuntimeException("Infix opcode $opcode not supported for Integer")
        }
    }

    private fun runBooleanBinaryOperation(opcode: OpCode, left: MBoolean, right: MBoolean): MObject {
        return when (opcode) {
            OpCode.EQUAL -> MBoolean(left == right)
            OpCode.NOT_EQUAL -> MBoolean(left != right)
            else -> throw RuntimeException("Infix opcode $opcode not supported for Boolean")
        }
    }

    private fun runStringBinaryOperation(opcode: OpCode, left: MString, right: MString): MObject {
        return when (opcode) {
            OpCode.ADD -> left + right
            else -> throw java.lang.RuntimeException("Infix opcode $opcode not supported for String")
        }
    }

    private fun dereference(pointer: MemoryAddress): MObject? {
        if (bytecode.constants.size <= pointer.toInt()) {
            return null
        }

        return bytecode.constants[pointer.toInt()]
    }

    private fun runBangOperation() {
        when (stack.pop()) {
            MBoolean.TRUE -> stack.push(MBoolean.FALSE)
            MBoolean.FALSE -> stack.push(MBoolean.TRUE)
            Null -> stack.push(MBoolean.TRUE)
            else -> stack.push(MBoolean.FALSE)
        }
    }

    private fun negateTopOperation() {
        val top = stack.pop()
        if (top !is MInteger) {
            throw RuntimeException("Cannot negate value $top of type ${top?.type}")
        }

        stack.push(MInteger.from(0 - top.value))
    }

    private fun runJumpOperation() {
        val jumpInstruction = instructionPointerOnwards()
        val jumpAddress = memoryAddressOperandOf(jumpInstruction).toInt()
        // minus 1 because we increment it for the opcode after every instruction
        instructionPointer = jumpAddress - 1
    }

    private fun runJumpIfNotTrue() {
        if (!isTruthy(stack.pop())) {
            runJumpOperation()
        } else {
            instructionPointer += operandsWidth(OpCode.JUMP_IF_NOT_TRUE)
        }
    }

    private fun runSetGlobal() {
        val global = stack.pop() ?: throw RuntimeException("Cannot set a global without a value on the stack!")
        val globalIndex = memoryAddressOperandOf(instructionPointerOnwards())
        instructionPointer += operandsWidth(OpCode.SET_GLOBAL)

        globalScope[globalIndex] = global
    }

    private fun runGetGlobal() {
        val globalIndex = memoryAddressOperandOf(instructionPointerOnwards())
        instructionPointer += operandsWidth(OpCode.GET_GLOBAL)

        stack.push(globalScope[globalIndex])
    }

    private fun buildAndPushArray() {
        val arrayLength = intOperandOf(instructionPointerOnwards()).toInt()
        instructionPointer += operandsWidth(OpCode.ARRAY)

        val array = Array<MObject>(arrayLength) { Null }
        val lastIndex = arrayLength - 1
        for (i in lastIndex downTo 0) {
            array[i] = stack.pop() ?: Null
        }

        stack.push(MArray(array))
    }

    private fun buildAndPushHashmap() {
        val mapLength = intOperandOf(instructionPointerOnwards()).toInt()
        instructionPointer += operandsWidth(OpCode.HASH_MAP)

        val map = MHashMap()

        for (i in 0 until (mapLength / 2)) {
            // note that values appear higher on the stack than keys, so this ordering is important
            val value = stack.pop()
            val key = stack.pop()

            when {
                key == Null -> throw RuntimeException("Null is not a valid map key")
                key == null || value == null -> throw RuntimeException("Expected ${mapLength / 2} entries on the stack, but there were only ${i - 1}")
                else -> map[key] = value
            }
        }

        stack.push(map)
    }

    private fun runIndexOperation() {
        val key = stack.pop() ?: Null

        val result = when (val collection = stack.pop()) {
            is MHashMap -> collection[key]
            is MArray -> when (key) {
                is MInteger -> collection[key]
                else -> throw RuntimeException("Array index key must be an Integer, but was ${key.type}")
            }
            else -> throw RuntimeException("Index operator not supported for ${collection?.type}")
        }

        stack.push(result)
    }
}

private fun opcodeFrom(instruction: Instruction): OpCode {
    if (instruction.isEmpty()) {
        throw RuntimeException("Instruction was empty, and thus did not have an opcode")
    }

    return OpCode.from(instruction[0])
}

private fun operandsWidth(opcode: OpCode): Int = opcodeDefinitions[opcode]?.operandWidthBytes?.sum() ?: 0

// this converts the two bytes after the next opcode byte of the instruction into a memory address
private fun memoryAddressOperandOf(instruction: Instruction): MemoryAddress {
    if (instruction.size < 3) {
        throw RuntimeException("Instruction expected a 3 byte instruction, but got ${instruction.size}")
    }
    return MemoryAddress.from(instruction[1], instruction[2])
}

// this converts the two bytes after the next opcode byte of the instruction into a 16bit UInt
private fun intOperandOf(instruction: Instruction): UInt16 {
    if (instruction.size < 3) {
        throw RuntimeException("Instruction expected a 3 byte instruction, but got ${instruction.size}")
    }
    return UInt16.from(instruction[1], instruction[2])
}

private fun isTruthy(obj: MObject?): Boolean = when (obj) {
    null, Null -> false
    is MBoolean -> obj.value
    is MInteger -> obj.value > 0
    else -> true
}
