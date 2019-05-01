package com.fone.android.util

import com.google.gson.*
import java.lang.reflect.Type

object GsonHelper {
    val customGson = GsonBuilder().registerTypeHierarchyAdapter(ByteArray::class.java, ByteArrayToBase64TypeAdapter()).create()

    private class ByteArrayToBase64TypeAdapter : JsonSerializer<ByteArray>, JsonDeserializer<ByteArray> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ByteArray {
            return Base64.decode(json.asString)
        }

        override fun serialize(src: ByteArray, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return JsonPrimitive(Base64.encodeBytes(src))
        }
    }
}