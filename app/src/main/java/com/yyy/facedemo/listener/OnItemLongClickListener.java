package com.yyy.facedemo.listener;

import android.view.View;

/**
 * Created by v_liujialu01 on 2019/6/21.
 */

@FunctionalInterface
public interface OnItemLongClickListener {
    void onLongItemClick(View view, int position);
}
