package com.yjkj.chainup.freestaking.adapter

import android.app.Activity
import android.graphics.Bitmap
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.yjkj.chainup.R
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.app.ChainUpApp
import com.yjkj.chainup.freestaking.bean.CurrencyBean

/**
 * FreeStaking首页项目列表的适配器
 */
class FreeStakingRecyclerAdapter(data: ArrayList<CurrencyBean>) :
        BaseQuickAdapter<CurrencyBean, BaseViewHolder>(R.layout.item_freestaking_symbol, data) {

    override fun convert(helper: BaseViewHolder, item: CurrencyBean) {
        var simpleTarget: SimpleTarget<Bitmap> = object : SimpleTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                helper?.setImageBitmap(R.id.iv_logo, resource)
            }
        }

        Glide.with(context)
                .asBitmap()
                .load(item?.logo)
                .into(simpleTarget)
        //币种简称
        helper?.setText(R.id.tv_name, item?.shortName)
        helper?.setTextColor(R.id.tv_status, ContextCompat.getColor(ChainUpApp.appContext, R.color.normal_text_color))
        helper?.setVisible(R.id.iv_label, false)
        when (item?.projectType) {
            1 -> {
                when (item?.status) {
                    0 -> {


                    }
                    1 -> {
                        //项目状态（1:待开始 2:进行中 3:已结束 4:已满仓）
                        helper?.setText(R.id.tv_status, "「" + context.getString(R.string.pos_state_start) + "」")
                        helper?.setVisible(R.id.iv_label, true)
                        when (item.labelType) {
                            0 -> {
                            }
                            1 -> {
                                //标签类型（0.无1.热 2.新）
                                helper?.setImageResource(R.id.iv_label, R.drawable.personal_hot)
                            }
                            2 -> {
                                helper?.setImageResource(R.id.iv_label, R.drawable.personal_new)

                            }

                        }
                    }
                    2 -> {
                        //项目状态（1:待开始 2:进行中 3:已结束 4:已满仓）
                        helper?.setText(R.id.tv_status, "「" + context.getString(R.string.pos_state_processing) + "」")
                        helper?.setTextColor(R.id.tv_status, ContextCompat.getColor(ChainUpApp.appContext, R.color.main_blue))

                    }

                    3 -> {
                        helper?.setText(R.id.tv_status, "「" + context.getString(R.string.pos_state_end) + "」")

                    }
                }

            }
            3 -> {
                when (item?.status) {
                    0 -> {
//                        项目状态（1:待开始 2:进行中 3:已结束 4:已满仓）
                        helper?.setText(R.id.tv_status, "「" + context.getString(R.string.pos_state_start) + "」")
                        helper?.setVisible(R.id.iv_label, true)
                        when (item.labelType) {
                            0 -> {
                            }
                            1 -> {
                                //标签类型（0.无1.热 2.新）
                                helper?.setImageResource(R.id.iv_label, R.drawable.personal_hot)
                            }
                            2 -> {
                                helper?.setImageResource(R.id.iv_label, R.drawable.personal_new)

                            }

                        }
                    }
                    1 -> {
                        //项目状态（1:待开始 2:进行中 3:已结束 4:已满仓）
                        helper?.setText(R.id.tv_status, "「" + context.getString(R.string.pos_state_buying) + "」")
                        helper?.setVisible(R.id.iv_label, true)
                        helper?.setTextColor(R.id.tv_status, ContextCompat.getColor(ChainUpApp.appContext, R.color.main_blue))
                        when (item.labelType) {
                            0 -> {
                            }
                            1 -> {
                                //标签类型（0.无1.热 2.新）
                                helper?.setImageResource(R.id.iv_label, R.drawable.personal_hot)
                            }
                            2 -> {
                                helper?.setImageResource(R.id.iv_label, R.drawable.personal_new)

                            }

                        }
                    }
                    2 -> {
                        //项目状态（1:待开始 2:进行中 3:已结束 4:已满仓）
                        helper?.setText(R.id.tv_status, "「" + context.getString(R.string.pos_state_waitInterest) + "」")
                    }

                    3 -> {
                        helper?.setText(R.id.tv_status, "「" + context.getString(R.string.pos_state_InterestIng) + "」")

                    }
                    4 -> {
                        helper?.setText(R.id.tv_status, "「" + context.getString(R.string.pos_state_InterestEnd) + "」")
                    }
                    5 -> {
                        helper?.setText(R.id.tv_status, "「" + context.getString(R.string.pos_state_release) + "」")
                    }
                    6 -> {
                        helper?.setText(R.id.tv_status, "「" + context.getString(R.string.pos_state_fulled) + "」")
                    }
                }

            }
        }


        //项目名称
        helper?.setText(R.id.tv_current, item?.name)
        //年化收益
        helper?.setText(R.id.tv_percentage, item?.gainRate.toString() + "%")
        when (item?.projectType) {
            0 -> {

            }
            1 -> {
                helper?.setGone(R.id.tv_lockDay, !false)
                helper?.setGone(R.id.tv_dayNumber, !false)
                helper?.setGone(R.id.tv_lockProgress, !false)
                helper?.setGone(R.id.tv_progressNumber, !false)
            }
            2 -> {

            }
            3 -> {
                helper?.setGone(R.id.tv_lockDay, !true)
                helper?.setGone(R.id.tv_dayNumber, !true)
                helper?.setGone(R.id.tv_lockProgress, !true)
                helper?.setGone(R.id.tv_progressNumber, !true)
                //锁仓天数
                helper?.setText(R.id.tv_dayNumber, item.lockDay.toString())
                //锁仓进度
                helper?.setText(R.id.tv_progressNumber, item.progress)
            }

        }

    }

}