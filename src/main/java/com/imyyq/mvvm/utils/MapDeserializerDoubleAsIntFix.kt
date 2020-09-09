package com.imyyq.mvvm.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.internal.LinkedTreeMap
import java.lang.reflect.Type
import java.util.*

/**
 * 解决Gson 转Map 产生 int->Double 问题
 */
class MapDeserializerDoubleAsIntFix :
    JsonDeserializer<Map<String, Any?>> {

    @Throws(JsonParseException::class)
    override fun deserialize(
        jsonElement: JsonElement,
        type: Type?,
        jsonDeserializationContext: JsonDeserializationContext?
    ): Map<String, Any?> {
        return read(jsonElement) as Map<String, Any?>
    }

    fun read(`in`: JsonElement): Any? {
        if (`in`.isJsonArray) {
            val list: MutableList<Any?> = ArrayList()
            val arr = `in`.asJsonArray
            for (anArr in arr) {
                list.add(read(anArr))
            }
            return list
        } else if (`in`.isJsonObject) {
            val map: MutableMap<String, Any?> =
                LinkedTreeMap()
            val obj = `in`.asJsonObject
            val entitySet =
                obj.entrySet()
            for ((key, value) in entitySet) {
                map[key] = read(value)
            }
            return map
        } else if (`in`.isJsonPrimitive) {
            val prim = `in`.asJsonPrimitive
            if (prim.isBoolean) {
                return prim.asBoolean
            } else if (prim.isString) {
                return prim.asString
            } else if (prim.isNumber) {
                val num = prim.asNumber
                // here you can handle double int/long values
                // and return any type you want
                // this solution will transform 3.0 float to long values
                return if (Math.ceil(num.toDouble()) == num.toLong()
                        .toDouble()
                ) num.toLong() else {
                    num.toDouble()
                }
            }
        }
        return null
    }

}