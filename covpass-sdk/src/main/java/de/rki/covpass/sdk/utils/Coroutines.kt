/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

import de.rki.covpass.logging.Lumber
import kotlinx.coroutines.*
import kotlin.math.min

/** Runs the given [block] and if any exception happens it re-executes the [block] with an exponential delay. */
@ExperimentalHCertApi
public suspend fun <T> retry(
    times: Int? = null,
    retryStrategy: RetryStrategy = ExponentialBackoffRetryStrategy(),
    block: suspend () -> T,
): T {
    var attempt = 0
    while (true) {
        if (times != null) {
            attempt += 1
        }
        try {
            return block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Lumber.e(e)
            if (times != null && attempt > times) {
                throw e
            }
            retryStrategy.delayRetry()
        }
    }
}

@ExperimentalHCertApi
public interface RetryStrategy {
    public suspend fun delayRetry()
}

@ExperimentalHCertApi
public open class ExponentialBackoffRetryStrategy(
    public val startDelayMillis: Long = 1000,
    public val delayFactor: Double = 2.0,
    public val maxDelayMillis: Long = 1000 * 60 * 60,
) : RetryStrategy {
    public var delay: Long = startDelayMillis
        private set

    public fun resetDelay() {
        delay = startDelayMillis
    }

    public fun increaseDelay() {
        delay = min((delay * delayFactor).toLong(), maxDelayMillis)
    }

    override suspend fun delayRetry() {
        delay(delay)
        increaseDelay()
    }
}

/**
 * A parallel version of [Iterable.map], executing [block] on each element of [values].
 *
 * This function ensures maximum progress under errors. If any [block] execution throws an exception this function
 * still waits for all other [block] executions to finish and only after that it re-throws any one of the exceptions
 * that happened.
 */
@ExperimentalHCertApi
public suspend fun <T, R> parallelMap(values: Iterable<T>, block: suspend (value: T) -> R): List<R> =
    supervisorScope {
        // Run update jobs in parallel, but wait for all of them to finish and only rethrow an exception at the end.
        // This way we can guarantee maximum progress if some jobs succeed and others fail.
        val results = values.map {
            async {
                try {
                    Result.success(block(it))
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    Result.failure(e)
                }
            }
        }.awaitAll()
        results.map { it.getOrThrow() }
    }
