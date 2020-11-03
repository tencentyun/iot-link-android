package com.tencent.iot.explorer.link.kitlink.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.squareup.picasso.Picasso;
import com.tencent.iot.explorer.link.App;
import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.T;
import com.tencent.iot.explorer.link.kitlink.entity.Action;
import com.tencent.iot.explorer.link.kitlink.entity.Automation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class IntelligenceAdapter extends RecyclerView.Adapter<IntelligenceAdapter.ViewHolder> {

    private List<Automation> list = new LinkedList<>();
    private List<Automation> automicList = new ArrayList<>();
    private List<Automation> manualList = new ArrayList<>();

    public List<Automation> getList() {
        return list;
    }

    public List<Automation> getAutomicList() {
        return automicList;
    }

    public List<Automation> getManualList() {
        return manualList;
    }

    // 每修改一个元素都要进行一次数组的全修改，效率不高后续可以提高，依赖 linkedlist 的特性做插入修改
    public void addAutomic(Automation automic) {
        this.automicList.add(automic);
        refreashList();
    }

    public void addManual(Automation manual) {
        this.manualList.add(manual);
        refreashList();
    }

    public void setAutomic(int pos, Automation automic) {
        this.automicList.set(pos, automic);
        refreashList();
    }

    public void setManual(int pos, Automation manual) {
        this.manualList.set(pos, manual);
        refreashList();
    }

    public void clearAutomicList() {
        this.automicList.clear();
        refreashList();
    }

    public void clearManualList() {
        this.manualList.clear();
        refreashList();
    }

    public void setManualList(List<Automation> manualList) {
        this.manualList = manualList;
        refreashList();
    }

    public void setAutomicList(List<Automation> automicList) {
        this.automicList = automicList;
        refreashList();
    }

    public void refreashList() {
        list.clear();
        list.addAll(manualList);
        list.addAll(automicList);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View layoutView;
        TextView titleName;
        TextView intelligenceName;
        ImageView btn;
        ImageView background;
        Switch switchBtn;
        TextView desc;

        ViewHolder(View view) {
            super(view);
            layoutView = view;
            titleName = view.findViewById(R.id.tv_tip_title);
            intelligenceName = view.findViewById(R.id.tv_intelligence_name);
            btn = view.findViewById(R.id.iv_open_btn);
            background = view.findViewById(R.id.iv_background);
            switchBtn = view.findViewById(R.id.iv_switch_btn);
            desc = view.findViewById(R.id.tv_content_tip);
        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_intelligence, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        holder.layoutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view1) {
                int position = holder.getAdapterPosition();
                if (onItemClicked != null) {
                    onItemClicked.onItemClicked(list.get(position));
                }
            }
        });
        return holder;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (list.get(position).getType() == 0) {
            holder.btn.setVisibility(View.VISIBLE);
            holder.switchBtn.setVisibility(View.GONE);
        } else {
            holder.btn.setVisibility(View.GONE);
            holder.switchBtn.setVisibility(View.VISIBLE);
        }

        if (list.get(position).getType() == 0) {
            if (position == 0) { // 手动的标题只会出现在第一行
                holder.titleName.setText(R.string.manual);
                holder.titleName.setVisibility(View.VISIBLE);
            } else { // 手动标签不是第一行就不显示
                holder.titleName.setVisibility(View.GONE);
            }

        } else if (list.get(position).getType() == 1) {
            if (position == 0) { // 自动的标签可能出现在第一行
                holder.titleName.setText(R.string.automic);
                holder.titleName.setVisibility(View.VISIBLE);
            } else if (list.get(position - 1).getType() == 0) { // 前一个元素不是自动类型的时候，也显示自动的标签
                holder.titleName.setText(R.string.automic);
                holder.titleName.setVisibility(View.VISIBLE);
            } else {
                holder.titleName.setVisibility(View.GONE);
            }
        }

        if (!TextUtils.isEmpty(list.get(position).getIcon())) {
            Picasso.get().load(list.get(position).getIcon()).into(holder.background);
        }
        holder.intelligenceName.setText(list.get(position).getName());

        List<Action> allActions = JSON.parseArray(list.get(position).getActions().toJSONString(), Action.class);
        if (allActions != null && allActions.size() > 0) {
            Set<String> caculateTotal = new HashSet<>();
            for (int i = 0; i < allActions.size(); i++) {
                if (!TextUtils.isEmpty(allActions.get(i).getProductId()) && !TextUtils.isEmpty(allActions.get(i).getDeviceName())) {
                    caculateTotal.add(allActions.get(i).getProductId() + "/" + allActions.get(i).getDeviceName());
                }
            }

            holder.desc.setText(T.getContext().getString(R.string.num_devices, "" + caculateTotal.size()));
        } else {
            holder.desc.setText(T.getContext().getString(R.string.num_devices, "" + 0));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnItemClicked {
        void onItemClicked(Automation automation);
    }

    private OnItemClicked onItemClicked;

    public void setOnItemClicked(OnItemClicked onItemClicked) {
        this.onItemClicked = onItemClicked;
    }

}
