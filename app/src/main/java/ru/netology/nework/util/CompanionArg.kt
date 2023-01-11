package ru.netology.nework.util

import android.os.Bundle

class CompanionArg {

    companion object{
        var Bundle.textArg: String? by StringArg
        var Bundle.longArg: Long by LongArg
        var Bundle.doubleArg1: Double by DoubleArg
        var Bundle.doubleArg2: Double by DoubleArg
    }
}