package com.tony.newsmth.controller;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.tony.newsmth.model.Topic;
import com.tony.newsmth.model.TopicDetail;
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
 * Created by l00151177 on 2016/9/23.
 */
public class TopicDetailControllerImpl implements ITopicDetailController{

    private static final String TAG = "TopicDetailController";
    private Context mContext = null;
    private Topic mTopic = null;
    public TopicDetailControllerImpl(Context context) {
        mContext = context;
        mTopic = new Topic();
    }

    private OnTopicDetailResultListener onTopicDetailResultListener = null;
    @Override
    public void setOnTopicDetailResultListener(OnTopicDetailResultListener l) {
        onTopicDetailResultListener = l;
    }

    @Override
    public List<TopicDetail> onGetTopicDetails(String boardEngName, String topicId, int page, String author) {
        final List<TopicDetail> result = new ArrayList<>();
        SmthApi helper = SmthApi.getInstance(mContext);
        helper.getService().getTopicDetailByPage(boardEngName, topicId, page, author)
                .flatMap(new Func1<ResponseBody, Observable<TopicDetail>>() {
                    @Override
                    public Observable<TopicDetail> call(ResponseBody responseBody) {
                        try {
                            String response = responseBody.string();
                            List<TopicDetail> posts = parseTopicDetailFromWwwResp(response, mTopic);
                            return Observable.from(posts);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<TopicDetail>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted");
                        if (onTopicDetailResultListener != null) {
                            onTopicDetailResultListener.onResult(TopicDetailResult.SUCCESS, result, "current page completed");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(mContext, "加载失败！\n" + e.toString(), Toast.LENGTH_LONG).show();
                        if (onTopicDetailResultListener != null) {
                            onTopicDetailResultListener.onResult(TopicDetailResult.FAILED_COMMON, result, "connection failed");
                        }
                    }

                    @Override
                    public void onNext(TopicDetail topicDetail) {
                        Log.d(TAG, "onNext");
                        result.add(topicDetail);
                        if (onTopicDetailResultListener != null) {
                            onTopicDetailResultListener.onNewTopicDetail(topicDetail);
                        }
                    }
                });
        return result;
    }

    @Override
    public void setTopic(Topic topic) {
        if (topic != null) {
            mTopic = topic;
        }
    }

    public List<TopicDetail> parseTopicDetailFromWwwResp(String content, Topic topic) {
        List<TopicDetail> results = new ArrayList<>();

        Document doc = Jsoup.parse(content);

        // find total posts for this topic, and total pages
        Elements lis = doc.select("li.page-pre");
        if(lis.size() > 0) {
            Element li = lis.first();
            // 贴数:152 分页:
//            Log.d(TAG, li.text());

            Pattern pattern = Pattern.compile("(\\d+)", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(li.text());
            if (matcher.find()) {
                String totalPostString = matcher.group(0);
                topic.setTotalPostNoFromString(totalPostString);
//                Log.d(TAG, totalPostString);
            }
        }

        // find all posts
        Elements tables = doc.select("table.article");
        for (Element table: tables) {
            TopicDetail topicDetail = new TopicDetail();

            // find author for this post
            // <span class="a-u-name"><a href="/nForum/user/query/CZB">CZB</a></span>
            Elements authors = table.select("span.a-u-name");
            if(authors.size() > 0){
                Element author = authors.get(0);
                String authorName = author.text();
                topicDetail.setAuthor(authorName);
            }

            // find post id for this post
            // <samp class="ico-pos-reply"></samp><a href="/nForum/article/WorkLife/post/1113865" class="a-post">回复</a></li>
            Elements links = table.select("li a.a-post");
            if(links.size() > 0){
                Element link = links.first();
                String postID = getLastStringSegment(link.attr("href"));
                topicDetail.setPostID(postID);
            }

            // find post position
            // <span class="a-pos">第1楼</span>
            Elements positions = table.select("span.a-pos");
            if(positions.size() > 0){
                Element position = positions.first();
                topicDetail.setPosition(position.text());
            }


            // find & parse post content
            Elements contents = table.select("td.a-content");
            if(contents.size() == 1) {
                ParsePostContentFromWWW(contents.get(0), topicDetail);
            }
//            Log.d(TAG, post.toString());
            results.add(topicDetail);
        }

        if(results.size() == 0) {
            // there might be some problems with the response
//            <div class="error">
//            <h5>产生错误的可能原因：</h5>
//            <ul>
//            <li>
//            <samp class="ico-pos-dot"></samp>指定的文章不存在或链接错误</li>
//            </ul>
//            </div>
            Elements divs = doc.select("div.error");
            if(divs.size() > 0) {
                Element div = divs.first();

                topic.setTotalPostNoFromString("1");

                TopicDetail topicDetail = new TopicDetail();
                topicDetail.setAuthor("错误信息");
                topicDetail.setRawContent(div.toString());
                results.add(topicDetail);
            }
        }

        return results;
    }

    // called by ParsePostListFromWWW
    // this method will call ParseLikeElementInPostContent & ParsePostBodyFromWWW
    // sample response: assets/post_content_from_www.html
    public static void ParsePostContentFromWWW(Element content, TopicDetail post) {
        // 1. find, parse and remove likes node first
        // <div class="likes">
        Elements nodes = content.select("div.likes");
        List<String> likes = null;
        if(nodes.size() == 1) {
            Element node = nodes.first();
            likes = ParseLikeElementInPostContent(node);
        }

        // 2. find post content, the first <p> node in the td.a-content
        // <button class="button add_like"
        nodes = content.getElementsByTag("p");
        if(nodes.size() >= 1) {
            Element node = nodes.first();
            // 2. set post content
            post.setLikesAndPostContent(likes, node);
        }
    }
    // parse like list in post content
    public static List<String> ParseLikeElementInPostContent(Element like) {
        List<String> likes = new ArrayList<>();

        // <div class="like_name">有36位用户评价了这篇文章：</div>
        Elements nodes = like.select("div.like_name");
        if(nodes.size() == 1) {
            Element node = nodes.first();
            likes.add(node.text());
        }

        // <li><span class="like_score_0">[&nbsp;&nbsp;]</span><span class="like_user">fly891198061:</span>
        // <span class="like_msg">无法忍受，我不会变节，先斗智，不行就自杀！来个痛快的~！</span>
        // <span class="like_time">(2016-03-27 15:04)</span></li>
        nodes = like.select("li");
        for(Element n: nodes) {
            likes.add(n.text());
        }

        return likes;
    }
    // /nForum/board/ADAgent_TG ==> ADAgent_TG
    // /nForum/article/RealEstate/5017593 ==> 5017593
    public String getLastStringSegment(String content) {
        if(content == null || content.length() == 0){
            return "";
        }
        String[] segments = content.split("/");
        if(segments.length > 0) {
            return segments[segments.length - 1];
        }
        return "";
    }
}
