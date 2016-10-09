package com.tony.newsmth.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tony.newsmth.R;
import com.tony.newsmth.model.Board;

import java.util.List;

/**
 * Created by l00151177 on 2016/9/29.
 */
public class BoardListAdapter extends RecyclerView.Adapter {
    private static final String TAG = "BoardListAdapter";
    private Context mContext = null;
    private List<Board> mListBoard = null;
    private OnRecyclerViewListener onRecyclerViewListener = null;
    public interface OnRecyclerViewListener {
        void onItemClick(int position);
        boolean onItemLongClick(int position);
    }
    public void setOnRecyclerViewListener(OnRecyclerViewListener l) {
        onRecyclerViewListener = l;
    }

    public BoardListAdapter(Context context, List<Board> list) {
        mContext = context;
        mListBoard = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_board_item, parent, false);
        return new BoardListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        BoardListViewHolder h = (BoardListViewHolder) holder;
        Board b = mListBoard.get(position);
        if (b == null) {
            Log.e(TAG, "could not get board info");
            return;
        }
        h.mBoardName.setText(b.getBoardChsName());
        h.mBoardSection.setText(b.getCategoryName());
        h.mBoardOwner.setText(b.getModerator());
    }

    @Override
    public int getItemCount() {
        return mListBoard.size();
    }

    private class BoardListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        public TextView mBoardName;
        public TextView mBoardOwner;
        public TextView mBoardSection;
        public BoardListViewHolder(View itemView) {
            super(itemView);
            mBoardName = (TextView) itemView.findViewById(R.id.tv_board_name);
            mBoardOwner = (TextView) itemView.findViewById(R.id.tv_board_owner);
            mBoardSection = (TextView) itemView.findViewById(R.id.tv_board_section);
            mBoardSection.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (onRecyclerViewListener != null) {
                onRecyclerViewListener.onItemClick(getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (onRecyclerViewListener != null) {
                onRecyclerViewListener.onItemLongClick(getAdapterPosition());
            }
            return true;
        }
    }
}
