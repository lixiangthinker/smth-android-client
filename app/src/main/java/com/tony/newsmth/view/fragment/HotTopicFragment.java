package com.tony.newsmth.view.fragment;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.tony.newsmth.R;
import com.tony.newsmth.adapter.HotTopicAdapter;
import com.tony.newsmth.controller.HotTopicControllerImpl;
import com.tony.newsmth.controller.IHotTopicController;
import com.tony.newsmth.model.Topic;
import com.tony.newsmth.view.activity.TopicDetailActivy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by l00151177 on 2016/9/27.
 */
public class HotTopicFragment extends Fragment {
    private static final String TAG = "HotTopicFragment";
    private RecyclerView mRecyclerView = null;
    private SwipeRefreshLayout swipeRefreshLayout = null;
    private HotTopicAdapter mAdapter = null;
    private IHotTopicController hotTopicController = null;
    private List<Topic> listTopic = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_hot_topic, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_hot_topic);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new HotTopicAdapter(getActivity(), listTopic);
        mAdapter.setOnRecyclerViewListener(new HotTopicAdapter.OnRecyclerViewListener() {
            @Override
            public void onItemClick(int position) {
                Log.d(TAG, "onItemClick position = " + position);
                handleShowTopicDetail(listTopic.get(position));
            }

            @Override
            public boolean onItemLongClick(int position) {
                Log.d(TAG, "onItemLongClick position = " + position);
                handleShowTopicDetail(listTopic.get(position));
                return true;
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        hotTopicController = new HotTopicControllerImpl(getActivity());

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_hot_topic);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "onRefresh, refresh data");
                refreshListData();
            }
        });
        //swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "initialize data");
                swipeRefreshLayout.setRefreshing(true);
                refreshListData();
            }
        });
        return view;
    }

    private void handleShowTopicDetail(Topic topic) {
        if (topic == null) {
            Log.e(TAG, "return, null topic");
            return;
        }
        Log.d(TAG, "handleShowTopicDetail id " + topic.getTopicID() + " board " + topic.getBoardEngName());
        Intent intent = new Intent("com.tony.newsmth.SHOW_TOPIC_DETAIL");
        intent.putExtra(TopicDetailActivy.EXTRA_KEY_TOPIC, topic);
        startActivity(intent);
    }

    private void refreshListData() {
        hotTopicController.onGetHotTopics();
        hotTopicController.setOnHotTopicResultListener(new IHotTopicController.OnHotTopicResultListener() {
            @Override
            public boolean onStart() {
                listTopic.clear();
                return true;
            }

            @Override
            public boolean onNewTopic(Topic topic) {
                listTopic.add(topic);
                return true;
            }

            @Override
            public boolean onResult(IHotTopicController.HotTopicResult result, List<Topic> topicList, String prompt) {
                swipeRefreshLayout.setRefreshing(false);
                switch (result) {
                    case SUCCESS:
                        mAdapter.notifyDataSetChanged();
                        break;
                    default:
                        Toast.makeText(getActivity(), prompt, Toast.LENGTH_LONG).show();
                        break;
                }
                return true;
            }
        });
    }
}
