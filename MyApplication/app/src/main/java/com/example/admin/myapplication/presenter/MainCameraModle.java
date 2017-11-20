package com.example.admin.myapplication.presenter;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.TextureView;

import com.example.admin.myapplication.model.RtItemModel;
import com.example.admin.myapplication.util.CameraSettings;
import com.example.admin.myapplication.util.RtRefocusManager;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

public class MainCameraModle implements TextureView.SurfaceTextureListener {

    private final String TAG = "MainCameraModle";
    private Camera mMainCamera;
    private int camId = 0;
    private int mFormat = ImageFormat.NV21;
    private int mMainPreviewWidth = 640;
    private int mMainPreviewHeight = 480;
    private String mCurrent = "none";
    private byte[][] mMemory;
    private IHandlePreviewFrame mHandlePreviewFrame;
    private int currentCameraId;

    private RtItemModel mRtMainItemModel;
    private RtItemModel mRtSubItemModel;
    private RtItemModel mOutItemModel;
    private RtRefocusManager mRtRefocusManager;
    private int count;

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
                mRtMainItemModel = new RtItemModel();
                mRtSubItemModel = new RtItemModel();
                mOutItemModel = new RtItemModel();
                //preview size: 1280x960;1280x720;640x480;768*432;480*360;640*360
                parameters.setPreviewSize(mMainPreviewWidth, mMainPreviewHeight);
                //parameters.setPictureFormat(ImageFormat.NV21);
                mMainCamera.setParameters(parameters);
                mMainCamera.setPreviewTexture(surfaceTexture);
                mMainCamera.setDisplayOrientation(90);
                parameters.setPreviewFormat(ImageFormat.NV21);
                // android.hardware.Camera.Parameters p = mMainCamera.getParameters();
                // android.hardware.Camera.Size preview = p.getPreviewSize();
                //  preview = p.getPreviewSize();
                // mMainPreviewWidth = preview.width;
                //  mMainPreviewHeight = preview.height;
                if (mHandlePreviewFrame != null) {
                    mHandlePreviewFrame.initYUVBuffer(mMainPreviewWidth, mMainPreviewHeight);
                }
                int length = mMainPreviewWidth * mMainPreviewHeight * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8;
                //final android.hardware.Camera.Size size = preview;
                //RtRefocusManager;
                mRtRefocusManager = new RtRefocusManager();
                mRtRefocusManager.init(640, 480, 640, 480);
                //添加一个点击对焦回调，然后保存对焦点，换算成preview surface的对焦掉
                mRtRefocusManager.aflocked();

                mMemory = new byte[2][length];
                mMainCamera.addCallbackBuffer(mMemory[0]);
                mMainCamera.setPreviewCallbackWithBuffer(new android.hardware.Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, android.hardware.Camera camera) {
                        // updateFrameData(data, yBuffer, uBuffer, vBuffer, ImageFormat.NV21, mSubPreviewWidth, mSubPreviewHeight);
                        synchronized (this) {
                            switch (currentCameraId) {
                                case CameraSettings.MAIN_CAMERA_ID:
                                    count++;
                                    if (count == 30) {
                                        /*
                                        // mRtMainItemModel.setMainBuffer(data);
                                        //  mRtMainItemModel.setMainW(640);
                                        // mRtMainItemModel.setMainH(480);
                                        // mRtMainItemModel.setMainFormat(ImageFormat.NV21);
                                        // mRtMainItemModel.setMainRotation(0);
                                        // mRtMainItemModel.setMainSW(-1);
                                        //  mRtMainItemModel.setMainSH(-1);
                                        // Log.d(TAG, "onPreviewFrame->" + Thread.currentThread().getName());
                                        //mRtRefocusManager.process(mRtMainItemModel, mRtSubItemModel, mOutItemModel);
                                        */
                                        mRtRefocusManager.postMainModelItem(mRtMainItemModel);
                                    }
                                    if (mHandlePreviewFrame != null) {
                                        mHandlePreviewFrame.handPreviewFrame(data, ImageFormat.NV21, mMainPreviewWidth, mMainPreviewHeight);
                                    }
                                    break;

                                case CameraSettings.SUB_CAMERA_ID:
                                    mRtRefocusManager.postSubModelItem(mRtSubItemModel);

                                    break;

                                default:
                                    //nothing to do
                                    break;


                            }


                            if (mMainCamera != null) {
                                mMainCamera.addCallbackBuffer(mMemory[0]);
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
        return mMainPreviewWidth;
    }

    public int getSubPreviewHeight() {
        return mMainPreviewHeight;
    }

    public interface IHandlePreviewFrame {
        public void handPreviewFrame(final byte[] data, int yv12, int mSubPreviewWidth, int mSubPreviewHeight);

        public void initYUVBuffer(int width, int height);
    }

    public void setCurrentCameraId(int cameraId) {
        this.currentCameraId = cameraId;
    }

}
