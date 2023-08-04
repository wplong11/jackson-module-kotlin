package com.fasterxml.jackson.module.kotlin

import java.lang.reflect.Constructor
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.kotlinFunction

val <T : Any> Constructor<T>.kotlinCtorFunction: KFunction<T>?
    get() {
        kotlinFunction?.apply { return this }

        // The javaConstructor that corresponds to the KFunction of the constructor that
        // takes value class as an argument is a synthetic constructor.
        // Therefore, in Kotlin 1.5.30, KFunction cannot be obtained from a constructor that is processed
        // by jackson-module-kotlin.
        // To deal with this situation, a synthetic constructor is obtained and a KFunction is obtained from it.
        return try {
            // The arguments of the synthetic constructor are the normal constructor arguments
            // with the DefaultConstructorMarker appended to the end.
            declaringClass
                .getDeclaredConstructor(*parameterTypes, Constants.DEFAULT_CONSTRUCTOR_MARKER)
                .kotlinFunction
        } catch (t: Throwable) {
            null
        }
    }
