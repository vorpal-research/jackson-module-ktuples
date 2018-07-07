package ru.spbstu.ktuples.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.testng.annotations.Test
import ru.spbstu.ktuples.Tuple
import ru.spbstu.ktuples.Tuple0
import ru.spbstu.ktuples.Variant0
import ru.spbstu.ktuples.Variant1
import kotlin.test.assertEquals

class SerializationTest {
    val om = ObjectMapper()
            .registerKotlinModule()
            .registerKTuplesModule()

    private fun Any?.toJsonString() = om.writeValueAsString(this)

    fun <T> assertJsonEquals(expected: String, actual: T) {
        val vExpected = om.readTree(expected)
        val vActual = om.readTree(om.writeValueAsString(actual))
        assertEquals(vExpected, vActual)
    }

    @Test
    fun testTupleSimple() {
        assertEquals("[]", Tuple0.toJsonString())
        assertEquals("[1]", Tuple(1).toJsonString())
        assertEquals("[2,\"Hello\"]", Tuple(2, "Hello").toJsonString())
    }

    @Test
    fun testTupleNested() {
        assertEquals("[[],[]]", Tuple(Tuple0, Tuple0).toJsonString())
        assertJsonEquals(
                // language=JSON
                """
                    {
                      "first":[2],
                      "second":[1,4]
                    }
                """,
                (Tuple(2) to Tuple(1, 4))
        )

        assertJsonEquals(
                // language=JSON
                """
                    [[], [1], [1,2]]
                """,
                listOf(Tuple(), Tuple(1), Tuple(1,2))
        )
    }

    @Test
    fun testTupleNullable() {
        assertJsonEquals(
                // language=JSON
                """
                    [2, "a"]
                """,
                Tuple(2, "a")
        )

        assertJsonEquals(
                // language=JSON
                """
                    [null, "a"]
                """,
                Tuple(null as Int?, "a")
        )
    }

    @Test
    fun testTupleObject() {
        assertJsonEquals(
                // language=JSON
                """
                    [{ "first": 2, "second": "hello" }, [ true, false ]]
                """,
                Tuple(2 to "hello", listOf(true, false))
        )

        assertJsonEquals(
                // language=JSON
                """
                    {
                      "foo": [1, 2],
                      "bar": [1, { "f": 2 }]
                    }
                """,
                object {
                    val foo = Tuple(1,2)
                    val bar = Tuple(1, object { val f = 2 })
                }
        )
    }

    @Test
    fun testVariantSimple() {
        assertJsonEquals("{ \"value\" : 2, \"index\": 0 }", Variant0(2))
        assertJsonEquals("{ \"value\" : 2, \"index\": 1 }", Variant1(2))

        assertJsonEquals(
                // language=JSON
                """
                    {
                    "index": 0,
                    "value": 2
                    }
                """,
                Variant0(2)
        )
        assertJsonEquals(
                // language=JSON
                """
                    {
                    "index": 1,
                    "value": "Hello"
                    }
                """,
                Variant1("Hello")
        )
    }
}
