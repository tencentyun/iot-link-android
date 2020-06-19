package com.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Interpolator
import android.widget.Scroller
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import java.lang.reflect.AccessibleObject.setAccessible


class CustomViewPager : ViewPager {

    private var list = arrayListOf<View>()

    //是否可以左右滑动？true 可以，像Android原生ViewPager一样。
    // false 禁止ViewPager左右滑动。
    private var scrollable = false
    private val mScroller: ViewPagerScroller by lazy {
        ViewPagerScroller(context)
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    fun setScrollable(scrollable: Boolean) {
        this.scrollable = scrollable
    }

    fun addViewToList(layoutRes: Int) {
        if (layoutRes == 0) return
        addViewToList(LayoutInflater.from(context).inflate(layoutRes, this, false))
    }

    fun setScrollDuration(duration: Int) {
        mScroller.mScrollDuration = duration
        mScroller.initViewPagerScroll(this)
    }

    fun addViewToList(view: View) {
        list.add(view)
        if (adapter == null) {
            initAdapter()
        } else {
            adapter?.notifyDataSetChanged()
        }
    }

    private fun initAdapter() {
        adapter = object : PagerAdapter() {
            override fun isViewFromObject(view: View, any: Any): Boolean {
                return view == any
            }

            override fun getCount(): Int {
                return list.size
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                container.addView(list[position])
                return list[position]
            }

            override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
                container.removeView(list[position])
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return scrollable
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return scrollable
    }

    inner class ViewPagerScroller : Scroller {

        var mScrollDuration = 1000

        constructor(context: Context?) : super(context)
        constructor(context: Context?, interpolator: Interpolator?) : super(context, interpolator)
        constructor(context: Context?, interpolator: Interpolator?, flywheel: Boolean) : super(
            context,
            interpolator,
            flywheel
        )

        override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int) {
            super.startScroll(startX, startY, dx, dy, mScrollDuration)
        }

        override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
            super.startScroll(startX, startY, dx, dy, mScrollDuration)
        }

        fun initViewPagerScroll(viewPager: ViewPager) {
            try {
                val mScroller = ViewPager::class.java.getDeclaredField("mScroller")
                mScroller.isAccessible = true
                mScroller.set(viewPager, this)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

}