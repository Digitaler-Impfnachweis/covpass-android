/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CoroutineUtilsTest {

    @Test
    fun `retry with exponential backoff`() = runTest {
        var count = 0
        val deferred = async {
            retry {
                count += 1
                if (count < 4) {
                    throw IllegalStateException("")
                }
                count
            }
        }
        assertFalse(deferred.isCompleted)
        testScheduler.apply { advanceTimeBy(1000 + 2000 + 3900); runCurrent() }
        assertFalse(deferred.isCompleted)
        testScheduler.apply { advanceTimeBy(200); runCurrent() }
        assertTrue(deferred.isCompleted)
        assertEquals(count, deferred.await())
    }

    @Test
    fun `retry throws last exception`() = runTest {
        assertFailsWith<IllegalStateException> {
            retry(2) {
                throw IllegalStateException("")
            }
        }
    }

    @Test
    fun `parallel mapping`() = runTest {
        val deferred = async {
            listOf(1, 2, 3, 4, 5).parallelMap {
                delay(it * 1000L)
                it + 10
            }
        }
        testScheduler.apply { advanceTimeBy(4900); runCurrent() }
        assertFalse(deferred.isCompleted)
        testScheduler.apply { advanceTimeBy(200); runCurrent() }
        assertTrue(deferred.isCompleted)
        assertEquals(listOf(11, 12, 13, 14, 15), deferred.await())
    }
}
