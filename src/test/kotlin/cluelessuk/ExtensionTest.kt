package cluelessuk

import java.nio.ByteBuffer
import kotlin.test.Test
import kotlin.test.assertEquals

class ExtensionTest {

    @Test
    fun `Bytebuffer extensions write and read unsigned shorts as expected`() {
        val input = 4.toUShort()
        val buffer = ByteBuffer
            .allocate(UShort.SIZE_BYTES)
            .putUShort(input)

        assertEquals(input, buffer.flip().readUShort())
    }
}