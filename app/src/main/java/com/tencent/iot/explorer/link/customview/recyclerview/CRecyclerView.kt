package com.tencent.iot.explorer.link.customview.recyclerview

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.customview.WrapContentLinearLayoutManager
import java.lang.Exception
import java.util.function.Predicate

class CRecyclerView : RecyclerView {

    companion object {
        const val HEAD_VIEW_TYPE = 3000
        const val FOOT_VIEW_TYPE = 6000
    }

    private var list: List<*>? = null
    private var headList = arrayListOf<HeadHolderEntity>()
    private var footList = arrayListOf<FootHolderEntity>()
    private var recyclerItemView: RecyclerItemView? = null
    private var enableScroll = true
    private var scroller: TopSmoothScroller? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        initialize(attrs)
    }

    fun setList(list: List<*>) {
        this.list = list
    }

    fun setList(list: List<*>, layoutManager: LayoutManager) {
        this.list = list
        this.layoutManager = layoutManager
    }

    /**
     * 判断全选
     */
    fun isSelectAll(): Boolean {
        (list as? SelectedArrayList)?.run {
            return isSelectAll()
        }
        return false
    }

    /**
     * 判断全不选
     */
    fun isNoSelect(): Boolean {
        (list as? SelectedArrayList)?.run {
            return this.isNoSelect()
        }
        return false
    }

    fun isSelected(position: Int): Boolean {
        (list as? SelectedArrayList)?.run {
            return this.isSelect(position)
        }
        return false
    }

    fun addSelect(position: Int) {
        (list as? SelectedArrayList)?.run {
            this.addSelect(position)
        }
    }

    fun removeSelect(position: Int) {
        (list as? SelectedArrayList)?.run {
            this.removeSelect(position)
        }
    }

    fun addSingleSelect(position: Int) {
        (list as? SelectedArrayList)?.run {
            this.addSingleSelect(position)
        }
    }

    fun invertSelect() {
        (list as? SelectedArrayList)?.run {
            this.invertSelect()
        }
    }

    fun allSelect() {
        (list as? SelectedArrayList)?.run {
            this.allSelect()
        }
    }

    fun removeAllSelect() {
        (list as? SelectedArrayList)?.run {
            this.removeAllSelect()
        }
    }

    fun getSelectList(listener: TraversalListener) {
        (list as? SelectedArrayList)?.run {
            this.traversal(listener)
        }
    }

    fun addHeader(headViewHolder: HeadViewHolder<*>) {
        if (containHeader(headViewHolder) == null)
            headList.add(HeadHolderEntity(HEAD_VIEW_TYPE + headList.size, headViewHolder))
    }

    fun removeHeader(headViewHolder: HeadViewHolder<*>) {
        containHeader(headViewHolder)?.run {
            headList.remove(this)
            this@CRecyclerView.removeView(this.viewHolder.itemView)
            notifyDataChanged()
        }
    }

    private fun containHeader(headViewHolder: HeadViewHolder<*>): HeadHolderEntity? {
        headList.forEach {
            if (headViewHolder == it.viewHolder) {
                return it
            }
        }
        return null
    }

    fun addFooter(footViewHolder: FootViewHolder<*>) {
        if (containFooter(footViewHolder) == null)
            footList.add(FootHolderEntity(FOOT_VIEW_TYPE + footList.size, footViewHolder))
    }

    fun removeFooter(footViewHolder: FootViewHolder<*>) {
        containFooter(footViewHolder)?.run {
            footList.remove(this)
            this@CRecyclerView.removeView(this.viewHolder.itemView)
            notifyDataChanged()
        }
    }

    private fun containFooter(footViewHolder: FootViewHolder<*>): FootHolderEntity? {
        footList.forEach {
            if (footViewHolder == it.viewHolder) {
                return it
            }
        }
        return null
    }

    fun setEnableScroll(isEnable: Boolean) {
        enableScroll = isEnable
    }

    fun addRecyclerItemView(recyclerItemView: RecyclerItemView) {
        this.recyclerItemView = recyclerItemView
        initAdapter()
    }

    fun notifyDataChanged() {
        adapter?.notifyDataSetChanged()
    }

    fun scrollPosition(position: Int) {
        scrollPosition(position, false)
    }

    /**
     *  @param untilHead true表示滑动范围不包含头部，false表示滑动范围包含头部
     */
    fun scrollPosition(position: Int, untilHead: Boolean) {
        if (layoutManager == null) return
        if (layoutManager is LinearLayoutManager) {
            if (scroller == null)
                scroller = TopSmoothScroller(context)
            if (untilHead && position - headList.size > 0) {
                scroller!!.targetPosition = position - headList.size
            } else {
                scroller!!.targetPosition = position
            }
            layoutManager!!.startSmoothScroll(scroller)
        }
    }

    private fun initialize(attrs: AttributeSet?) {
        val arr = context.obtainStyledAttributes(attrs, R.styleable.CRecyclerView)
        mMaxHeight = arr.getLayoutDimension(R.styleable.CRecyclerView_max_height, mMaxHeight)
        arr.recycle()
    }

    private var mMaxHeight = 0

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        var height = heightSpec
        if (mMaxHeight > 0) {
            height = MeasureSpec.makeMeasureSpec(mMaxHeight, MeasureSpec.AT_MOST)
        }
        super.onMeasure(widthSpec, height)
    }

    /**
     * 没有列表数据时必须调用，且必须在添加头部或底部之前调用
     */
    fun initAdapter() {
        if (layoutManager == null) {
            layoutManager = WrapContentLinearLayoutManager(context)
        }
        if (adapter != null) {
            notifyDataChanged()
            return
        }
        adapter = object : Adapter<CViewHolder<*>>() {

            override fun getItemViewType(position: Int): Int {
                if (position < headList.size) {//在headList中
                    return headList[position].viewType
                }
                if (list != null && recyclerItemView != null && position < list!!.size + headList.size) {//在list中,
                    return recyclerItemView!!.getViewType(position - headList.size)
                }
                if (position >= itemCount - footList.size && position < itemCount) {//在footList中
                    return footList[position - (itemCount - footList.size)].viewType
                }
                //缺少数据，不展示
                return -1
            }

            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): CViewHolder<*> {
                if (viewType >= HEAD_VIEW_TYPE && viewType < HEAD_VIEW_TYPE + headList.size) {
                    return headList[viewType - HEAD_VIEW_TYPE].viewHolder
                }
                if (viewType >= FOOT_VIEW_TYPE && viewType < FOOT_VIEW_TYPE + footList.size) {
                    return footList[viewType - FOOT_VIEW_TYPE].viewHolder
                }
                if (recyclerItemView == null) return getDefaultHolder()
                return recyclerItemView!!.getViewHolder(parent, viewType)
            }

            override fun getItemCount(): Int {
                if (list == null) return headList.size + footList.size
                return list!!.size + headList.size + footList.size
            }

            override fun onBindViewHolder(holder: CViewHolder<*>, position: Int) {
                if (headList.size > position) {//有头部
                    holder.size = headList.size
                    holder.show(position)
                    return
                }
                if (list != null && recyclerItemView != null && position < list!!.size + headList.size) {//列表有数据且是列表数据
                    holder.size = list!!.size
                    holder.recyclerItemView = recyclerItemView
                    holder.setEntity(list!!, position - headList.size)
                    holder.show(position - headList.size)
                    return
                }
                if (position >= itemCount - footList.size && position < itemCount) {
                    holder.size = footList.size
                    holder.show(position - itemCount - footList.size)
                    return
                }
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_MOVE) {
            if (!enableScroll) {
                return true
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun getDefaultHolder(): CViewHolder<String> {
        return object : CViewHolder<String>(View(context)) {
            override fun show(position: Int) {
            }
        }
    }

    internal class TopSmoothScroller : LinearSmoothScroller {

        constructor(context: Context) : super(context)

        override fun getHorizontalSnapPreference(): Int {
            return SNAP_TO_START
        }

        override fun getVerticalSnapPreference(): Int {
            return SNAP_TO_START
        }
    }

    internal class HeadHolderEntity(viewType: Int, headViewHolder: HeadViewHolder<*>) {
        var viewType = viewType
        var viewHolder = headViewHolder
    }

    internal class FootHolderEntity(viewType: Int, footViewHolder: FootViewHolder<*>) {
        var viewType = viewType
        var viewHolder = footViewHolder
    }

    abstract class FootViewHolder<T> : CViewHolder<T> {
        var footListener: FootListener? = null
        var data: T? = null

        constructor(itemView: View) : super(itemView)

        constructor(context: Context, parent: ViewGroup, resId: Int) : super(
            context,
            parent,
            resId
        )

        override fun show(position: Int) {
            show()
        }

        abstract fun show()
    }

    abstract class HeadViewHolder<T> : CViewHolder<T> {

        var headListener: HeadListener? = null
        var data: T? = null

        constructor(itemView: View) : super(itemView)

        constructor(context: Context, parent: ViewGroup, resId: Int) : super(
            context,
            parent,
            resId
        )

        override fun show(position: Int) {
            show()
        }

        abstract fun show()
    }

    abstract class CViewHolder<T> : ViewHolder {

        private var crv: CRecyclerView? = null

        constructor(itemView: View) : super(itemView)

        constructor(context: Context, parent: ViewGroup, @LayoutRes resId: Int) : this(
            LayoutInflater.from(
                context
            ).inflate(resId, parent, false)
        ) {
            crv = (parent as? CRecyclerView)
        }

        var entity: T? = null

        var recyclerItemView: RecyclerItemView? = null

        var size = 0

        abstract fun show(position: Int)

        fun getString(@StringRes resId: Int): String {
            return itemView.context.getString(resId)
        }

        fun getDrawable(@DrawableRes resId: Int): Drawable {
            return itemView.resources.getDrawable(resId)
        }

        fun setEntity(list: List<*>, position: Int) {
            try {
                entity = list[position] as T
            } catch (e: Exception) {
                entity = null
            }
        }

        fun addSelect(position: Int) {
            crv?.addSelect(position)
        }

        fun removeSelect(position: Int) {
            crv?.removeSelect(position)
        }

        fun isSelected(position: Int): Boolean {
            return crv?.isSelected(position) ?: false
        }

    }

    interface RecyclerItemView {

        fun getViewType(position: Int): Int

        fun getViewHolder(parent: ViewGroup, viewType: Int): CViewHolder<*>

        fun doAction(viewHolder: CViewHolder<*>, clickView: View, position: Int)
    }

    interface HeadListener {
        fun doAction(holder: HeadViewHolder<*>, clickView: View, position: Int)
    }

    interface FootListener {
        fun doAction(holder: FootViewHolder<*>, clickView: View, position: Int)
    }

}

interface TraversalListener {
    fun onTraversal(position: Int, isSelected: Boolean)
}

class SelectedArrayList<T> : ArrayList<T>() {

    private val selectList = arrayListOf<Boolean>()

    fun traversal(listener: TraversalListener) {
        selectList.forEachIndexed { index, b ->
            listener.onTraversal(index, b)
        }
    }

    fun isSelect(position: Int): Boolean {
        return selectList[position]
    }

    /**
     * 判断全选
     */
    fun isSelectAll(): Boolean {
        selectList.forEach {
            if (!it) {
                return false
            }
        }
        return true
    }

    /**
     * 判断全不选
     */
    fun isNoSelect(): Boolean {
        selectList.forEach {
            if (it) {
                return false
            }
        }
        return true
    }

    fun addSelect(position: Int) {
        if (position >= 0 && position < selectList.size)
            selectList[position] = true
    }

    fun removeSelect(position: Int) {
        if (position >= 0 && position < selectList.size)
            selectList[position] = false
    }

    fun addSingleSelect(position: Int) {
        for (i in 0 until selectList.size) {
            selectList[i] = (position == i)
        }
    }

    fun invertSelect() {
        for (i in 0 until selectList.size) {
            selectList[i] = !selectList[i]
        }
    }

    fun allSelect() {
        for (i in 0 until selectList.size) {
            selectList[i] = true
        }
    }

    fun removeAllSelect() {
        for (i in 0 until selectList.size) {
            selectList[i] = false
        }
    }

    override fun add(element: T): Boolean {
        selectList.add(false)
        return super.add(element)
    }

    override fun add(index: Int, element: T) {
        selectList.add(index, false)
        super.add(index, element)
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        for (i in 0 until elements.size) {
            selectList.add(index + i, false)
        }
        return super.addAll(index, elements)
    }

    override fun addAll(elements: Collection<T>): Boolean {
        for (i in 0 until elements.size) {
            selectList.add(false)
        }
        return super.addAll(elements)
    }

    override fun remove(element: T): Boolean {
        val index = super.indexOf(element)
        if (super.removeAt(index) == element) {
            selectList.removeAt(index)
            return true
        }
        return false
    }

    override fun removeAt(index: Int): T {
        selectList.removeAt(index)
        return super.removeAt(index)
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        when (elements.size) {
            0 -> return true
            1 -> {
                val e = elements.first()
                val index = super.indexOf(e)
                if (super.removeAt(index) == e) {
                    selectList.removeAt(index)
                    return true
                }
            }
            in 2..size -> {
                val start = super.indexOf(elements.first())
                val end = super.indexOf(elements.last())
                super.removeRange(start, end)
                for (i in end - 1 downTo start) {
                    selectList.removeAt(i)
                }
                return true
            }
        }
        return false
    }

    override fun clear() {
        selectList.clear()
        super.clear()
    }

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        for (i in toIndex - 1 downTo fromIndex) {
            selectList.removeAt(i)
        }
        super.removeRange(fromIndex, toIndex)
    }
}