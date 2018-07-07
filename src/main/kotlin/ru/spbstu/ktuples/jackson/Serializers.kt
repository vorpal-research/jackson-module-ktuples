package ru.spbstu.ktuples.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import ru.spbstu.ktuples.Tuple
import ru.spbstu.ktuples.VariantBase
import kotlin.reflect.KClass

class EitherSerializer<T: VariantBase>(clazz: KClass<T>) : StdSerializer<T>(clazz.java) {
    override fun serialize(value: T, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeFieldName("index")
        gen.writeObject(value.index)
        gen.writeFieldName("value")
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

// XXX: generate this?
fun registerSerializers(module: SimpleModule): SimpleModule {
    try {
        var i = 0
        while (true) {
            module.addSerializer(Class.forName("ru.spbstu.ktuples.Tuple$i").kotlin as KClass<out Tuple>)
            module.addSerializer(Class.forName("ru.spbstu.ktuples.Variant$i").kotlin as KClass<out VariantBase>)
            module.addSerializer(Class.forName("ru.spbstu.ktuples.EitherOf${i + 2}").kotlin as KClass<out VariantBase>)
            ++i
        }
    } catch (nf: ClassNotFoundException) {}

    return module
}
