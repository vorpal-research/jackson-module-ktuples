package ru.spbstu.ktuples.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import ru.spbstu.ktuples.*
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class EitherDeserializer<T: VariantBase>(val clazz: KClass<T>) : StdDeserializer<T>(clazz.java), ContextualDeserializer {
    override fun createContextual(ctxt: DeserializationContext, property: BeanProperty?): JsonDeserializer<*> {
        val type = ctxt.contextualType ?: property?.type ?: return this
        return when {
            // VariantN handling
            type.isFinal -> SubVariant(type)
            else -> SubEither(type)
        }
    }

    inner class SubVariant(val contextualType: JavaType) : JsonDeserializer<T>() {
        override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): T {
            val codec = parser.codec!!
            require(parser.currentToken == JsonToken.START_OBJECT)
            var result: Any? = null
            var token: JsonToken = parser.nextValue()
            while(token != JsonToken.END_OBJECT) {
                val field = parser.currentName
                when (field) {
                    "value" -> result = codec.readValue(parser, contextualType.containedType(0))
                }
                token = parser.nextValue()
            }
            return checkNotNull(clazz.primaryConstructor).call(result)
        }
    }

    inner class SubEither(val contextualType: JavaType) : JsonDeserializer<T>() {
        override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): T {
            val codec = parser.codec!!
            require(parser.currentToken == JsonToken.START_OBJECT)

            var index: Int? = null
            var value: JsonNode? = null

            var result: Any? = null

            var token: JsonToken = parser.nextValue()

            while(token != JsonToken.END_OBJECT) {
                val field = parser.currentName
                when (field) {
                    "index" -> {
                        require(parser.currentToken == JsonToken.VALUE_NUMBER_INT || parser.currentToken == JsonToken.VALUE_NUMBER_FLOAT)
                        index = parser.intValue
                        if (value != null) {
                            result = readClassContents(index, codec.treeAsTokens(value))
                        }
                    }
                    "value" -> {
                        when (index) {
                            null -> {
                                value = codec.readTree(parser)
                            }
                            else -> {
                                result = readClassContents(index, parser)
                            }
                        }
                    }
                }
                token = parser.nextValue()
            }

            result ?: throw IllegalArgumentException("Either with no index or value field")
            index ?: throw IllegalArgumentException("Either with no index or value field")
            @Suppress("UNCHECKED_CAST")
            return Variant(index, result) as T
        }

        private fun readClassContents(index: Int, parser: JsonParser): Any? {
            val codec = parser.codec
            return when(index) {
                in (0 until contextualType.containedTypeCount()) ->
                    codec.readValue(parser, contextualType.containedType(index)) as Any?
                else ->
                    throw IllegalArgumentException("Index out of bounds: $index for EitherOf2")
            }
        }
    }

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): T {
        throw IllegalStateException("EitherDeserializer cannot parse types without type information =(")
    }
}

class TupleDeserializer<T: Tuple>(clazz: KClass<T>, val tsize: Int) : StdDeserializer<T>(clazz.java), ContextualDeserializer {
    override fun createContextual(ctxt: DeserializationContext, property: BeanProperty?): JsonDeserializer<*> {
        val type = ctxt.contextualType ?: property?.type ?: return this
        return Sub(type)
    }

    inner class Sub(val contextualType: JavaType) : JsonDeserializer<T>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): T {
            check(contextualType.containedTypeCount() == tsize)

            val codec = p.codec

            require(p.currentToken == JsonToken.START_ARRAY)

            if(tsize == 0) {
                require(p.nextToken() == JsonToken.END_ARRAY)
                @Suppress("UNCHECKED_CAST")
                return Tuple0 as T
            }

            p.nextToken()

            val res = arrayOfNulls<Any?>(tsize)

            for(i in 0 until tsize) {
                res[i] = codec.readValue(p, contextualType.containedType(i))
            }
            require(p.nextToken() == JsonToken.END_ARRAY)

            @Suppress("UNCHECKED_CAST")
            return Tuple.ofArray(res) as T
        }
    }

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): T {
        throw IllegalStateException("TupleDeserializer cannot parse types without type information =(")
    }

}

@JvmName("addDeserializerForVariant")
private fun <T: VariantBase> SimpleModule.addDeserializer(clazz: KClass<T>): SimpleModule {
    return addDeserializer(clazz.java, EitherDeserializer(clazz))
}

@JvmName("addDeserializerForTuple")
private fun <T: Tuple> SimpleModule.addDeserializer(clazz: KClass<T>, size: Int): SimpleModule {
    return addDeserializer(clazz.java, TupleDeserializer(clazz, size))
}

internal fun registerDeserializers(module: SimpleModule): SimpleModule {
    KTuplesSummary.tupleClasses.forEachIndexed { i, it -> module.addDeserializer(it, i) }
    KTuplesSummary.variantClasses.forEach { module.addDeserializer(it) }
    KTuplesSummary.eitherClasses.forEach { module.addDeserializer(it) }

    return module
}
