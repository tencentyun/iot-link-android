package com.tencent.iot.explorer.link.customview.dialog.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.tencent.iot.explorer.link.R;

import java.util.List;

public class ListOptionsAdapter extends RecyclerView.Adapter<ListOptionsAdapter.ViewHolder> {

    List<String> options;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View layout;
        TextView option;

        ViewHolder(View view) {
            super(view);
            layout = view;
            option = view.findViewById(R.id.tv_option);
        }

    }

    public ListOptionsAdapter(List<String> options) {
        this.options = options;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_option, parent, false);
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
