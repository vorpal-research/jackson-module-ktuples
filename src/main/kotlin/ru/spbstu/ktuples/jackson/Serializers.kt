package ru.spbstu.ktuples.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import ru.spbstu.ktuples.KTuplesSummary
import ru.spbstu.ktuples.Tuple
import ru.spbstu.ktuples.VariantBase
import kotlin.reflect.KClass

class EitherSerializer<T: VariantBase>(clazz: KClass<T>) : StdSerializer<T>(clazz.java) {
    override fun serialize(value: T, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeFieldName(INDEX_FIELD)
        gen.writeObject(value.index)
        gen.writeFieldName(VALUE_FIELD)
        gen.writeObject(value.value)
        gen.writeEndObject()
    }
}

class TupleSerializer<T: Tuple>(clazz: KClass<T>) : StdSerializer<T>(clazz.java) {
    override fun serialize(value: T, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeObject(value.toArray())
    }
}

@JvmName("addSerializerForVariant")
private fun <T: VariantBase> SimpleModule.addSerializer(clazz: KClass<T>): SimpleModule {
    return addSerializer(clazz.java, EitherSerializer(clazz))
}

@JvmName("addSerializerForTuple")
private fun <T: Tuple> SimpleModule.addSerializer(clazz: KClass<T>): SimpleModule {
    return addSerializer(clazz.java, TupleSerializer(clazz))
}

internal fun registerSerializers(module: SimpleModule): SimpleModule {
    KTuplesSummary.tupleClasses.forEach { module.addSerializer(it) }
    KTuplesSummary.variantClasses.forEach { module.addSerializer(it) }
    KTuplesSummary.eitherClasses.forEach { module.addSerializer(it) }

    return module
}
