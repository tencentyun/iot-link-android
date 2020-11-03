package com.tencent.iot.explorer.link.customview.dialog.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.customview.dialog.KeyBooleanValue;

import java.util.List;

public class DevModeOptionsAdapter extends RecyclerView.Adapter<DevModeOptionsAdapter.ViewHolder> {

    private List<KeyBooleanValue> options;
    private int index = -1;

    public void setIndex(int index) {
        this.index = index;
    }

    public int getCurrentIndex() {
        return index;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View layout;
        TextView optionName;
        ImageView select;
        ImageView selectTag;

        ViewHolder(View view) {
            super(view);
            layout = view;
            optionName = view.findViewById(R.id.tv_option_name);
            select = view.findViewById(R.id.iv_status);
            selectTag = view.findViewById(R.id.iv_selected);
        }
    }

    public DevModeOptionsAdapter(List<KeyBooleanValue> options) {
        this.options = options;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dev_mode_option, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if (onItemClicked != null) {
                    onItemClicked.onItemClicked(position, options.get(position));
                }
            }
        });
        return holder;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.optionName.setText(options.get(position).getValue());
        if (position == index) {
            holder.select.setImageResource(R.mipmap.dev_mode_sel);
            holder.selectTag.setVisibility(View.VISIBLE);
        } else {
            holder.select.setImageResource(R.mipmap.dev_mode_unsel);
            holder.selectTag.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return options.size();
    }

    public interface OnItemClicked {
        void onItemClicked(int postion, KeyBooleanValue option);
    }

    private OnItemClicked onItemClicked;

    public void setOnItemClicked(OnItemClicked onItemClicked) {
        this.onItemClicked = onItemClicked;
    }

}
