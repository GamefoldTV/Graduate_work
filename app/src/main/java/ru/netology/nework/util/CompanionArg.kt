package ru.netology.nework.util

import android.os.Bundle
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class CompanionArg {

    companion object{
        var Bundle.textArg : String? by StringArg
        var Bundle.longArg : Long by LongArg
    }

    object StringArg : ReadWriteProperty<Bundle, String?> {
        override fun getValue(thisRef: Bundle, property: KProperty<*>): String? {
            return thisRef.getString(property.name)
        }

        override fun setValue(thisRef: Bundle, property: KProperty<*>, value: String?) {
            thisRef.putString(property.name, value)
        }
    }

    object LongArg : ReadWriteProperty<Bundle, Long> {
        override fun getValue(thisRef: Bundle, property: KProperty<*>): Long {
            return thisRef.getLong(property.name)
        }

        override fun setValue(thisRef: Bundle, property: KProperty<*>, value: Long) {
            thisRef.putLong(property.name, value)
        }
    }
}