package cluelessuk

import cluelessuk.bytecode.ByteEncoder
import cluelessuk.bytecode.OpCode
import kotlin.UShortArray

class TestUtils {
    static def encoder = new ByteEncoder()

    static def ushortArrayOf(Integer... nums) {
        return new UShortArray(nums as short[])
    }

    static def ushortListOf(Integer... nums) {
        def list = []
        ushortArrayOf(nums).forEach { item -> list.add(item) }
        return list
    }

    static def equalValues(UShortArray first, UShortArray second) {
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

    static bytecodeConstant(int memoryAddress) {
        return make(OpCode.CONSTANT, memoryAddress)
    }

    static bytecode(OpCode opcode) {
        return encoder.make(opcode)
    }

    static make(OpCode opcode, Integer... operands) {
        return encoder.make(opcode, ushortListOf(operands))
    }

    static boolean deepEqual(byte[] arr1, List<byte[]> arr2) {
        def flattened = arr2.flatten().toArray()
        if (arr1.length != flattened.length) {
            return false
        }

        for (int i = 0; i < arr1.length; i++) {
            if (arr1[i] != flattened[i]) {
                return false
            }
        }
        return true
    }
}
