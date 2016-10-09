package com.tony.newsmth.model;

import android.text.Html;

import com.tony.newsmth.adapter.base.ItemWraper;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by l00151177 on 2016/9/23.
 */
public class TopicDetail extends ItemWraper {
    private static final String ATTACHMENT_MARK = "###ZSMTH_ATTACHMENT###";
    public static int ACTION_DEFAULT = 0;
    public static int ACTION_FIRST_POST_IN_SUBJECT = 1;
    public static int ACTION_PREVIOUS_POST_IN_SUBJECT = 2;
    public static int ACTION_NEXT_POST_IN_SUBJECT = 3;
    private String postID;
    private String title;
    private String author;
    private String nickName;
    private Date date;
    private String position;

    private List<String> likes;
    private List<Attachment> attachFiles;

    private String htmlContent; // likes are not included
    //    private String htmlCompleteContent; // likes are included
    private List<ContentSegment> mSegments;  // parsed from htmlCompleteContent

    public TopicDetail() {
        date = new Date();
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        if (nickName == null || nickName.length() == 0) {
            return this.author;
        } else {
            return String.format("%s(%s)", this.author, this.nickName);
        }
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getRawAuthor() {
        return this.author;
    }

    public void setNickName(String nickName) {
        final int MAX_NICKNAME_LENGTH = 12;
        if (nickName.length() > MAX_NICKNAME_LENGTH) {
            nickName = nickName.substring(0, MAX_NICKNAME_LENGTH) + "..";
        }
        this.nickName = nickName;
    }

    public void setDate(Date date) {
        this.date = date;
    }
    private static SimpleDateFormat dateformat = null;
    public static String getFormattedString(Date date){
        if(dateformat == null)
            dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if(date != null)
            return dateformat.format(date);
        return "";
    }
    public String getFormatedDate() {
        return getFormattedString(this.date);
    }


    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }


    // set likes and content together, then merge them to htmlCompleteContent, then split it into htmlSegments
    public void setLikesAndPostContent(List<String> likes, Element content) {
        // save likes
        this.likes = likes;

//                Log.d("setLikesAndPostContent", content.html());

        // find all attachment from node
        // <a target="_blank" href="http://att.newsmth.net/nForum/att/AutoWorld/1939790539/4070982">
        // <img border="0" title="单击此查看原图" src="http://att.newsmth.net/nForum/att/AutoWorld/1939790539/4070982/large" class="resizeable">
        // </a>
        Elements as = content.select("a[href]");
        for (Element a : as) {
            Elements imgs = a.select("img[src]");
            if (imgs.size() == 1) {
                // find one image attachment
                String origImageSrc = a.attr("href");
                if (origImageSrc != null && origImageSrc.startsWith("/nForum")) {
                    origImageSrc = "http://att.newsmth.net" + origImageSrc;
                }

                Element img = imgs.first();
                String resizedImageSrc = img.attr("src");
                if (resizedImageSrc != null && resizedImageSrc.startsWith("/nForum")) {
                    resizedImageSrc = "http://att.newsmth.net" + resizedImageSrc;
                }

                Attachment attach = new Attachment(origImageSrc, resizedImageSrc);
                this.addAttachFile(attach);

                // replace a[href] with MARK
                // we will split the string with MARK, so make sure no two MAKR will stay together
                a.html(ATTACHMENT_MARK + " ");
            }
        }

        // process pure post content
        String formattedPlainText = Html.fromHtml(content.html()).toString();
        this.htmlContent = this.processPostContent(formattedPlainText);

        // it's important to know that not all HTML tags are supported by Html.fromHtml, see the supported list
        // https://commonsware.com/blog/Android/2010/05/26/html-tags-supported-by-textview.html
        // http://stackoverflow.com/questions/18295881/android-textview-html-font-size-tag
        String htmlCompleteContent = this.htmlContent;

        if (likes != null && likes.size() > 0) {
            StringBuilder wordList = new StringBuilder();
            wordList.append("<br/><small><cite>");
            for (String word : likes) {
                wordList.append(word).append("<br/>");
            }
            wordList.append("</cite></small>");
            htmlCompleteContent += new String(wordList);
        }
        // now htmlCompleteContent has both post content and likes content

        // parse htmlCompleteContent to htmlSegments
        parseContentToSegments(htmlCompleteContent);
    }

