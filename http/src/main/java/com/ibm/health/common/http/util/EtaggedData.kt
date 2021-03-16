package com.ibm.health.common.http.util

public data class EtaggedData<T>(val data: T, val etag: String)
