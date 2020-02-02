package cluelessuk.vm

import cluelessuk.bytecode.from

sealed class MObject(val type: String)

data class MInteger(val value: UShort) : MObject("INTEGER") {
    companion object {
        @JvmStatic
        fun from(integer: Int): MInteger {
            return MInteger(integer.toUShort())
        }

        fun from(integer: UInt): MInteger {
            return MInteger(integer.toUShort())
        }

        fun from(b1: Byte, b2: Byte): MInteger {
            return MInteger(UShort.from(b1, b2))
        }
    }
}

data class MBoolean(val value: Boolean) : MObject("BOOLEAN") {
    companion object {
        val TRUE = MBoolean(true)
        val FALSE = MBoolean(false)
    }
}

