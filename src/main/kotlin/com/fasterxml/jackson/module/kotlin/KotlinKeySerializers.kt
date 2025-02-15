package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.annotation.JsonKey
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.Serializers
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.lang.reflect.Method
import java.lang.reflect.Modifier

internal object ValueClassUnboxKeySerializer : StdSerializer<Any>(Any::class.java) {
    override fun serialize(value: Any, gen: JsonGenerator, provider: SerializerProvider) {
        val method = value::class.java.getMethod("unbox-impl")
        val unboxed = method.invoke(value)

        if (unboxed == null) {
            val javaType = provider.typeFactory.constructType(method.genericReturnType)
            provider.findNullKeySerializer(javaType, null).serialize(null, gen, provider)
            return
        }

        provider.findKeySerializer(unboxed::class.java, null).serialize(unboxed, gen, provider)
    }
}

// Class must be UnboxableValueClass.
private fun Class<*>.getStaticJsonKeyGetter(): Method? = this.declaredMethods.find { method ->
    Modifier.isStatic(method.modifiers) && method.annotations.any { it is JsonKey && it.value }
}

internal class ValueClassStaticJsonKeySerializer<T>(
    t: Class<T>,
    private val staticJsonKeyGetter: Method
) : StdSerializer<T>(t) {
    private val keyType: Class<*> = staticJsonKeyGetter.returnType
    private val unboxMethod: Method = t.getMethod("unbox-impl")

    override fun serialize(value: T, gen: JsonGenerator, provider: SerializerProvider) {
        val unboxed = unboxMethod.invoke(value)
        val jsonKey: Any? = staticJsonKeyGetter.invoke(null, unboxed)

        val serializer = jsonKey
            ?.let { provider.findKeySerializer(keyType, null) }
            ?: provider.findNullKeySerializer(provider.constructType(keyType), null)

        serializer.serialize(jsonKey, gen, provider)
    }

    companion object {
        fun createOrNull(t: Class<*>): StdSerializer<*>? =
            t.getStaticJsonKeyGetter()?.let { ValueClassStaticJsonKeySerializer(t, it) }
    }
}

internal class KotlinKeySerializers : Serializers.Base() {
    override fun findSerializer(
        config: SerializationConfig,
        type: JavaType,
        beanDesc: BeanDescription
    ): JsonSerializer<*>? = when {
        type.rawClass.isUnboxableValueClass() -> ValueClassStaticJsonKeySerializer.createOrNull(type.rawClass)
            ?: ValueClassUnboxKeySerializer
        else -> null
    }
}
