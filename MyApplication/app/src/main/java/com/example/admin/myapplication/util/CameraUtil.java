package com.example.admin.myapplication.util;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by admin on 2017/11/20.
 */

public class CameraUtil {
    public static void getFile(byte[] bfile, String filePath, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(filePath);
            if (!dir.exists() && dir.isDirectory()) {//判断文件目录是否存在
                dir.mkdirs();
            }
            file = new File(filePath + "//" + fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public static byte[] nv21ToYV12(ByteBuffer byteBuffer, int frameSize) {

        int totalSize = frameSize + frameSize / 2;
        //int dataSize = frameSize + frameSize / 2;
        byte[] nv21Data = byteBuffer.array();
        byte[] dataY = new byte[frameSize];
        byte[] dataV = new byte[frameSize >> 2];
        byte[] dataU = new byte[frameSize >> 2];

        for (int i = 0; i < frameSize; i++) {
            dataY[i] = nv21Data[i];
        }
        int k = 0;
        for (int j = 0; j < frameSize >> 1; j += 2) {
            dataV[k] = nv21Data[frameSize + j];
            dataU[k] = nv21Data[frameSize + j + 1];
            k++;
        }
        ByteBuffer yv12Buffer = ByteBuffer.allocate(totalSize);
        yv12Buffer.put(dataY, 0, frameSize);
        yv12Buffer.put(dataV, 0, frameSize >> 2);
        yv12Buffer.put(dataU, 0, frameSize >> 2);
        yv12Buffer.position(0);
        return yv12Buffer.array();
    }
}
