package ru.spbstu.ktuples.jackson

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.testng.annotations.Test
import ru.spbstu.ktuples.EitherOf2
import ru.spbstu.ktuples.Tuple
import ru.spbstu.ktuples.Variant0
import ru.spbstu.ktuples.Variant1
import java.util.*
import kotlin.test.assertEquals

class DeserializationTest {

    val om = ObjectMapper()
            .registerKotlinModule()
            .registerKTuplesModule()

    private inline fun <reified T : Any> parse(s: String) = om.readValue<T>(s, object : TypeReference<T>() {})

    @Test
    fun testTupleSimple() {
        assertEquals(Tuple(), parse("[]"))
        assertEquals(Tuple(1), parse("[1]"))
        assertEquals(Tuple(3, "Hello", 4.0), parse("[3, \"Hello\", 4.0]"))
    }

    @Test
    fun testTupleNested() {
        assertEquals(Tuple(1) to 3, parse(
                // language=JSON
                """
            {
              "first": [1],
              "second": 3
            }
        """.trimIndent()))

        assertEquals(Tuple(1, "Ho", 4) to 3, parse(
                // language=JSON
                """
            {
              "first": [1, "Ho", 4],
              "second": 3
            }
        """.trimIndent()))

        assertEquals(listOf(Tuple(2, 3, 8)), parse(
                // language=JSON
                """
                    [[2,3,8]]
                """.trimIndent()
        ))
    }

    @Test
    fun testTupleNullable() {
        assertEquals(Tuple(2 ?: null, "a"), parse(
                // language=JSON
                """
                    [2, "a"]
                """.trimIndent()
        ))

        assertEquals(Tuple(null as Int?, "a"), parse(
                // language=JSON
                """
                    [null, "a"]
                """.trimIndent()
        ))
    }

    @Test
    fun testTupleObject() {
        assertEquals(Tuple(2 to "hello", listOf(true, false)), parse(
                // language=JSON
                """
                    [{ "first": 2, "second": "hello" }, [ true, false ]]
                """.trimIndent()
        ))
    }

    @Test
    fun testVariantSimple() {
        assertEquals(Variant0(2), parse("{ \"value\" : 2 }"))
        assertEquals(Variant1(2), parse("{ \"value\" : 2 }"))

        assertEquals(Variant0(2) ?: Variant1("Hello"), parse(
                // language=JSON
                """
                    {
                    "index": 0,
                    "value": 2
                    }
                """.trimIndent()
        ))

        assertEquals(Variant1("Hello") ?: Variant0(2), parse(
                // language=JSON
                """
                    {
                    "index": 1,
                    "value": "Hello"
                    }
                """.trimIndent()
        ))
    }
}
