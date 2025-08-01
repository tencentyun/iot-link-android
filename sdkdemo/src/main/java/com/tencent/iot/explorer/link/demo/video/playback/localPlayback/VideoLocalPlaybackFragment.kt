package com.tencent.iot.explorer.link.demo.video.playback.localPlayback

import android.graphics.SurfaceTexture
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.SeekBar
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.demo.App
import com.tencent.iot.explorer.link.demo.BuildConfig
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.common.customView.CalendarView
import com.tencent.iot.explorer.link.demo.common.customView.timeline.TimeLineView
import com.tencent.iot.explorer.link.demo.common.customView.timeline.TimeLineViewChangeListener
import com.tencent.iot.explorer.link.demo.common.util.CommonUtils
import com.tencent.iot.explorer.link.demo.common.util.ImageSelect
import com.tencent.iot.explorer.link.demo.databinding.FragmentVideoLocalPlaybackBinding
import com.tencent.iot.explorer.link.demo.video.Command
import com.tencent.iot.explorer.link.demo.video.CommandResp
import com.tencent.iot.explorer.link.demo.video.DevInfo
import com.tencent.iot.explorer.link.demo.video.playback.*
import com.tencent.iot.explorer.link.demo.video.utils.ToastDialog
import com.tencent.iot.video.link.callback.VideoCallback
import com.tencent.iot.video.link.service.VideoBaseService
import com.tencent.xnet.XP2P
import com.tencent.xnet.XP2PAppConfig
import com.tencent.xnet.XP2PCallback
import com.tencent.xnet.annotations.XP2PProtocolType
import kotlinx.coroutines.*
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.collections.ArrayList

