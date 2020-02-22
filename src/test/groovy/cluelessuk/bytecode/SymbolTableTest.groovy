package cluelessuk.bytecode

import spock.lang.Specification

class SymbolTableTest extends Specification {
    def symbolTable = new SymbolTable()

    def "Symbols definition returns the created symbol with the next index"() {
        given:
        def symbolName = "myVarName"

        when:
        def output = symbolTable.define(symbolName)

        then:
        output.name == symbolName
        output.index == 0
    }

    def "Symbols can be defined and resolved in the global scope"() {
        given:
        def symbolName = "myVarName"

        when:
        def createdSymbol = symbolTable.define(symbolName)
        def resolvedSymbol = symbolTable.resolve(symbolName)

        then:
        createdSymbol == resolvedSymbol
    }

}
