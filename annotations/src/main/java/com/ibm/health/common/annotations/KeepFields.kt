/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.annotations

/**
 * Used to mark any types whose field are reflected over at runtime.
 *
 * Your class/interface needs to implement `KeepFields` in order to prevent obfuscation of field names.
 */
public interface KeepFields
