package com.tencent.iot.explorer.link.customview.dialog.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.T;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DayTypeOptionsAdapter extends RecyclerView.Adapter<DayTypeOptionsAdapter.ViewHolder> {

    List<String> options;
    Set<Integer> index = new HashSet<>();
    boolean singleType = true;

    public Set<Integer> getIndex() {
        return index;
    }

    public void setIndex(Set<Integer> index) {
        this.index = index;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View layout;
        TextView option;

        ViewHolder(View view) {
            super(view);
            layout = view;
            option = view.findViewById(R.id.tv_item_day);
        }

    }

    public DayTypeOptionsAdapter(List<String> options) {
        this(options, true);
    }

    public DayTypeOptionsAdapter(List<String> options, boolean singleType) {
        this.options = options;
        this.singleType = singleType;
        if (this.singleType) {
            index.add(0);
        } else {}   // 默认全部不选
    }

    public void setSelectOption(Set<Integer> options) {
        if (!singleType) {
            index.clear();
            Iterator<Integer> it = options.iterator();
            while (it.hasNext()) {
                index.add(it.next());
            }
            DayTypeOptionsAdapter.this.notifyDataSetChanged();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_option_day, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if (singleType) {
                    if (position != options.size() - 1) {
                        index.clear();
                        index.add(position);
                    }

                } else {
                    if (index.contains(position)) {
                        index.remove(position);
                    } else {
                        index.add(position);
                    }
                }
                DayTypeOptionsAdapter.this.notifyDataSetChanged();

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
        if (index.contains(position)) {
            holder.option.setBackgroundResource(R.drawable.background_bule_rounded_cell);
            holder.option.setTextColor(T.getContext().getResources().getColor(R.color.complete_progress));
        } else {
            holder.option.setBackgroundResource(R.drawable.background_grey_rounded_cell);
            holder.option.setTextColor(T.getContext().getResources().getColor(R.color.black));
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
