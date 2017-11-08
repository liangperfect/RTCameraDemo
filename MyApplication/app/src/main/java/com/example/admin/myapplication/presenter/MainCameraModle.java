package com.example.admin.myapplication.presenter;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.TextureView;

import java.lang.reflect.Method;

public class MainCameraModle implements TextureView.SurfaceTextureListener {

    private Camera mMainCamera;
    private int camId = 0;
    private int mFormat = ImageFormat.NV21;
    private int mSubPreviewWidth = 640;
    private int mSubPreviewHeight = 480;
    private String mCurrent = "none";
    private byte[][] mMemory;
    private IHandlePreviewFrame mHandlePreviewFrame;

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        if (mMainCamera == null) {
            try {
                if (camId == 0) {
                    mMainCamera = Camera.open(camId);
                } else {
                    Method openMethod = Class.forName("android.hardware.Camera").getMethod(
                            "openLegacy", int.class, int.class);
                    mMainCamera = (Camera) openMethod.invoke(null, camId, 0x100);
                }
                android.hardware.Camera.Parameters parameters = mMainCamera.getParameters();
                if (parameters.getSupportedFocusModes().contains(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    parameters.setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                }
                //preview size: 1280x960;1280x720;640x480;768*432;480*360;640*360
                parameters.setPreviewSize(640, 480);
                //parameters.setPictureFormat(ImageFormat.NV21);
                mMainCamera.setParameters(parameters);
                Log.d("liang.chen", "liang.chen setPreviewTexture");
                mMainCamera.setPreviewTexture(surfaceTexture);
                mMainCamera.setDisplayOrientation(90);
                parameters.setPreviewFormat(ImageFormat.NV21);
                android.hardware.Camera.Parameters p = mMainCamera.getParameters();
                android.hardware.Camera.Size preview = p.getPreviewSize();
                preview = p.getPreviewSize();
                mSubPreviewWidth = preview.width;
                mSubPreviewHeight = preview.height;
                if (mHandlePreviewFrame != null) {
                    mHandlePreviewFrame.initYUVBuffer(mSubPreviewWidth, mSubPreviewHeight);
                }
                int length = preview.width * preview.height * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8;
                final android.hardware.Camera.Size size = preview;
                mMemory = new byte[2][length];
                mMainCamera.addCallbackBuffer(mMemory[0]);
                mMainCamera.setPreviewCallbackWithBuffer(new android.hardware.Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, android.hardware.Camera camera) {
                        // updateFrameData(data, yBuffer, uBuffer, vBuffer, ImageFormat.NV21, mSubPreviewWidth, mSubPreviewHeight);
                        synchronized (this) {
                            if (mHandlePreviewFrame != null) {
                                mHandlePreviewFrame.handPreviewFrame(data, ImageFormat.NV21, mSubPreviewWidth, mSubPreviewHeight);

                            }
                            if (mMainCamera != null) {
                                mMainCamera.addCallbackBuffer(mMemory[0]);
                                Log.d("liang.chen", "liang.chen->" + camId);
                            }
                        }

                    }
                });
                mMainCamera.startPreview();
            } catch (Exception e) {
                return;
            }
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
        if (mMainCamera != null) {
            mMainCamera.stopPreview();
            mMainCamera.release();
            mMainCamera = null;
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture texture) {

    }

    public void setIHandlePreviewFrame(IHandlePreviewFrame handlePreviewFrame) {
        this.mHandlePreviewFrame = handlePreviewFrame;
    }

    public void setCameraId(int id) {
        this.camId = id;
    }

    public int getFormat() {
        return mFormat;
    }

    public int getSubPreviewWidth() {
        return mSubPreviewWidth;
    }

    public int getSubPreviewHeight() {
        return mSubPreviewHeight;
    }

    public interface IHandlePreviewFrame {
        public void handPreviewFrame(final byte[] data, int yv12, int mSubPreviewWidth, int mSubPreviewHeight);

        public void initYUVBuffer(int width, int height);
    }




}
