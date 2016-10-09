package com.tony.newsmth.controller;

import android.content.Context;
import android.util.Log;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.tony.newsmth.model.Board;
import com.tony.newsmth.model.BoardListContent;
import com.tony.newsmth.model.BoardSection;
import com.tony.newsmth.net.SmthApi;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
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
 * Created by l00151177 on 2016/9/28.
 */
public class BoardListControllerImpl implements IBoardListController {
    private static final String TAG = "BoardListControllerImpl";
    // All boards cache file
    public static int BOARD_TYPE_FAVORITE = 1;
    public static int BOARD_TYPE_ALL = 2;
    static private final String ALL_BOARD_CACHE_FILE = "SMTH_ALLBD_CACHE_KRYO";
    static private final String FAVORITE_BOARD_CACHE_PREFIX = "SMTH_FAVBD_CACHE_KYRO";
    private final Context mContext;

    public BoardListControllerImpl(Context context) {
        mContext = context;
    }

    public List<Board> getAllBoards() {
        final List<Board> result = new ArrayList<>();
        // all boards loaded in cached file
        final Observable<List<Board>> cache = Observable.create(new Observable.OnSubscribe<List<Board>>() {
            @Override
            public void call(Subscriber<? super List<Board>> subscriber) {
                List<Board> boards = getBoardListFromCache(BOARD_TYPE_ALL, null);
                if (boards != null && boards.size() > 0) {
                    subscriber.onNext(boards);
                } else {
                    subscriber.onCompleted();
                }
            }
        });

        // all boards loaded from network
        final Observable<List<Board>> network = Observable.create(new Observable.OnSubscribe<List<Board>>() {
            @Override
            public void call(Subscriber<? super List<Board>> subscriber) {
                List<Board> boards = getAllBoardsFromWWW();
                if (boards != null && boards.size() > 0) {
                    subscriber.onNext(boards);
                } else {
                    subscriber.onCompleted();
                }
            }
        });

        // use the first available source to load all boards
        Observable.concat(cache, network)
                .first()
                .flatMap(new Func1<List<Board>, Observable<Board>>() {
                    @Override
                    public Observable<Board> call(List<Board> boards) {
                        return Observable.from(boards);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Board>() {
                    @Override
                    public void onStart() {
                        if (onBoardListResultListener != null) {
                            onBoardListResultListener.onStart();
                        }
                    }

                    @Override
                    public void onCompleted() {
                        if (onBoardListResultListener != null) {
                            onBoardListResultListener.onResult(BoardListResult.SUCCESS, result, "get all board lists");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (onBoardListResultListener != null) {
                            onBoardListResultListener.onResult(BoardListResult.FAILED_COMMON, result, "fail to get board list");
                        }
                    }

                    @Override
                    public void onNext(Board board) {
                        result.add(board);
                        if (onBoardListResultListener != null) {
                            onBoardListResultListener.onNewBoard(board);
                        }
                    }
                });
        return result;
    }

    // load all boards from WWW, recursively
    // http://stackoverflow.com/questions/31246088/how-to-do-recursive-observable-call-in-rxjava
    public List<Board> getAllBoardsFromWWW() {
        final String[] SectionNames = {"社区管理", "国内院校", "休闲娱乐", "五湖四海", "游戏运动", "社会信息", "知性感性", "文化人文", "学术科学", "电脑技术", "终止版面"};
        final String[] SectionURLs = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A"};
        final List<BoardSection> sections = new ArrayList<>();
        for (int index = 0; index < SectionNames.length; index++) {
            BoardSection section = new BoardSection();
            section.sectionURL = SectionURLs[index];
            section.sectionName = SectionNames[index];
            sections.add(section);
        }

        List<Board> boards = Observable.from(sections)
                .flatMap(new Func1<BoardSection, Observable<Board>>() {
                    @Override
                    public Observable<Board> call(BoardSection section) {
                        return loadBoardsInSectionFromWWW(section);
                    }
                })
                .flatMap(new Func1<Board, Observable<Board>>() {
                    @Override
                    public Observable<Board> call(Board board) {
                        return loadChildBoardsRecursivelyFromWWW(board);
                    }
                })
                .filter(new Func1<Board, Boolean>() {
                    @Override
                    public Boolean call(Board board) {
                        // keep board only
                        return !board.isFolder();
                    }
                })
                // http://stackoverflow.com/questions/26311513/convert-observable-to-list
                .toList().toBlocking().single();

        // sort the board list by chinese name
        Collections.sort(boards, new BoardListContent.ChineseComparator());
        Log.d("LoadAllBoardsFromWWW", String.format("%d boards loaded from network", boards.size()));

        // save boards to disk
        saveBoardListToCache(boards, BOARD_TYPE_ALL, null);

        return boards;
    }

    private OnBoardListResultListener onBoardListResultListener = null;

    @Override
    public void setOnBoardListResultListener(OnBoardListResultListener l) {
        onBoardListResultListener = l;
    }

    public Observable<Board> loadBoardsInSectionFromWWW(final BoardSection section) {
        String sectionURL = section.sectionURL;
        SmthApi netApi = SmthApi.getInstance(mContext);
        return netApi.getService().getBoardsBySection(sectionURL)
                .flatMap(new Func1<ResponseBody, Observable<Board>>() {
                    @Override
                    public Observable<Board> call(ResponseBody responseBody) {
                        try {
                            String response = responseBody.string();
                            List<Board> boards = parseBoardsInSectionFromWWW(response, section);
                            return Observable.from(boards);

                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                });
    }

    public List<Board> parseBoardsInSectionFromWWW(String content, BoardSection section) {
        List<Board> boards = new ArrayList<>();

//        <tr><td class="title_1"><a href="/nForum/section/Association">协会社团</a><br />Association</td><td class="title_2">[二级目录]<br /></td><td class="title_3">&nbsp;</td><td class="title_4 middle c63f">&nbsp;</td><td class="title_5 middle c09f">&nbsp;</td><td class="title_6 middle c63f">&nbsp;</td><td class="title_7 middle c09f">&nbsp;</td></tr>
//        <tr><td class="title_1"><a href="/nForum/board/BIT">北京理工大学</a><br />BIT</td><td class="title_2"><a href="/nForum/user/query/mahenry">mahenry</a><br /></td><td class="title_3"><a href="/nForum/article/BIT/250116">今年几万斤苹果都滞销了，果农欲哭无泪！</a><br />发贴人:&ensp;jingling6787 日期:&ensp;2016-03-22 09:19:09</td><td class="title_4 middle c63f">11</td><td class="title_5 middle c09f">2</td><td class="title_6 middle c63f">5529</td><td class="title_7 middle c09f">11854</td></tr>
//        <tr><td class="title_1"><a href="/nForum/board/Orienteering">定向越野</a><br />Orienteering</td><td class="title_2"><a href="/nForum/user/query/onceloved">onceloved</a><br /></td><td class="title_3"><a href="/nForum/article/Orienteering/59193">圆明园定向</a><br />发贴人:&ensp;jiang2000 日期:&ensp;2016-03-19 14:19:10</td><td class="title_4 middle c63f">0</td><td class="title_5 middle c09f">0</td><td class="title_6 middle c63f">4725</td><td class="title_7 middle c09f">18864</td></tr>

        Document doc = Jsoup.parse(content);
        // get all tr
        Elements trs = doc.select("table.board-list tr");
        for (Element tr : trs) {
//            Log.d("Node", tr.toString());

            Elements t1links = tr.select("td.title_1 a[href]");
            if (t1links.size() == 1) {
                Element link1 = t1links.first();
                String temp = link1.attr("href");

                String chsBoardName = "";
                String engBoardName = "";
                String moderator = "";
                String folderChsName = "";
                String folderEngName = "";

                Pattern boardPattern = Pattern.compile("/nForum/board/(\\w+)");
                Matcher boardMatcher = boardPattern.matcher(temp);
                if (boardMatcher.find()) {
                    engBoardName = boardMatcher.group(1);
                    chsBoardName = link1.text();
                    // it's a normal board
                    Elements t2links = tr.select("td.title_2 a[href]");
                    if (t2links.size() == 1) {
                        // if we can find link to moderator, set moderator
                        // it's also possible that moderator is empty, so no link can be found
                        Element link2 = t2links.first();
                        moderator = link2.text();
                    }

                    Board board = new Board("", chsBoardName, engBoardName);
                    board.setModerator(moderator);
                    board.setCategoryName(section.getBoardCategory());
                    boards.add(board);

                }

                Pattern sectionPattern = Pattern.compile("/nForum/section/(\\w+)");
                Matcher sectionMatcher = sectionPattern.matcher(temp);
                if (sectionMatcher.find()) {
                    // it's a section
                    folderEngName = sectionMatcher.group(1);
                    folderChsName = link1.text();

                    Board board = new Board(folderEngName, folderChsName);
                    board.setCategoryName(section.sectionName);
                    boards.add(board);
                }

//                Log.d("parse", String.format("%s, %s, %s, %s, %s", chsBoardName, engBoardName, folderChsName, folderEngName, moderator));
            }

        }

        return boards;
    }

    public Observable<Board> loadChildBoardsRecursivelyFromWWW(Board board) {
        if (board.isFolder()) {
            BoardSection section = new BoardSection();
            section.sectionURL = board.getFolderID();
            section.sectionName = board.getFolderName();
            section.parentName = board.getCategoryName();

            // load recruisively
            return loadBoardsInSectionFromWWW(section)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .flatMap(new Func1<Board, Observable<Board>>() {
                        @Override
                        public Observable<Board> call(Board board) {
                            return loadChildBoardsRecursivelyFromWWW(board);
                        }
                    });
        } else {
            return Observable.just(board);
        }
    }

    public void saveBoardListToCache(List<Board> boards, int type, String folder) {
        String filename = getCacheFile(type, folder);
        try {
            Kryo kryo = new Kryo();
            Output output = new Output(mContext.getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE));
            kryo.writeObject(output, boards);
            output.close();
            Log.d("SaveBoardListToCache", String.format("%d boards saved to cache file %s", boards.size(), filename));
        } catch (Exception e) {
            Log.d("SaveBoardListToCache", e.toString());
            Log.d("SaveBoardListToCache", "failed to save boards to cache file " + filename);
        }
    }

    /*
    * All Boards related methods
    * Starts here
     */
    public String getCacheFile(int type, String folder) {
        if (type == BOARD_TYPE_ALL) {
            return ALL_BOARD_CACHE_FILE;
        } else if (type == BOARD_TYPE_FAVORITE) {
            if (folder == null || folder.length() == 0) {
                folder = "ROOT";
            }
            return String.format("%s-%s", FAVORITE_BOARD_CACHE_PREFIX, folder);
        }
        return null;
    }

    public List<Board> getBoardListFromCache(int type, String folder) {
        String filename = getCacheFile(type, folder);
        List<Board> boards = new ArrayList<>();
        try {
            Kryo kryo = new Kryo();
            Input input = new Input(mContext.getApplicationContext().openFileInput(filename));
            boards = (List<Board>) kryo.readObject(input, ArrayList.class);
            input.close();
            Log.d(TAG, "boards size = " + boards.size() + " cache file = " + filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return boards;
    }
}
