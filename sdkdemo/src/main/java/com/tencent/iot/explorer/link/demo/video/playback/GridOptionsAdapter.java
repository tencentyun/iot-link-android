package com.tencent.iot.explorer.link.demo.video.playback;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.tencent.iot.explorer.link.demo.R;

import java.util.List;

public class GridOptionsAdapter extends RecyclerView.Adapter<GridOptionsAdapter.ViewHolder> {
    private List<String> options;
    private int index = -1;

    class ViewHolder extends RecyclerView.ViewHolder {
        View layout;
        TextView option;

        ViewHolder(View view) {
            super(view);
            layout = view;
            option = view.findViewById(R.id.tv_option);
        }

    }

    public GridOptionsAdapter(List<String> options, int index) {
        this.options = options;
        this.index = index;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid_option, parent, false);
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
        holder.option.setText(options.get(position));
        if (position == index) {
            holder.option.setBackgroundResource(R.drawable.background_blue_cell);
        } else {
            holder.option.setBackgroundResource(R.drawable.background_gray_cell);
        }
    }

    @Override
    public int getItemCount() {
        return options.size();
    }

    public interface OnItemClicked {
        void onItemClicked(int postion, String option);
    }

    private OnItemClicked onItemClicked;

    public void setOnItemClicked(OnItemClicked onItemClicked) {
        this.onItemClicked = onItemClicked;
    }

}
