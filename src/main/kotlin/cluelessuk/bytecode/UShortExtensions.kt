package cluelessuk.bytecode

import java.nio.ByteBuffer

fun ByteBuffer.putUShort(input: UShort): ByteBuffer {
    this.putShort(input.toShort())
    return this
}

fun ByteBuffer.readUShort(): UShort {
    return UShort.from(this.get(), this.get())
}

fun UShort.Companion.from(one: Byte, two: Byte): UShort {
    return ((one.toInt() shl 8) or two.toInt()).toUShort()
}

fun ByteBuffer.toAddressArray(): MemoryAddressArray {
    this.rewind()
    val array = UShortArray(this.limit() / 2) { 0.toMemoryAddress() }
    var index = 0
    while (this.hasRemaining()) {
        array[index] = this.readUShort()
        index += 2
    }
    return array
}

