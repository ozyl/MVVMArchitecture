import android.os.Parcelable
import com.google.gson.Gson
import com.imyyq.mvvm.utils.fromJson
import com.tencent.mmkv.MMKV
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

// Preferences.kt
/**
 * Created by TanJiaJun on 2020-01-11.
 */

val mmkv: MMKV by lazy { MMKV.defaultMMKV() }

private inline fun <T> MMKV.delegate(
    key: String? = null,
    defaultValue: T,
    crossinline getter: MMKV.(String, T) -> T,
    crossinline setter: MMKV.(String, T) -> Boolean
): ReadWriteProperty<Any, T> =
    object : ReadWriteProperty<Any, T> {
        override fun getValue(thisRef: Any, property: KProperty<*>): T =
            getter(key ?: property.name, defaultValue)

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
            setter(key ?: property.name, value)
        }
    }

fun MMKV.boolean(
    key: String? = null,
    defaultValue: Boolean = false
): ReadWriteProperty<Any, Boolean> =
    delegate(key, defaultValue, MMKV::decodeBool, MMKV::encode)

fun MMKV.int(key: String? = null, defaultValue: Int = 0): ReadWriteProperty<Any, Int> =
    delegate(key, defaultValue, MMKV::decodeInt, MMKV::encode)

fun MMKV.long(key: String? = null, defaultValue: Long = 0L): ReadWriteProperty<Any, Long> =
    delegate(key, defaultValue, MMKV::decodeLong, MMKV::encode)

fun MMKV.float(key: String? = null, defaultValue: Float = 0.0F): ReadWriteProperty<Any, Float> =
    delegate(key, defaultValue, MMKV::decodeFloat, MMKV::encode)

fun MMKV.double(key: String? = null, defaultValue: Double = 0.0): ReadWriteProperty<Any, Double> =
    delegate(key, defaultValue, MMKV::decodeDouble, MMKV::encode)

private inline fun <T> MMKV.nullableDefaultValueDelegate(
    key: String? = null,
    defaultValue: T?,
    crossinline getter: MMKV.(String, T?) -> T,
    crossinline setter: MMKV.(String, T) -> Boolean
): ReadWriteProperty<Any, T> =
    object : ReadWriteProperty<Any, T> {
        override fun getValue(thisRef: Any, property: KProperty<*>): T =
            getter(key ?: property.name, defaultValue)

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
            setter(key ?: property.name, value)
        }
    }

fun MMKV.string(key: String? = null, defaultValue: String? = null): ReadWriteProperty<Any, String?> =
    nullableDefaultValueDelegate(key, defaultValue, MMKV::decodeString, MMKV::encode)

inline fun <reified T> MMKV.any(key: String? = null, defaultValue: T? = null): ReadWriteProperty<Any, T?> {
    return object : ReadWriteProperty<Any, T?> {
        override fun getValue(thisRef: Any, property: KProperty<*>): T? {
           return defaultValue?.let {Gson().fromJson(decodeString(key ?: property.name, Gson().toJson(defaultValue)))}}

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
            encode(key ?: property.name,value?.let { Gson().toJson(value) })
        }

    }
}

fun MMKV.stringSet(
    key: String? = null,
    defaultValue: Set<String>? = null
): ReadWriteProperty<Any, Set<String>> =
    nullableDefaultValueDelegate(key, defaultValue, MMKV::decodeStringSet, MMKV::encode)

inline fun <reified T : Parcelable> MMKV.parcelable(
    key: String? = null,
    defaultValue: T? = null
): ReadWriteProperty<Any, T> =
    object : ReadWriteProperty<Any, T> {
        override fun getValue(thisRef: Any, property: KProperty<*>): T =
            decodeParcelable(key ?: property.name, T::class.java, defaultValue)

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
            encode(key ?: property.name, value)
        }
    }