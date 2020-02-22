package cluelessuk.bytecode

typealias SymbolScope = String

const val GlobalScope: SymbolScope = "GLOBAL"

data class Symbol(val name: String, val scope: SymbolScope, val index: Int)

class SymbolTable {
    private val store = mutableMapOf<String, Symbol>()

    fun define(variableName: String): Symbol {
        val symbol = Symbol(variableName, GlobalScope, store.size)
        store[variableName] = symbol
        return symbol
    }

    fun resolve(variableName: String): Symbol? {
        return store[variableName]
    }
}
