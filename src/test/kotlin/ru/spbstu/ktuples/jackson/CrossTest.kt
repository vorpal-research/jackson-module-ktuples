package ru.spbstu.ktuples.jackson

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.testng.annotations.Test
import ru.spbstu.ktuples.Tuple
import kotlin.test.assertEquals

class CrossTest {
    val om = ObjectMapper()
            .registerKotlinModule()
            .registerKTuplesModule()

    inline fun <reified T> T.crossTest() =
            assertEquals(this, om.readValue(om.writeValueAsBytes(this), object : TypeReference<T>(){}))

    @Test
    fun testTupleSimple() {
        Tuple().crossTest()
        Tuple(1, 2).crossTest()
        Tuple("Hello", null as Int?, 3).crossTest()
        Tuple(Tuple(1, "J"), 3.14).crossTest()
    }


    @Test
    fun testTupleNested() {
        listOf(Tuple(), Tuple()).crossTest()
        Triple(Tuple(1, 2), Tuple("Hello", null as Int?, 3), 3).crossTest()
        Pair(Pair(Tuple(1, 2), 4), Tuple("Hello", null as Int?, 3)).crossTest()
    }
}
