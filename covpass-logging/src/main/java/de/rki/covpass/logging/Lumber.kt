/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.logging

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
        public val enabled: Boolean
            get() = Timber.treeCount > 0

        /** Plants debug tree if none was planted yet. */
        public fun plantDebugTreeIfNeeded() {
            if (Timber.treeCount == 0) {
                Timber.plant(Timber.DebugTree())
            }
        }

        /** Adds the given logging [tree]. */
        public fun plant(tree: Timber.Tree) {
            Timber.plant(tree)
        }

        /** Removes the given logging [tree]. */
        public fun uproot(tree: Timber.Tree) {
            Timber.uproot(tree)
        }

        /** Removes all logging trees. */
        public fun uprootAll() {
            Timber.uprootAll()
        }

        /** Log a verbose exception and/or a message specified by [LogBlock]. */
        public inline fun v(err: Throwable? = null, crossinline block: LogBlock = { null }) {
            if (enabled) {
                Timber.v(err, block())
            }
        }

        /** Log a debug exception and/or a message specified by [LogBlock]. */
        public inline fun d(err: Throwable? = null, crossinline block: LogBlock = { null }) {
            if (enabled) {
                Timber.d(err, block())
            }
        }

        /** Log an info exception and/or a message specified by [LogBlock]. */
        public inline fun i(err: Throwable? = null, crossinline block: LogBlock = { null }) {
            if (enabled) {
                Timber.i(err, block())
            }
        }

        /** Log a warning exception and/or a message specified by [LogBlock]. */
        public inline fun w(err: Throwable? = null, crossinline block: LogBlock = { null }) {
            if (enabled) {
                Timber.w(err, block())
            }
        }

        /** Log an assert exception and/or a message specified by [LogBlock]. */
        public inline fun wtf(err: Throwable? = null, crossinline block: LogBlock = { null }) {
            if (enabled) {
                Timber.wtf(err, block())
            }
        }

        /** Log an error exception and/or a message specified by [block]. */
        public inline fun e(err: Throwable? = null, crossinline block: LogBlock = { null }) {
            if (enabled) {
                Timber.e(err, block())
            }
        }

        /** Log with given priority an exception and/or a message specified by [LogBlock]. */
        public inline fun log(priority: Int, err: Throwable?, crossinline block: LogBlock) {
            if (enabled) {
                Timber.log(priority, err, block())
            }
        }
    }
}

/** A function returning a log message. Obfuscation can fully strip this, even when using string templates. */
public typealias LogBlock = () -> String?

/** Records logging calls. Can be useful for unit tests. */
public class LogRecorder : Timber.DebugTree() {
    public val logs: MutableList<String> = mutableListOf()

    public fun reset() {
        logs.clear()
    }

    public fun print() {
        logs.forEach(::println)
        logs.clear()
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        logs.add("$tag: $message")
    }
}
