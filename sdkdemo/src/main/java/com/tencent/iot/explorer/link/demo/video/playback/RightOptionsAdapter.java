package com.tencent.iot.explorer.link.demo.video.playback;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.iot.explorer.link.demo.R;
import com.tencent.iot.explorer.link.demo.databinding.ItemRightOptionBinding;

import java.util.List;

public class RightOptionsAdapter extends RecyclerView.Adapter<RightOptionsAdapter.ViewHolder> {
    private List<String> options;
    private int index = -1;
    private Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemRightOptionBinding binding;

        ViewHolder(ItemRightOptionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public View getView() {
            return binding.getRoot();
        }
    }

    public RightOptionsAdapter(Context context, List<String> options, int index) {
        this.options = options;
        this.index = index;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemRightOptionBinding binding = ItemRightOptionBinding.inflate(LayoutInflater.from(parent.getContext()));
        final ViewHolder holder = new ViewHolder(binding);

        holder.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                index = position;
                if (onItemClicked != null) {
                    onItemClicked.onItemClicked(position, options.get(position));
                }
                notifyDataSetChanged();
            }
        });
        return holder;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.binding.tvOption.setText(options.get(position));
        if (position == index) {
            holder.binding.tvOption.setTextColor(context.getResources().getColor(R.color.green_0ABF5B));
        } else {
            holder.binding.tvOption.setTextColor(context.getResources().getColor(R.color.white));
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
