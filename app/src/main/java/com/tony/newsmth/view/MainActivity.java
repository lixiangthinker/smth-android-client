package com.tony.newsmth.view;

/**
 * Created by l00151177 on 2016/9/27.
 */

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.tony.newsmth.R;
import com.tony.newsmth.view.fragment.BoardFragment;
import com.tony.newsmth.view.fragment.HotTopicFragment;
import com.tony.newsmth.view.fragment.MeFragment;
import com.tony.newsmth.view.fragment.MessageFragment;
import com.tony.newsmth.view.fragment.MyFavorFragment;

import cn.studyou.navigationviewlibrary.BottomNavigationItem;
import cn.studyou.navigationviewlibrary.BottomNavigationView;

public class MainActivity extends FragmentActivity {
    BottomNavigationView bottomNavigationView;
    private Fragment homeFragment;
    private Fragment boardFragment;
    private Fragment myFavorFragment;
    private Fragment messageFragment;
    private Fragment meFragment;
    private static int[] tabStringId = {R.string.tab_home, R.string.tab_board, R.string.tab_my_favor,
            R.string.tab_message, R.string.me};
    private static int[] tabColorId = {R.color.color_tab_icon_one, R.color.color_tab_icon_two, R.color.color_tab_icon_three,
            R.color.color_tab_icon_four, R.color.color_tab_icon_two};
    private static int[] tabIconId = {R.drawable.ic_home_24dp, R.drawable.ic_board_list_24dp, R.drawable.ic_favorite_24dp,
            R.drawable.ic_markunread_24dp, R.drawable.ic_user_info_24dp};
    private static final int ICON_NUM_MAX = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setStatusBar();
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.isWithText(true);
            bottomNavigationView.isColoredBackground(true);
            //bottomNavigationView.disableShadow();
            bottomNavigationView.isColoredBackground(false);
            bottomNavigationView.setItemActiveColorWithoutColoredBackground(getResources().getColor(R.color.color_tab_icon_four));
        }
        BottomNavigationItem[] bottomNavigationItem = {null, null, null, null, null};
        for (int i = 0; i < ICON_NUM_MAX; i++) {
            bottomNavigationItem[i] = new BottomNavigationItem(getString(tabStringId[i]),  getResources().getColor(tabColorId[i]),
                    tabIconId[i]);
        }
        selectedImages(0);
        for (int i = 0; i < ICON_NUM_MAX; i++) {
            bottomNavigationView.addTab(bottomNavigationItem[i]);
            bottomNavigationView.setOnBottomNavigationItemClickListener(new BottomNavigationView.OnBottomNavigationItemClickListener() {
                @Override
                public void onNavigationItemClick(int index) {
                    selectedImages(index);
                }
            });
        }
    }

    private void setStatusBar() {
        Window window = getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // finally change the color
        window.setStatusBarColor(getResources().getColor(R.color.colorPrimary));
    }

    /**
     * 设置选中
     *
     * @param i
     */
    private void selectedImages(int i) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        hideFragment(fragmentTransaction);
//        private Fragment homeFragment;
//        private Fragment boardFragment;
//        private Fragment myFavorFragment;
//        private Fragment messageFragment;
//        private Fragment meFragment;
        switch (i) {
            case 0:
                if (homeFragment == null) {
                    homeFragment = new HotTopicFragment();
                    fragmentTransaction.add(R.id.fragment_navigation, homeFragment);
                } else {
                    fragmentTransaction.show(homeFragment);
                }
                break;
            case 1:
                if (boardFragment == null) {
                    boardFragment = new BoardFragment();
                    fragmentTransaction.add(R.id.fragment_navigation, boardFragment);
                } else {
                    fragmentTransaction.show(boardFragment);
                }
                break;
            case 2:
                if (myFavorFragment == null) {
                    myFavorFragment = new MyFavorFragment();
                    fragmentTransaction.add(R.id.fragment_navigation, myFavorFragment);
                } else {
                    fragmentTransaction.show(myFavorFragment);
                }
                break;
            case 3:
                if (messageFragment == null) {
                    messageFragment = new MessageFragment();
                    fragmentTransaction.add(R.id.fragment_navigation, messageFragment);

                } else {
                    fragmentTransaction.show(messageFragment);
                }
                break;
            case 4:
                if (meFragment == null) {
                    meFragment = new MeFragment();
                    fragmentTransaction.add(R.id.fragment_navigation, meFragment);
                } else {
                    fragmentTransaction.show(meFragment);
                }
                break;
            default:
                break;

        }
        fragmentTransaction.commit();
    }

    /**
     * 初始化隐藏所有Fragment
     *
     * @param fragmentTransaction
     */
    private void hideFragment(FragmentTransaction fragmentTransaction) {
        if (homeFragment != null) {
            fragmentTransaction.hide(homeFragment);
        }
        if (messageFragment != null) {
            fragmentTransaction.hide(messageFragment);
        }
        if (meFragment != null) {
            fragmentTransaction.hide(meFragment);
        }
        if (boardFragment != null) {
            fragmentTransaction.hide(boardFragment);
        }
        if (myFavorFragment != null) {
            fragmentTransaction.hide(myFavorFragment);
        }
    }
}
