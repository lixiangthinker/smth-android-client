package com.tony.newsmth.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.tony.newsmth.R;
import com.tony.newsmth.adapter.TopicDetailFooterAdapter;
import com.tony.newsmth.adapter.base.OnRecyclerViewScrollListener;
import com.tony.newsmth.adapter.base.RecyclerViewAdapter;
import com.tony.newsmth.controller.ITopicDetailController;
import com.tony.newsmth.controller.TopicDetailControllerImpl;
import com.tony.newsmth.model.Topic;
import com.tony.newsmth.model.TopicDetail;
import com.tony.newsmth.view.activity.BaseActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by l00151177 on 2016/9/23.
 */
public class TopicDetailActivy extends BaseActivity {
    private static final String TAG = "TopicDetailActivy";
    private ITopicDetailController mTopicDetailController = null;
    private RecyclerView mTopicDetailList = null;
    private RecyclerViewAdapter mAdapter = null;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    public static String EXTRA_KEY_TOPIC = "EXTRA_KEY_TOPIC";
    private int mCurrentPage = 0;
    private String mAuthor = null;
    private List<TopicDetail> mTopicDetail = new ArrayList<>();
    private Topic mTopic = null;
    private OnRecyclerViewScrollListener<TopicDetail> onRecyclerViewScrollListener =
    new OnRecyclerViewScrollListener<TopicDetail>() {
        @Override
        public void onStart() {
            Log.d(TAG, "onStart set footerview");
            mAdapter.setFooter(R.layout.cardview_footer_loadmore);
            if (mAdapter.hasHeader()) {
                mTopicDetailList.smoothScrollToPosition(mAdapter.getItemCount() + 1);
            } else {
                mTopicDetailList.smoothScrollToPosition(mAdapter.getItemCount());
            }
        }

        @Override
        public void onLoadMore() {
            Log.d(TAG, "onLoadMore get next page");
            handleLoadMore(this);
        }

        @Override
        public void onFinish(List<TopicDetail> list) {
            Log.d(TAG, "onLoadMore");
            setLoadingMore(false);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_detail);
        getTopic();
        mTopicDetailController = new TopicDetailControllerImpl(this);
        mTopicDetailController.setOnTopicDetailResultListener(new ITopicDetailController.OnTopicDetailResultListener() {
            @Override
            public boolean onStart() {
                mTopicDetail.clear();
                return false;
            }

            @Override
            public boolean onNewTopicDetail(TopicDetail TopicDetail) {
                mTopicDetail.add(TopicDetail);
                mAdapter.notifyItemChanged(mAdapter.getItemCount() - 1);
                return true;
            }

            @Override
            public boolean onResult(ITopicDetailController.TopicDetailResult result, List<TopicDetail> topicList, String prompt) {
                mSwipeRefreshLayout.setRefreshing(false);
                onRecyclerViewScrollListener.onFinish(topicList);
                if (result != ITopicDetailController.TopicDetailResult.SUCCESS) {
                    Toast.makeText(getApplicationContext(), "could not get topic content, please try again.", Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });
        mTopicDetailList = (RecyclerView) findViewById(R.id.rv_topic_detail_list);
        mTopicDetailList.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new TopicDetailFooterAdapter(mTopicDetail);
        mTopicDetailList.setAdapter(mAdapter);
        mTopicDetailList.addOnScrollListener(onRecyclerViewScrollListener);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_topic_detail);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "onRefresh, refresh data");
                refreshData();
            }
        });
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "initialize data");
                refreshData();
            }
        });
    }

    private void handleLoadMore(OnRecyclerViewScrollListener<TopicDetail> onRecyclerViewScrollListener) {
        Topic topic = getTopic();
        mCurrentPage ++ ;
        List<TopicDetail> list = mTopicDetailController.onGetTopicDetails(mTopic.getBoardEngName(), topic.getTopicID(), mCurrentPage, mAuthor);
    }

    private void refreshData() {
        if (mCurrentPage == 1) {
            Toast.makeText(this, "current page is 1st page", Toast.LENGTH_LONG).show();
            mSwipeRefreshLayout.setRefreshing(false);
        } else {
            mSwipeRefreshLayout.setRefreshing(true);
            mCurrentPage++;
            Topic topic = getTopic();
            mTopicDetailController.setTopic(topic);
            mTopicDetailController.onGetTopicDetails(topic.getBoardEngName(), topic.getTopicID(), mCurrentPage, mAuthor);
        }
    }

    private Topic getTopic() {
        if (mTopic == null) {
            Intent intent = getIntent();
            mTopic = intent.getParcelableExtra(EXTRA_KEY_TOPIC);
        }
        return mTopic;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Topic topic = getTopic();
        mTopicDetailController.setTopic(topic);
        mTopicDetailController.onGetTopicDetails(topic.getBoardEngName(), topic.getTopicID(), mCurrentPage, mAuthor);
    }
}
