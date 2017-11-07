package com.example.admin.myapplication.util;

import android.graphics.ImageFormat;

import java.nio.ByteBuffer;

public class ImageHelper {
    private static final double ASPECT_TOLERANCE = 0.001d;

    public static boolean fillYUVBuffer(ImageData inData, ByteBuffer yBuffer, ByteBuffer uBuffer, ByteBuffer vBuffer) {
        if (inData == null || inData.data == null) {
            return false;
        }

        int nFrameSize = inData.width * inData.height;
        yBuffer.clear();
        uBuffer.clear();
        vBuffer.clear();
        if (inData.format == ImageFormat.YV12) {
            // yv12=YYYYYYYY VV UU
            yBuffer.put(inData.data, 0, nFrameSize).position(0);
            vBuffer.put(inData.data, nFrameSize, nFrameSize >> 2).position(0);
            uBuffer.put(inData.data, nFrameSize + (nFrameSize >> 2), nFrameSize >> 2).position(0);
        } else {
            byte[] yArray = new byte[yBuffer.limit()];
            byte[] uArray = new byte[uBuffer.limit()];
            byte[] vArray = new byte[vBuffer.limit()];
            int k = 0;
            int uvCount = nFrameSize >> 1;
            // NV21=YUV420SP=YYYYYYYY VUVU
            for (int i = 0; i < nFrameSize; i++) {
                yArray[k] = inData.data[i];
                k++;
            }

            k = 0;
            for (int i = 0; i < uvCount; i += 2) {
                vArray[k] = inData.data[nFrameSize + i]; // v
                uArray[k] = inData.data[nFrameSize + i + 1];// u
                k++;
            }
            yBuffer.put(yArray).position(0);
            uBuffer.put(uArray).position(0);
            vBuffer.put(vArray).position(0);
        }
        return true;
    }

    public static boolean equals(double d1, double d2) {
        return Math.abs(d1 - d2) <= ASPECT_TOLERANCE;
    }

    public static class ImageData {
        public ImageData(byte[] data, int format, int width, int height) {
            this.data = data;
            this.format = format;
            this.width = width;
            this.height = height;
        }

        byte[] data;
        int format;
        int width;
        int height;
    }
}
