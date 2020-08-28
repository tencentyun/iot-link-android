package com.tencent.iot.explorer.link.customview.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.iot.explorer.link.R;

import java.util.List;

public class WifiListAdapter extends BaseAdapter {

    private List<WifiInfo> allWifi;
    private Context context;

    public WifiListAdapter(Context context, List<WifiInfo> allWifi) {
        this.context = context;
        this.allWifi = allWifi;
    }

    @Override
    public int getCount() {
        if (allWifi == null) {
            return 0;
        }
        return allWifi.size();
    }

    @Override
    public Object getItem(int position) {
        return allWifi.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            holder = new Holder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_wifi_info, null);
            holder.connected = convertView.findViewById(R.id.iv_choosen);
            holder.wifi = convertView.findViewById(R.id.iv_wifi_flag);
            holder.withoutPwd = convertView.findViewById(R.id.iv_opened);
            holder.ssid = convertView.findViewById(R.id.tv_wifi_name);
            convertView.setTag(holder);
        } else {
            holder = (Holder)convertView.getTag();
        }

        holder.ssid.setText(allWifi.get(position).getSsid());
        holder.ssid.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (onWifiClicked != null) {
                    onWifiClicked.OnWifiClicked(allWifi.get(position));
                }
            }
        });

        if (allWifi.get(position).getConnected()) {
            holder.connected.setVisibility(View.VISIBLE);
        } else {
            holder.connected.setVisibility(View.INVISIBLE);
        }
        if (allWifi.get(position).getWithoutPwd()) {
            holder.withoutPwd.setVisibility(View.VISIBLE);
        } else {
            holder.withoutPwd.setVisibility(View.INVISIBLE);
        }
        holder.wifi.setVisibility(View.VISIBLE);

        return convertView;
    }

    public interface OnWifiClicked {
        void OnWifiClicked(WifiInfo item);
    }

    public void setOnWifiClicked(OnWifiClicked onWifiClicked) {
        this.onWifiClicked = onWifiClicked;
    }

    private OnWifiClicked onWifiClicked;

    class Holder {
        ImageView connected;
        TextView ssid;
        ImageView withoutPwd;
        ImageView wifi;
    }
}
