package cluelessuk.vm

const val defaultScopeSize = UShort.MAX_VALUE

class Scope(size: Int = defaultScopeSize.toInt()) {
    private val objects: Array<MObject?> = arrayOfNulls(size)

    operator fun get(index: Int): MObject? {
        return objects[index]
    }

    operator fun set(index: Int, value: MObject) {
        objects[index] = value
    }
}