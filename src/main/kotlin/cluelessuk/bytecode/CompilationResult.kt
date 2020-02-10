package cluelessuk.bytecode


sealed class CompilationResult<T> {

    fun <U> map(fn: (T) -> U): CompilationResult<U> {
        return when (this) {
            is Failure -> Failure(this.reasons)
            is Success -> Success(fn(this.value))
        }
    }

    fun <U> flatMap(fn: (CompilationResult<T>) -> CompilationResult<U>): CompilationResult<U> {
        if (this is Failure) {
            return Failure(this.reasons)
        }
        return fn(this)
    }

    fun then(block: () -> Unit): CompilationResult<T> {
        if (this is Failure) {
            return Failure(this.reasons)
        }

        block()
        return this
    }
}

data class Success<T>(val value: T) : CompilationResult<T>()
data class Failure<T>(val reasons: List<String>) : CompilationResult<T>()