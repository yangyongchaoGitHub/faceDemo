package com.yyy.facedemo.listener;

/**
 * 导入相关listener
 * Created by liujialu on 2019/6/3.
 */

public interface OnImportListener {

    void startUnzip();

    void showProgressView();

    void onImporting(int finishCount, int successCount, int failureCount, float progress);

    void endImport(int finishCount, int successCount, int failureCount);

    void showToastMessage(String message);
}
