package com.yjkj.chainup.wedegit

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewManager
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.yjkj.chainup.R
import com.yjkj.chainup.util.ColorUtil
import org.jetbrains.anko.custom.ankoView

class ChainHeaderView : RelativeLayout {

    private var ctx: Context

    private var tvTitle: TextView
    private var tvRight: TextView
    private var btnBack: ImageView
    private var btnRight: ImageView

    private var defaultColor: Int
    private var defaultRightType: Int
    private var defaultRightRes: Int
    private var line: View

    constructor(ctx: Context) : this(ctx, null)

    constructor(ctx: Context, attrs: AttributeSet?) : super(ctx, attrs) {
        this.ctx = ctx
        id = R.id.chain_layout_header

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.chain_header)
        defaultColor = ContextCompat.getColor(ctx, R.color.bg_card_color)
        var bgColor = typedArray.getColor(R.styleable.chain_header_chain_header_color_bg, defaultColor)
        var hide = typedArray.getBoolean(R.styleable.chain_header_chain_header_hide_line, true)
        defaultRightType = typedArray.getInteger(R.styleable.chain_header_chain_header_right_type, 0)
        defaultRightRes = typedArray.getInt(R.styleable.chain_header_chain_header_right_type, 0)
        var hideTran = typedArray.getBoolean(R.styleable.chain_header_chain_header_hide_tran, false)
        typedArray.recycle()


        LayoutInflater.from(this.ctx).inflate(R.layout.layout_header_v2, this)
        layoutParams = MarginLayoutParams(MarginLayoutParams.MATCH_PARENT, ViewUtil.dpToPx(45f))

        tvTitle = findViewById(R.id.tv_header_title)
        btnBack = findViewById(R.id.btn_header_back)
        btnRight = findViewById(R.id.btn_header_right)
        tvRight = findViewById(R.id.tx_header_right)
        line = findViewById(R.id.head_bottom_line)
        if (hide) line.visibility = View.INVISIBLE

        if (defaultRightType == 0) {
            btnRight.visibility = View.INVISIBLE
            tvRight.visibility = View.INVISIBLE
        } else {
            if (defaultRightType == 1) {
                btnRight.visibility = View.VISIBLE
                tvRight.visibility = View.INVISIBLE
            } else if (defaultRightType == 2) {
                tvRight.visibility = View.VISIBLE
                btnRight.visibility = View.INVISIBLE
            }
        }
        setBackgroundColor(bgColor)
        if(hideTran){
            tvTitle.setTextColor(ColorUtil.getColor(R.color.white))
            tvRight.setTextColor(ColorUtil.getColor(R.color.white))
            btnBack.setImageResource(R.drawable.ic_return)
        }
    }

    fun setOnBackPressListener(listener: View.OnClickListener) {
        btnBack.setOnClickListener(listener)
    }

    fun setOnBackPressListener(listener: () -> Unit) {
        btnBack.setOnClickListener { listener() }
    }

    fun setOnRightPressListener(listener: () -> Unit) {
        btnRight.setOnClickListener { listener() }
        tvRight.setOnClickListener { listener() }
    }

    fun setOnBackPressListener(drawResId: Int, listener: View.OnClickListener) {
        btnBack.setImageResource(drawResId)
        btnBack.setOnClickListener(listener)
    }
    fun setBackImageResource(drawResId: Int) {
        btnBack.setImageResource(drawResId)
    }

    fun setLeftImg(res: Int) {
        this.btnBack.setImageResource(res)
        this.btnBack.visibility = View.VISIBLE
    }

    fun setTitle(title: String) {
        this.tvTitle.text = title
    }

    fun setRightString(title: String) {
        this.tvRight.text = title
        this.tvRight.visibility = View.VISIBLE
        this.btnRight.visibility = View.INVISIBLE
    }

    fun setRightHide() {
        this.tvRight.visibility = View.INVISIBLE
        this.btnRight.visibility = View.INVISIBLE
    }

    fun setRightImg(res: Int) {
        this.btnRight.setImageResource(res)
        this.tvRight.visibility = View.INVISIBLE
        this.btnRight.visibility = View.VISIBLE
    }

    override fun setBackgroundColor(color: Int) {
        super.setBackgroundColor(color)
        btnBack.setImageResource(R.drawable.ic_return)
        tvTitle.setTextColor(ContextCompat.getColor(this.ctx, R.color.base_text_grey_0))
    }

    fun setTitle(title: Int) {
        this.tvTitle.setText(title)
    }
}

inline fun ViewManager.chainHeaderView(theme: Int = 0, init: ChainHeaderView.() -> Unit = {}): ChainHeaderView {
    return ankoView({ ChainHeaderView(it) }, theme, init)
}
