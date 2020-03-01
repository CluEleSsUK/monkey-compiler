package cluelessuk

import cluelessuk.bytecode.Bytecode
import cluelessuk.bytecode.Compiler
import cluelessuk.bytecode.Success
import cluelessuk.language.Lexer
import cluelessuk.language.Parser
import cluelessuk.vm.MArray
import cluelessuk.vm.MBoolean
import cluelessuk.vm.MHashMap
import cluelessuk.vm.MInteger
import cluelessuk.vm.MObject
import cluelessuk.vm.MString
import cluelessuk.vm.VirtualMachine
import spock.lang.Specification

class VirtualMachineTest extends Specification {
    def compiler = new Compiler()
    def static Null = KotlinTestUtils.createNull()

    def "Integer arithmetic creates the expected output"(String input, MInteger expected) {
        given:
        def bytecode = successfullyCompiled(input)
        def output = new VirtualMachine(bytecode).run()

        expect:
        output.result() == expected

        where:
        input              | expected
        "1"                | MInteger.from(1)
        "2"                | MInteger.from(2)
        "1 + 2"            | MInteger.from(3)
        "10 * 10 / 10 - 9" | MInteger.from(1)
        "10 - 9 * 10 / 10" | MInteger.from(1)
        "-5"               | MInteger.from(-5)
        "-5 + 5"           | MInteger.from(0)
        "-5 * -5"          | MInteger.from(25)
    }

    def "Boolean logic creates the expected output"(String input, MBoolean expected) {
        given:
        def bytecode = successfullyCompiled(input)
        def output = new VirtualMachine(bytecode).run()

        expect:
        output.result() == expected

        where:
        input                   | expected
        "true"                  | new MBoolean(true)
        "false"                 | new MBoolean(false)
        "true == true"          | new MBoolean(true)
        "true != true"          | new MBoolean(false)
        "true == false"         | new MBoolean(false)
        "2 > 1"                 | new MBoolean(true)
        "2 < 1"                 | new MBoolean(false)
        "(2 > 1) == false"      | new MBoolean(false)
        "(10 + 10) / 10 > 2"    | new MBoolean(false)
        "!true"                 | new MBoolean(false)
        "!false"                | new MBoolean(true)
        "!!true"                | new MBoolean(true)
        "!5"                    | new MBoolean(false)
        "!!5"                   | new MBoolean(true)
        "!(if (false) { 5; } )" | new MBoolean(true)
    }

    def "Conditionals evaluate to the correct output"(String input, MObject expected) {
        given:
        def bytecode = successfullyCompiled(input)
        def output = new VirtualMachine(bytecode).run()

        expect:
        output.result() == expected

        where:
        input                                        | expected
        "if (true) { 10; }"                          | MInteger.from(10)
        "if (true) { 10; } else { 20; }"             | MInteger.from(10)
        "if (false) { 10; } else { 20; }"            | MInteger.from(20)
        "if (1) { 10; }"                             | MInteger.from(10)
        "if (1 < 2) { 10; }"                         | MInteger.from(10)
        "if (1 < 2) { 10; } else { 20 + 10; }"       | MInteger.from(10)
        "if (1 > 2) { 10; } else { 20 + 10; }"       | MInteger.from(30)
        "if (1 >  2) { 10; }"                        | Null
        "if (false) { 10; } "                        | Null
        "if (if (false) { 5; }) { 5; } else { 20; }" | MInteger.from(20)
    }

    def "Global let statements can resolve values"(String input, MObject expected) {
        given:
        def bytecode = successfullyCompiled(input)
        def output = new VirtualMachine(bytecode).run()

        expect:
        output.result() == expected

        where:
        input                                             | expected
        "let one = 1; one;"                               | MInteger.from(1)
        "let one = 1; let two = 2; one + two;"            | MInteger.from(3)
        "let one = 1; let two = 2; one + one; one + two;" | MInteger.from(3)
    }

    def "String expressions output the correct String values"(String input, MObject expected) {
        given:
        def bytecode = successfullyCompiled(input)
        def output = new VirtualMachine(bytecode).run()

        expect:
        output.result() == expected

        where:
        input              | expected
        '"something"'      | new MString("something")
        '"some" + "thing"' | new MString("something")
    }

    def "Array expressions evaluate sub-expressions and output the correct types"(String input, MObject expected) {
        given:
        def bytecode = successfullyCompiled(input)
        def output = new VirtualMachine(bytecode).run()

        expect:
        output.result() == expected

        where:
        input                        | expected
        '[]'                         | MArray.from([])
        '[1]'                        | MArray.from([MInteger.from(1)])
        '[9, 2]'                     | MArray.from([MInteger.from(9), MInteger.from(2)])
        '[1 + 1, 2 * 2]'             | MArray.from([MInteger.from(2), MInteger.from(4)])
        '["abc" + "def", 3 + 2 * 2]' | MArray.from([new MString("abcdef"), MInteger.from(7)])
        '[if (false) { 5; }]'        | MArray.from([Null])
    }

    def "Hash literals evaluate sub-expressions"(String input, MObject expected) {
        given:
        def bytecode = successfullyCompiled(input)
        def output = new VirtualMachine(bytecode).run()

        expect:
        output.result() == expected

        where:
        input                            | expected
        '{}'                             | MHashMap.from([:])
        '{ 1: 2, 2: 3 }'                 | MHashMap.from([(MInteger.from(1)): MInteger.from(2), (MInteger.from(2)): MInteger.from(3)])
        '{ 1 + 1: 2 + 2, 2 + 2: 3 + 3 }' | MHashMap.from([(MInteger.from(2)): MInteger.from(4), (MInteger.from(4)): MInteger.from(6)])
    }

    private Bytecode successfullyCompiled(String input) {
        def program = new Parser(new Lexer(input)).parseProgram()
        def compiled = compiler.compile(program)
        assert (compiled instanceof Success)
        return (Bytecode) compiled.value
    }
}
