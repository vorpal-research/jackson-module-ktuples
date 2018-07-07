package ru.spbstu.ktuples.jackson

import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule

fun KTuplesModule(): Module = registerSerializers(registerDeserializers(SimpleModule()))
fun ObjectMapper.registerKTuplesModule() = registerModule(KTuplesModule())
