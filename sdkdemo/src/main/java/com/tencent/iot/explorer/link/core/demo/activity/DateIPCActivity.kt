package com.tencent.iot.explorer.link.core.demo.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.View.OnTouchListener
import android.widget.MediaController
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.entity.BaseResponse
import com.tencent.iot.explorer.link.core.demo.entity.VideoHistory
import com.tencent.iot.explorer.link.core.demo.view.ProgressItem
import com.tencent.iot.explorer.link.core.demo.view.TimeAdapter
import com.tencent.iot.explorer.link.core.demo.view.TimeNum
import com.tencent.iot.video.link.callback.VideoCallback
import com.tencent.iot.video.link.service.VideoBaseService
import kotlinx.android.synthetic.main.activity_date_ipc.*
import kotlinx.android.synthetic.main.activity_date_ipc.view.*
import kotlinx.android.synthetic.main.activity_ipc.vedio_player
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread


class DateIPCActivity : BaseActivity() {
    var productId = ""
    var devName = ""
    var secretKey = ""
    var secretId = ""
    var REQ_DATE_CODE = 0x1101
    var cd = Calendar.getInstance()
    private var progressItemList: ArrayList<ProgressItem>? = null
    var adapter: TimeAdapter? = null
    var paramDateStr = ""
    var paramDateParamStr = ""
    var baseUrl = ""
    var playUrl = ""
    var freshThread: Thread? = null

    override fun getContentView(): Int {
        return R.layout.activity_date_ipc
    }

    override fun initView() {
        productId = intent.getStringExtra(PRODUCTID)
        devName = intent.getStringExtra(DEV_NAME)
        secretKey = intent.getStringExtra(SCRE_KEY)
        secretId = intent.getStringExtra(SCRE_ID)

        initDataToSeekbar()
    }

