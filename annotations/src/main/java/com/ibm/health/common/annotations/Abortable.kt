package com.ibm.health.common.annotations

public sealed class Abortable
public object Abort : Abortable()
public object Continue : Abortable()
