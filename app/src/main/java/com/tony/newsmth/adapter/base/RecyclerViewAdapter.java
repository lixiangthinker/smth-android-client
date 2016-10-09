package com.tony.newsmth.adapter.base;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by l00151177 on 2016/9/26.
 */
public abstract class RecyclerViewAdapter<T extends RecyclerViewAdapter.Item> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public interface Item {
        int TYPE_HEADER = 0;
        int TYPE_FOOTER = 1;
        int TYPE_CONTENT = 2;

        /**
         * 返回item类型，其值不能为0或者1；
         *
         * @return
         */
        int getType();
    }

    protected List<T> list = null;
    protected int headerViewRes = -1;
    protected int footerViewRes = -1;
    protected boolean hasHeader = false;
    protected boolean hasFooter = false;

    public RecyclerViewAdapter(List<T> list) {
        this.list = list;
    }

    public RecyclerViewAdapter(List<T> list, int headerViewRes) {
        this.list = list;
        setHeader(headerViewRes);
    }

    public RecyclerViewAdapter(List<T> list, int headerViewRes, int footerViewRes) {
        this.list = list;
        setHeader(headerViewRes);
        setFooter(footerViewRes);
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public boolean isHeader(int position) {
        return (hasHeader() && (position == 0));
    }

    public boolean isFooter(int position) {
        if (!hasFooter()) {
            return false;
        }
        if (hasHeader()) {
            return position == (list.size() + 1);
        } else {
            return (position == list.size());
        }
    }

    public int getHeaderView() {
        return headerViewRes;
    }

    public int getFooterViewRes() {
        return footerViewRes;
    }

    public void setHeader(int resId) {
        this.headerViewRes = resId;
        if (resId >= 0) {
            this.hasHeader = true;
            if (!hasHeader()) {
                notifyItemInserted(0);
            } else {
                notifyDataSetChanged();
            }
        } else {
            this.hasHeader = false;
            if (hasHeader()) {
                notifyItemRemoved(0);
            }
        }
    }

    public void setFooter(int resId) {
        this.footerViewRes = resId;
        if (resId >= 0) {
            this.hasFooter = true;
            if (hasFooter()) {
                notifyDataSetChanged();
            } else {
                if (hasHeader()) {
                    notifyItemInserted(list.size() + 1);
                } else {
                    notifyItemInserted(list.size());
                }
            }
        } else {
            this.hasFooter = false;
            if (hasFooter()) {
                if (hasHeader()) {
                    notifyItemRemoved(list.size() + 1);
                } else {
                    notifyItemRemoved(list.size());
                }
            }
        }
    }

    public boolean hasHeader() {
        return hasHeader;
    }

    public boolean hasFooter() {
        return hasFooter;
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class FooterViewHolder extends RecyclerView.ViewHolder {
        public FooterViewHolder(View itemView) {
            super(itemView);
        }
    }

    @Override
    public int getItemCount() {
        int result = list.size();
        if (hasHeader()) {
            result++;
        }
        if (hasFooter()) {
            result++;
        }
        return result;
    }

    @Override
    public int getItemViewType(int position) {
        if (hasHeader() && position == 0) {
            return Item.TYPE_HEADER;
        }
        if (hasFooter()) {
            int footerPosition = hasHeader() ? (list.size() + 1) : list.size();
            if (position == footerPosition) {
                return Item.TYPE_FOOTER;
            }
        }
        return list.get(position).getType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater lf = LayoutInflater.from(parent.getContext());
        View view;
        switch (viewType) {
            case Item.TYPE_HEADER:
                view = lf.inflate(this.headerViewRes, parent, false);
                return new HeaderViewHolder(view);
            case Item.TYPE_FOOTER:
                view = lf.inflate(this.footerViewRes, parent, false);
                return new FooterViewHolder(view);
            default:
                return onCreateHolder(parent, viewType);
        }
    }
    public abstract RecyclerView.ViewHolder onCreateHolder(ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            View headerView = headerHolder.itemView;
            onBindHeaderView(headerView);
        } else if (holder instanceof FooterViewHolder) {
            FooterViewHolder footerHolder = (FooterViewHolder) holder;
            View footerView = footerHolder.itemView;
            onBindFooterView(footerView);
        } else {
            T i = getItemByPosition(position);
            onBindItemView(holder, i);
        }
    }

    private T getItemByPosition(int position) {
        int contentPosition = position;
        if (hasHeader()) {
            contentPosition = position - 1;
        }
        return list.get(contentPosition);
    }

    protected abstract void onBindHeaderView(View headerView);
    protected abstract void onBindFooterView(View footerView);
    protected abstract void onBindItemView(RecyclerView.ViewHolder holder, T item);
    public interface OnTopicDetailClickListener {
        void onItemClick(int position, int viewId);
        boolean onItemLongClick(int position, int viewId);
    }
    protected OnTopicDetailClickListener onTopicDetailClickListener = null;

    public void setOnTopicDetailClickListener(OnTopicDetailClickListener onTopicDetailClickListener) {
        this.onTopicDetailClickListener = onTopicDetailClickListener;
    }

    protected class ClickableViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        public ClickableViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void onClick(View v) {
            if (onTopicDetailClickListener != null) {
                onTopicDetailClickListener.onItemClick(getAdapterPosition(), v.getId());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (onTopicDetailClickListener != null) {
                onTopicDetailClickListener.onItemLongClick(getAdapterPosition(), v.getId());
            }
            return true;
        }
    }
}