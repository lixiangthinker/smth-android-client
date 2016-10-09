package com.tony.newsmth.controller;

import com.tony.newsmth.model.Topic;
import com.tony.newsmth.model.TopicDetail;

import java.util.List;

/**
 * Created by l00151177 on 2016/9/23.
 */
public interface ITopicDetailController {
    List<TopicDetail> onGetTopicDetails(String boardEngName, String topicId, int page, String author);

    void setTopic(Topic topic);

    enum TopicDetailResult {
        SUCCESS, FAILED_COMMON, FAILED_TIMEOUT, FAILED_INVALIDE_PARAM, FAILED_TOO_MUCH_ACTION
    }
    void setOnTopicDetailResultListener(OnTopicDetailResultListener l);
    interface OnTopicDetailResultListener {
        boolean onStart();
        boolean onNewTopicDetail(TopicDetail topicDetail);
        boolean onResult(TopicDetailResult result, List<TopicDetail> topicDetailList, String prompt);
    }
}
