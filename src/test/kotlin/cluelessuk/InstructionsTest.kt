package cluelessuk

import cluelessuk.bytecode.ByteEncoder
import cluelessuk.bytecode.Instructions
import cluelessuk.bytecode.OpCode
import org.junit.Test
import kotlin.test.assertEquals

class InstructionsTest {

    private val encoder = ByteEncoder()

    @Test
    fun `Printing an instruction is nice and readable`() {
        // given
        val instructions = byteArrayOf(
            *encoder.make(OpCode.CONSTANT, 1.toUShort()),
            *encoder.make(OpCode.ADD, emptyList()),
            *encoder.make(OpCode.CONSTANT, 65534.toUShort())
        )
        val expected = "0000 CONSTANT 1\n0003 ADD\n0004 CONSTANT 65534\n"

        // when
        val result = Instructions(instructions).toString()

        // then
        assertEquals(expected, result)
    }
}