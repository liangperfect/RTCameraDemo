package com.example.admin.myapplication.presenter;

import java.nio.ByteBuffer;

public interface PreviewFrameCallback {
    public void onFrame(byte[] data, ByteBuffer y, ByteBuffer u, ByteBuffer v, int format, int w, int h);
}
