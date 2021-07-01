package com.tencent.iot.explorer.link.core.demo.video.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.tencent.iot.explorer.link.core.demo.R;
import java.util.List;

public class ListOptionsAdapter extends RecyclerView.Adapter<ListOptionsAdapter.ViewHolder> {
    List<String> options;

    class ViewHolder extends RecyclerView.ViewHolder {
        View layout;
        View line;
        TextView option;

        ViewHolder(View view) {
            super(view);
            layout = view;
            option = view.findViewById(R.id.tv_option);
            line = view.findViewById(R.id.v_line);
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
        if (position == options.size() - 1) {
            holder.line.setVisibility(View.GONE);
        } else {
            holder.line.setVisibility(View.VISIBLE);
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
