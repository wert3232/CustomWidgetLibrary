package zyf.custom.eqchart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import io.reactivex.FlowableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

data class EqData(var index: Int ,var gain: Int,var q: Float,var frequency: Int = -1)
open class EqBezierChart  @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr){
    val a = context.obtainStyledAttributes(attrs, R.styleable.eqBezierChartAttr)
    private var isBuild = false
    val eqPointNum = a.getInteger(R.styleable.eqBezierChartAttr_eqChartPointNum,15)
    var maxY = 100f
    var eqSpan = 127
    private val defaultQ = 4f
    private val unit = (1000).toFloat() / eqPointNum
    val frequencies = if(eqPointNum == 31) {
        resources.getIntArray(R.array.frequency_31)
    } else {
        resources.getIntArray(R.array.frequency_15)
    }
    val xPositions by lazy {
        frequencies.map {
            toPosition(it)
        }.toMutableList()
    }
    private val eqDatas = arrayListOf<EqData>().apply {
        for(index in 0 until eqPointNum){
            this.add(index,EqData(index,0,defaultQ, frequencies[index]))
        }
    }.toTypedArray()
    private lateinit var mEqBezierPath: Path
    private var e: FlowableEmitter<EqData>?= null
    private var frequencyChangeEmitter: FlowableEmitter<Int>?= null
    private lateinit var eqs: ArrayList<EqObj>
    private var xAxisLength  = 0
        set(value) {
            if(field == value){

            }
            else{
                field = value
                initBezierPath()
            }
        }
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        strokeWidth = 2f
        color = Color.BLUE
        style = Paint.Style.STROKE
    }
    private val mPaintA = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        strokeWidth = 2f
        color = Color.RED
        style = Paint.Style.STROKE

    }
    private val mPaintB = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        strokeWidth = 4f
        color = 0x5500FF00
        style = Paint.Style.FILL
    }
    private val mPaintC = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        strokeWidth = 2f
        color = 0x55000000
        style = Paint.Style.STROKE
    }
    private var canvas: Canvas?= null
    private var eqDataObservable: Disposable?= null
    private var eqFrequencyObservable: Disposable?= null
    init {
        eqDataObservable = Flowable.create(FlowableOnSubscribe<EqData>{
            emitter ->
            e = emitter
        }, BackpressureStrategy.LATEST).filter {
            canvas != null
        }.observeOn(Schedulers.computation())
                .map { data ->
                    if(data.index >= 0 && data.index < eqs.size){
                        val y = maxY * (data.gain.toFloat() / eqSpan)
                        eqs[data.index].setValue(y = y, q = data.q)
                        EqObj.buildAndPath(eqs, xAxisLength, maxY)
                    }else mEqBezierPath
                }.observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = {path ->
                            mEqBezierPath = path
                            invalidate()
                        },
                        onComplete = {},
                        onError = {
                            it.printStackTrace()
                        }
                )
        eqFrequencyObservable = Flowable
                .create(FlowableOnSubscribe<Int>{
                    emitter ->
                    frequencyChangeEmitter = emitter
                }, BackpressureStrategy.LATEST)
                .filter {
                    canvas != null
                }
                .observeOn(Schedulers.computation())
                .map { index ->
                    if(index in 0 until  eqs.size){
                        val x = measuredWidth * xPositions[index]
                        eqs[index].setValue(x = x.toInt())
                        EqObj.buildAndPath(eqs, xAxisLength, maxY)
                    }else{
                        mEqBezierPath
                    }
                }.observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = {path ->
                            mEqBezierPath = path
                            invalidate()
                        },
                        onError = {
                            it.printStackTrace()
                        }
                )
        mPaintA.color = a.getColor(R.styleable.eqBezierChartAttr_eqChartZeroLineColor, Color.RED)
        mPaint.color = a.getColor(R.styleable.eqBezierChartAttr_eqChartActionLineColor, Color.BLUE)
        a.recycle()
    }
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        this.canvas = canvas
        canvas?.apply {
            canvas.translate(0f,(measuredHeight / 2) .toFloat())
            val baseLine = Path().apply {
                moveTo(0f, 0f)
                lineTo(measuredWidth.toFloat(), 0f)
            }
            drawPath(baseLine, mPaint)
            drawPath(mEqBezierPath,mPaintA)
        }
    }
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initBezierPath()
    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        xAxisLength = measuredWidth
    }

    override fun onDetachedFromWindow() {
        eqDataObservable?.dispose()
        eqFrequencyObservable?.dispose()
        frequencyChangeEmitter = null
        e = null
        super.onDetachedFromWindow()
    }

    fun setEqValue(index: Int, gain: Int, q: Float){
        if (index > eqPointNum - 1 || !isBuild) {
            return
        }
        e?.onNext(EqData(index,gain,q,frequencies[index]))
    }
    fun setEqFrequency(index: Int, frequency: Int){
        if(index in 0 until eqPointNum) {
            frequencies[index] = frequency
            val position = toPosition(frequency)
            if (position in 0f..1f) {
                xPositions[index] = position
                frequencyChangeEmitter?.onNext(index)
                //Log.e("test","index: ${index} frequency: ${frequency}  position: ${position}")
            }
        }
    }
    private lateinit var b:Context
    fun reset(){
        ::b.isInitialized
        for(index in 0 until eqs.size){
            eqDatas[index].let {
                it.index = index
                it.gain = 0
                it.q = defaultQ
            }
            val x = measuredWidth * xPositions[index]
            eqs[index].setValue(x = x.toInt(),y = 0f,q = defaultQ)
            mEqBezierPath.set(EqObj.buildAndPath(eqs, measuredWidth, maxY))
        }
        invalidate()
    }
    private fun initBezierPath(){
        maxY = (measuredHeight / 2) .toFloat()
//        Log.e(javaClass.simpleName, "measuredWidth :${measuredWidth}    measuredHeight: ${measuredHeight}")
        eqs = initEqs(measuredWidth,measuredHeight)
        mEqBezierPath = EqObj.buildAndPath(eqs, measuredWidth, maxY)
        isBuild = true
    }
    fun setData(data: List<EqData>) = setData(data.toTypedArray())
    fun setData(data: Array<EqData>){

        data.forEach { d ->
            if(d.index in 0 until eqDatas.size){
                eqDatas[d.index] = d
                if(d.frequency != -1){
                    xPositions[d.index] = toPosition(d.frequency )
                }
            }
        }
        if(isBuild){
            initBezierPath()
            postInvalidate()
        }
    }
    private fun initEqs(width: Int, height: Int):  ArrayList<EqObj>{
        return ArrayList<EqObj>().apply {
            maxY = height.toFloat() / 2
            for(index in 0 until eqPointNum){
                val x = width * when{
                    eqDatas[index].frequency != -1 -> toPosition(eqDatas[index].frequency)
                    else -> xPositions[index]
                }
                val y = gain2Y(eqDatas[index].gain ,maxY.toInt())
                val q = eqDatas[index].q
//                Log.e(this@EqBezierChart.javaClass.simpleName,"${index} : Y[${y}] gain[${eqDatas[index]!!.gain}] q[${q}]")
                add(EqObj(x.toInt(), y, q, width, maxY))
            }
        }
    }
    private fun gain2Y(gain: Int, maxY: Int) : Float{
        return maxY * (gain.toFloat() / eqSpan)
    }
    private fun toPosition(frequency: Int) : Float{
        fun compute(min: Int,max: Int,frequency: Int, startFrequency :Int,divider :Int) : Float{
            val index = (frequency - startFrequency) / divider
            val d = (max - min).toFloat() * (index.toFloat() / 10)
            return (min + d) / 1011
        }
        val mark = arrayOf(0,60,99,134,162,185,203,220,236, // 20 - 100
                337,398,438,472,498,523,542,559,575, //200 - 1000
                677,735,776,807,837,861,879,895,911, // 2000 - 10000
                1009 // 10000 - 20000
        )
        return when(frequency){
            in 20 .. 30 ->{
                val min = mark[0]
                val max = mark[1]
                compute(min,max,frequency,20,1)
            }
            in 30 .. 40 ->{
                val min = mark[1]
                val max = mark[2]
                compute(min,max,frequency,30,1)
            }
            in 40 .. 50 ->{
                val min = mark[2]
                val max = mark[3]
                compute(min,max,frequency,40,1)
            }
            in 50 .. 60 ->{
                val min = mark[3]
                val max = mark[4]
                compute(min,max,frequency,50,1)
            }
            in 60 .. 70 ->{
                val min = mark[4]
                val max = mark[5]
                compute(min,max,frequency,60,1)
            }
            in 70 .. 80 ->{
                val min = mark[5]
                val max = mark[6]
                compute(min,max,frequency,70,1)
            }
            in 80 .. 90 ->{
                val min = mark[6]
                val max = mark[7]
                compute(min,max,frequency,80,1)
            }
            in 90 .. 100 ->{
                val min = mark[7]
                val max = mark[8]
                compute(min,max,frequency,90,1)
            }
            in 100 .. 200 ->{
                val min = mark[8]
                val max = mark[9]
                compute(min,max,frequency,100, 10)
            }
            in 200 .. 300->{
                val min = mark[9]
                val max = mark[10]
                compute(min,max,frequency,200, 10)
            }
            in 300 .. 400 ->{
                val min = mark[10]
                val max = mark[11]
                compute(min,max,frequency,300, 10)
            }
            in 400 .. 500 ->{
                val min = mark[11]
                val max = mark[12]
                compute(min,max,frequency,400, 10)
            }
            in 500 .. 600 ->{
                val min = mark[12]
                val max = mark[13]
                compute(min,max,frequency,500, 10)
            }
            in 600 .. 700 ->{
                val min = mark[13]
                val max = mark[14]
                compute(min,max,frequency,600, 10)
            }
            in 700 .. 800 ->{
                val min = mark[14]
                val max = mark[15]
                compute(min,max,frequency,700, 10)
            }
            in 800 .. 900 ->{
                val min = mark[15]
                val max = mark[16]
                compute(min,max,frequency,800, 10)
            }
            in 900 .. 1000 ->{
                val min = mark[16]
                val max = mark[17]
                compute(min,max,frequency,900, 10)
            }
            in 1000 .. 2000 ->{
                val min = mark[17]
                val max = mark[18]
                compute(min,max,frequency,1000, 100)
            }
            in 2000 .. 3000 ->{
                val min = mark[18]
                val max = mark[19]
                compute(min,max,frequency,2000, 100)
            }
            in 3000 .. 4000 ->{
                val min = mark[19]
                val max = mark[20]
                compute(min,max,frequency,3000, 100)
            }
            in 4000 .. 5000 ->{
                val min = mark[20]
                val max = mark[21]
                compute(min,max,frequency,4000, 100)
            }
            in 5000 .. 6000 ->{
                val min = mark[21]
                val max = mark[22]
                compute(min,max,frequency,5000, 100)
            }
            in 6000 .. 7000 ->{
                val min = mark[22]
                val max = mark[23]
                compute(min,max,frequency,6000, 100)
            }
            in 7000 .. 8000 ->{
                val min = mark[23]
                val max = mark[24]
                compute(min,max,frequency,7000, 100)
            }
            in 8000 .. 9000 ->{
                val min = mark[24]
                val max = mark[25]
                compute(min,max,frequency,8000, 100)
            }
            in 9000 .. 10000 ->{
                val min = mark[25]
                val max = mark[26]
                compute(min,max,frequency,9000, 100)
            }
            in 10000 .. 20000 ->{
                val min = mark[26]
                val max = mark[27]
                compute(min,max,frequency,10000,1000)
            }
            else ->{
                -1f
            }
        }
    }
}

