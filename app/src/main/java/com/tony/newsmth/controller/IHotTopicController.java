package com.tony.newsmth.controller;

import com.tony.newsmth.model.Topic;

import java.util.List;

/**
 * Created by l00151177 on 2016/9/22.
 */
public interface IHotTopicController {
    List<Topic> onGetHotTopics();
    enum HotTopicResult {
        SUCCESS, FAILED_COMMON, FAILED_TIMEOUT, FAILED_INVALIDE_PARAM, FAILED_TOO_MUCH_ACTION
    }
    void setOnHotTopicResultListener(OnHotTopicResultListener l);
    interface OnHotTopicResultListener {
        boolean onStart();
        boolean onNewTopic(Topic topic);
        boolean onResult(HotTopicResult result, List<Topic> topicList, String prompt);
    }
}
