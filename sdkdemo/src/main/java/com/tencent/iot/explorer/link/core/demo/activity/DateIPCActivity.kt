package com.tencent.iot.explorer.link.core.demo.activity

import android.app.DatePickerDialog.OnDateSetListener
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View.OnTouchListener
import android.widget.DatePicker
import android.widget.MediaController
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.view.ProgressItem
import com.tencent.iot.explorer.link.core.demo.view.TimeAdapter
import com.tencent.iot.explorer.link.core.demo.view.TimeNum
import kotlinx.android.synthetic.main.activity_date_ipc.*
import kotlinx.android.synthetic.main.activity_ipc.vedio_player
import java.util.*
import kotlin.collections.ArrayList


class DateIPCActivity : BaseActivity() {
    var REQ_DATE_CODE = 0x1101
    var url = ""
    var cd = Calendar.getInstance()
    private var progressItemList: ArrayList<ProgressItem>? = null
    var adapter: TimeAdapter? = null

    override fun getContentView(): Int {
        return R.layout.activity_date_ipc
    }

    override fun initView() {
        url = intent.getStringExtra(IPCActivity.URL)
        initDataToSeekbar()
    }

    private fun initDataToSeekbar() {
        progressItemList = ArrayList()
        // red span
        var item = ProgressItem()
        item.startHour = 1
        item.endHour = 2
        item.progressItemPercentage = ProgressItem.getProgressItemPercentage(item)
        item.progressItemPercentageEnd = ProgressItem.getProgressItemPercentageEnd(item)
        progressItemList!!.add(item)
        // blue span
        var item1 = ProgressItem()
        item1.startHour = 3
        item1.endHour = 6
        item1.progressItemPercentage = ProgressItem.getProgressItemPercentage(item1)
        item1.progressItemPercentageEnd = ProgressItem.getProgressItemPercentageEnd(item1)
        progressItemList!!.add(item1)

        var item2 = ProgressItem()
        item2.startHour = 23
        item2.endHour = 23
        item2.endMin = 59
        item2.progressItemPercentage = ProgressItem.getProgressItemPercentage(item2)
        item2.progressItemPercentageEnd = ProgressItem.getProgressItemPercentageEnd(item2)
        progressItemList!!.add(item2)

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
            var intent = Intent(this@DateIPCActivity, DateSelectActivity::class.java)
            startActivityForResult(intent, REQ_DATE_CODE)
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
            var month = data?.getIntExtra("month", 0)
            var day = data?.getIntExtra("day", 0)
            tv_date.setText("" + year + "/" + (month!! + 1) + "/" + day)
        }
    }

    private var onSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            var h = progress /60
            var m = progress % 60
            tv_current_time.setText("" + h + "时" + m + "分")
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }

    private fun playVideo(url: String) {
        val uri: Uri = Uri.parse(url)
        vedio_player.setVideoURI(uri)
        var mediaController = MediaController(this)
        vedio_player.setMediaController(mediaController)
        vedio_player.start()
    }

    private fun stopPlay() {
        vedio_player.stopPlayback()
    }

}
