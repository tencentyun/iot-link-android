package com.tencent.iot.explorer.link.demo.video.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tencent.iot.explorer.link.demo.R;
import com.tencent.iot.explorer.link.demo.databinding.ItemOptionBinding;

import java.util.List;

public class ListOptionsAdapter extends RecyclerView.Adapter<ListOptionsAdapter.ViewHolder> {
    List<String> options;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemOptionBinding binding;

        ViewHolder(ItemOptionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public View getView() {
            return binding.getRoot();
        }
    }

    public ListOptionsAdapter(List<String> options) {
        this.options = options;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemOptionBinding binding = ItemOptionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
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
        if (position == options.size() - 1) {
            holder.binding.vLine.setVisibility(View.GONE);
        } else {
            holder.binding.vLine.setVisibility(View.VISIBLE);
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
