package com.hzh.eq_bezier

import android.graphics.Path
import android.util.Log

data class Point(var x: Int,var y: Float)

data class EqObj(var x: Int,var y: Float = 0f,var q: Float = 1f,val width: Int,val maxY: Float ){
    var startIndex: Int = 0
        private set;
    var endIndex: Int = 1
        private set;
    var points = ArrayList<Point>().apply {
        for(i in 0 until width){
            add(Point(i, 0f));
        }
    }

    init {
        createEqPoint()
    }
    fun setValue(x: Int = this.x, y: Float = this.y, q: Float = this.q){
        this.x = x
        this.y = y
        this.q = q
        startIndex = 0
        endIndex = 1
        points.forEach {
            it.y = 0f
        }
        createEqPoint()
    }
    //创建路径 1,先等到控制点
    private fun createEqPoint(){
        if(width <= 10) return
        val zeroLineY = 0f
        var dx = x
        var dy = y
        val y2Max = maxY
        /*val d = (q * y2Max * 1f).toInt()*/
        var coe = if(q <= 0){
            0.04f * 10
        }else{
            q * 10
        }
        val d = (width.toFloat() / coe).toInt()
        val p0 = Point(dx, dy)
        val p1 = Point((dx - d), dy)
        val p2 = Point((dx - d), zeroLineY)
        val p3 = Point((dx - 2 * d), zeroLineY)
        val p11 = Point((dx + d), dy)
        val p22 = Point((dx + d), zeroLineY)
        val p33 = Point((dx + 2 * d), zeroLineY)

        if(p3.x <= 0){
            startIndex = 0
        }else{
            startIndex = p3.x
        }

        if(p3.x >= width){
            endIndex = width - 1
        }else{
            endIndex = p33.x
        }

        // -> 2
        var bezierPoints = computeBezier(p3,p2,p1,p0).apply {
            addAll(computeBezier(p0,p11,p22,p33))
        }
        // -> 3
        for(i in 0 until bezierPoints.size - 1){
            putPoint(points,bezierPoints[i],bezierPoints[i + 1])
        }
    }

    //2.计算贝塞尔曲线
    private fun computeBezier(p0: Point, p1: Point, p2: Point, p3: Point) : ArrayList<Point>{
        val countNum = 20
        var points = arrayListOf(p0,p1,p2,p3) //控制点
        var rPoints = ArrayList<Point>() //途径点集合
        var n = points.size
        var xs = arrayOfNulls<Float>(n - 1)
        var ys = arrayOfNulls<Float>(n - 1)
        for(t in 0..20){
            var vt = t.toFloat() / 20
            for(i in 1 until n){
                for(j in 0 until n - i){
                    if(i == 1){//xarray[j] = arrayCoordinate.GetAt(j).x * (1 - t) + arrayCoordinate[j + 1].x * t;
                        xs[j] = points[j].x * (1 - vt) + points[j + 1].x * vt
                        ys[j] = points[j].y * (1 - vt) + points[j + 1].y * vt
                    }else{
                        xs[j] = xs[j]!! * (1 - vt) + xs[j + 1]!! * vt
                        ys[j] = ys[j]!! * (1 - vt) + ys[j + 1]!! * vt
                    }
                }
            }
            var x = xs[0]!!.toInt()
            var y = ys[0]!!
            rPoints.add(Point(x,y))
        }
        return rPoints
    }

    //3 计算更多的点坐标
    private fun putPoint(points : ArrayList<Point>, p1: Point, p2: Point){
        var x1 = 0
        var y1 = 0f
        var x2 = 0
        var y2 = 0f

        if(p1.x > p2.x){
            x2 = p1.x
            y2 = p1.y

            x1 = p2.x
            y1 = p2.y
        }else{
            x1 = p1.x
            y1 = p1.y

            x2 = p2.x
            y2 = p2.y
        }

        if(y1 == y2 && x1 != x2){
            for(x in x1 .. x2){
                points.apply {
                    if(x >= 0 && x < this.size){
                        this[x].y =  this[x].y + y1
                    }
                }
            }
        }else if(x1 == x2){

        }else{
            //两点直线方程求Y

            for(x in x1 .. x2){
                val xf = x.toFloat()
                val x1f = x1.toFloat()
                val x2f = x2.toFloat()
                val y = (xf - x1f) / (x2f - x1f) * (y2 - y1) + y1
                points.apply {
                    val dy = y
                    if(x < points.size && x >= 0){
                        if(dy > maxY){
                            this[x].y = maxY.toFloat()
                        }else{
                            this[x].y  = dy
                        }
                    }
                }
            }
        }
    }
    fun getPath() : Path{
        return Path().apply {
            moveTo(points[startIndex].x.toFloat(), points[startIndex].y)
            for(i in startIndex + 1 .. endIndex){
                lineTo(points[i].x.toFloat(), - points[i].y)
            }
        }
    }
    companion object {
        fun buildAndPath(arr : Array<EqObj>, width: Int, maxY: Float) : Path {
            fun getY(y: Float) : Float{
                if(y > maxY){
                    return maxY
                }else return y
            }
            var points = ArrayList<Point>().apply {
                for(i in 0 until width){
                    add(Point(i, 0f));
                }
            }

            arr.forEach {mEqObj ->
                for(i in mEqObj.startIndex .. mEqObj.endIndex){
                    points[i].y = points[i].y + mEqObj.points[i].y
                }
            }
            return Path().apply {
                moveTo(points[0].x.toFloat(), getY(- points[0].y))
                for(i in 1 until  points.size){
                    lineTo(points[i].x.toFloat(), - points[i].y)
                }
            }
        }

        fun buildAndPath(arr : ArrayList<EqObj>, width: Int, maxY: Float) : Path {
//            Log.e(javaClass.simpleName,"width:${width}")
            fun getY(y: Float) : Float{
                if(y > maxY){
                    return maxY
                }else if(y < -maxY){
                    return -maxY
                }
                else return y
            }
            var points = ArrayList<Point>().apply {
                for(i in 0 until width){
                    add(Point(i, 0f));
                }
            }

            arr.forEach {mEqObj ->
                for(i in mEqObj.startIndex .. mEqObj.endIndex){
                    if(points.size != mEqObj.points.size){
                    }
                    if(i < points.size && i >= 0 && i < mEqObj.points.size){
                        points[i].y = points[i].y + mEqObj.points[i].y
                    }
                }
            }
            return Path().apply {
                moveTo(points[0].x.toFloat(), getY(- points[0].y))
                for(i in 1 until  points.size){
                    lineTo(points[i].x.toFloat(), getY(- points[i].y))
                }
            }
        }
    }
}