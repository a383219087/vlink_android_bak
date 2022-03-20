package com.yjkj.chainup.new_version.activity.like

import android.graphics.Canvas
import android.graphics.Color
import android.view.MotionEvent
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import androidx.core.view.MotionEventCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.loopeer.itemtouchhelperextension.ItemTouchHelperExtension
import com.yjkj.chainup.R
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.util.ColorUtil
import com.yjkj.chainup.util.getArraysSymbols
import com.yjkj.chainup.util.getSymbols
import org.jetbrains.anko.backgroundDrawable
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

/**
 * @Author: Bertking
 * @Date：2018/12/10-2:55 PM
 * @Description:
 */

class MarketEditAdapter(data: ArrayList<JSONObject>, var itemTouchHelperExtension: ItemTouchHelperExtension? = null) : BaseQuickAdapter<JSONObject, BaseViewHolder>(R.layout.item_market_edit, data) {
    var editDragListener: EditDragListener? = null
    var selectMap = hashMapOf<String, Boolean>()
    override fun convert(helper: BaseViewHolder, item: JSONObject) {
        var name = NCoinManager.showAnoterName(item)
        val split = name.split("/")
        helper.setText(R.id.tv_coin_name, split[0])
        helper.setText(R.id.tv_market_name, "/" + split[1])
        val checkBox = helper.getView<CheckBox>(R.id.check_select)
        checkBox.isChecked = selectMap.containsKey(item?.getString("symbol"))
        helper.getView<ImageView>(R.id.iv_topping).onClick {
            val itemIndex = helper.adapterPosition
            if (itemIndex == 0) {
                return@onClick
            }
            editDragListener?.apply {
                val topIndex = 0
                Collections.swap(data, itemIndex, topIndex)
                notifyItemMoved(itemIndex, topIndex)
                onDragListener()
            }
        }
        helper.getView<ImageView>(R.id.tv_drag).setOnTouchListener { v, event ->
            if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                itemTouchHelperExtension?.startDrag(helper)
            }
            return@setOnTouchListener true
        }
    }

    fun getSelectSymbol(): String {
        return selectMap.getSymbols()
    }

    fun isSelectSymbol(): Boolean {
        return selectMap.size > 0
    }

    fun isSelectAllSymbol(): Boolean {
        return selectMap.size == data.size && selectMap.size != 0
    }

    fun selectAllCoins() {
        val isAll = isSelectAllSymbol()
        data.forEach {
            val key = it.optString("symbol")
            if (isAll) {
                selectMap.remove(key)
            } else {
                selectMap.put(key, true)
            }
        }
        this.notifyDataSetChanged()
    }

    fun selectCurrent(position: Int) {
        val key = getItem(position)?.optString("symbol")
        if (selectMap.containsKey(key)) {
            selectMap.remove(key)
        } else {
            selectMap.put(key!!, true)
        }
        this.notifyItemChanged(position)
    }

    /**
     * 获取当前选中 反集
     */
    fun getSelectSymbolsInvert(): String {
        if (isSelectAllSymbol()) {
            return ""
        }
        val allCoins = (data as ArrayList).getSymbols()
        val selectCoins = getSelectSymbol()
        val updateCoins = allCoins.split(",").subtract(selectCoins.split(","))
        return updateCoins.getArraysSymbols()
    }

    /**
     *  删除(选中的item)后 生成新列表
     */
    fun getNewSymbolsInvert(): ArrayList<JSONObject> {
        val allCoins = data.filter {
            !selectMap.keys.contains(it.optString("symbol"))
        }
        return allCoins as ArrayList<JSONObject>
    }

    fun resetSelect() {
        selectMap.clear()
    }

}

interface EditDragListener {
    fun onDragListener()
}

class CoinsManageTouchHelperCallback constructor(var adapter: MarketEditAdapter) : ItemTouchHelperExtension.Callback() {

    override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int {
        return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.START or ItemTouchHelper.END)
    }

    override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
        //通知Adapter更新数据和视图
        Collections.swap(adapter.data, viewHolder!!.adapterPosition, target!!.adapterPosition)
        adapter.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
        //若返回false则表示不支持上下拖拽
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {

    }

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && viewHolder != null) {//这个方法是长按事件触发时回调，这里我们把选中的条目透明度或者背景色改变掉
            (viewHolder as? BaseViewHolder)?.setBackgroundColor(R.id.content, ColorUtil.getColorByMode(R.color.market_drop_bg_day))
        }
    }

    override fun clearView(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?) {
        super.clearView(recyclerView, viewHolder)//相应的我们需要在下面这个方法里恢复视图的状态，防止列表复用问题引起的状态不正确。
        (viewHolder as? BaseViewHolder)?.apply {
            getView<View>(R.id.content)?.apply {
                translationX = 0f
                backgroundDrawable = null
            }
            adapter.apply {
                editDragListener?.apply {
                    onDragListener()
                }
            }
        }
    }

    override fun onChildDraw(c: Canvas?, recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        if (dY != 0f && dX == 0f) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }

    }

}

