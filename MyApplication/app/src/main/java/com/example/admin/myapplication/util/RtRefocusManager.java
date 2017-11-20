package com.example.admin.myapplication.util;

import android.util.Log;

import com.example.admin.myapplication.model.RtItemModel;
import com.example.admin.myapplication.model.RtProcessor;

import java.io.File;

public class RtRefocusManager {

    private final String TAG = "RtRefocusManager";
    private RtRefocusThread mThread;
    private RtItemModel mMainRtModel;
    private RtItemModel mSubRtModel;
    private RtItemModel mOutRtModel;

    public RtRefocusManager() {
        mOutRtModel = new RtItemModel();
        mThread = new RtRefocusThread(this);
        mThread.start();
    }

    public int init(int mainW, int mainH, int subW, int subH) {
        int r;
        r = RtProcessor.init(mainW, mainH, subW, subH);
        if (0 == r) {
            return r;
        } else {
            Log.e(TAG, "init dcs error");
            return r;
        }
    }

    public int aflocked() {
        int r;
        r = RtProcessor.aflocked();
        if (0 == r) {
            return r;
        } else {
            Log.d(TAG, "aflocked is error");
            return r;
        }
    }

    public int process(RtItemModel mainFrame, RtItemModel subFrame, RtItemModel outFrame) {
        int r = 0;
        //Log.d(TAG, "liang.chen ->process");
        //CameraUtil.getFile(mainFrame.getMainBuffer(), "123", "123");
        Log.d(TAG, "liang.chen process");
        return r = 0;
    }

    public int dump() {
        int r = 0;

        return r;
    }

    public int uninit() {
        int r = 0;


        return r;
    }

    public int getVersion() {
        int r = 0;

        return r;
    }

    public synchronized void postMainModelItem(RtItemModel mainItemModel) {
        if (mainItemModel != null) {
            this.mMainRtModel = mainItemModel;
            if (mMainRtModel != null && mSubRtModel != null && mOutRtModel != null) {
                mThread.setRtItemModel(mMainRtModel, mSubRtModel, mOutRtModel);
            }
        }
    }

    public synchronized void postSubModelItem(RtItemModel subItemModel) {
        if (subItemModel != null) {
            this.mSubRtModel = subItemModel;
        }
    }

    public synchronized RtItemModel getOutRtModel() {
        if (mOutRtModel != null)
            return mOutRtModel;
        return null;
    }

}
