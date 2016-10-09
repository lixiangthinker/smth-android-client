package com.tony.newsmth.adapter.base;

import android.support.v7.widget.RecyclerView;

/**
 * Created by l00151177 on 2016/9/26.
 */
public class ItemWraper implements RecyclerViewAdapter.Item{
    @Override
    public int getType() {
        return RecyclerViewAdapter.Item.TYPE_CONTENT;
    }
}
