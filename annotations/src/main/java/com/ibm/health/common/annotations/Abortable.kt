/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.annotations

/**
 * This can be used as a function return value, to indicate if a process should be aborted or continued after the
 * function. This is more meaningful than a simple boolean return value.
 */
public sealed class Abortable

/**
 * Return Abort to indicate that the process shall be aborted now.
 */
public object Abort : Abortable()

/**
 * Return Continue to indicate that the process shall be continued.
 */
public object Continue : Abortable()
