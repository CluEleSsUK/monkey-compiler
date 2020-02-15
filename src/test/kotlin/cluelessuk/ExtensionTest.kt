package cluelessuk

import cluelessuk.bytecode.putUShort
import cluelessuk.bytecode.readUShort
import cluelessuk.bytecode.toAddressArray
import java.nio.ByteBuffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExtensionTest {

    @Test
    fun `Bytebuffer extensions write and read unsigned shorts as expected`() {
        val input = 4.toUShort()
        val buffer = ByteBuffer
            .allocate(UShort.SIZE_BYTES)
            .putUShort(input)

        assertEquals(input, buffer.flip().readUShort())
    }

    @Test
    fun `ByteBuffer toShortArray returns a valid short array`() {
        val input = 65534.toUShort()
        val buffer = ByteBuffer
            .allocate(UShort.SIZE_BYTES)
            .putUShort(input)
        val expected = ushortArrayOf(input)

        assertTrue(expected.contentEquals(buffer.toAddressArray()))
    }
}