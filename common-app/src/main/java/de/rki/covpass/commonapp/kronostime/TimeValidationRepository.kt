/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.kronostime

import com.lyft.kronos.KronosClock
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Duration
import java.time.Instant
import kotlin.math.absoluteValue

public class TimeValidationRepository(private val kronosClock: KronosClock) {

    public val state: MutableStateFlow<TimeValidationState> = MutableStateFlow(TimeValidationState.Success)

    public fun validate() {
        val kronosTime = Instant.ofEpochMilli(kronosClock.getCurrentTimeMs())
        val phoneTime = Instant.now()
        val difference = Duration.between(kronosTime, phoneTime)
        if (difference.toMinutes().absoluteValue > MAX_TIME_OFFSET_MINUTES) {
            state.value = TimeValidationState.Failed(kronosTime)
        } else {
            state.value = TimeValidationState.Success
        }
    }

    private companion object {
        const val MAX_TIME_OFFSET_MINUTES: Int = 120
    }
}

public sealed interface TimeValidationState {
    public class Failed(public val realTime: Instant) : TimeValidationState
    public object Success : TimeValidationState
}
