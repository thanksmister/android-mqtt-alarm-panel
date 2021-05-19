/*
 * Copyright (c) 2018 ThanksMister LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.todddavies.components.progressbar

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.View
import com.thanksmister.iot.mqtt.alarmpanel.R

/**
 * An indicator of progress, similar to Android's ProgressBar.
 * Can be used in 'spin mode' or 'increment mode'
 *
 * @author Todd Davies
 *
 *
 * Licensed under the Creative Commons Attribution 3.0 license see:
 * http://creativecommons.org/licenses/by/3.0/
 */
class ProgressWheel(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    //Sizes (with defaults)
    private var layout_height = 0
    private var layout_width = 0
    private var fullRadius = 100
    var circleRadius = 80
    var barLength = 60
    var barWidth = 20
    var rimWidth = 20
    var textSize = 20

    private var contourSize = 0f

    //Padding (with defaults)
    private var paddingTop = 0
    private var paddingBottom = 0
    private var paddingLeft = 0
    private var paddingRight = 0

    //Colors (with defaults)
    var barColor = -0x56000000
    private var contourColor = -0x56000000
    var circleColor = 0x00000000
    var rimColor = -0x55222223
    var textColor = -0x1000000

    //Paints
    private val barPaint = Paint()
    private val circlePaint = Paint()
    private val rimPaint = Paint()
    private val textPaint = Paint()
    private val contourPaint = Paint()

    //Rectangles
    private var rectBounds = RectF()
    private var circleBounds = RectF()
    private var circleOuterContour = RectF()
    private var circleInnerContour = RectF()

    //Animation
    //The amount of pixels to move the bar by on each draw
    var spinSpeed = 2

    //The number of milliseconds to wait inbetween each draw
    var delayMillis = 0
    private val spinHandler: Handler = object : Handler() {
        /**
         * This is the code that will increment the progress variable
         * and so spin the wheel
         */
        override fun handleMessage(msg: Message) {
            invalidate()
            if (isSpinning) {
                progress += spinSpeed
                if (progress > 360) {
                    progress = 0
                }
                sendEmptyMessageDelayed(0, delayMillis.toLong())
            }
            //super.handleMessage(msg);
        }
    }
    var progress = 0
    var isSpinning = false

    //Other
    private var text: String? = ""
    private var splitText = arrayOf<String>()

    //----------------------------------
    //Setting up stuff
    //----------------------------------
    /*
     * When this is called, make the view square.
     * From: http://www.jayway.com/2012/12/12/creating-custom-android-views-part-4-measuring-and-how-to-force-a-view-to-be-square/
     * 
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // The first thing that happen is that we call the superclass 
        // implementation of onMeasure. The reason for that is that measuring 
        // can be quite a complex process and calling the super method is a 
        // convenient way to get most of this complexity handled.
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // We can’t use getWidth() or getHight() here. During the measuring 
        // pass the view has not gotten its final size yet (this happens first 
        // at the start of the layout pass) so we have to use getMeasuredWidth() 
        // and getMeasuredHeight().
        var size = 0
        val width = measuredWidth
        val height = measuredHeight
        val widthWithoutPadding = width - getPaddingLeft() - getPaddingRight()
        val heigthWithoutPadding = height - getPaddingTop() - getPaddingBottom()

        // Finally we have some simple logic that calculates the size of the view 
        // and calls setMeasuredDimension() to set that size.
        // Before we compare the width and height of the view, we remove the padding, 
        // and when we set the dimension we add it back again. Now the actual content 
        // of the view will be square, but, depending on the padding, the total dimensions 
        // of the view might not be.
        size = if (widthWithoutPadding > heigthWithoutPadding) {
            heigthWithoutPadding
        } else {
            widthWithoutPadding
        }

        // If you override onMeasure() you have to call setMeasuredDimension(). 
        // This is how you report back the measured size.  If you don’t call
        // setMeasuredDimension() the parent will throw an exception and your 
        // application will crash.        
        // We are calling the onMeasure() method of the superclass so we don’t 
        // actually need to call setMeasuredDimension() since that takes care 
        // of that. However, the purpose with overriding onMeasure() was to 
        // change the default behaviour and to do that we need to call 
        // setMeasuredDimension() with our own values.
        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(), size + getPaddingTop() + getPaddingBottom())
    }

    /**
     * Use onSizeChanged instead of onAttachedToWindow to get the dimensions of the view,
     * because this method is called after measuring the dimensions of MATCH_PARENT & WRAP_CONTENT.
     * Use this dimensions to setup the bounds and paints.
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Share the dimensions
        layout_width = w
        layout_height = h
        setupBounds()
        setupPaints()
        invalidate()
    }

    /**
     * Set the properties of the paints we're using to
     * draw the progress wheel
     */
    private fun setupPaints() {
        barPaint.color = barColor
        barPaint.isAntiAlias = true
        barPaint.style = Paint.Style.STROKE
        barPaint.strokeWidth = barWidth.toFloat()
        rimPaint.color = rimColor
        rimPaint.isAntiAlias = true
        rimPaint.style = Paint.Style.STROKE
        rimPaint.strokeWidth = rimWidth.toFloat()
        circlePaint.color = circleColor
        circlePaint.isAntiAlias = true
        circlePaint.style = Paint.Style.FILL
        textPaint.color = textColor
        textPaint.style = Paint.Style.FILL
        textPaint.isAntiAlias = true
        textPaint.textSize = textSize.toFloat()
        contourPaint.color = contourColor
        contourPaint.isAntiAlias = true
        contourPaint.style = Paint.Style.STROKE
        contourPaint.strokeWidth = contourSize
    }

    /**
     * Set the bounds of the component
     */
    private fun setupBounds() {
        // Width should equal to Height, find the min value to steup the circle
        val minValue = Math.min(layout_width, layout_height)

        // Calc the Offset if needed
        val xOffset = layout_width - minValue
        val yOffset = layout_height - minValue

        // Add the offset
        paddingTop = getPaddingTop() + yOffset / 2
        paddingBottom = getPaddingBottom() + yOffset / 2
        paddingLeft = getPaddingLeft() + xOffset / 2
        paddingRight = getPaddingRight() + xOffset / 2
        val width = width //this.getLayoutParams().width;
        val height = height //this.getLayoutParams().height;
        rectBounds = RectF(paddingLeft.toFloat(),
                paddingTop.toFloat(),
                (width - paddingRight).toFloat(),
                (height - paddingBottom).toFloat())
        circleBounds = RectF((paddingLeft + barWidth).toFloat(),
                (paddingTop + barWidth).toFloat(),
                (width - paddingRight - barWidth).toFloat(),
                (height - paddingBottom - barWidth).toFloat())
        circleInnerContour = RectF(circleBounds.left + rimWidth / 2.0f + contourSize / 2.0f, circleBounds.top + rimWidth / 2.0f + contourSize / 2.0f, circleBounds.right - rimWidth / 2.0f - contourSize / 2.0f, circleBounds.bottom - rimWidth / 2.0f - contourSize / 2.0f)
        circleOuterContour = RectF(circleBounds.left - rimWidth / 2.0f - contourSize / 2.0f, circleBounds.top - rimWidth / 2.0f - contourSize / 2.0f, circleBounds.right + rimWidth / 2.0f + contourSize / 2.0f, circleBounds.bottom + rimWidth / 2.0f + contourSize / 2.0f)
        fullRadius = (width - paddingRight - barWidth) / 2
        circleRadius = fullRadius - barWidth + 1
    }

    /**
     * Parse the attributes passed to the view from the XML
     *
     * @param a the attributes to parse
     */
    private fun parseAttributes(a: TypedArray) {
        barWidth = a.getDimension(R.styleable.ProgressWheel_barWidth,
                barWidth.toFloat()).toInt()
        rimWidth = a.getDimension(R.styleable.ProgressWheel_rimWidth,
                rimWidth.toFloat()).toInt()
        spinSpeed = a.getDimension(R.styleable.ProgressWheel_spinSpeed,
                spinSpeed.toFloat()).toInt()
        delayMillis = a.getInteger(R.styleable.ProgressWheel_delayMillis,
                delayMillis)
        if (delayMillis < 0) {
            delayMillis = 0
        }
        barColor = a.getColor(R.styleable.ProgressWheel_barColor, barColor)
        barLength = a.getDimension(R.styleable.ProgressWheel_barLength,
                barLength.toFloat()).toInt()
        textSize = a.getDimension(R.styleable.ProgressWheel_textSize,
                textSize.toFloat()).toInt()
        textColor = a.getColor(R.styleable.ProgressWheel_textColor,
                textColor)

        //if the text is empty , so ignore it
        if (a.hasValue(R.styleable.ProgressWheel_text)) {
            setText(a.getString(R.styleable.ProgressWheel_text))
        }
        rimColor = a.getColor(R.styleable.ProgressWheel_rimColor,
                rimColor)
        circleColor = a.getColor(R.styleable.ProgressWheel_circleColor,
                circleColor)
        contourColor = a.getColor(R.styleable.ProgressWheel_contourColor, contourColor)
        contourSize = a.getDimension(R.styleable.ProgressWheel_contourSize, contourSize)


        // Recycle
        a.recycle()
    }

    //----------------------------------
    //Animation stuff
    //----------------------------------
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //Draw the inner circle
        canvas.drawArc(circleBounds, 360f, 360f, false, circlePaint)
        //Draw the rim
        canvas.drawArc(circleBounds, 360f, 360f, false, rimPaint)
        canvas.drawArc(circleOuterContour, 360f, 360f, false, contourPaint)
        canvas.drawArc(circleInnerContour, 360f, 360f, false, contourPaint)
        //Draw the bar
        if (isSpinning) {
            canvas.drawArc(circleBounds, (progress - 90).toFloat(), barLength.toFloat(), false,
                    barPaint)
        } else {
            canvas.drawArc(circleBounds, -90f, progress.toFloat(), false, barPaint)
        }
        //Draw the text (attempts to center it horizontally and vertically)
        val textHeight = textPaint.descent() - textPaint.ascent()
        val verticalTextOffset = textHeight / 2 - textPaint.descent()
        for (s in splitText) {
            val horizontalTextOffset = textPaint.measureText(s) / 2
            canvas.drawText(s, this.width / 2 - horizontalTextOffset,
                    this.height / 2 + verticalTextOffset, textPaint)
        }
    }

    /**
     * Reset the count (in increment mode)
     */
    fun resetCount() {
        progress = 0
        setText("0%")
        invalidate()
    }

    /**
     * Turn off spin mode
     */
    fun stopSpinning() {
        isSpinning = false
        progress = 0
        spinHandler.removeMessages(0)
    }

    /**
     * Puts the view on spin mode
     */
    fun spin() {
        isSpinning = true
        spinHandler.sendEmptyMessage(0)
    }

    /**
     * Increment the progress by 1 (of 360)
     */
    fun incrementProgress() {
        isSpinning = false
        progress++
        if (progress > 360) progress = 0
        //        setText(Math.round(((float) progress / 360) * 100) + "%");
        spinHandler.sendEmptyMessage(0)
    }

    /**
     * Set the progress to a specific value
     */
    fun setWheelProgress(i: Int) {
        isSpinning = false
        progress = i
        spinHandler.sendEmptyMessage(0)
    }

    //----------------------------------
    //Getters + setters
    //----------------------------------
    /**
     * Set the text in the progress bar
     * Doesn't invalidate the view
     *
     * @param text the text to show ('\n' constitutes a new line)
     */
    fun setText(text: String?) {
        this.text = text
        splitText = this.text!!.split("\n".toRegex()).toTypedArray()
    }

    override fun getPaddingTop(): Int {
        return paddingTop
    }

    fun setPaddingTop(paddingTop: Int) {
        this.paddingTop = paddingTop
    }

    override fun getPaddingBottom(): Int {
        return paddingBottom
    }

    fun setPaddingBottom(paddingBottom: Int) {
        this.paddingBottom = paddingBottom
    }

    override fun getPaddingLeft(): Int {
        return paddingLeft
    }

    fun setPaddingLeft(paddingLeft: Int) {
        this.paddingLeft = paddingLeft
    }

    override fun getPaddingRight(): Int {
        return paddingRight
    }

    fun setPaddingRight(paddingRight: Int) {
        this.paddingRight = paddingRight
    }

    var rimShader: Shader?
        get() = rimPaint.shader
        set(shader) {
            rimPaint.shader = shader
        }

    /**
     * The constructor for the ProgressWheel
     *
     * @param context
     * @param attrs
     */
    init {
        parseAttributes(context.obtainStyledAttributes(attrs,
                R.styleable.ProgressWheel))
    }
}