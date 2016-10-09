package com.tony.newsmth.adapter.base;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.List;

/**
 * Created by l00151177 on 2016/9/26.
 */
public abstract class GridLayoutAdapter<T extends RecyclerViewAdapter.Item> extends RecyclerViewAdapter<T> {
    public GridLayoutAdapter(List list) {
        super(list);
    }

    public GridLayoutAdapter(List list, int headerViewRes) {
        super(list, headerViewRes);
    }

    public GridLayoutAdapter(List list, int headerViewRes, int footerViewRes) {
        super(list, headerViewRes, footerViewRes);
    }

    class GridSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {
        @Override
        public int getSpanSize(int position) {
            if (isHeader(position) || isFooter(position)) {
                return gridManager.getSpanCount();
            }
            return 1;
        }
    }

    private GridSpanSizeLookup mGridSpanSizeLookup;
    private GridLayoutManager gridManager;

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            gridManager = ((GridLayoutManager) manager);
            if (mGridSpanSizeLookup == null) {
                mGridSpanSizeLookup = new GridSpanSizeLookup();
            }
            gridManager.setSpanSizeLookup(mGridSpanSizeLookup);
        }
    }
}