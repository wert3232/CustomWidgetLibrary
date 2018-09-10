package com.hzh.eq_bezier

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.library.R
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.lang.Deprecated


open class EqBezierChart  @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr){
    val a = context.obtainStyledAttributes(attrs, R.styleable.eqBezierChart)
    private var isBuild = false
    val eqPointNum = a.getInteger(R.styleable.eqBezierChart_eqPointNum,15)
    var maxY = 100f
    var eqSpan = 127
    private val defaultQ = 4f
    private val unit = (1000).toFloat() / eqPointNum
    val frequencies = if(eqPointNum == 31) {
        resources.getIntArray(R.array.frequency_31)
    } else {
        resources.getIntArray(R.array.frequency_15)
    }
    /*val xPositions = arrayOf(
            0.5 * unit / 1000f, //#1 25hz
            1.5 * unit / 1000f , //#2 40hz
            2.5 * unit / 1000f , //#3 63hz
            3.5 * unit / 1000f , //#4 100hz
            4.5 * unit / 1000f , //#5 160hz
            5.5 * unit / 1000f , //#6 250hz
            6.5 * unit / 1000f , //#7 400hz
            7.5 * unit / 1000f , //#8 630hz
            8.5 * unit / 1000f , //#9 1khz
            9.5 * unit / 1000f , //#10 1.6khz
            10.5 * unit / 1000f , //#11 2.5khz
            11.5 * unit / 1000f , //#12 4khz
            12.5 * unit / 1000f , //#13 6.3khz
            13.5 * unit / 1000f , //#14 10khz
            14.5 * unit / 1000f  //#15 16khz
    )*/
    val xPositions by lazy {
        frequencies.map {
            toPosition(it)
        }.toMutableList()
    }
    private var eqDatas = arrayOfNulls<EqData>(eqPointNum).apply {
        for(index in 0 until eqPointNum){
            this[index] = EqData(index,0,defaultQ)
        }
    }
    private lateinit var mEqBezierPath: Path
    private var e: ObservableEmitter<EqData> ?= null
    private var frequencyChangeEmitter: ObservableEmitter<Int> ?= null
    private lateinit var eqs: ArrayList<EqObj>
    private var xAxislength  = 0
            set(value) {
                if(field == value){

                }
                else{
                    field = value
                    initBezierPath()
                }
            }
    private val mPaint = Paint().apply {
        isAntiAlias = true
        strokeWidth = 2f
        color = Color.BLUE
        style = Paint.Style.STROKE
    }
    private val mPaintA = Paint().apply {
        isAntiAlias = true
        strokeWidth = 2f
        color = Color.RED
        style = Paint.Style.STROKE

    }
    private val mPaintB = Paint().apply {
        isAntiAlias = true
        strokeWidth = 4f
        color = 0x5500FF00.toInt()
        style = Paint.Style.FILL
    }
    private val mPaintC = Paint().apply {
        isAntiAlias = true
        strokeWidth = 2f
        color = 0x55000000.toInt()
        style = Paint.Style.STROKE
    }
    private var canvas: Canvas ?= null
    init {
        Observable.create(ObservableOnSubscribe<EqData>{
            emitter ->
            e = emitter
        }).filter {
            canvas != null
        }.observeOn(Schedulers.computation())
        .concatMap { data ->
            // Log.d(javaClass.simpleName,"index: ${tag.index}  value: ${tag.gain} q: ${tag.q}")
            if(data.index >= 0 && data.index < eqs.size){
                val y = maxY * (data.gain.toFloat() / eqSpan)
                eqs[data.index].setValue(y = y, q = data.q)
                val p = EqObj.buildAndPath(eqs, xAxislength, maxY)
                return@concatMap  Observable.just(p)
            }
            Observable.just(mEqBezierPath)
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
        Observable.create(ObservableOnSubscribe<Int>{
            emitter ->
            frequencyChangeEmitter = emitter
        }).filter {
            canvas != null
        }.observeOn(Schedulers.computation())
        .concatMap { index ->
            if(index in 0 until  eqs.size){
                val x = measuredWidth * xPositions[index]
                eqs[index].setValue(x = x.toInt())
                val p = EqObj.buildAndPath(eqs, xAxislength, maxY)
                return@concatMap  Observable.just(p)
            }
            Observable.just(mEqBezierPath)
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
        a.recycle()
    }
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        this.canvas = canvas
        /*var p1 = createEqPath(2 * maxY,maxY,1f)
        var p2 = createEqPath((2 * 1.5 * maxY).toFloat(),maxY,1f)
        var p3 = createEqPath((2 * 2.0 * maxY).toFloat(),maxY,1f)
        var p4 = createEqPath((2 * 2.5 * maxY).toFloat(), - maxY,1f)*/

        canvas?.apply {
            canvas.translate(0f,(measuredHeight / 2) .toFloat())
            var baseLine = Path().apply {
                moveTo(0f, 0f)
                lineTo(measuredWidth.toFloat(), 0f)
            }
            drawPath(baseLine, mPaint)
           /* var eq1 = EqObj(2 * maxY.toInt(),0f,1f, measuredWidth, maxY)
            var eq2 = EqObj((2 * 1.5 * maxY).toInt(),0f,1f, measuredWidth, maxY)
            var eq3 = EqObj((2 * 2.0 * maxY).toInt(), 0f,1f, measuredWidth, maxY)
            var eq4 = EqObj((2 * 2.5 * maxY).toInt(),0f,1f, measuredWidth, maxY)
            eq1.setValue(y = maxY)
            eq2.setValue(y = maxY / 2)
            eq3.setValue(y = maxY)
            eq4.setValue(y = - maxY)*/
            /*var eq1 = EqObj(2 * maxY.toInt(),maxY,1f, measuredWidth, maxY)
            var eq2 = EqObj((2 * 1.5 * maxY).toInt(),maxY / 2,1f, measuredWidth, maxY)
            var eq3 = EqObj((2 * 2.0 * maxY).toInt(), maxY,1f, measuredWidth, maxY)
            var eq4 = EqObj((2 * 2.5 * maxY).toInt(),- maxY,1f, measuredWidth, maxY)*/
            /*drawPath(eq1.getPath(),mPaintC)
            drawPath(eq2.getPath(),mPaintC)
            drawPath(eq3.getPath(),mPaintC)
            drawPath(eq4.getPath(),mPaintC)*/
//            val p = EqObj.buildAndPath(arrayOf(eq1,eq2,eq3,eq4), measuredWidth, maxY)
            mEqBezierPath?.let {
                drawPath(it,mPaintA)
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initBezierPath()
    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        xAxislength = measuredWidth
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)
    }

    @Deprecated
    private fun createEqPath(px: Float, py: Float, q: Float = defaultQ) : Path{
        val zeroLineY = (measuredHeight / 2) .toFloat()
        var x = px
        var y = zeroLineY + ( - py) //因为canvas往下为正轴,往上为负轴,所以让py反过来
        var coe = if(q <= 0){
            0.04f * 10
        }else{
            q * 10
        }
        val d = measuredWidth.toFloat() / coe
        val p0 = EqPoint(x,y)
        val p1 = EqPoint(x - d, y)
        val p2 = EqPoint(x - d, zeroLineY)
        val p3 = EqPoint(x - 2 * d, zeroLineY)
        val p11 = EqPoint(x + d, y)
        val p22 = EqPoint(x + d, zeroLineY)
        val p33 = EqPoint(x + 2 * d, zeroLineY)
        return Path().apply {
            moveTo(p3.x,p3.y)
            cubicTo(p2.x, p2.y, p1.x, p1.y, p0.x, p0.y)
//            lineTo(p0.x,p0.y)
            cubicTo(p11.x, p11.y, p22.x, p22.y, p33.x, p33.y)
        }
    }

    fun setEqValue(index: Int, gain: Int, q: Float){
        if (index > eqPointNum - 1) {
//            Log.e(javaClass.simpleName,"index > eqPointNum - 1")
            return
        }
        e?.onNext(EqData(index,gain,q))
    }
    fun setEqFrequency(index: Int, frequency: Int){
        if(index in 0 until eqPointNum) {
            val position = toPosition(frequency)
            if (position in 0f..1f) {
                xPositions[index] = position
                frequencyChangeEmitter?.onNext(index)
                //Log.e("test","index: ${index} frequency: ${frequency}  position: ${position}")
            }
        }
    }
    fun reset(){
        for(index in 0 until eqs.size){
            eqDatas[index]?.let {
                it.index = index
                it.gain = 0
                it.q = defaultQ
            }
            val x = measuredWidth * xPositions[index]
            eqs[index].setValue(x = x.toInt(),y = 0f,q = defaultQ)
            mEqBezierPath?.set(EqObj.buildAndPath(eqs, measuredWidth, maxY))
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
            }
        }
        if(isBuild){
            initBezierPath()
            invalidate()
        }
    }
    private fun initEqs(width: Int, height: Int):  ArrayList<EqObj>{
        return ArrayList<EqObj>().apply {
            maxY = height.toFloat() / 2
            for(index in 0 until eqPointNum){
                val x = width * xPositions[index]
                val y = gain2Y(eqDatas[index]!!.gain ,maxY.toInt())
                val q = eqDatas[index]!!.q
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
@Deprecated data class EqPoint(var x: Float,var y: Float)
data class EqData(var index: Int ,var gain: Int,var q: Float)