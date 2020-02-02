package cluelessuk

import cluelessuk.bytecode.Bytecode
import cluelessuk.bytecode.Compiler
import cluelessuk.bytecode.Success
import cluelessuk.language.Lexer
import cluelessuk.language.Parser
import cluelessuk.vm.MInteger
import cluelessuk.vm.MObject
import cluelessuk.vm.VirtualMachine
import spock.lang.Specification

class VirtualMachineTest extends Specification {
    def compiler = new Compiler()

    def "Constant allocations dereference from the constant pool"(String input, MObject expected) {
        given:
        def bytecode = successfullyCompiled(input)
        def output = new VirtualMachine(bytecode).run()

        expect:
        output.result() == expected

        where:
        input   | expected
        "1"     | MInteger.from(1)
        "2"     | MInteger.from(2)
        "1 + 2" | MInteger.from(3)
    }

    private Bytecode successfullyCompiled(String input) {
        def program = new Parser(new Lexer(input)).parseProgram()
        def compiled = compiler.compile(program)
        assert (compiled instanceof Success)
        return compiler.bytecode()
    }
}
