package com.tony.newsmth.model;

/**
 * Created by l00151177 on 2016/9/23.
 */
public class Attachment {
    public static int ATTACHMENT_TYPE_IMAGE = 1;
    public static int ATTACHMENT_TYPE_DOWNLOADABLE = 2;
    private String mOriginalImageSource;
    private String mResizedImageSource;
    private int type;

    public Attachment(String originalImgSrc, String resizedImageSrc) {
        this.mOriginalImageSource = originalImgSrc;
        this.mResizedImageSource = resizedImageSrc;
    }

    public String getOriginalImageSource() {
        return mOriginalImageSource;
    }

    public String getResizedImageSource() {
        return mResizedImageSource;
    }
}