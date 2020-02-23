package cluelessuk.bytecode

typealias MemoryAddress = UShort
typealias UInt16 = UShort
typealias MemoryAddressArray = UShortArray

fun Int.toMemoryAddress() = this.toUShort()
fun Int.toUInt16() = this.toUShort()

typealias Instruction = ByteArray
typealias BytesRead = Int