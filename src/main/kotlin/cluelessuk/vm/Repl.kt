package cluelessuk.vm

import cluelessuk.language.Lexer
import cluelessuk.language.Parser
import cluelessuk.bytecode.Compiler
import cluelessuk.bytecode.Failure
import cluelessuk.bytecode.Success
import cluelessuk.language.Program

const val exitKeyword = "exit"

fun main() {
    startRepl()
}

fun startRepl() {
    println("Monkey REPL! Type `exit` to exit.")


    while (true) {
        val compiler = Compiler()
        val input = readConsoleInput()
        if (input.isNullOrBlank() || input == exitKeyword) {
            break
        }

        val parser = Parser(Lexer(input))
        val program = parser.parseProgram()

        if (program.hasErrors()) {
            renderError(program)
            continue
        }

        when (val bytecode = compiler.compile(program)) {
            is Failure -> renderCompileError(bytecode)
            is Success -> {
                val vm = VirtualMachine(bytecode.value)
                val topOfStack = vm.run().peek()

                render(topOfStack ?: MInteger.from(0))
            }
        }
    }
}

fun renderCompileError(output: Failure) {
    println("Compilation failed:")
    output.reasons.forEach(::println)
}

fun readConsoleInput(): String? {
    print(">> ")
    return readLine()
}

fun render(obj: MObject) {
    println(obj)
}

fun renderError(program: Program) {
    println("Error(s) in program!")
    program.errors.forEach(::println)
}
