package com.example.admin.myapplication.model;

/**
 * Created by admin on 2017/11/20.
 */

public class RtItemModel {
    /*main frame data*/
    private byte[] mainBuffer;
    private int mainW;
    private int mainH;
    private int mainFormat;
    private int mainRotation;
    private int mainSW;
    private int mainSH;

    /*sub frame data*/
    private byte[] subBuffer;
    private int subW;
    private int subH;
    private int subFormat;
    private int subRotation;
    private int subSW;
    private int subSH;

    public byte[] getMainBuffer() {
        return mainBuffer;
    }

    public void setMainBuffer(byte[] mainBuffer) {
        this.mainBuffer = mainBuffer;
    }

    public int getMainW() {
        return mainW;
    }

    public void setMainW(int mainW) {
        this.mainW = mainW;
    }

    public int getMainH() {
        return mainH;
    }

    public void setMainH(int mainH) {
        this.mainH = mainH;
    }

    public int getMainFormat() {
        return mainFormat;
    }

    public void setMainFormat(int mainFormate) {
        this.mainFormat = mainFormate;
    }

    public int getMainRotation() {
        return mainRotation;
    }

    public void setMainRotation(int mainRotation) {
        this.mainRotation = mainRotation;
    }

    public int getMainSW() {
        return mainSW;
    }

    public void setMainSW(int mainSW) {
        this.mainSW = mainSW;
    }

    public int getMainSH() {
        return mainSH;
    }

    public void setMainSH(int mainSH) {
        this.mainSH = mainSH;
    }

    public byte[] getSubBuffer() {
        return subBuffer;
    }

    public void setSubBuffer(byte[] subBuffer) {
        this.subBuffer = subBuffer;
    }

    public int getSubW() {
        return subW;
    }

    public void setSubW(int subW) {
        this.subW = subW;
    }

    public int getSubH() {
        return subH;
    }

    public void setSubH(int subH) {
        this.subH = subH;
    }

    public int getSubFormat() {
        return subFormat;
    }

    public void setSubFormat(int subFormat) {
        this.subFormat = subFormat;
    }

    public int getSubRotation() {
        return subRotation;
    }

    public void setSubRotation(int subRotation) {
        this.subRotation = subRotation;
    }

    public int getSubSW() {
        return subSW;
    }

    public void setSubSW(int subSW) {
        this.subSW = subSW;
    }

    public int getSubSH() {
        return subSH;
    }

    public void setSubSH(int subSH) {
        this.subSH = subSH;
    }
}
