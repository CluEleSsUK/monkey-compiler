package cluelessuk


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

    def "Compiler emits the expected code with references to the constant pool"(String input, MObject[] constants, List<byte[]> expected) {
        given:
        def program = new Parser(new Lexer(input)).parseProgram()
        def result = compiler.compile(program)

        expect:
        result instanceof Success
        compiler.constants.get() == constants
        deepEqual(compiler.output.get(), expected)

        where:
        input            | constants                                              | expected
        "1"              | [MInteger.from(1)]                                     | [bytecodeConstant(0), bytecode(OpCode.POP)]
        "1 + 2"          | [MInteger.from(1), MInteger.from(2)]                   | [bytecodeConstant(0), bytecodeConstant(1), bytecode(OpCode.ADD), bytecode(OpCode.POP)]
        "1; 2"           | [MInteger.from(1), MInteger.from(2)]                   | [bytecodeConstant(0), bytecode(OpCode.POP), bytecodeConstant(1), bytecode(OpCode.POP)]
        "1 * 2"          | [MInteger.from(1), MInteger.from(2)]                   | [bytecodeConstant(0), bytecodeConstant(1), bytecode(OpCode.MULTIPLY), bytecode(OpCode.POP)]
        "1 / 2 - 3"      | [MInteger.from(1), MInteger.from(2), MInteger.from(3)] | [bytecodeConstant(0), bytecodeConstant(1), bytecode(OpCode.DIVIDE), bytecodeConstant(2), bytecode(OpCode.SUBTRACT), bytecode(OpCode.POP)]
        "true; false"    | []                                                     | [bytecode(OpCode.TRUE), bytecode(OpCode.POP), bytecode(OpCode.FALSE), bytecode(OpCode.POP)]
        "1 == 2"         | [MInteger.from(1), MInteger.from(2)]                   | [bytecodeConstant(0), bytecodeConstant(1), bytecode(OpCode.EQUAL), bytecode(OpCode.POP)]
        "1 != 2"         | [MInteger.from(1), MInteger.from(2)]                   | [bytecodeConstant(0), bytecodeConstant(1), bytecode(OpCode.NOT_EQUAL), bytecode(OpCode.POP)]
        "1 > 2"          | [MInteger.from(1), MInteger.from(2)]                   | [bytecodeConstant(0), bytecodeConstant(1), bytecode(OpCode.GREATER_THAN), bytecode(OpCode.POP)]
        "1 < 2"          | [MInteger.from(2), MInteger.from(1)]                   | [bytecodeConstant(0), bytecodeConstant(1), bytecode(OpCode.GREATER_THAN), bytecode(OpCode.POP)]
        "true == false"  | []                                                     | [bytecode(OpCode.TRUE), bytecode(OpCode.FALSE), bytecode(OpCode.EQUAL), bytecode(OpCode.POP)]
        "true != false"  | []                                                     | [bytecode(OpCode.TRUE), bytecode(OpCode.FALSE), bytecode(OpCode.NOT_EQUAL), bytecode(OpCode.POP)]
        "!true"          | []                                                     | [bytecode(OpCode.TRUE), bytecode(OpCode.BANG), bytecode(OpCode.POP)]
        "!true == false" | []                                                     | [bytecode(OpCode.TRUE), bytecode(OpCode.BANG), bytecode(OpCode.FALSE), bytecode(OpCode.EQUAL), bytecode(OpCode.POP)]
        "-5 > 10"        | [MInteger.from(5), MInteger.from(10)]                  | [bytecodeConstant(0), bytecode(OpCode.MINUS), bytecodeConstant(1), bytecode(OpCode.GREATER_THAN), bytecode(OpCode.POP)]
    }

    def "Conditionals with an else branch emit jump logic"() {
        given:
        def input = "if (true) { 10 } else { 20 }; 3333;"
        def program = new Parser(new Lexer(input)).parseProgram()

        def expectedConstants = [MInteger.from(10), MInteger.from(20), MInteger.from(3333)]
        def expected = [
                // 0000
                bytecode(OpCode.TRUE),
                // 0001
                make(OpCode.JUMP_IF_NOT_TRUE, 10),
                // 0004
                bytecodeConstant(0),
                // 0007
                make(OpCode.JUMP, 13),
                // 0010
                bytecodeConstant(1),
                // 0013
                bytecode(OpCode.POP),
                // 0014
                bytecodeConstant(2),
                // 0017
                bytecode(OpCode.POP),
        ]

        when:
        def result = compiler.compile(program)

        then:
        ByteUtils.assertExpected(expected, result)
        ByteUtils.assertExpectedConstants(expectedConstants, result)
    }

    def "Conditionals without an else branch emit jump logic but with a null consequence"() {
        given:
        def input = "if (true) { 10 }; 3333;"
        def program = new Parser(new Lexer(input)).parseProgram()

        def expectedConstants = [MInteger.from(10), MInteger.from(3333)]
        def expected = [
                // 0000
                bytecode(OpCode.TRUE),
                // 0001
                make(OpCode.JUMP_IF_NOT_TRUE, 10),
                // 0004
                bytecodeConstant(0),
                // 0007
                make(OpCode.JUMP, 11),
                // 0010
                make(OpCode.NULL),
                // 0011
                bytecode(OpCode.POP),
                // 0012
                bytecodeConstant(1),
                // 0015
                bytecode(OpCode.POP),
        ]

        when:
        def result = compiler.compile(program)

        then:
        ByteUtils.assertExpected(expected, result)
        ByteUtils.assertExpectedConstants(expectedConstants, result)
    }

}

