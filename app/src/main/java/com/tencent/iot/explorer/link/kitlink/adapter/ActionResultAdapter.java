package com.tencent.iot.explorer.link.kitlink.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tencent.iot.explorer.link.App;
import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity;
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse;
import com.tencent.iot.explorer.link.kitlink.entity.ActionResult;
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest;
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class ActionResultAdapter extends BaseAdapter {
    private Context context;
    private List<ActionResult> allInfo;

    public ActionResultAdapter(Context context, List<ActionResult> allInfo) {
        this.context = context;
        this.allInfo = allInfo;
    }

    @Override
    public int getCount() {
        if (allInfo == null) {
            return 0;
        }
        return allInfo.size();
    }

    @Override
    public Object getItem(int position) {
        return allInfo.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder = null;
        if (convertView == null) {
            holder = new Holder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_action_result, null);
            holder.name = convertView.findViewById(R.id.tv_name);
            holder.desc = convertView.findViewById(R.id.tv_desc);
            convertView.setTag(holder);
        } else {
            holder = (Holder)convertView.getTag();
        }

        // 只有第一次加载的时候，或者没有别名的时候，才会触发请求别名的动作，否则直接加载别名，避免过度的网络请求
        if (TextUtils.isEmpty(allInfo.get(position).getDeviceAliasName())) {
            String[] devInfo = allInfo.get(position).getDeviceId().split("/");
            if (devInfo.length == 2) {
                final Holder tmpHolder = holder;
                HttpRequest.Companion.getInstance().getDeviceInfo(devInfo[0], devInfo[1],
                        App.Companion.getData().getCurrentFamily().getFamilyId(), new MyCallback() {
                            @Override
                            public void fail(@Nullable String msg, int reqCode) { }

                            @Override
                            public void success(@NotNull BaseResponse response, int reqCode) {
                                if (response.isSuccess()) {
                                    JSONObject json = JSON.parseObject(response.getData().toString());
                                    if (json == null) return;

                                    String data = json.getString("Data");
                                    if (TextUtils.isEmpty(data)) return;

                                    DeviceEntity deviceEntity = JSON.parseObject(data, DeviceEntity.class);
                                    Log.e("XXX", "deviceEntity json " + JSON.toJSONString(deviceEntity));
                                    tmpHolder.name.setText(deviceEntity.getAlias());
                                    allInfo.get(position).setDeviceAliasName(deviceEntity.getAlias());
                                }
                            }
                        });
            }
        } else {
            holder.name.setText(allInfo.get(position).getDeviceAliasName());
        }

        holder.desc.setText(allInfo.get(position).getResult());
        return convertView;
    }

    class Holder {
        TextView name;
        TextView desc;
    }
}
