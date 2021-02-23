package com.tencent.iot.explorer.link.customview.dialog.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.customview.dialog.entity.AddItem;

import java.util.List;

public class AddOptionsAdapter extends RecyclerView.Adapter<AddOptionsAdapter.ViewHolder> {

    List<AddItem> options;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View layout;
        TextView optionTv;
        ImageView optionIv;

        ViewHolder(View view) {
            super(view);
            layout = view;
            optionTv = view.findViewById(R.id.tv_add_option);
            optionIv = view.findViewById(R.id.iv_add_option);
        }

    }

    public AddOptionsAdapter(List<AddItem> options) {
        this.options = options;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_option, parent, false);
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
        holder.optionTv.setText(options.get(position).getOption());
        holder.optionIv.setImageResource(options.get(position).getResId());
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
