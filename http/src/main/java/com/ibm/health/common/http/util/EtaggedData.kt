/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.http.util

public data class EtaggedData<T>(val data: T, val etag: String)
