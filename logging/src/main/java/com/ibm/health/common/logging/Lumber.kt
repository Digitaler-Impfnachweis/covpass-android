package com.ibm.health.common.logging

import timber.log.Timber

/**
 * This logger wraps Timber and should be used instead of Timber because it allows full obfuscation/removal of logs.
 *
 * Usage:
 *
 * ```kotlin
 * Lumber.d { "Message: ${some.value}" }
 * Lumber.e(throwable) { "Extra message ${some.value}" }
 * Lumber.e(throwable)
 * ```
 *
 * This way, the release builds can remove the whole log message and all log strings (unlike Timber).
 */
public class Lumber {
    public companion object {
        /** Plants debug tree if none was planted yet. */
        public fun plantDebugTreeIfNeeded(shouldPlant: Boolean = true) {
            if (shouldPlant && Timber.treeCount() == 0) {
                Timber.plant(LoggerTree())
            }
        }

        public fun v(err: Throwable? = null, block: LogBlock? = null) {
            Timber.v(err, block?.invoke())
        }

        public fun d(err: Throwable? = null, block: LogBlock? = null) {
            Timber.d(err, block?.invoke())
        }

        public fun i(err: Throwable? = null, block: LogBlock? = null) {
            Timber.i(err, block?.invoke())
        }

        public fun w(err: Throwable? = null, block: LogBlock? = null) {
            Timber.w(err, block?.invoke())
        }

        public fun wtf(err: Throwable? = null, block: LogBlock? = null) {
            Timber.wtf(err, block?.invoke())
        }

        public fun e(err: Throwable? = null, block: LogBlock? = null) {
            Timber.e(err, block?.invoke())
        }

        public fun log(priority: Int, err: Throwable?, block: LogBlock) {
            Timber.log(priority, err, block())
        }
    }
}

public fun interface LogBlock {
    public operator fun invoke(): String
}

// Since we wrap the call stack in another layer we have to fix the tag by going to the caller of Lumber, not of Timber
private class LoggerTree : Timber.DebugTree() {
    override fun createStackElementTag(element: StackTraceElement): String? {
        // See the Timber.DebugTree implementation. They use CALL_STACK_INDEX = 5 within getTag(). We additionally
        // skip the Lumber code.
        val callStackIndex = 5
        val stackTrace = Throwable().stackTrace.toMutableList()
        stackTrace.removeAll {
            it.className == LoggerTree::class.java.name ||
                it.className == Lumber.Companion::class.java.name
        }
        val stackTraceElement = if (stackTrace.size > callStackIndex) stackTrace[callStackIndex] else element
        return super.createStackElementTag(stackTraceElement)
    }
}
