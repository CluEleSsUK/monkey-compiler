package cluelessuk.vm

import java.util.ArrayDeque
import java.util.Deque

class CallStack<T> {
    private val stack: Deque<T> = ArrayDeque()
    var lastPoppedValue: T? = null

    fun push(value: T?): CallStack<T> {
        stack.push(value)
        return this
    }

    fun pop(): T? {
        return stack.pop().also { lastPoppedValue = it }
    }

    fun size() = stack.size
}