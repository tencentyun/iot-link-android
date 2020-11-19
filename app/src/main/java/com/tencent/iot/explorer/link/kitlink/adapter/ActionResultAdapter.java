package com.tencent.iot.explorer.link.kitlink.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.kitlink.entity.ActionResult;

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

//        holder.name.setText("XXXXXX");
        holder.desc.setText(allInfo.get(position).getResult());
        return convertView;
    }

    class Holder {
        TextView name;
        TextView desc;
    }
}
