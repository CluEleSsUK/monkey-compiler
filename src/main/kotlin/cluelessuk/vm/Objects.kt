package cluelessuk.vm

import cluelessuk.bytecode.from

sealed class MObject(val type: String)
object Null : MObject("NULL")

data class MInteger(val value: Int) : MObject("INTEGER") {

    operator fun plus(other: MInteger): MInteger {
        return from(this.value + other.value)
    }

    operator fun minus(other: MInteger): MInteger {
        return from(this.value - other.value)
    }

    operator fun times(other: MInteger): MInteger {
        return from(this.value * other.value)
    }

    operator fun div(other: MInteger): MInteger {
        return from(this.value / other.value)
    }

    operator fun compareTo(other: MInteger): Int {
        return this.value - other.value
    }

    companion object {
        @JvmStatic
        fun from(integer: Int): MInteger {
            return MInteger(integer)
        }

        fun from(ushort: UShort): MInteger {
            return MInteger(ushort.toInt())
        }

        fun from(b1: Byte, b2: Byte): MInteger {
            return from(UShort.from(b1, b2))
        }
    }
}

data class MBoolean(val value: Boolean) : MObject("BOOLEAN") {

    companion object {
        val TRUE = MBoolean(true)
        val FALSE = MBoolean(false)
    }
}

data class MString(val value: String) : MObject("STRING") {
    operator fun plus(other: MString): MString {
        return MString(this.value + other.value)
    }
}

data class MArray(val values: Array<MObject>) : MObject("ARRAY") {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MArray

        if (!values.contentEquals(other.values)) return false

        return true
    }

    override fun hashCode(): Int {
        return values.contentHashCode()
    }

    companion object {
        @JvmStatic
        fun from(list: List<MObject>): MArray {
            return MArray(list.toTypedArray())
        }
    }
}