package com.tencent.iot.explorer.link.demo.video.playback;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.iot.explorer.link.demo.R;
import com.tencent.iot.explorer.link.demo.databinding.ItemGridOptionBinding;

import java.util.List;

public class GridOptionsAdapter extends RecyclerView.Adapter<GridOptionsAdapter.ViewHolder> {
    private List<String> options;
    private int index = -1;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemGridOptionBinding binding;

        ViewHolder(ItemGridOptionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public View getView() {
            return binding.getRoot();
        }
    }

    public GridOptionsAdapter(List<String> options, int index) {
        this.options = options;
        this.index = index;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemGridOptionBinding binding = ItemGridOptionBinding.inflate(LayoutInflater.from(parent.getContext()));
        final ViewHolder holder = new ViewHolder(binding);

        holder.getView().setOnClickListener(new View.OnClickListener() {
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
        holder.binding.tvOption.setText(options.get(position));
        if (position == index) {
            holder.binding.tvOption.setBackgroundResource(R.drawable.background_blue_cell);
        } else {
            holder.binding.tvOption.setBackgroundResource(R.drawable.background_gray_cell);
        }
    }

    @Override
    public int getItemCount() {
        return options.size();
    }

    public interface OnItemClicked {
        void onItemClicked(int position, String option);
    }

    private OnItemClicked onItemClicked;

    public void setOnItemClicked(OnItemClicked onItemClicked) {
        this.onItemClicked = onItemClicked;
    }

}
