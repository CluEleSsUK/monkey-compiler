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

    static makeConstant(int memoryAddress) {
        return encoder.make(OpCode.CONSTANT, ushortListOf(memoryAddress))
    }

    static boolean deepEqual(byte[][] arr1, byte[][] arr2) {
        if (arr1.length != arr2.length) {
            return true
        }

        for (int i = 0; i < arr1.length; i++) {
            if (arr1[i] != arr2[i]) {
                return false
            }
        }
        return true
    }
}
