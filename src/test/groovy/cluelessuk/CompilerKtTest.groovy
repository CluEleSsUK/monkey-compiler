package cluelessuk

import cluelessuk.bytecode.ByteEncoder
import cluelessuk.bytecode.OpCode
import cluelessuk.bytecode.Success
import cluelessuk.language.Lexer
import cluelessuk.language.Parser
import cluelessuk.bytecode.Compiler
import cluelessuk.vm.MInteger
import cluelessuk.vm.MObject

import static cluelessuk.TestUtils.*


import spock.lang.Specification

class CompilerKtTest extends Specification {

    def compiler = new Compiler()
    static def encoder = new ByteEncoder()

    def "Compiler emits the expected code with references to the constant pool"(String input, byte[][] expected) {
        given:
        def program = new Parser(new Lexer(input)).parseProgram()
        def result = compiler.compile(program)

        expect:
        result instanceof Success
        deepEqual(compiler.output.get(), expected)

        where:
        input   | expected
        "1"     | [makeConstant(0)]
        "1 + 2" | [makeConstant(0), makeConstant(1), encoder.make(OpCode.ADD)]
    }

    def "Compiler emits the expected constant pool"(String input, MObject[] expected) {
        given:
        def program = new Parser(new Lexer(input)).parseProgram()
        def result = compiler.compile(program)

        expect:
        result instanceof Success

        where:
        input   | expected
        "1"     | [MInteger.from(1)]
        "1 + 2" | [MInteger.from(1), MInteger.from(2)]
    }

}

