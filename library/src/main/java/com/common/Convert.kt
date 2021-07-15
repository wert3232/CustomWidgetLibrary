package com.common

fun Array<CharSequence>.toInt(): IntArray {
    return this.map {
        Integer.parseInt(it.toString())
    }.toIntArray()
}

fun Array<CharSequence>.toFloat(): FloatArray {
    return this.map {
        it.toString().toFloat()
    }.toFloatArray()
}

fun Array<String>.toFloat(): FloatArray {
    return this.map {
        it.toFloat()
    }.toFloatArray()
}