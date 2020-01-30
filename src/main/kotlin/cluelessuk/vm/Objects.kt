package cluelessuk.vm

import cluelessuk.bytecode.from

sealed class MObject(val type: String)
data class MInteger(val value: UShort) : MObject("INTEGER") {
    companion object {
        @JvmStatic
        fun from(integer: Int): MInteger {
            return MInteger(integer.toUShort())
        }

        fun from(b1: Byte, b2: Byte): MInteger {
            return MInteger(UShort.from(b1, b2))
        }
    }
}

data class MBoolean(val value: Boolean) : MObject("BOOLEAN") {
    companion object {
        fun from(input: Boolean): MBoolean {
            return if (input) True else False
        }
    }
}

data class MString(val value: String) : MObject("STRING")

val True = MBoolean(true)
val False = MBoolean(false)
