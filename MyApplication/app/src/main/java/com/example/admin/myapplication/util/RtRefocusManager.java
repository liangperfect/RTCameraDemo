package com.example.admin.myapplication.util;

import android.graphics.Camera;
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
    private boolean isProcess = false;
    private int count;

    public RtRefocusManager() {

        mThread = new RtRefocusThread(this);
        mThread.start();
    }

    ;

    public int init(int mainW, int mainH, int subW, int subH) {
        int r;
        r = RtProcessor.init(mainW, mainH, subW, subH);
        mOutRtModel = new RtItemModel();
        byte[] outBuffer = new byte[mainW * mainH * 3 >> 1];
        mOutRtModel.setMainBuffer(outBuffer);
        mOutRtModel.setMainW(mainW);
        mOutRtModel.setMainH(mainH);

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

        //已经是YV12的数据了
        /*
        count++;
        if (count == 15) {
            CameraUtil.getFile(mainFrame.getMainBuffer(), "/sdcard", "main15.yuv");
            CameraUtil.getFile(subFrame.getSubBuffer(), "/sdcard", "sub15.yuv");
        }


        if (count == 60) {
            CameraUtil.getFile(mainFrame.getMainBuffer(), "/sdcard", "main60.yuv");
            CameraUtil.getFile(subFrame.getSubBuffer(), "/sdcard", "sub60.yuv");
        }

        if (count == 100) {
            CameraUtil.getFile(mainFrame.getMainBuffer(), "/sdcard", "main100.yuv");
            CameraUtil.getFile(subFrame.getSubBuffer(), "/sdcard", "sub100.yuv");
        }

        if (count == 150) {
            CameraUtil.getFile(mainFrame.getMainBuffer(), "/sdcard", "main150.yuv");
            CameraUtil.getFile(subFrame.getSubBuffer(), "/sdcard", "sub150.yuv");
        }
*/
        byte[] mainBuffer = mainFrame.getMainBuffer();
        int mainW = mainFrame.getMainW();
        int mainH = mainFrame.getMainH();
        int mainFormat = mainFrame.getMainFormat();
        int mainRotation = mainFrame.getMainRotation();
        int mainSW = mainFrame.getMainSW();
        int mainSH = mainFrame.getMainSH();


        byte[] subBuffer = subFrame.getSubBuffer();
        int subW = mainFrame.getSubW();
        int subH = mainFrame.getSubH();
        int subFormat = mainFrame.getSubFormat();
        int subRotation = mainFrame.getSubRotation();
        int subSW = mainFrame.getSubSW();
        int subSH = mainFrame.getSubSH();

        byte[] outBuffer = outFrame.getMainBuffer();
        int outW = mainW;
        int outH = mainH;
        int outFormat = mainFormat;
        int outRotation = mainRotation;
        int outSW = mainSW;
        int outSH = mainSH;

        RtProcessor.process(mainBuffer, mainW, mainH, mainFormat, mainRotation, mainSW, mainSH,
                subBuffer, subW, subH, subFormat, subRotation, subSW, subSH,
                outBuffer, outW, outH, outFormat, outRotation, outSW, outSH);

        return 0;
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
            if (mSubRtModel != null && mOutRtModel != null) {
                Log.d(TAG, "can post data");
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
