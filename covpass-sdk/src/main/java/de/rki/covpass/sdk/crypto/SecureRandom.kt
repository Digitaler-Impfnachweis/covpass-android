package de.rki.covpass.sdk.utils

import java.security.SecureRandom
import kotlin.random.Random
import kotlin.random.asKotlinRandom

public val secureRandom: Random by lazy { SecureRandom().asKotlinRandom() }
