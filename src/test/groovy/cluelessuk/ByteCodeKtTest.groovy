package cluelessuk

import spock.lang.Specification

class ByteCodeKtTest extends Specification {
    def bytecode = new ByteCode()

    def "Opcodes return the correct number of bytes"(OpCode opcode, List<Short> operand, byte[] expected) {
        given:
        def result = bytecode.make(opcode, operand)

        expect:
        result.array() == expected

        where:
        opcode          | operand | expected
        OpCode.CONSTANT | [65534] | [OpCode.CONSTANT.byte(), 255, 254] // 32 bit number uses up 2 * 16bit words
    }
}
