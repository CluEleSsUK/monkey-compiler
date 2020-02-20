package cluelessuk.bytecode

import cluelessuk.vm.MObject

class ConstantPool {
    private val objects = mutableListOf<MObject>()

    // returns last index of constant pool as an ID for the item added
    fun addConstantForIndex(obj: MObject): MemoryAddress {
        objects += obj
        return objects.lastIndex.toMemoryAddress()
    }

    fun get(): Array<MObject> = objects.toTypedArray()
}
