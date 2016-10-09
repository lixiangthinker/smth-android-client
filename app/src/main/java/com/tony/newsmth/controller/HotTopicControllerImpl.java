package com.tony.newsmth.controller;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.tony.newsmth.model.Topic;
import com.tony.newsmth.net.SmthApi;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by l00151177 on 2016/9/22.
 */
public class HotTopicControllerImpl implements IHotTopicController {
    private static final String TAG = "HotTopicControllerImpl";
    private Context mContext = null;
    public HotTopicControllerImpl(Context context) {
        mContext = context;
    }
    public List<Topic> onGetHotTopics() {
        final List<Topic> listTopic = new ArrayList<>();
        SmthApi netApi = SmthApi.getInstance(mContext);

        netApi.getService().getAllHotTopics()
                .flatMap(new Func1<ResponseBody, Observable<Topic>>() {
                    @Override
                    public Observable<Topic> call(ResponseBody responseBody) {
                        try {
                            String response = responseBody.string();
                            List<Topic> results = parseHotTopicsFromResponse(response);
                            return Observable.from(results);
                        } catch (Exception e) {
                            Log.d(TAG, Log.getStackTraceString(e));
                        }
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Topic>() {
                    @Override
                    public void onStart() {
                        // clearHotTopics current hot topics
                        Log.d(TAG, "onGetHotTopics onStart");
                        if (onHotTopicResultListener != null) {
                            onHotTopicResultListener.onStart();
                        }
                    }

                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onGetHotTopics onCompleted");
                        if (onHotTopicResultListener != null) {
                            onHotTopicResultListener.onResult(HotTopicResult.SUCCESS, listTopic, "get topic list success");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Toast.makeText(mContext, "获取热帖失败!", Toast.LENGTH_LONG).show();
                        if (onHotTopicResultListener != null) {
                            onHotTopicResultListener.onResult(HotTopicResult.FAILED_COMMON, null, "connection failure.");
                        }
                    }

                    @Override
                    public void onNext(Topic topic) {
                        Log.d(TAG, topic.toString());
                        listTopic.add(topic);
                        if (onHotTopicResultListener != null) {
                            onHotTopicResultListener.onNewTopic(topic);
                        }
                    }
                });
        return listTopic;
    }

    private OnHotTopicResultListener onHotTopicResultListener = null;
    @Override
    public void setOnHotTopicResultListener(OnHotTopicResultListener l) {
        onHotTopicResultListener = l;
    }

    // parse guidance page, to find all hot topics
    public List<Topic> parseHotTopicsFromResponse(String content) {
        List<Topic> results = new ArrayList<>();
        if (content == null || content.length() == 0) {
            return results;
        }

        Topic topic;
        Document doc = Jsoup.parse(content);

        // find top10
        // <div id="top10">
        Elements top10s = doc.select("div#top10");
        if(top10s.size() == 1) {
            // add separator
            topic = new Topic("本日十大热门话题");
            results.add(topic);

            // parse hot hopic
            Element top10 = top10s.first();
            Elements lis = top10.getElementsByTag("li");

            for(Element li: lis) {
                topic = parseTopicFromElement(li, "top10");
                if(topic != null) {
                    //Log.d(TAG, topic.toString());
                    results.add(topic);
                }
            }
        }

        // find hot picture
        // <div id="pictures" class="block">
        Elements pictures = doc.select("div#pictures");
        for(Element section: pictures) {
            // add separator
            Elements sectionNames = section.getElementsByTag("h3");
            if(sectionNames.size() == 1) {
                Element sectionName = sectionNames.first();
                topic = new Topic(sectionName.text());
                results.add(topic);
            }

            Elements lis = section.select("div li");
            for (Element li: lis) {
                //Log.d(TAG, li.toString());
                topic = parseTopicFromElement(li, "pictures");
                if(topic != null) {
                    //Log.d(TAG, topic.toString());
                    results.add(topic);
                }
            }
        }

        // find hot topics from each section
        // <div id="hotspot" class="block">
        Elements sections = doc.select("div.b_section");
        for(Element section: sections) {
            // add separator
            Elements sectionNames = section.getElementsByTag("h3");
            if(sectionNames.size() == 1) {
                Element sectionName = sectionNames.first();
                String name = sectionName.text();
                if(name == null || name.equals("系统与祝福")){
                    continue;
                }
                topic = new Topic(name);
                results.add(topic);
            }

            Elements lis = section.select("div.topics li");
            for (Element li: lis) {
                //Log.d(TAG, li.toString());
                topic = parseTopicFromElement(li, "sectionhot");
                if(topic != null) {
                    //Log.d(TAG, topic.toString());
                    results.add(topic);
                }
            }
        }

        return results;
    }

    public Topic parseTopicFromElement(Element ele, String type) {
        if("top10".equals(type) ||  "sectionhot".equals(type)) {
            // two <A herf> nodes

            // normal hot topic
            // <li><a href="/nForum/article/OurEstate/1685281" title="lj让我走垫资(114)">lj让我走垫资&nbsp;(114)</a></li>

            // special hot topic -- 近期热帖: 1. board信息，没有reply_count
            // <li>
            // <div><a href="/nForum/board/Picture"><span class="board">[贴图]</span></a><a href="/nForum/article/ShiDa/59833" title=" 南都副总编及编辑被处分开除"><span class="title"> 南都副总编及编辑被处分开除</span></a></div>
            // </li>

            Elements as = ele.select("a[href]");
            if(as.size() == 2) {
                Element a1 = as.get(0);
                Element a2 = as.get(1);

                String boardChsName = a1.text().replace("]", "").replace("[", "");
                String boardEngName = getLastStringSegment(a1.attr("href"));

                String title = a2.attr("title");
                String topicID = getLastStringSegment(a2.attr("href"));

                Topic topic = new Topic();
                String reply_count = getReplyCountInParentheses(title);
                if(reply_count.length() > 0) {
                    title = title.substring(0, title.length() - reply_count.length() - 2);
                    topic.setTotalPostNoFromString(reply_count);
                }

                topic.setBoardEngName(boardEngName);
                topic.setBoardChsName(boardChsName);
                topic.setTopicID(topicID);
                topic.setTitle(title);

                //Log.d(TAG, topic.toString());
                return topic;
            }
        } else if("pictures".equals(type)) {
            // three <A herf> nodes

            // <li>
            // <a href="/nForum/article/SchoolEstate/430675"><img src="http://images.newsmth.net/nForum/img/hotpic/SchoolEstate_430675.jpg" title="点击查看原帖" /></a>
            // <br /><a class="board" href="/nForum/board/SchoolEstate">[学区房]</a>
            // <br /><a class="title" href="/nForum/article/SchoolEstate/430675" title="这个小学排名还算靠谱吧， AO爸爸排的。。。">这个小学排名还算靠谱吧， AO爸爸排的。。。</a>
            // </li>
            Elements as = ele.select("a[href]");
            if(as.size() == 3) {
                Element a1 = as.get(1);
                Element a2 = as.get(2);

                String boardChsName = a1.text().replace("]", "").replace("[", "");
                String boardEngName = getLastStringSegment(a1.attr("href"));

                String title = a2.attr("title");
                String topicID = getLastStringSegment(a2.attr("href"));

                Topic topic = new Topic();
                topic.setBoardEngName(boardEngName);
                topic.setBoardChsName(boardChsName);
                topic.setTopicID(topicID);
                topic.setTitle(title);

                //Log.d(TAG, topic.toString());
                return topic;
            }

        }
        return null;
    }
    // /nForum/board/ADAgent_TG ==> ADAgent_TG
    // /nForum/article/RealEstate/5017593 ==> 5017593
    private String getLastStringSegment(String content) {
        if(content == null || content.length() == 0){
            return "";
        }
        String[] segments = content.split("/");
        if(segments.length > 0) {
            return segments[segments.length - 1];
        }
        return "";
    }

    // [团购]3.28-4.03 花的传说饰品团购(18) ==> 18
    public String getReplyCountInParentheses(String content) {
        Pattern hp = Pattern.compile("\\((\\d+)\\)$", Pattern.DOTALL);
        Matcher hm = hp.matcher(content);
        if (hm.find()) {
            String count = hm.group(1);
            return count;
        }

        return "";
    }
}