    // split complete content with ATTACHMENT_MARK
    private void parseContentToSegments(String htmlCompleteContent) {
        if (mSegments == null) {
            mSegments = new ArrayList<>();
        }
        mSegments.clear();

        if (attachFiles == null || attachFiles.size() == 0) {
            // no attachment, add all content as one segment
            mSegments.add(new ContentSegment(ContentSegment.SEGMENT_TEXT, htmlCompleteContent));
        } else {
            // when there are attachments here, separate them one by one
            String[] segments = htmlCompleteContent.split(ATTACHMENT_MARK);

            // add segments and attachments together
            int attachIndex = 0;
            for (String segment : segments) {
//                Log.d("Splited Result:", String.format("{%s}", segment));
                // add segment to results if it's not empty,
                // MARK are seperated by several <br />, we should skip these seperated text
                if (!isEmptyString(segment) || attachIndex == 0) {
                    // since we expect there will always be a textview before imageview
                    // even the first text segment is empty, we still add it
                    mSegments.add(new ContentSegment(ContentSegment.SEGMENT_TEXT, segment));
                }

                // add next image attachment to results
                if (attachFiles != null && attachIndex < attachFiles.size()) {
                    Attachment attach = attachFiles.get(attachIndex);
                    String imageURL = null;
//                    if (Settings.getInstance().isLoadOriginalImage()) {
//                        imageURL = attach.getOriginalImageSource();
//                    } else {
                        imageURL = attach.getResizedImageSource();
                    //}
                    ContentSegment img = new ContentSegment(ContentSegment.SEGMENT_IMAGE, imageURL);
                    img.setImgIndex(attachIndex);
                    mSegments.add(img);
                }

                attachIndex++;
            }
        }

//        Log.d("ContentSegment", String.format("Total segments here: %d", mSegments.size()));
//        for (ContentSegment content : mSegments) {
//            if (content.getType() == ContentSegment.SEGMENT_IMAGE) {
//                Log.d("ContentSegment", String.format("Image %s, index = %d", content.getUrl(), content.getImgIndex()));
//            } else if (content.getType() == ContentSegment.SEGMENT_TEXT) {
//                Log.d("ContentSegment", String.format("Text, {%s}", content.getSpanned().toString()));
//            }
//        }
    }

    /*
    the expected input is formatted plain text, no html tag is expected
    line break by \n, but not <br>
    */
    private String processPostContent(String content) {
        // &nbsp; is converted as code=160, but not a whitespace (ascii=32)
        // http://stackoverflow.com/questions/4728625/why-trim-is-not-working
        content = content.replace(String.valueOf((char) 160), " ");

        // it's important to know that not all HTML tags are supported by Html.fromHtml, see the supported list
        // https://commonsware.com/blog/Android/2010/05/26/html-tags-supported-by-textview.html
        String[] lines = content.split("\n");

        // find signature start line
        int signatureStartLine = -1;
        for (int i = lines.length - 1; i >= 0; i--) {
            String line = lines[i];
            if (line.startsWith("--") && line.length() <= 3) {
                // find the first "--" from the last to the first
                signatureStartLine = i;
                break;
            }
        }

        // process content line by line
        StringBuilder sb = new StringBuilder();
        int linebreak = 0;
        int signatureMode = 0;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if ((line.startsWith("发信人:") || line.startsWith("寄信人:")) && i <= 3) {
                // find nickname for author here, skip the line
                // 发信人: schower (schower), 信区: WorkLife
                String nickName = subStringBetween(line, "(", ")");
                if (nickName != null && nickName.length() > 0) {
                    this.setNickName(nickName);
                }
                continue;
            } else if (line.startsWith("标  题:") && i <= 3) {
                // skip this line
                continue;
            } else if (line.startsWith("发信站:") && i <= 3) {
                // find post date here, skip the line
                // <br /> 发信站: 水木社区 (Fri Mar 25 11:52:04 2016), 站内
                line = subStringBetween(line, "(", ")");
                SimpleDateFormat simpleFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy", Locale.US);
                try {
                    Date localdate = simpleFormat.parse(line);
                    this.setDate(localdate);
                    continue;
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
            }

            // handle ATTACH_MARK
            if (line.contains(ATTACHMENT_MARK)) {
                sb.append(line);
                continue;
            }

            // handle quoted content
            if (line.startsWith(":")) {
                line = "<font color=#00b4ae>" + line + "</font>";
                sb.append(line).append("<br />");
                continue;
            }

            if (line.trim().length() == 0) {
                linebreak++;
                if (linebreak >= 2) {
                    // continuous linebreak, skip extra linebreak
                    continue;
                } else {
                    sb.append(line).append("<br />");
                    continue;
                }
            } else {
                // reset counter
                linebreak = 0;
            }

            // handle siguature
            // we have to make sure "--" is the last one, it might appear in post content body
            if (i == signatureStartLine) {
                // entering signature mode
                signatureMode = 1;
                sb.append(line).append("<br />");
                continue;
            }

            // ※ 修改:·wpd419 于 Mar 29 09:43:17 2016 修改本文·[FROM: 111.203.75.*]
            // ※ 来源:·水木社区 http://www.newsmth.net·[FROM: 111.203.75.*]
            if (line.contains("※ 来源:·")) {
                // jump out of signature mode
                signatureMode = 0;
                line = line.replace("·", "")
                        .replace("http://www.newsmth.net", "")
                        .replace("http://m.newsmth.net", "")
                        .replace("http://newsmth.net", "")
                        .replace("newsmth.net", "")
                        .replace("m.newsmth.net", "")
                        .replace("官方应用", "")
                        .replace("客户端", "");

                line = "<font color=#727272>" + lookupIPLocation(line) + "</font>";
                sb.append(line).append("<br />");
                continue;
            } else if (line.contains("※ 修改:·")) {
                // jump out of signature mode
                signatureMode = 0;
                line = line.replace("·", "").replace("修改本文", "");
                line = "<font color=#727272>" + lookupIPLocation(line) + "</font>";
                sb.append(line).append("<br />");
                continue;
            }

            // after handle last part of post content, if it's still in signature mode, add signature
            if (signatureMode == 1) {
                line = "<small><font color=#727272>" + line + "</font></small>";
                sb.append(line).append("<br />");
                continue;
            }

            // for other normal line, add it directly
            sb.append(line).append("<br />");
        }

        return sb.toString().trim();
    }


