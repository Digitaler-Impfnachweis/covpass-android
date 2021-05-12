package com.ibm.health.vaccination.sdk.android.utils

import assertk.assertThat
import assertk.assertions.*
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

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
        assertThat(deferred.isCompleted).isFalse()
        advanceTimeBy(1000 + 2000 + 3900)
        assertThat(deferred.isCompleted).isFalse()
        advanceTimeBy(200)
        assertThat(deferred.isCompleted).isTrue()
        assertThat(deferred.await()).isEqualTo(count)
    }

    @Test
    fun `retry throws last exception`() = runBlockingTest {
        assertThat {
            retry(2) {
                throw IllegalStateException("")
            }
        }.isFailure().isInstanceOf(IllegalStateException::class)
    }

    @Test
    fun `parallel mapping`() = runBlockingTest {
        val deferred = async {
            parallelMap(listOf(1, 2, 3, 4, 5)) {
                delay(it * 1000L)
                it + 10
            }
        }
        advanceTimeBy(4900)
        assertThat(deferred.isCompleted).isFalse()
        advanceTimeBy(200)
        assertThat(deferred.isCompleted).isTrue()
        assertThat(deferred.await()).isEqualTo(listOf(11, 12, 13, 14, 15))
    }
}
