package cluelessuk.vm

import cluelessuk.bytecode.MemoryAddress

const val defaultScopeSize = UShort.MAX_VALUE

class Scope(size: Int = defaultScopeSize.toInt()) {
    private val objects: Array<MObject?> = arrayOfNulls(size)

    operator fun get(index: MemoryAddress): MObject? {
        return objects[index.toInt()]
    }

    operator fun set(index: MemoryAddress, value: MObject) {
        objects[index.toInt()] = value
    }
}