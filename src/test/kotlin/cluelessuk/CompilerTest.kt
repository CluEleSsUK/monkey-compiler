package cluelessuk

import cluelessuk.bytecode.ByteEncoder
import cluelessuk.bytecode.OpCode
import cluelessuk.language.Lexer
import cluelessuk.language.Parser
import org.junit.Test
import kotlin.test.assertTrue

class CompilerTest {

    private val encoder = ByteEncoder()
    private val compiler = Compiler()

    @Test
    fun `Integer arithmetic returns expected integer constants`() {
        // given
        val input = "1 + 2"
        val program = Parser(Lexer(input)).parseProgram()
        val expectedOutput = arrayOf(MInteger(1), MInteger(2))

        // when
        val result = compiler.compile(program)

        // then
        assertTrue { result is Success }
        assertTrue { compiler.bytecode().constants.contentEquals(expectedOutput) }
    }

    @Test
    fun `Integer arithmetic adds integer constants to the constant pool`() {
        // given
        val input = "1 + 2"
        val program = Parser(Lexer(input)).parseProgram()
        val makeConstant = { memoryAddress: Int -> encoder.make(OpCode.CONSTANT, memoryAddress.toUShort()) }
        val expectedConstantsWithAddress = arrayOf(makeConstant(0), makeConstant(1))

        // when
        val result = compiler.compile(program)

        // then
        assertTrue { result is Success }
        assertTrue { compiler.bytecode().instructions.contentDeepEquals(expectedConstantsWithAddress) }
    }
}
