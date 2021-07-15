@file:Suppress("EXTENSION_SHADOWED_BY_MEMBER")

package com.yfz.customwidgetlibrary

import org.junit.Test

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
class MyTest{
    @Test
    fun box1(){
        val a: Animal = Dog()
        val d = Dog()
        a.bark()
        d.bark()
    }
    fun box2(){
        var d1 = Dog()
        val d2 = Dog()
        d1-=d2
    }
    operator fun Dog.minus(d: Dog): Dog{
        return d
    }
    fun Animal.bark(){
        println("咪")
    }
    fun Dog.bark(){
        println("喵")
    }

}

interface Animal{
//    fun bark(){
//        println("萨卡啦卡拉")
//    }
}
class Dog : Animal{
    fun bark(){
        println("汪")
    }
}
fun Animal.bark(){
    println("咕")
}
fun Dog.bark(){
    println("呜")
}

fun method(){
    val m1 = mutableListOf<Int>()
    val m2 = mutableListOf<Int>()
    var m3: List<Int>? = null
    m3 = m1 - m2
    m1-=m2
}