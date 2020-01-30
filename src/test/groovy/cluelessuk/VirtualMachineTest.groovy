package cluelessuk

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
    def vm = new VirtualMachine()

    def "Constant allocations return the correct reference to the constant pool"(String input, MObject expected) {
        given:
        def program = new Parser(new Lexer(input)).parseProgram()
        def compiled = compiler.compile(program)

        expect:
        compiled instanceof Success
        def runtimeOutput = vm.run(compiler.bytecode())
        runtimeOutput.peek() == expected

        where:
        input   | expected
        "1"     | MInteger.from(0)
        "2"     | MInteger.from(0)
        "1 + 2" | MInteger.from(1)
    }
}
