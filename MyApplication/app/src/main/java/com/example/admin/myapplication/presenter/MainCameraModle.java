package com.example.admin.myapplication.presenter;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.TextureView;
import android.widget.TableLayout;

import com.example.admin.myapplication.model.RtItemModel;
import com.example.admin.myapplication.util.CameraSettings;
import com.example.admin.myapplication.util.CameraUtil;
import com.example.admin.myapplication.util.RtRefocusManager;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

public class MainCameraModle implements TextureView.SurfaceTextureListener {

    private final String TAG = "MainCameraModel";
    private Camera mMainCamera;
    private int camId = 0;
    private int mFormat = ImageFormat.NV21;
    private int mMainPreviewWidth = 1440;
    private int mMainPreviewHeight = 1080;
    private String mCurrent = "none";
    private byte[][] mMemory;
    private IHandlePreviewFrame mHandlePreviewFrame;
    private int currentCameraId;

    private RtItemModel mRtMainItemModel;
    private RtItemModel mRtSubItemModel;
    private RtItemModel mOutItemModel;
    private RtRefocusManager mRtRefocusManager;

    private int mFrameSize = mMainPreviewWidth * mMainPreviewHeight + mMainPreviewWidth * mMainPreviewHeight / 2;
    private int mFrameSize1 = mMainPreviewWidth * mMainPreviewHeight;
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
                parameters.setPreviewSize(mMainPreviewWidth, mMainPreviewHeight);
                mMainCamera.setParameters(parameters);
                mMainCamera.setPreviewTexture(surfaceTexture);
                mMainCamera.setDisplayOrientation(90);
                parameters.setPreviewFormat(ImageFormat.NV21);
                if (mHandlePreviewFrame != null) {
                    mHandlePreviewFrame.initYUVBuffer(mMainPreviewWidth, mMainPreviewHeight);
                }
                int length = mMainPreviewWidth * mMainPreviewHeight * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8;
                mMemory = new byte[2][length];
                mMainCamera.addCallbackBuffer(mMemory[0]);
                mMainCamera.setPreviewCallbackWithBuffer(new android.hardware.Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, android.hardware.Camera camera) {
                        synchronized (this) {
                            switch (currentCameraId) {
                                case CameraSettings.MAIN_CAMERA_ID:
                                    ByteBuffer mainBuffer = ByteBuffer.allocate(mFrameSize);
                                    mainBuffer.put(data, 0, mFrameSize);
                                    mRtMainItemModel.setMainBuffer(CameraUtil.nv21ToYV12(mainBuffer, mFrameSize1));
                                    mRtMainItemModel.setMainW(mMainPreviewWidth);
                                    mRtMainItemModel.setMainH(mMainPreviewHeight);
                                    mRtMainItemModel.setMainFormat(ImageFormat.YV12);
                                    mRtMainItemModel.setMainRotation(0);
                                    mRtMainItemModel.setMainSW(-1);
                                    mRtMainItemModel.setMainSH(-1);
                                    mRtRefocusManager.postMainModelItem(mRtMainItemModel);
                                    if (mHandlePreviewFrame != null) {
                                        mHandlePreviewFrame.handPreviewFrame(mRtRefocusManager.getOutRtModel().getMainBuffer(), ImageFormat.NV21, mMainPreviewWidth, mMainPreviewHeight);
                                    }
                                    break;
                                case CameraSettings.SUB_CAMERA_ID:
                                    ByteBuffer subBuffer = ByteBuffer.allocate(mFrameSize);
                                    subBuffer.put(data, 0, mFrameSize);
                                    mRtSubItemModel.setSubBuffer(CameraUtil.nv21ToYV12(subBuffer, mFrameSize1));
                                    mRtSubItemModel.setSubW(mMainPreviewWidth);
                                    mRtSubItemModel.setSubH(mMainPreviewHeight);
                                    mRtSubItemModel.setSubFormat(ImageFormat.YV12);
                                    mRtSubItemModel.setSubRotation(0);
                                    mRtSubItemModel.setSubSW(-1);
                                    mRtSubItemModel.setSubSH(-1);
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

    public void setRtRefocusManager(RtRefocusManager manager) {
        this.mRtRefocusManager = manager;
    }

}
