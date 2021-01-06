package com.tencent.iot.explorer.link.core.demo.view;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.tencent.iot.explorer.link.core.demo.R;

import java.util.List;

public class TimeAdapter extends RecyclerView.Adapter<TimeAdapter.ViewHolder> {

    List<TimeNum> list;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View moreView;
        TextView timeNumTxt;
        View startLine;
        View endLine;

        ViewHolder(View view) {
            super(view);
            moreView = view;
            timeNumTxt = view.findViewById(R.id.tv_time_txt);
            startLine = view.findViewById(R.id.v_line);
            endLine = view.findViewById(R.id.v_line_end);
        }

    }

    public TimeAdapter(List<TimeNum> list) {
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time_layout, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.timeNumTxt.setText("" + list.get(position).getTimeNum());
        if (position == list.size() - 1) {
            holder.endLine.setVisibility(View.VISIBLE);
        } else {
            holder.endLine.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
