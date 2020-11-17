package com.tencent.iot.explorer.link.kitlink.adapter;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.chauthai.swipereveallayout.SwipeRevealLayout;
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

    public int indexOpen2Keep = -1;

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

    public void removeItem(int pos) {
        list.remove(pos);
    }

    public void setItemStatus(int pos, int status) {
        list.get(pos).setStatus(status);
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
        SwipeRevealLayout swipeRevealLayout;
        ImageView deleteBtn;
        ConstraintLayout contentLayout;

        ViewHolder(View view) {
            super(view);
            layoutView = view;
            titleName = view.findViewById(R.id.tv_tip_title);
            intelligenceName = view.findViewById(R.id.tv_intelligence_name);
            btn = view.findViewById(R.id.iv_open_btn);
            background = view.findViewById(R.id.iv_background);
            switchBtn = view.findViewById(R.id.iv_switch_btn);
            desc = view.findViewById(R.id.tv_content_tip);
            swipeRevealLayout = view.findViewById(R.id.swipeRevealLayout);
            deleteBtn = view.findViewById(R.id.iv_delete);
            contentLayout = view.findViewById(R.id.content_layout);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_intelligence, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        holder.contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if (onItemClicked != null) {
                    onItemClicked.onItemClicked(list.get(position));
                }
            }
        });

        holder.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if (onItemClicked != null) {
                    onItemClicked.onRunTaskClicked(list.get(position));
                }
            }
        });

        holder.switchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isPressed()) {
                    int position = holder.getAdapterPosition();
                    if (onItemClicked != null) {
                        onItemClicked.onSwitchStatus(position, isChecked, list.get(position));
                    }
                }
            }
        });

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if (onItemClicked != null) {
                    onItemClicked.onItemDeleteClicked(position, list.get(position));
                    indexOpen2Keep = -1;
                    IntelligenceAdapter.this.notifyDataSetChanged();
                }
            }
        });

        holder.swipeRevealLayout.setSwipeListener(new SwipeRevealLayout.SwipeListener() {
            @Override
            public void onClosed(SwipeRevealLayout view) {
                int position = holder.getAdapterPosition();
                if (position == indexOpen2Keep) {
                    indexOpen2Keep = -1;
                    IntelligenceAdapter.this.notifyDataSetChanged();
                }
            }

            @Override
            public void onOpened(SwipeRevealLayout view) {
                int position = holder.getAdapterPosition();
                indexOpen2Keep = position;
                IntelligenceAdapter.this.notifyDataSetChanged();
            }

            @Override
            public void onSlide(SwipeRevealLayout view, float slideOffset) { }
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

            if (list.get(position).getStatus() == 0) {
                holder.switchBtn.setChecked(true);
            } else {
                holder.switchBtn.setChecked(false);
            }
        }

        if (!TextUtils.isEmpty(list.get(position).getIcon())) {
            Picasso.get().load(list.get(position).getIcon()).into(holder.background);
        }
        holder.intelligenceName.setText(list.get(position).getName());

        if (list.get(position).getType() == 0) {
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
        } else {  // 自动需要重新发起网络请求才能获取到设备内容
            holder.desc.setText("");
        }

        if (position != indexOpen2Keep) {
            holder.swipeRevealLayout.close(false);
        } else {
            holder.swipeRevealLayout.open(false);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnItemClicked {
        void onItemClicked(Automation automation);
        void onRunTaskClicked(Automation automation);
        void onSwitchStatus(int postion, boolean isChecked, Automation automation);
        void onItemDeleteClicked(int postion, Automation automation);
    }

    private OnItemClicked onItemClicked;

    public void setOnItemClicked(OnItemClicked onItemClicked) {
        this.onItemClicked = onItemClicked;
    }

}