data class EqObj(var x: Int,var y: Float = 0f,var q: Float = 1f,val width: Int,val maxY: Float ){
    private var startIndex: Int = 0
        private set;
    private var endIndex: Int = 1
        private set;
    private var points = ArrayList<Point>().apply {
        for(i in 0 until width){
            add(Point(i, 0f))
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
        val dx = x
        val dy = y
        val y2Max = maxY
        /*val d = (q * y2Max * 1f).toInt()*/
        val coe = if(q <= 0){
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
        val bezierPoints = computeBezier(p3,p2,p1,p0).apply {
            addAll(computeBezier(p0,p11,p22,p33))
        }
        // -> 3
        for(i in 0 until bezierPoints.size - 1){
            putPoint(points,bezierPoints[i],bezierPoints[i + 1])
        }
    }

    //2.计算贝塞尔曲线
    private fun computeBezier(p0: Point, p1: Point, p2: Point, p3: Point) : ArrayList<Point>{
        val points = arrayListOf(p0,p1,p2,p3) //控制点
        val rPoints = ArrayList<Point>() //途径点集合
        val n = points.size
        val xs = arrayOfNulls<Float>(n - 1)
        val ys = arrayOfNulls<Float>(n - 1)
        for(t in 0..20){
            val vt = t.toFloat() / 20
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
            val x = xs[0]!!.toInt()
            val y = ys[0]!!
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
            return buildAndPath(arr.toMutableList(), width, maxY)
        }

        fun buildAndPath(arr : List<EqObj>, width: Int, maxY: Float) : Path {
            //            Log.e(javaClass.simpleName,"width:${width}")
            fun getY(y: Float) : Float{
                if(y > maxY){
                    return maxY
                }else if(y < -maxY){
                    return -maxY
                }
                else return y
            }
            val points = arrayListOf<Point>().apply {
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
                points.clear()
            }
        }
    }
}

class Point(var x: Int,var y: Float)