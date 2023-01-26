package ru.netology.nework.util

import android.os.Bundle
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object DoubleArg: ReadWriteProperty<Bundle, Double> {

    override fun setValue(thisRef: Bundle, property: KProperty<*>, value: Double) {
        thisRef.putDouble(property.name, value)
    }

    override fun getValue(thisRef: Bundle, property: KProperty<*>): Double =
        thisRef.getDouble(property.name)
}