package com.ibm.health.common.vaccination.app.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Formats a local date to e.g. "12.03.1989".
 */
// TODO support locale-dependent patterns automatically
public fun LocalDate.getFormattedDate(): String {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    return format(formatter)
}
