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
import com.tony.newsmth.adapter.base.RecyclerViewAdapter;
import com.tony.newsmth.model.ContentSegment;
import com.tony.newsmth.model.TopicDetail;

import java.util.List;

/**
 * Created by l00151177 on 2016/9/26.
 */
public class TopicDetailFooterAdapter extends RecyclerViewAdapter {
    private static final String TAG = "TopicDetailAdapter";

    public TopicDetailFooterAdapter(List list) {
        super(list);
    }

    @Override
    public RecyclerView.ViewHolder onCreateHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_topic_detail_item, parent, false);
        return new TopicDetailViewHolder(view);
    }

    @Override
    protected void onBindHeaderView(View headerView) {
        Log.d(TAG, "onBindFooterView, init header view here.");
    }

    @Override
    protected void onBindFooterView(View footerView) {
        Log.d(TAG, "onBindFooterView, init footer view here.");
    }

    @Override
    protected void onBindItemView(RecyclerView.ViewHolder holder, Item item) {
        TopicDetailViewHolder h = (TopicDetailViewHolder) holder;
        TopicDetail td = (TopicDetail) item;
        if (td == null) {
            Log.e(TAG, "could not get TopicDetail");
            return;
        }
        h.tvAuthor.setText(td.getAuthor());
        h.tvTimeStamp.setText(td.getFormatedDate());
        h.tvFloor.setText(td.getPosition());
        setContentText(h, td);
    }

    private void setContentText(TopicDetailViewHolder h, TopicDetail td) {
        List<ContentSegment> contentSegments = td.getContentSegments();
        if (contentSegments.size() > 0) {
            h.tvContent.setText(contentSegments.get(0).getSpanned());
        }
    }

    class TopicDetailViewHolder extends ClickableViewHolder {
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
    }
}
