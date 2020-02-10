package cluelessuk.vm

import java.util.ArrayDeque
import java.util.Deque

class CallStack<T> {
    private val stack: Deque<T> = ArrayDeque()
    var lastPoppedValue: T? = null

    fun push(value: T?): CallStack<T> {
        if (value == null) {
            return this
        }
        stack.push(value)
        return this
    }

    fun pop(): T? {
        if (stack.isEmpty()) {
            lastPoppedValue = null
            return lastPoppedValue
        }
        return stack.pop().also { lastPoppedValue = it }
    }

    fun size() = stack.size
}