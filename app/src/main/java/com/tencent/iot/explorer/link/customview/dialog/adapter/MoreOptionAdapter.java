package com.tencent.iot.explorer.link.customview.dialog.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.customview.dialog.entity.DevOption;

import java.util.List;

public class MoreOptionAdapter extends RecyclerView.Adapter<MoreOptionAdapter.ViewHolder> {

    List<DevOption> options;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View layout;
        TextView optionTv;
        ImageView optionIv;
        ImageView optionBackground;
        TextView optionValue;

        ViewHolder(View view) {
            super(view);
            layout = view;
            optionTv = view.findViewById(R.id.tv_option);
            optionIv = view.findViewById(R.id.iv_option);
            optionValue = view.findViewById(R.id.tv_option_value);
            optionBackground = view.findViewById(R.id.iv_option_background);

        }

    }

    public MoreOptionAdapter(List<DevOption> options) {
        this.options = options;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_more_dev_option, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if (onItemClicked != null) {
                    onItemClicked.onItemClicked(position);
                }
            }
        });
        return holder;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.optionTv.setText(options.get(position).getOptionName());

        if (TextUtils.isEmpty(options.get(position).getRes())) {
            holder.optionIv.setVisibility(View.INVISIBLE);
            Picasso.get().load(R.mipmap.device_placeholder).into(holder.optionIv);
        } else {
            holder.optionIv.setVisibility(View.VISIBLE);
            Picasso.get().load(options.get(position).getRes()).into(holder.optionIv);
        }

        holder.optionValue.setText(options.get(position).getValue());
    }

    @Override
    public int getItemCount() {
        return options.size();
    }

    public interface OnItemClicked {
        void onItemClicked(int postion);
    }

    private OnItemClicked onItemClicked;

    public void setOnItemClicked(OnItemClicked onItemClicked) {
        this.onItemClicked = onItemClicked;
    }

}
