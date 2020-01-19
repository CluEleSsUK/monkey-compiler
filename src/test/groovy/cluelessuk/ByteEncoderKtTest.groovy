package cluelessuk

import kotlin.UShortArray
import spock.lang.Specification

class ByteEncoderKtTest extends Specification {
    def bytecode = new ByteEncoder()

    def "Opcodes return the correct number of bytes"(OpCode opcode, UShortArray operands, byte[] expected) {
        given:
        def result = bytecode.make(opcode, operands.toList())

        expect:
        result == expected

        where:
        opcode          | operands             | expected
        OpCode.CONSTANT | ushortArrayOf(65534) | [OpCode.CONSTANT.byte(), 255, 254] as byte[] // 32 bit number uses up 2 * 16bit words
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
    }

    def ushortArrayOf(Integer... nums) {
        return new UShortArray(nums as short[])
    }

    def equalValues(UShortArray first, UShortArray second) {
        if (first.size != second.size) {
            return false
        }

        for (int i = 0; i < first.size; i++) {
            if (first[i] != second[i]) {
                return false
            }
        }

        return true
    }

}
