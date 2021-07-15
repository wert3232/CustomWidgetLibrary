package com.yfz.customwidgetlibrary

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)

    }
    @Test
    fun closureTest(){
        println(add(1)(4))
    }
}


fun add(x: Int):(Int) -> Int{
    //x=13
    return fun(y:Int):Int{
        //y=56
        return x+y
    }
    //最终69
}
