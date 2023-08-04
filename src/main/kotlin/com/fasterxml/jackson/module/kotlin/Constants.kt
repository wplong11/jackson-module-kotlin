package com.fasterxml.jackson.module.kotlin

internal object Constants {
    /**
     * Identification class for synthetic constructor generated for default arguments and value classes.
     */
    val DEFAULT_CONSTRUCTOR_MARKER: Class<*> = try {
        Class.forName("kotlin.jvm.internal.DefaultConstructorMarker")
    } catch (ex: ClassNotFoundException) {
        throw IllegalStateException(
            "DefaultConstructorMarker not on classpath. Make sure the Kotlin stdlib is on the classpath."
        )
    }
}
