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

    def "Compiler emits the expected code with references to the constant pool"(String input, MObject[] constants, byte[][] expected) {
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

}