    public List<ContentSegment> getContentSegments() {
        return mSegments;
    }

    // used by copy post content menu, or quoted content while replying
    public String getRawContent() {
        return Html.fromHtml(this.htmlContent.replace(ATTACHMENT_MARK, "")).toString();
    }

    // this method should not be called, unless we set error message
    public void setRawContent(String rawContent) {
        this.htmlContent = rawContent;
        parseContentToSegments(this.htmlContent);
    }

    public void addAttachFile(Attachment attach) {
        if (attachFiles == null) {
            attachFiles = new ArrayList<>();
        }
        if (attach != null) {
            attachFiles.add(attach);
        }
    }

    public List<Attachment> getAttachFiles() {
        return attachFiles;
    }

    @Override
    public String toString() {
        return "Post{" +
                "postID='" + postID + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", nickName='" + nickName + '\'' +
                ", date=" + date +
                ", position='" + position + '\'' +
                '}';
    }

    public String subStringBetween(String line, String str1, String str2) {
        if (line == null || line.length() == 0) {
            return "";
        }

        int idx1 = line.indexOf(str1);
        int idx2 = line.lastIndexOf(str2);
        if (idx1 != -1 && idx2 != -1) {
            return line.substring(idx1 + str1.length(), idx2);
        }
        return "";
    }

    public String lookupIPLocation(String content) {
        Pattern myipPattern = Pattern.compile("FROM[: ]*(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.)[\\d\\*]+");
        Matcher myipMatcher = myipPattern.matcher(content);
        while (myipMatcher.find()) {
            String ipl = myipMatcher.group(1);
            if (ipl.length() > 5) {
                //TODO: set geo DB
                //ipl = "$1\\*(" + SMTHApplication.geoDB.getLocation(ipl + "1") + ")";
                ipl = "$1\\*";
            } else {
                ipl = "$1\\*";
            }
            content = myipMatcher.replaceAll(ipl);
        }
        return content;
    }

    public static boolean isEmptyString(String content){
        // non-empty if it's long enough
        if(content.length() > 20){
            return false;
        }

        String text = Html.fromHtml(content).toString();
        for(int i = 0; i < text.length(); i++) {
            int value = Character.codePointAt(text, i);
            // http://www.utf8-chartable.de/unicode-utf8-table.pl?utf8=dec
            // 如果value不是控制符(0~31)，value不是SPACE(32)，不是"NO-BREAK SPACE"(160), 则认为是非空的
            if(value > 32 && value != 160){
                return false;
            }
        }
        return true;
    }
}