    private fun initDataToSeekbar() {
        progressItemList = ArrayList()
        seekbar.initData(progressItemList)
        seekbar.invalidate()

        var allItemData = ArrayList<TimeNum>()
        for (i in 0 .. 23) {
            var ele = TimeNum()
            ele.timeNum = i
            allItemData.add(ele)
        }

        Thread(Runnable{
            while(true) {
                if (seekbar.thumb.bounds.height() > 0) {
                    break;
                }
                Thread.sleep(100)
            }

            runOnUiThread {
                val layoutManager = GridLayoutManager(this@DateIPCActivity, 24)
                v_grid.setLayoutManager(layoutManager)
                adapter = TimeAdapter(allItemData)
                v_grid.setAdapter(adapter)
                v_grid.setPadding(seekbar.thumb.bounds.height(),0,seekbar.thumb.bounds.height(),0);
            }
        }).start()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun setListener() {
        btn_sel_date.setOnClickListener {

            VideoBaseService(secretId, secretKey).getIPCDateData(productId, devName, object:
                VideoCallback {
                override fun fail(msg: String?, reqCode: Int) {
                    Toast.makeText(this@DateIPCActivity, msg, Toast.LENGTH_SHORT).show()
                }

                override fun success(response: String?, reqCode: Int) {
                    var resp = JSONObject.parseObject(response, BaseResponse::class.java)
                    var intent = Intent(this@DateIPCActivity, DateSelectActivity::class.java)
                    intent.putExtra("dataArr", resp.Response?.data)
                    startActivityForResult(intent, REQ_DATE_CODE)
                }
            })

        }

        seekbar.setOnSeekBarChangeListener(onSeekBarChangeListener)
        seekbar.setOnTouchListener(OnTouchListener { v, me ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        })

        seekbar.setProgress(1, false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_DATE_CODE && resultCode == RESULT_OK) {
            var year = data?.getIntExtra("year", 0)
            var month = data?.getIntExtra("month", 0)!! + 1
            var day = data?.getIntExtra("day", 0)
            tv_date.setText("" + year + "/" + month + "/" + day)

            var date = Date()
            date.year = year!!
            date.month = month - 1
            date.date = day

            paramDateStr = String.format("%04d-%02d-%02d", year, month, day)
            paramDateParamStr = String.format("%04d%02d%02d", year, month, day)

            VideoBaseService(secretId, secretKey).getIPCTimeData(productId, devName, paramDateStr, object:
                VideoCallback {
                override fun fail(msg: String?, reqCode: Int) {
                    Toast.makeText(this@DateIPCActivity, msg, Toast.LENGTH_SHORT).show()
                }

                override fun success(response: String?, reqCode: Int) {
                    var resp = JSONObject.parseObject(response, BaseResponse::class.java)
                    var history = JSONObject.parseObject(resp.Response?.data, VideoHistory::class.java)
                    if (history == null) {
                        return
                    }
                    baseUrl = history.VideoURL
                    var allTimeBlock = history.TimeList
                    if (allTimeBlock != null && allTimeBlock.size > 0) {
                        progressItemList = ArrayList()
                        var i = 0
                        while (i < allTimeBlock.size) {
                            var start = Date(allTimeBlock.get(i).StartTime * 1000)
                            while (i + 1 < allTimeBlock.size &&
                                ((allTimeBlock.get(i).EndTime == allTimeBlock.get(i + 1).StartTime) ||   // 上一次的结束时间和下一次的开始时间重合
                                        ((allTimeBlock.get(i).EndTime + 60) == allTimeBlock.get(i + 1).StartTime))) {  // 上一次的结束时间和下一次的开始时间相差一分钟
                                i++
                            }
                            var end = Date(allTimeBlock.get(i).EndTime * 1000)

                            var item = ProgressItem()
                            item.startTimeMillis = allTimeBlock.get(i).StartTime
                            item.endTimeMillis = allTimeBlock.get(i).EndTime
                            item.startHour = start.hours
                            item.startMin = start.minutes
                            item.startSec = start.seconds
                            item.endHour = end.hours
                            item.endMin = end.minutes
                            item.endSec = end.seconds
                            item.progressItemPercentage = ProgressItem.getProgressItemPercentage(item)
                            item.progressItemPercentageEnd = ProgressItem.getProgressItemPercentageEnd(item)
                            progressItemList!!.add(item)
                            i++
                        }

                        seekbar.initData(progressItemList)
                        seekbar.invalidate()
                    }
                }
            })
        }
    }

    private var onSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            var h = progress /60
            var m = progress % 60
            tv_current_time.setText("" + h + "时" + m + "分")
            if (!fromUser) return  // 程序逻辑造成的进度刷新，不触发重新获取 url 的过程

            if (TextUtils.isEmpty(paramDateStr)) {
                Toast.makeText(this@DateIPCActivity, "请选择回放日期", Toast.LENGTH_SHORT).show()
            } else {
                if (progressItemList != null && progressItemList!!.size > 0) {
                    for (j in 0 until progressItemList!!.size) {
                        var tagA = progressItemList!!.get(j).startHour * 60 + progressItemList!!.get(j).startMin
                        var tagB = progressItemList!!.get(j).endHour * 60 + progressItemList!!.get(j).endMin
                        var seletedTime = h * 60 + m
                        if (tagA <= seletedTime && tagB >= seletedTime) {
                            // 选择了合适的时间

                            playUrl = baseUrl + "?starttime=" + paramDateParamStr + String.format("%02d%02d%02d", progressItemList!!.get(j).startHour, progressItemList!!.get(j).startMin, progressItemList!!.get(j).startSec) +
                                    "&endtime=" + paramDateParamStr + String.format("%02d%02d%02d", progressItemList!!.get(j).endHour, progressItemList!!.get(j).endMin, progressItemList!!.get(j).endSec)
                            Log.e("XXX", "playUrl " + playUrl)

                            playVideo(playUrl, tagA, (progress - tagA) * 60 * 1000)
                            return
                        }
                    }
                }
                Toast.makeText(this@DateIPCActivity, "选择时间段内无回放录像", Toast.LENGTH_SHORT).show()

            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }

    /**
     * offset : 录像偏移的毫秒数
     */
    private fun playVideo(url: String, start: Int, offset: Int) {
        if (freshThread != null) {      // 已经处在播放过程中，拖拽进度条，清理上一次的刷新线程
            freshThread?.interrupt()
        }
        val uri: Uri = Uri.parse(url)
        vedio_player.setVideoURI(uri)
        vedio_player.start()
        vedio_player.seekTo(offset) // 设置进度为毫秒级
        freshThread = Thread(Runnable {
            while (true) {
                try {
                    Thread.sleep(30000) // 半分钟检查一次进度值并刷新
                } catch (e : InterruptedException) {
                    e.printStackTrace()
                    break
                }

                runOnUiThread(Runnable {
                    seekbar.setProgress(start + vedio_player.currentPosition / (1000 * 60)) // 设置进度为分钟级
                })
            }
        })
        freshThread?.start()
        vedio_player.setOnCompletionListener {
            freshThread?.interrupt()
        }

        vedio_player.setOnErrorListener { mp, what, extra ->
            freshThread?.interrupt()
            false
        }
    }

    private fun stopPlay() {
        vedio_player.stopPlayback()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (freshThread != null) {
            freshThread?.interrupt()
        }
    }

    companion object {
        const val PRODUCTID = "productId"
        const val DEV_NAME = "devName"
        const val SCRE_KEY = "secretKey"
        const val SCRE_ID = "secretId"
    }

}
