package com.ibm.health.common.http.utils

public data class EtaggedData<T>(val data: T, val etag: String)
