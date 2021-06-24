package com.tencent.iot.explorer.link.kitlink.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.holder.FeedbackViewHolder
import com.tencent.iot.explorer.link.kitlink.popup.CameraPopupWindow
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.mvp.model.UploadModel
import com.tencent.iot.explorer.link.mvp.view.UploadView
import com.tencent.cos.xml.exception.CosXmlClientException
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import com.tencent.iot.explorer.link.kitlink.util.picture.imp.ImageSelectorUtils
import kotlinx.android.synthetic.main.activity_feedback.*
import kotlinx.android.synthetic.main.menu_back_layout.*
import java.lang.StringBuilder

class FeedbackActivity : BaseActivity(), UploadView, CRecyclerView.RecyclerItemView, MyCallback {

    private var isCommit = false
    private var popupWindow: CameraPopupWindow? = null
    private lateinit var uploadModel: UploadModel
    private val successList = arrayListOf<PathUrlEntity>()
    private val maxSize = 4
    private val emptyEntity = PathUrlEntity("", "")
    private var mPosition = 0

    private var permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun getContentView(): Int {
        return R.layout.activity_feedback
    }


    override fun initView() {
        successList.add(emptyEntity)
        uploadModel = UploadModel(this)
        tv_title.text = getString(R.string.feedback)
        crv_feedback.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        crv_feedback.setList(successList)
        crv_feedback.addRecyclerItemView(this)
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_feedback_commit.setOnClickListener {
            commit()
        }
        et_feedback_problem.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (it.length <= 200) {
                        tv_feedback_count.text = "${it.length}/200"
                        return
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })
    }

    override fun getViewType(position: Int): Int {
        return 0
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): CRecyclerView.CViewHolder<*> {
        return FeedbackViewHolder(
            LayoutInflater.from(this).inflate(R.layout.item_feekback, parent, false)
        )
    }

    override fun doAction(
        viewHolder: CRecyclerView.CViewHolder<*>,
        clickView: View,
        position: Int
    ) {
        if (clickView is ImageView) {
            deleteFile(position)
            return
        }
        if (successList.size > 4) return
        mPosition = position
        if (checkPermissions(permissions))
            showCameraPopup(mPosition)
        else requestPermission(permissions)

    }

    private fun showCameraPopup(position: Int) {
        if (position < successList.size - 1) return
        if (getCount() <= 0) return
        if (popupWindow == null) {
            popupWindow = CameraPopupWindow(this)
        }
        popupWindow?.setBg(feedback_popup_bg)
        popupWindow?.show(feedback, getCount())
    }

    override fun permissionAllGranted() {
        showCameraPopup(mPosition)
    }

    private fun commit() {
        if (isCommit) return
        val problem = et_feedback_problem.text.trim().toString()
        var phone = et_feedback_phone.text.trim().toString()
        if (TextUtils.isEmpty(phone))
            phone = "13800138000"
        if (TextUtils.isEmpty(problem)) {
            T.show(getString(R.string.add_question)) //请填写问题描述
            return
        }
        if (problem.trim().length < 10) {
            T.show(getString(R.string.question_desc_less_20_char)) //请填写不少于10个字的问题描述
            return
        }

        HttpRequest.instance.feedback(problem, phone, getLogUrl(), this)
        isCommit = true
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        isCommit = false
        if (response.isSuccess()) {
            T.show(getString(R.string.commit_success)) //提交成功
            et_feedback_problem.setText("")
            et_feedback_phone.setText("")
            successList.clear()
            successList.add(emptyEntity)
            successAll()
        } else {
            T.show(response.msg)
        }
    }

    /**
     * 上传图片第一步：获取签名
     */
    private fun upload(context: Context, srcPath: List<String>) {
        uploadModel.uploadMultiFile(context, srcPath)
    }

    override fun successAll() {
        runOnUiThread {
            crv_feedback.notifyDataChanged()
        }
    }

    override fun uploadSuccess(loadPath: String, fileUrl: String, all: Boolean) {
        successList.add(successList.size - 1, PathUrlEntity(loadPath, fileUrl))
        if (successList.size > maxSize) {
            for (i in maxSize until successList.size) {
                successList.removeAt(i)
            }
        }
        runOnUiThread {
            crv_feedback.notifyDataChanged()
        }
    }


    override fun uploadFail(loadPath: String, exception: CosXmlClientException?) {
        L.e("上传失败：$loadPath")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            val list = ImageSelectorUtils.getImageSelectorList(requestCode, resultCode, data)
            if (list != null && list.size > 0) {
                upload(this, list)
                list.forEach {
                    L.e("图片地址:$it")
                }
            }
        }
    }

    private fun getLogUrl(): String {
        val sb = StringBuilder()
        successList.forEachIndexed { index, pathUrlEntity ->
            sb.append(pathUrlEntity.url)
            if (index < successList.size - 1)
                sb.append(",")
        }
        return sb.toString()
    }

    private fun deleteFile(position: Int) {
        successList.removeAt(position)
        if (!successList.contains(emptyEntity)) {
            successList.add(emptyEntity)
        }
        crv_feedback.notifyDataChanged()
    }

    private fun getCount(): Int {
        val c = maxSize + 1 - successList.size
        return if (c < 0) 0 else c
    }

    inner class PathUrlEntity(path: String, url: String) {
        var path = path
        var url = url
    }

}
