package dev.mateux

import java.util.concurrent.locks.ReentrantLock

class KeyLock<T> {
    private val locks = mutableMapOf<T, ReentrantLock>()

    fun <R> withLock(key: T, block: () -> R): R {
        val lock = locks.computeIfAbsent(key) { ReentrantLock() }
        lock.lock()
        try {
            return block()
        } finally {
            lock.unlock()
        }
    }
}
