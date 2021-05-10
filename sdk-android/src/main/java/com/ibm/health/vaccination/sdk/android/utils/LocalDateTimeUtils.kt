package com.ibm.health.common.vaccination.app.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Formats a local date to e.g. "12.03.1989, 14:52".
 */
// TODO support locale-dependent patterns automatically
public fun LocalDateTime.formatDateTime(): String {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm")
    return format(formatter)
}
