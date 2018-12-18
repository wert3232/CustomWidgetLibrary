package com.common

fun noGetter(): Nothing = throw RuntimeException("Property does not have a getter")
const val NO_GETTER = "Property does not have a getter"