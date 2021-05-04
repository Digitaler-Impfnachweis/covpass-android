package com.ibm.health.common.vaccination.app.dependencies

import com.ibm.health.common.vaccination.app.errorhandling.CommonErrorHandler

/**
 * Global var for making the [CommonDependencies] accessible.
 */
public lateinit var commonDeps: CommonDependencies

/**
 * Access to various dependencies for common-app module.
 */
public abstract class CommonDependencies {

    /**
     * The [CommonErrorHandler].
     */
    public abstract val errorHandler: CommonErrorHandler
}
