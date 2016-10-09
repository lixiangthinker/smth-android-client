package com.tony.newsmth.model;

/**
 * Created by l00151177 on 2016/9/28.
 */
public class BoardSection {
    public String sectionURL;
    public String sectionName;
    public String parentName;

    public String getBoardCategory (){
        if(parentName == null || parentName.length() == 0) {
            return sectionName;
        } else {
            return String.format("%s %s", parentName, sectionName);
        }
    }
}