class VideoLocalPlaybackFragment : VideoPlaybackBaseFragment<FragmentVideoLocalPlaybackBinding>(),
    TextureView.SurfaceTextureListener,
    XP2PCallback, VideoCallback {
    private var TAG = VideoLocalPlaybackFragment::class.java.simpleName
    var devInfo: DevInfo? = null
    private var player: IjkMediaPlayer = IjkMediaPlayer()
    private lateinit var surface: Surface

    @Volatile
    private var currentPostion = -1L // 小于 0 不需要恢复录像的进度，大于等于 0 需要恢复录像的进度

    @Volatile
    private var currentPlayerState = true
    private var dateDataSet: MutableSet<String> = CopyOnWriteArraySet()
    private var dlg: CalendarDialog? = null
    private var urlPrefix = ""

    @Volatile
    private var recordingState = false
    private var seekBarJob: Job? = null
    private var keepStartTime = 0L
    private var keepEndTime = 0L
    private var filePath: String? = null

    @Volatile
    private var connected = false

    @Volatile
    private var isShowing = false
    private var updateSeekBarAble = true  // 手动拖拽过程的标记

    private var adapter: LocalPlaybackListAdapter? = null
    private var playbacks: MutableList<PlaybackFile> = ArrayList()

    private var out: FileOutputStream? = null

    @Volatile
    private var isDownloading = false
    private lateinit var toastDialog: ToastDialog

    private var xp2pInfo: String? = null

    private fun sendCmd(id: String, cmd: String): String {
        if (connected)
            XP2P.postCommandRequestSync(
                id,
                cmd.toByteArray(),
                cmd.toByteArray().size.toLong(),
                2 * 1000 * 1000
            )?.let {
                return it
            }
        return ""
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if (isVisibleToUser && connected) {
            refreshDateTime(Date())
        } else {
            player.let {
                if (currentPlayerState) {
                    Log.d(TAG, "setUserVisibleHint playVideo isVisibleToUser $isVisibleToUser")
                    // 滑动该页面时，如果处于播放状态，暂停播放
                    launch(Dispatchers.Main) {
                        binding.ivStart.performClick()
                    }
                }
            }
        }
        isShowing = isVisibleToUser
        Log.d(TAG, "setUserVisibleHint isShowing $isShowing")
    }

    override fun startHere(view: View) {
        with(binding) {
            initVideoPlaybackView(this)
            IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_DEBUG)
            tvDate.text = dateFormat.format(System.currentTimeMillis())

            val linearLayoutManager = LinearLayoutManager(context)
            adapter = LocalPlaybackListAdapter(context, playbacks)
            listLocalPlayback.layoutManager = linearLayoutManager
            listLocalPlayback.adapter = adapter

            setListener()
            getDeviceP2PInfo()
            XP2P.setCallback(this@VideoLocalPlaybackFragment)

            dlg = CalendarDialog(context, ArrayList(), onMonthChanged)
            dlg?.setOnClickedListener(dlgOnClickedListener)
            playSpeed.setText(R.string.play_speed_1)
            recordView()
            localPalaybackVideo.surfaceTextureListener = this@VideoLocalPlaybackFragment
            timeLine.setTimelineChangeListener(timeLineViewChangeListener)
            videoSeekbar.setOnSeekBarChangeListener(onSeekBarChangeListener)
        }
    }

    private fun getDeviceP2PInfo() {
        if (devInfo?.DeviceName.isNullOrEmpty()) return
        App.data.accessInfo?.let {
            VideoBaseService(it.accessId, it.accessToken).getDeviceXp2pInfo(
                it.productId, devInfo?.DeviceName!!, this
            )
        }
    }

    /**
     * 开启xp2p服务
     */
    private fun startService() {
        Log.d(TAG, "startService")
        if (App.data.accessInfo == null || devInfo == null || TextUtils.isEmpty(devInfo?.DeviceName)) return
        val xP2PAppConfig = XP2PAppConfig().apply {
            appKey = BuildConfig.TencentIotLinkSDKDemoAppkey
            appSecret = BuildConfig.TencentIotLinkSDKDemoAppSecret
            autoConfigFromDevice = false
            type = XP2PProtocolType.XP2P_PROTOCOL_AUTO
        }
        XP2P.startService(
            context, App.data.accessInfo?.productId, devInfo?.DeviceName, xp2pInfo, xP2PAppConfig
        )
    }

    private var onCompletionListener = object : IMediaPlayer.OnCompletionListener {
        override fun onCompletion(mp: IMediaPlayer?) {
            Log.d(TAG, "onCompletion")
            binding.ivStart.setImageResource(R.mipmap.start)
            binding.pauseTipLayout.visibility = View.VISIBLE
            seekBarJob?.cancel()
            currentPlayerState = false
            playVideo(keepStartTime, keepEndTime, 0)
        }
    }

    private var timeLineViewChangeListener = object : TimeLineViewChangeListener {
        override fun onChange(date: Date?, timeLineView: TimeLineView?) {
            currentPlayerState = true
            if (timeLineView == null) return
            date?.let {
                for (j in 0 until timeLineView!!.timeBlockInfos.size) {
                    var blockTime = timeLineView!!.timeBlockInfos!!.get(j)
                    if (blockTime.startTime.time <= date.time && blockTime.endTime.time >= date.time) {
                        var offest = date.time - blockTime.startTime.time
                        playVideo(
                            blockTime.startTime.time / 1000,
                            blockTime.endTime.time / 1000,
                            offest / 1000
                        )
                        return@onChange
                    }
                }
            }
        }
    }

    override fun videoViewNeeResize(marginStart: Int, marginEnd: Int) {
        val videoLayoutParams =
            binding.localPalaybackVideo.layoutParams as ConstraintLayout.LayoutParams
        videoLayoutParams.marginStart = marginStart
        videoLayoutParams.marginEnd = marginEnd
        binding.localPalaybackVideo.layoutParams = videoLayoutParams
    }

    override fun getLayoutVideo(): ConstraintLayout = binding.layoutVideo

    private fun startJobRereshTimeAndProgress() {
        seekBarJob = launch {
            while (isActive) {
                delay(1000)
                binding.tvCurrentPos.text = CommonUtils.formatTime(player.currentPosition)
                if (updateSeekBarAble) {
                    binding.videoSeekbar.progress = (player.currentPosition / 1000).toInt()
                }
            }
        }
    }

    private var onErrorListener = object : IMediaPlayer.OnErrorListener {
        override fun onError(mp: IMediaPlayer?, what: Int, extra: Int): Boolean {
            with(binding) {
                videoSeekbar.progress = 0
                videoSeekbar.max = 0
                tvCurrentPos.setText("00:00:00")
                tvAllTime.setText("00:00:00")
                ivStart.setImageResource(R.mipmap.start)
                pauseTipLayout.visibility = View.GONE
                Toast.makeText(context, getString(R.string.no_data), Toast.LENGTH_SHORT).show()
                ivStart.isClickable = false
            }

            currentPlayerState = false
            return true
        }
    }

    private var onInfoListener = object : IMediaPlayer.OnInfoListener {
        override fun onInfo(mp: IMediaPlayer?, what: Int, extra: Int): Boolean {
            mp?.let {
                if (it.isPlaying) {
                    binding.ivStart.setImageResource(R.mipmap.stop)
                    currentPlayerState = true
                    binding.pauseTipLayout.visibility = View.GONE
                    return true
                }
            }

            binding.ivStart.setImageResource(R.mipmap.start)
            binding.pauseTipLayout.visibility = View.VISIBLE
            currentPlayerState = false
            return true
        }

        override fun onInfoSEI(
            mp: IMediaPlayer?,
            what: Int,
            extra: Int,
            sei_content: String?
        ): Boolean {
            return false
        }

        override fun onInfoAudioPcmData(mp: IMediaPlayer?, arrPcm: ByteArray?, length: Int) {

        }
    }

    private var dlgOnClickedListener = object : CalendarDialog.OnClickedListener {
        override fun onOkClicked(checkedDates: MutableList<String>?) {
            checkedDates?.let {
                if (it.size <= 0) return@let
                binding.tvDate.text = CommonUtils.dateConvertionWithSplit(it[0])
                var parseDate =
                    SimpleDateFormat(CalendarView.SECOND_DATE_FORMAT_PATTERN).parse(binding.tvDate.text.toString())
                launch(Dispatchers.Main) {
                    refreshDateTime(parseDate)
                }
            }
        }

        override fun onOkClickedCheckedDateWithoutData() {
            ToastDialog(
                context,
                ToastDialog.Type.WARNING,
                getString(R.string.checked_date_no_video),
                2000
            ).show()
        }

        override fun onOkClickedWithoutDateChecked() {
            ToastDialog(
                context,
                ToastDialog.Type.WARNING,
                getString(R.string.checked_date_first),
                2000
            ).show()
        }
    }

    private var onMonthChanged = object : CalendarDialog.OnMonthChanged {
        override fun onMonthChanged(dlg: CalendarDialog, it: String) {
            // 包含对应数据，不再重新获取当前日期的数据
            if (dateDataSet.contains(it)) return

            launch(Dispatchers.Main) {
                var resp = requestTagDateInfo(it)
                Log.d(TAG, "resp $resp")
                if (TextUtils.isEmpty(resp)) return@launch

                try {
                    var respJson = JSONObject.parseObject(resp)
                    if (!respJson.containsKey("video_list")) return@launch

                    // 转换成实际有录像的日期
                    dateDataSet.add(it)
                    var datesStrArr = Integer.toBinaryString(respJson.getInteger("video_list"))
                    datesStrArr = datesStrArr.reversed()
                    var dateParam: MutableList<String> = ArrayList()
                    for (i in datesStrArr.indices) {
                        if (datesStrArr[i].toString() == "1") {
                            var eleDate = it.replace("-", "") + String.format("%02d", i + 1)
                            dateParam.add(eleDate)
                        }
                    }
                    dlg.addTagDates(dateParam)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentVideoLocalPlaybackBinding =
        FragmentVideoLocalPlaybackBinding.inflate(inflater, container, false)

    private fun setListener() {
        with(binding) {
            pauseTipLayout.setOnClickListener { ivStart.performClick() }
            ivVideoBack.setOnClickListener { }
            playbackControl.setOnClickListener { }
            ivLeftGo.setOnClickListener { timeLine.last() }
            ivRightGo.setOnClickListener { timeLine.next() }
            layoutSelectDate.setOnClickListener { showCalendarDialog() }

            playSpeed.setOnClickListener {
                if (!portrait) {
                    val dlg = RightPlaySpeedDialog(context, getIndexByText())
                    dlg.setOnDismisListener(rightPlaySpeedDialogListener)
                    dlg.show()
                } else {
                    val dlg = BottomPlaySpeedDialog(
                        context,
                        getString(R.string.play_speed_title),
                        getIndexByText()
                    )
                    dlg.setOnDismisListener(bottomPlaySpeedDialogListener)
                    dlg.show()
                }
            }

            ivVideoPhoto.setOnClickListener {
                ImageSelect.saveBitmap(
                    this@VideoLocalPlaybackFragment.context,
                    localPalaybackVideo.bitmap
                )
                ToastDialog(
                    context,
                    ToastDialog.Type.SUCCESS,
                    getString(R.string.capture_successed),
                    2000
                ).show()
            }

            ivVideoRecord.setOnClickListener {
                recordingState = !recordingState
                recordView()
                if (recordingState) {
                    filePath = CommonUtils.generateFileDefaultPath()
                    var ret = player.startRecord(filePath)
                    if (ret != 0) {
                        ToastDialog(
                            context,
                            ToastDialog.Type.WARNING,
                            getString(R.string.record_failed),
                            2000
                        ).show()
                    }
                } else {
                    player.stopRecord()
                    context?.let { ctx ->
                        CommonUtils.refreshVideoList(ctx, filePath)
                    }
                }
            }
            ivStart.setOnClickListener {
                devInfo ?: let { return@setOnClickListener }
                if (TextUtils.isEmpty(tvAllTime.text.toString())) return@setOnClickListener

                var id = "${App.data.accessInfo?.productId}/${devInfo?.DeviceName}"
                Log.d(TAG, "setOnClickListener currentPlayerState $currentPlayerState")

                if (currentPlayerState) {
                    launch(Dispatchers.IO) {
                        var stopCommand = Command.pauseLocalVideoUrl(devInfo!!.Channel)
                        var resp = sendCmd(id, stopCommand)
                        var commandResp = JSONObject.parseObject(resp, CommandResp::class.java)
                        if (commandResp != null && commandResp.status == 0) {
                            launch(Dispatchers.Main) {
                                ivStart.setImageResource(R.mipmap.start)
                                pauseTipLayout.visibility = View.VISIBLE
                                seekBarJob?.cancel()
                                currentPlayerState = false
                                player.pause()
                            }
                        }
                    }

                } else {
                    launch(Dispatchers.IO) {
                        var startCommand = Command.resumeLocalVideoUrl(devInfo!!.Channel)
                        var resp = sendCmd(id, startCommand)
                        var commandResp = JSONObject.parseObject(resp, CommandResp::class.java)
                        if (commandResp != null && commandResp.status == 0) {
                            launch(Dispatchers.Main) {
                                ivStart.setImageResource(R.mipmap.stop)
                                pauseTipLayout.visibility = View.GONE
                                startJobRereshTimeAndProgress()
                                currentPlayerState = true
                                player.start()
                            }
                        }
                    }
                }
            }
        }

        adapter?.setOnDownloadClickedListenter(onDownloadClickedListener)
        adapter?.setOnItemClickedListener(onItemClickedListener)
    }

    private var onDownloadClickedListener =
        object : LocalPlaybackListAdapter.OnDownloadClickedListener {
            override fun onItemDownloadClicked(pos: Int) {
                if (!isDownloading) {
                    val offset = initDownload(playbacks[pos])
                    if (offset >= 0) {
                        XP2P.startAvRecvService(
                            "${App.data.accessInfo?.productId}/${devInfo?.DeviceName}",
                            "action=download&channel=0&file_name=${playbacks[pos].file_name}&offset=${offset}",
                            false
                        )
                        isDownloading = true
                        toastDialog = ToastDialog(
                            context,
                            ToastDialog.Type.SUCCESS,
                            getString(R.string.downloading),
                            600000
                        )
                        toastDialog.setCancelable(false)
                        toastDialog.show()
                    }
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.download_one_at_a_time),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    private var onItemClickedListener = object : LocalPlaybackListAdapter.OnItemClickedListener {
        override fun onItemClicked(pos: Int) {
            currentPlayerState = true
            playVideo(playbacks[pos].start_time, playbacks[pos].end_time, 0)
        }
    }

    private fun recordView() {
        with(binding) {
            if (recordingState) {
                ivVideoRecord.setImageResource(R.mipmap.recording)
                recordingLayout.visibility = View.VISIBLE
            } else {
                ivVideoRecord.setImageResource(R.mipmap.record_white)
                recordingLayout.visibility = View.GONE
            }
        }
    }

    private var rightPlaySpeedDialogListener = object : RightPlaySpeedDialog.OnDismisListener {
        override fun onItemClicked(pos: Int) {
            binding.playSpeed.setText(getTextByIndex(pos))
        }

        override fun onDismiss() {}
    }

    private var bottomPlaySpeedDialogListener = object : BottomPlaySpeedDialog.OnDismisListener {
        override fun onItemClicked(pos: Int) {
            binding.playSpeed.setText(getTextByIndex(pos))
        }
    }

    private fun getTextByIndex(index: Int): String {
        when (index) {
            0 -> return getString(R.string.play_speed_0_5)
            1 -> return getString(R.string.play_speed_0_75)
            2 -> return getString(R.string.play_speed_1)
            3 -> return getString(R.string.play_speed_1_25)
            4 -> return getString(R.string.play_speed_1_5)
            5 -> return getString(R.string.play_speed_2)
        }
        return getString(R.string.play_speed_1)
    }

    private fun getIndexByText(): Int {
        when (binding.playSpeed.text.toString()) {
            getString(R.string.play_speed_0_5) -> return 0
            getString(R.string.play_speed_0_75) -> return 1
            getString(R.string.play_speed_1) -> return 2
            getString(R.string.play_speed_1_25) -> return 3
            getString(R.string.play_speed_1_5) -> return 4
            getString(R.string.play_speed_2) -> return 5
        }
        return -1
    }

    private fun formateDateParam(dateStr: String): String {
        var timeStr = dateStr
        timeStr = timeStr.replace("-", "")
        if (timeStr.length >= 6) {
            return timeStr.substring(0, 6)
        }
        return ""
    }

    private fun requestTagDateInfo(dateStr: String): String {
        if (App.data.accessInfo == null || devInfo == null ||
            TextUtils.isEmpty(devInfo?.DeviceName)
        ) return ""

        var id = "${App.data.accessInfo?.productId}/${devInfo?.DeviceName}"
        var timeStr = formateDateParam(dateStr)
        Log.d(TAG, "request timeStr $timeStr")
        var command = Command.getMonthDates(devInfo!!.Channel, timeStr)
        if (TextUtils.isEmpty(command)) return ""
        return sendCmd(id, command)
    }

    private fun refreshDateTime(date: Date) {
        if (App.data.accessInfo == null || devInfo == null ||
            TextUtils.isEmpty(devInfo?.DeviceName)
        ) return

        var id = "${App.data.accessInfo?.productId}/${devInfo?.DeviceName}"
        var command = Command.getDayTimeBlocks(devInfo!!.Channel, date)
        var resp = sendCmd(id, command)

        if (TextUtils.isEmpty(resp)) return
        val json = JSON.parseObject(resp)
        val currentPlaybacks =
            JSONArray.parseArray(json["file_list"].toString(), PlaybackFile::class.java)
        playbacks.clear()
        if (currentPlaybacks != null) {
            for (item in currentPlaybacks) {
                if (item.file_type == 0) playbacks.add(item) //只显示视频文件,0：视频，1：图片，其他：自定义
            }
            adapter?.notifyDataSetChanged()
            playVideo(playbacks[0].start_time, playbacks[0].end_time, 0)
        }
    }

    private fun playVideo(startTime: Long, endTime: Long, offset: Long) {
        Log.d(TAG, "startTime $startTime endTime $endTime offset $offset")
        keepStartTime = startTime
        keepEndTime = endTime
        devInfo?.let {
            Log.d(TAG, "isShowing $isShowing")
            launch(Dispatchers.Main) {
                delay(1000)
                if (!isShowing) currentPlayerState = false
                Log.d(TAG, "playVideo currentPlayerState $currentPlayerState")
                setPlayerUrl(Command.getLocalVideoUrl(it.Channel, startTime, endTime), offset)
                binding.tvAllTime.text = CommonUtils.formatTime(endTime * 1000 - startTime * 1000)
                binding.videoSeekbar.max = (endTime - startTime).toInt()
            }
        }
    }

    private fun setPlayerUrl(suffix: String, offset: Long) {
        player.release()
        launch(Dispatchers.Main) {
            binding.layoutVideo.removeView(binding.localPalaybackVideo)
            binding.layoutVideo.addView(binding.localPalaybackVideo, 0)
        }

        player = IjkMediaPlayer()
        player.setOnInfoListener(onInfoListener)
        player.setOnErrorListener(onErrorListener)
        player.setOnCompletionListener(onCompletionListener)
        player.let {
            var url = urlPrefix + suffix
            Log.d(TAG, "setPlayerUrl url $url")
            it.reset()

            it.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100)
            it.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 50 * 1024)
            it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0)
            if (!currentPlayerState) {
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0)
            } else {
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1)
            }
            it.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "threads", 1)
            it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "sync-av-start", 0)
            it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1)
            it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1)
            it.setOption(
                IjkMediaPlayer.OPT_CATEGORY_PLAYER,
                "mediacodec-handle-resolution-change",
                1
            )

            it.setSurface(this.surface)
            it.dataSource = url
            it.prepareAsync()
            it.start()

            if (offset > 0) {
                devInfo?.let { dev ->
                    var seekCommand = Command.seekLocalVideo(dev.Channel, offset)
                    var id = "${App.data.accessInfo?.productId}/${dev.DeviceName}"

                    var seekResp = sendCmd(id, seekCommand)
                    var commandResp = JSON.parseObject(seekResp, CommandResp::class.java)
                    L.e(TAG, "seekCommandResp code " + commandResp?.status)
                }
            }

            startJobRereshTimeAndProgress()
        }
    }

    private fun showCalendarDialog() {
        dateDataSet.clear()
        dlg?.show()
    }

    private var onSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            updateSeekBarAble = false
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            seekBar?.let {
                if (keepStartTime <= 0 && keepEndTime <= 0) {
                    seekBar.progress = 0
                    return
                }
                playVideo(keepStartTime, keepEndTime, it.progress.toLong())
                player.seekTo(it.progress.toLong() * 1000)
            }
            updateSeekBarAble = true
        }
    }

    private fun initDownload(playbackFile: PlaybackFile): Long {
        var offset = 0L
        val path: String =
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) { // 优先保存到SD卡中
                Environment.getExternalStorageDirectory().absolutePath + File.separator + "local_playback"
            } else { // 如果SD卡不存在，就保存到本应用的目录下
                context?.filesDir?.absolutePath + File.separator + "local_playback"
            }

        //创建下载目录
        val file = File(path)
        if (!file.exists()) {
            if (file.mkdirs()) {
                L.d("创建本地回放存储路径成功")
            } else {
                L.e("创建本地回放存储路径失败")
                Toast.makeText(context, getString(R.string.fail_to_create_path), Toast.LENGTH_SHORT)
                    .show()
                return -1L
            }
        }

        //创建video文件
        val videoName = path + File.separator + playbackFile.file_name
        val videoFile = File(videoName)
        if (videoFile.exists()) {
            offset = if (playbackFile.file_size - videoFile.length() == 0L) {
                launch(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        getString(R.string.do_not_download_repeat),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                -2L // 已经下载过该文件，不需要重复下载
            } else {
                videoFile.length() // 断点续传的偏移量
            }
        }
        Toast.makeText(context, "path: ${videoName}", Toast.LENGTH_LONG).show()
        out = FileOutputStream(videoFile, true)
        return offset
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        surface?.let {
            this.surface = Surface(surface)
            player.setSurface(this.surface)
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {}
    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        return false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {}
    override fun fail(msg: String?, errorCode: Int) {}
    override fun success(response: String?, reqCode: Int) {
        response?.let {
            val responseObject = org.json.JSONObject(it).getJSONObject("Response")
            xp2pInfo = responseObject.getString("P2PInfo")
            startService()
        }
    }

    override fun commandRequest(id: String?, msg: String?) {}
    override fun avDataRecvHandle(id: String?, data: ByteArray?, len: Int) {
        out?.write(data)
        L.d("==== data len: $len")
    }

    override fun avDataCloseHandle(id: String?, msg: String?, errorCode: Int) {}
    override fun onDeviceMsgArrived(id: String?, data: ByteArray?, len: Int): String {
        return "app reply to device"
    }

    override fun xp2pEventNotify(id: String?, msg: String?, event: Int) {
        Log.e(TAG, "id=${id}, event=${event}")
        if (event == 1003) {
            if (isDownloading) {
                launch(Dispatchers.Main) {
                    Toast.makeText(context, getString(R.string.download_fail), Toast.LENGTH_SHORT)
                        .show()
                    toastDialog.dismiss()
                    isDownloading = false
                }
            } // 唤醒守护线程
            currentPostion = player.currentPosition / 1000
            L.e(TAG, "xp2pEventNotify currentPostion $currentPostion")
            launch(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    getString(R.string.error_with_code, id, msg),
                    Toast.LENGTH_SHORT
                ).show()
            }
            connected = false

        } else if (event == 1004 || event == 1005) {
            launch(Dispatchers.Main) {
                Toast.makeText(context, getString(R.string.connected, id), Toast.LENGTH_SHORT)
                    .show()
            }
            if (!connected && isShowing) {
                refreshDateTime(Date())
            }
            connected = true
        } else if (event == 1009) { //设备停止推流，下载结束
            launch(Dispatchers.Main) {
                out?.flush()
                toastDialog.dismiss()
                isDownloading = false
                ToastDialog(
                    context,
                    ToastDialog.Type.SUCCESS,
                    getString(R.string.download_complete),
                    2000
                ).show()
            }
        } else if (event == 1010) {
            Log.e(tag, "====event === 1010, 校验失败，info撞库防止串流： $msg")
        }
    }

    override fun onPause() {
        super.onPause()
        if (context is VideoPlaybackActivity) {
            if ((context as VideoPlaybackActivity).isFinishing) {
                finishAll()
                return
            }
        }
        player.let {
            if (currentPlayerState) {
                launch(Dispatchers.Main) {
                    binding.ivStart.performClick()
                }
            }
        }
    }

    private fun finishAll() {
        player?.release()
        out?.close()
        isDownloading = false
        connected = false
        if (recordingState) {
            player.stopRecord()
            context?.let {
                CommonUtils.refreshVideoList(it, filePath)
            }
        }

        App.data.accessInfo?.let { access ->
            devInfo?.let {
                XP2P.stopService("${access.productId}/${it.DeviceName}")
            }
        }
        XP2P.setCallback(null)
        cancel()
    }
}