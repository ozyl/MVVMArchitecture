package com.imyyq.mvvm.utils

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.util.*


class CustomizedObjectTypeAdapter : TypeAdapter<Any?>() {
    private val delegate =
        Gson().getAdapter(Any::class.java)

    @Throws(IOException::class)
    override fun write(out: JsonWriter?, value: Any?) {
        delegate.write(out, value)
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader): Any? {
        val token: JsonToken = `in`.peek()
        return when (token) {
            JsonToken.BEGIN_ARRAY -> {
                val list: MutableList<Any?> = ArrayList()
                `in`.beginArray()
                while (`in`.hasNext()) {
                    list.add(read(`in`))
                }
                `in`.endArray()
                list
            }
            JsonToken.BEGIN_OBJECT -> {
                val map: MutableMap<String, Any?> =
                    LinkedTreeMap()
                `in`.beginObject()
                while (`in`.hasNext()) {
                    map[`in`.nextName()] = read(`in`)
                }
                `in`.endObject()
                map
            }
            JsonToken.STRING -> `in`.nextString()
            JsonToken.NUMBER -> {
                //return in.nextDouble();
                val n: String = `in`.nextString()
                if (n.indexOf('.') != -1) {
                    n.toDouble()
                } else n.toLong()
            }
            JsonToken.BOOLEAN -> `in`.nextBoolean()
            JsonToken.NULL -> {
                `in`.nextNull()
                null
            }
            else -> throw IllegalStateException()
        }
    }

}