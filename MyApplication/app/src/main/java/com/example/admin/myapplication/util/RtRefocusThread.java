package com.example.admin.myapplication.util;

import android.util.Log;

import com.example.admin.myapplication.model.RtItemModel;

public class RtRefocusThread extends Thread {

    private final String TAG = "RtRefocusThread";
    private RtRefocusManager mRtRefocusManager;
    private RtItemModel mMainItemModel;
    private RtItemModel mSubItemModel;
    private RtItemModel mOutItemModel;

    public RtRefocusThread(RtRefocusManager manager) {
        this.mRtRefocusManager = manager;
    }

    @Override
    public void run() {
        super.run();
        synchronized (mRtRefocusManager) {
            while (true) {
                try {
                    if (mMainItemModel != null && mSubItemModel != null && mOutItemModel != null) {
                        mRtRefocusManager.process(mMainItemModel, mSubItemModel, mOutItemModel);
                    }
                    mRtRefocusManager.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void setRtItemModel(RtItemModel mainModel, RtItemModel subModel, RtItemModel outItemModel) {
        synchronized (mRtRefocusManager) {
            this.mMainItemModel = mainModel;
            this.mSubItemModel = subModel;
            this.mOutItemModel = outItemModel;
            mRtRefocusManager.notify();
        }
    }
}



