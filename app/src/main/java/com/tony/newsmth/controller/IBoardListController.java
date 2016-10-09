package com.tony.newsmth.controller;

import com.tony.newsmth.model.Board;

import java.util.List;

/**
 * Created by l00151177 on 2016/9/28.
 */
public interface IBoardListController {
    enum BoardListResult {
        SUCCESS, FAILED_COMMON, FAILED_TIMEOUT, FAILED_INVALIDE_PARAM, FAILED_TOO_MUCH_ACTION
    }

    List<Board> getAllBoards();

    void setOnBoardListResultListener(OnBoardListResultListener l);

    interface OnBoardListResultListener {
        boolean onStart();

        boolean onNewBoard(Board board);

        boolean onResult(BoardListResult result, List<Board> boardsList, String prompt);
    }
}
