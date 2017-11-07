package com.example.admin.myapplication.util;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.example.admin.myapplication.presenter.PreviewFrameCallback;

public class MagicFilterView extends GLSurfaceView implements GLSurfaceView.Renderer, PreviewFrameCallback {
    private static String TAG = "MagicFilterView";

    private Context mContext;
    private int mPreViewFrameWidth = 0;
    private int mPreViewFrameHeight = 0;
    private int yuvFrameSize = 0;

    private int mSurfaceWidth;
    private int mSurfaceHeight;

    private MagicFilterDefault mDefaultFilterDraw;
    private MagicFilterDefault mFilterDraw;

    private ByteBuffer yBuffer = null;
    private ByteBuffer uBuffer = null;
    private ByteBuffer vBuffer = null;

    private int[] mYTextureID = new int[1];
    private int[] mUTextureID = new int[1];
    private int[] mVTextureID = new int[1];

    private boolean mSuspend = false;
    private int mFilterIndex = 4;
    public MagicFilterView(Context context) {
        super(context);
    }

    public MagicFilterView(Context context, int width, int height) {
        super(context);
        mContext = context;
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        setPreViewSize(width, height);
        initDataBuffer();
        //setZOrderOnTop(true);
    }

    public MagicFilterView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        GLES20.glEnable(GLES20.GL_TEXTURE_2D);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);

        if (mDefaultFilterDraw == null) {
            mDefaultFilterDraw = new MagicFilterDefault();
        }
        mDefaultFilterDraw.onInit();

        createTextureID(mPreViewFrameWidth, mPreViewFrameHeight, GLES20.GL_LUMINANCE, mYTextureID);
        createTextureID(mPreViewFrameWidth >> 1, mPreViewFrameHeight >> 1, GLES20.GL_LUMINANCE, mUTextureID);
        createTextureID(mPreViewFrameWidth >> 1, mPreViewFrameHeight >> 1, GLES20.GL_LUMINANCE, mVTextureID);

        setFilter(0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mSurfaceWidth = width;
        mSurfaceHeight = height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        if (!mSuspend) {
            if (yBuffer != null && yBuffer.hasArray()) {
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mYTextureID[0]);
                GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, mPreViewFrameWidth, mPreViewFrameHeight,
                        GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, yBuffer);

                GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mUTextureID[0]);
                GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, mPreViewFrameWidth >> 1, mPreViewFrameHeight >> 1,
                        GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, uBuffer);

                GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mVTextureID[0]);
                GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, mPreViewFrameWidth >> 1, mPreViewFrameHeight >> 1,
                        GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, vBuffer);

                if (mFilterDraw == null) {
                    mDefaultFilterDraw.draw();
                } else {
                    mFilterDraw.draw();
                }
            }
        }
    }

    public void setFilter(final int type) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mFilterDraw != null)
                    mFilterDraw.onDestroy();
                mFilterDraw = null;
                mFilterDraw = new MagicFilterPrague();
                if (mFilterDraw != null)
                    mFilterDraw.onInit();
                onFilterChanged();
            }
        });
    }

    public void setFilterIndex(int position) {
        mFilterIndex = position;
    }

    private void setPreViewSize(int width, int height) {
        mPreViewFrameWidth = width;
        mPreViewFrameHeight = height;
        yuvFrameSize = mPreViewFrameWidth * mPreViewFrameHeight;
    }

    private void initDataBuffer() {
        yBuffer = ByteBuffer.allocate(yuvFrameSize).order(ByteOrder.nativeOrder());
        yBuffer.position(0);
        uBuffer = ByteBuffer.allocate(yuvFrameSize >> 2).order(ByteOrder.nativeOrder());
        uBuffer.position(0);
        vBuffer = ByteBuffer.allocate(yuvFrameSize >> 2).order(ByteOrder.nativeOrder());
        vBuffer.position(0);
    }

    private void createTextureID(int width, int height, int format, int[] textureId) {
        //create and bind texture
        GLES20.glGenTextures(1, textureId, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, format, width, height,
                0, format, GLES20.GL_UNSIGNED_BYTE, null);
    }


    private void onFilterChanged() {
        if (mFilterDraw != null) {
            mFilterDraw.onDisplaySizeChanged(mSurfaceWidth, mSurfaceHeight);
            mFilterDraw.onInputSizeChanged(mSurfaceWidth, mSurfaceHeight);
        }
    }

    private void rotateYUV(byte[] data, int width, int height) {
        byte[] yArray = new byte[yBuffer.limit()];
        byte[] uArray = new byte[uBuffer.limit()];
        byte[] vArray = new byte[vBuffer.limit()];
        int nFrameSize = width * height;
        int k = 0;
        int uvCount = nFrameSize >> 1;

        for (int i = 0; i < nFrameSize; i++) {
            yArray[k] = data[i];
            k++;
        }

        k = 0;
        for (int i = 0; i < uvCount; i += 2) {
            vArray[k] = data[nFrameSize + i]; //v
            uArray[k] = data[nFrameSize + i + 1];//u
            k++;
        }

        yBuffer.put(yArray).position(0);
        uBuffer.put(uArray).position(0);
        vBuffer.put(vArray).position(0);
    }


    @Override
    public void onFrame(byte[] data, ByteBuffer y, ByteBuffer u, ByteBuffer v, int format, int w, int h) {
        yBuffer.clear();
        uBuffer.clear();
        vBuffer.clear();
        yBuffer = y;
        uBuffer = u;
        vBuffer = v;
        //加一个算法处理即可
        requestRender();
    }

    public void suspendRendering() {
        mSuspend = true;
    }

    public void resumeRendering() {
        mSuspend = false;
    }
}
