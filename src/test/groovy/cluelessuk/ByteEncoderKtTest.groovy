package cluelessuk

import cluelessuk.bytecode.ByteEncoder
import cluelessuk.bytecode.OpCode
import kotlin.UShortArray
import spock.lang.Specification
import static cluelessuk.TestUtils.*

class ByteEncoderKtTest extends Specification {
    def bytecode = new ByteEncoder()

    def "Opcodes return the correct number of bytes"(OpCode opcode, UShortArray operands, byte[] expected) {
        given:
        def result = bytecode.make(opcode, operands.toList())

        expect:
        result == expected

        where:
        opcode          | operands             | expected
        OpCode.CONSTANT | ushortArrayOf(0)     | [OpCode.CONSTANT.byte(), 0, 0] as byte[]
        OpCode.CONSTANT | ushortArrayOf(1)     | [OpCode.CONSTANT.byte(), 0, 1] as byte[]
        OpCode.CONSTANT | ushortArrayOf(65534) | [OpCode.CONSTANT.byte(), 255, 254] as byte[]
        OpCode.CONSTANT | ushortArrayOf(65535) | [OpCode.CONSTANT.byte(), 255, 255] as byte[]
        OpCode.ADD      | ushortArrayOf()      | [OpCode.ADD.byte()] as byte[]
    }

    def "Reading operands returns the correct operands"(OpCode opcode, UShortArray operands, int bytesRead) {
        given:
        def instruction = bytecode.make(opcode, operands.toList())
        def result = bytecode.readOperands(instruction)

        expect:
        equalValues(result.first, operands)
        result.second == bytesRead

        where:
        opcode          | operands             | bytesRead
        OpCode.CONSTANT | ushortArrayOf(65534) | 2
        OpCode.ADD      | ushortArrayOf()      | 0
    }


}
