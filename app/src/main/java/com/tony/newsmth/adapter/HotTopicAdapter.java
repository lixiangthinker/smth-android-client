package com.tony.newsmth.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tony.newsmth.R;
import com.tony.newsmth.model.Topic;

import java.util.List;

/**
 * Created by l00151177 on 2016/9/23.
 */
public class HotTopicAdapter extends RecyclerView.Adapter {
    private static final String TAG = "HotTopicAdapter";
    private Context mContext = null;
    private List<Topic> mListTopic = null;

    public HotTopicAdapter(Context context, List<Topic> list) {
        mContext = context;
        mListTopic = list;
    }

    public enum TopicViewType {
        CATEGORY, CONTENT
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder viewType = " + viewType);
        if (viewType == TopicViewType.CATEGORY.ordinal()) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_topic_category, parent, false);
            return new HotTopicCategoryViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_topic_item, parent, false);
            return new HotTopicViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder position: " + position);
        Topic topic = mListTopic.get(position);
        if (holder instanceof HotTopicViewHolder) {
            HotTopicViewHolder htHolder = (HotTopicViewHolder) holder;
            htHolder.position = position;
            htHolder.tvTitle.setText(topic.getTitle());
            htHolder.tvBoardName.setText(topic.getBoardChsName());
            htHolder.tvViewCount.setText(topic.getTotalPostNoAsStr());
        } else if (holder instanceof HotTopicCategoryViewHolder){
            HotTopicCategoryViewHolder htHolder = (HotTopicCategoryViewHolder) holder;
            htHolder.position = position;
            htHolder.tvCategory.setText(topic.getCategory());
        }
    }

    @Override
    public int getItemCount() {
        return mListTopic == null ? 0 : mListTopic.size();
    }

    public interface OnRecyclerViewListener {
        void onItemClick(int position);

        boolean onItemLongClick(int position);
    }

    @Override
    public int getItemViewType(int position) {
        if (mListTopic == null || mListTopic.size() == 0) {
            return super.getItemViewType(position);
        }
        Topic tp = mListTopic.get(position);
        if (tp == null) {
            return super.getItemViewType(position);
        }
        if (tp.isCategory) {
            return TopicViewType.CATEGORY.ordinal();
        } else {
            return TopicViewType.CONTENT.ordinal();
        }
    }

    private OnRecyclerViewListener onRecyclerViewListener;

    /**
     * when item is clicked by user, need to show detail content.
     *
     */
    public void setOnRecyclerViewListener(OnRecyclerViewListener l) {
        onRecyclerViewListener = l;
    }

    class HotTopicViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public TextView tvTitle = null;
        public TextView tvBoardName = null;
        public TextView tvViewCount = null;
        public CardView cvCard = null;
        public LinearLayout llTopicContent = null;
        public int position = -1;

        public HotTopicViewHolder(View itemView) {
            super(itemView);
            cvCard = (CardView) itemView.findViewById(R.id.cv_topic_item);
            tvTitle = (TextView) itemView.findViewById(R.id.tv_hot_topic_title);
            tvBoardName = (TextView) itemView.findViewById(R.id.tv_hot_topic_board);
            tvViewCount = (TextView) itemView.findViewById(R.id.tv_hot_topic_viewed_count);
            llTopicContent = (LinearLayout) itemView.findViewById(R.id.ll_hot_topic_content);
            cvCard.setOnClickListener(this);
            cvCard.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (onRecyclerViewListener != null) {
                onRecyclerViewListener.onItemClick(position);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            boolean result = false;
            if (onRecyclerViewListener != null) {
                result = onRecyclerViewListener.onItemLongClick(position);
            }
            return result;
        }
    }

    private class HotTopicCategoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public int position = -1;
        public TextView tvCategory = null;
        public CardView cvCard = null;
        public HotTopicCategoryViewHolder(View itemView) {
            super(itemView);
            cvCard = (CardView) itemView.findViewById(R.id.cv_topic_item);
            tvCategory = (TextView) itemView.findViewById(R.id.tv_hot_topic_category);
            cvCard.setOnClickListener(this);
            cvCard.setOnLongClickListener(this);
        }
        @Override
        public void onClick(View v) {
            if (onRecyclerViewListener != null) {
                onRecyclerViewListener.onItemClick(position);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            boolean result = false;
            if (onRecyclerViewListener != null) {
                result = onRecyclerViewListener.onItemLongClick(position);
            }
            return result;
        }
    }
}
