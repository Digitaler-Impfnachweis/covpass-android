/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runBlockingTest
import kotlin.test.*

internal class CoroutineUtilsTest {

    @Test
    fun `retry with exponential backoff`() = runBlockingTest {
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
        advanceTimeBy(1000 + 2000 + 3900)
        assertFalse(deferred.isCompleted)
        advanceTimeBy(200)
        assertTrue(deferred.isCompleted)
        assertEquals(count, deferred.await())
    }

    @Test
    fun `retry throws last exception`() = runBlockingTest {
        assertFailsWith<IllegalStateException> {
            retry(2) {
                throw IllegalStateException("")
            }
        }
    }

    @Test
    fun `parallel mapping`() = runBlockingTest {
        val deferred = async {
            listOf(1, 2, 3, 4, 5).parallelMap {
                delay(it * 1000L)
                it + 10
            }
        }
        advanceTimeBy(4900)
        assertFalse(deferred.isCompleted)
        advanceTimeBy(200)
        assertTrue(deferred.isCompleted)
        assertEquals(listOf(11, 12, 13, 14, 15), deferred.await())
    }
}
