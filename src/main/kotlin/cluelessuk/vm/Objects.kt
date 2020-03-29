package cluelessuk.vm

import cluelessuk.bytecode.from
import kotlin.math.absoluteValue

sealed class MObject(val type: String) {
    fun hashKey(): MInteger {
        return MInteger.from(this.hashCode())
    }
}

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

    operator fun get(mIndex: MInteger): MObject {
        val index = mIndex.value

        if (index < 0) {
            return Null
        }

        if (values.lastIndex < index) {
            return Null
        }

        return values[index]
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MArray

        return values.contentEquals(other.values)
    }

    override fun hashCode(): Int = values.contentHashCode()

    companion object {
        @JvmStatic
        fun from(list: List<MObject>): MArray {
            return MArray(list.toTypedArray())
        }
    }
}

// currently does not account for collisions
class MHashMap : MObject("HASHMAP") {

    private val maxHashmapSize = 1024 * 1024
    private var store = Array<MObject?>(1024) { null }

    operator fun set(key: MObject, value: MObject?): MHashMap {
        increaseStoreSizeIfNecessary(key)
        store[storeIndexFor(key)] = value

        return this
    }

    operator fun get(key: MObject): MObject {
        val hashedKey = storeIndexFor(key)

        if (hashedKey > store.lastIndex) {
            return Null
        }

        return store[hashedKey] ?: Null
    }

    private fun increaseStoreSizeIfNecessary(key: MObject) {
        val positiveHashKey = hashKeyOf(key).absoluteValue

        if (store.lastIndex < positiveHashKey) {
            store = store.copyOf(positiveHashKey * 2)
        }
    }

    private fun hashKeyOf(key: MObject): Int {
        return key.hashKey().value % maxHashmapSize
    }

    private fun storeIndexFor(key: MObject): Int {
        val hash = hashKeyOf(key)

        if (hash < 0) {
            return store.lastIndex + hash
        }

        return hash
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MHashMap

        return store.contentEquals(other.store)
    }

    override fun hashCode(): Int = store.contentHashCode()

    companion object {
        @JvmStatic
        fun from(values: Map<MObject, MObject>): MHashMap {
            val map = MHashMap()
            values.forEach {
                map[it.key] = it.value
            }
            return map
        }
    }
}
