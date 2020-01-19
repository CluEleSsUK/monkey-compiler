package cluelessuk

import cluelessuk.language.Lexer
import cluelessuk.language.Parser
import spock.lang.Specification

class CompilerKtTest extends Specification {

    def encoder = new ByteEncoder()
    def compiler = new Compiler()

    def "Integer arithmetic returns expected integer constants"() {
        given:
        def input = "1 + 2"
        def program = new Parser(new Lexer(input)).parseProgram()

        when:
        def result = compiler.compile(program)

        then:
        result instanceof Success
        compiler.bytecode().constants == [1, 2] as Object[]
        compiler.bytecode().instructions == [encoder.make(OpCode.CONSTANT, 0.shortValue()), encoder.make(OpCode.CONSTANT, 1.shortValue())] as byte[][]
    }

    def "Printing an instruction is nice and readable"() {
        given:
        def instructions = asInstruction(make(OpCode.CONSTANT, 1), make(OpCode.CONSTANT, 2), make(OpCode.CONSTANT, 65534))
        def expected = "0000 CONSTANT 1\n0003 CONSTANT 2\n0006 CONSTANT 65534"

        when:
        def result = instructions.toString()

        then:
        result == expected
    }

    Instructions asInstruction(byte[] ... input) {
        def arr = []
        for (byte[] i : input) {
            arr.addAll(i)
        }
        return new Instructions(arr as byte[])
    }

    byte[] make(OpCode opcode, int value) {
        return encoder.make(opcode, value.shortValue())
    }

}
