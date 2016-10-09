package com.tony.newsmth.view.fragment;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.tony.newsmth.R;
import com.tony.newsmth.adapter.BoardListAdapter;
import com.tony.newsmth.controller.BoardListControllerImpl;
import com.tony.newsmth.controller.IBoardListController;
import com.tony.newsmth.model.Board;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by l00151177 on 2016/9/27.
 */
public class BoardFragment extends android.support.v4.app.Fragment {
    private static final String TAG = "BoardFragment";
    private RecyclerView mRecyclerView = null;
    private SwipeRefreshLayout swipeRefreshLayout = null;
    private BoardListAdapter mAdapter = null;
    private IBoardListController boardListController = null;
    private List<Board> listBoard = new ArrayList<>();
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_board_list, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_board_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new BoardListAdapter(getActivity(), listBoard);
        mAdapter.setOnRecyclerViewListener(new BoardListAdapter.OnRecyclerViewListener() {
            @Override
            public void onItemClick(int position) {
                handleShowBoardTopicList();
            }

            @Override
            public boolean onItemLongClick(int position) {
                handleShowBoardTopicList();
                return false;
            }
        });
        mRecyclerView.setAdapter(mAdapter);

        boardListController = new BoardListControllerImpl(getContext());

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_board_list);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "onRefresh, refresh data");
                refreshListData();
            }
        });
        //swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "initialize data");
                swipeRefreshLayout.setRefreshing(true);
                refreshListData();
            }
        });
        return view;
    }

    private void handleShowBoardTopicList() {
        //TODO: show board topic list;
    }

    private void refreshListData() {
        boardListController.getAllBoards();
        boardListController.setOnBoardListResultListener(new IBoardListController.OnBoardListResultListener() {
            @Override
            public boolean onStart() {
                listBoard.clear();
                return true;
            }

            @Override
            public boolean onNewBoard(Board board) {
                listBoard.add(board);
                return true;
            }

            @Override
            public boolean onResult(IBoardListController.BoardListResult result, List<Board> boardList, String prompt) {
                swipeRefreshLayout.setRefreshing(false);
                switch (result) {
                    case SUCCESS:
                        mAdapter.notifyDataSetChanged();
                        break;
                    default:
                        Toast.makeText(getActivity(), prompt, Toast.LENGTH_LONG).show();
                        break;
                }
                return true;
            }
        });
    }
}
