/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.truetime

import com.instacart.library.truetime.TrueTime
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Duration
import java.time.Instant
import kotlin.math.absoluteValue

public class TimeValidationRepository {

    public val state: MutableStateFlow<TimeValidationState> = MutableStateFlow(TimeValidationState.NotInitialized)

    public fun validate() {
        if (TrueTime.isInitialized()) {
            val trueTime = TrueTime.now().toInstant()
            val phoneTime = Instant.now()
            val difference = Duration.between(trueTime, phoneTime)
            if (difference.toHours().absoluteValue > MAX_TIME_OFFSET_HOURS) {
                state.value = TimeValidationState.Failed(trueTime)
            } else {
                state.value = TimeValidationState.Success
            }
        } else {
            state.value = TimeValidationState.NotInitialized
        }
    }

    private companion object {
        const val MAX_TIME_OFFSET_HOURS: Double = 2.0
    }
}

public sealed interface TimeValidationState {
    public object NotInitialized : TimeValidationState
    public class Failed(public val realTime: Instant) : TimeValidationState
    public object Success : TimeValidationState
}
