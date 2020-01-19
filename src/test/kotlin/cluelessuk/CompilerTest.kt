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

        // when
        val result = compiler.compile(program)

        // then
        assertTrue { result is Success }
        assertTrue { compiler.bytecode().constants.contentEquals(arrayOf(1, 2)) }
        assertTrue {
            compiler.bytecode().instructions.contentEquals(arrayOf(encoder.make(OpCode.CONSTANT, 1.toUShort()), encoder.make(OpCode.CONSTANT, 2.toUShort())))
        }
    }
}