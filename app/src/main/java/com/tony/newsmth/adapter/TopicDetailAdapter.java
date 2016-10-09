package com.tony.newsmth.adapter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tony.newsmth.R;
import com.tony.newsmth.model.ContentSegment;
import com.tony.newsmth.model.TopicDetail;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by l00151177 on 2016/9/23.
 */
public class TopicDetailAdapter extends RecyclerView.Adapter {
    private static final String TAG = "TopicDetailAdapter";
    private OnTopicDetailClickListener onTopicDetailClickListener = null;
    private List<TopicDetail> mTopicDetailList = new ArrayList<>();

    public void setOnTopicDetailClickListener(OnTopicDetailClickListener onTopicDetailClickListener) {
        this.onTopicDetailClickListener = onTopicDetailClickListener;
    }

    public TopicDetailAdapter(List<TopicDetail> topicDetail) {
        mTopicDetailList = topicDetail;
    }

    public interface OnTopicDetailClickListener {
        void onItemClick(int position, int viewId);

        boolean onItemLongClick(int position, int viewId);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_topic_detail_item, parent, false);
        return new TopicDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TopicDetailViewHolder) {
            TopicDetailViewHolder h = (TopicDetailViewHolder) holder;
            TopicDetail td = mTopicDetailList.get(position);
            if (td == null) {
                Log.e(TAG, "could not get TopicDetail");
                return;
            }
            h.tvAuthor.setText(td.getAuthor());
            h.tvTimeStamp.setText(td.getFormatedDate());
            h.tvFloor.setText(td.getPosition());
            setContentText(h, td);
        }
    }

    private void setContentText(TopicDetailViewHolder h, TopicDetail td) {
        List<ContentSegment> contentSegments = td.getContentSegments();
        if (contentSegments.size() > 0) {
            h.tvContent.setText(contentSegments.get(0).getSpanned());
        }
    }

    @Override
    public int getItemCount() {
        return (mTopicDetailList == null) ? 0 : mTopicDetailList.size();
    }


    class TopicDetailViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView tvAuthor = null;
        TextView tvFloor = null;
        TextView tvTimeStamp = null;
        TextView tvContent = null;
        CardView cvCard = null;
        LinearLayout llContentHolder = null;

        public TopicDetailViewHolder(View itemView) {
            super(itemView);
            tvAuthor = (TextView) itemView.findViewById(R.id.tv_topic_detail_author);
            tvFloor = (TextView) itemView.findViewById(R.id.tv_topic_detail_floor);
            tvTimeStamp = (TextView) itemView.findViewById(R.id.tv_topic_detail_time);
            tvContent = (TextView) itemView.findViewById(R.id.tv_topic_detail_content);
            cvCard = (CardView) itemView.findViewById(R.id.cv_topic_detail_item);
            llContentHolder = (LinearLayout) itemView.findViewById(R.id.ll_topic_detaild_content_holder);
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick");
            if (onTopicDetailClickListener != null) {
                onTopicDetailClickListener.onItemClick(getAdapterPosition(), v.getId());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            Log.d(TAG, "onLongClick");
            if (onTopicDetailClickListener != null) {
                onTopicDetailClickListener.onItemLongClick(getAdapterPosition(), v.getId());
            }
            return true;
        }
    }
}
