package com.example.admin.myapplication.util;

import android.graphics.PointF;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * Created by admin on 2017/06/29   .
 */

public class MagicFilterDefault {
    private int mPositionHandle;
    private int mTexCoordHandle;
    private int mYTextureUniformHandle;
    private int mUTextureUniformHandle;
    private int mVTextureUniformHandle;

    private int vertexShaderHandle;
    private int fragementShaderHandle;
    protected int mProgramHandle;

    private FloatBuffer mModelDataBuffer;
    private FloatBuffer mTexCoordBuffer;

    private final int mBytesPerFloat = 4;

    private String mVertexShader;
    private String mFragmentShader;

    private final ArrayList<Runnable> mRunOnDraw;
    protected int mOutputWidth, mOutputHeight;

    protected static final String mVertexSource =
            "attribute vec4 a_Position;\n" +
                    "attribute vec2 a_TexCoord;\n" +
                    "\n" +
                    "varying lowp vec2 v_TexCoord;\n" +
                    "\n" +
                    "void main()\n" +
                    "{\n" +
                    "    gl_Position = a_Position;\n" +
                    "    v_TexCoord =  a_TexCoord;\n" +
                    "}";
    private static final String mFragmentSource = "" +
            "varying lowp vec2 v_TexCoord;\n" +
            "uniform sampler2D SamplerY;\n" +
            "uniform sampler2D SamplerU;\n" +
            "uniform sampler2D SamplerV;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "    gl_FragColor = vec4(0,0,0,0);\n" +
            "}";


    private final float[] positionData = {
            -1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,
            1.0f, -1.0f,

    };

    private final float[] texCoord = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f

    };

    public MagicFilterDefault() {
        this(mVertexSource, mFragmentSource);
    }

    public MagicFilterDefault(final String vertexShader, final String fragmentShader) {
        mRunOnDraw = new ArrayList<>();
        mVertexShader = vertexShader;
        mFragmentShader = fragmentShader;

        mModelDataBuffer = ByteBuffer.allocateDirect(positionData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mModelDataBuffer.put(positionData).position(0);

        mTexCoordBuffer = ByteBuffer.allocateDirect(texCoord.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTexCoordBuffer.put(texCoord).position(0);
    }

    public void onInit() {
        vertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, mVertexShader);
        fragementShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, mFragmentShader);
        mProgramHandle = ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragementShaderHandle,
                new String[]{"a_Position", "a_TexCoord"});
        if (mProgramHandle != -1) {
            mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");
            mTexCoordHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_TexCoord");
            mYTextureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "SamplerY");
            mUTextureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "SamplerU");
            mVTextureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "SamplerV");
        }
    }

    protected void onInitialized() {

    }

    public void draw() {
        GLES20.glUseProgram(mProgramHandle);
        runPendingOnDrawTasks();
        GLES20.glUniform1i(mYTextureUniformHandle, 0);
        GLES20.glUniform1i(mUTextureUniformHandle, 1);
        GLES20.glUniform1i(mVTextureUniformHandle, 2);

        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glEnableVertexAttribArray(mTexCoordHandle);

        mModelDataBuffer.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 0, mModelDataBuffer);

        mTexCoordBuffer.position(0);
        GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mTexCoordBuffer);

        onDrawArraysPre();
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordHandle);
        onDrawArraysAfter();
    }

    protected void runPendingOnDrawTasks() {
        while (!mRunOnDraw.isEmpty()) {
            mRunOnDraw.remove(0).run();
        }
    }

    protected void onDrawArraysPre() {
    }

    protected void onDrawArraysAfter() {
    }

    public void onDestroy() {
        destroyFramebuffers();
    }

    public void destroyFramebuffers() {
    }

    public void onInputSizeChanged(final int width, final int height) {
        setTexelSize(width, height);

    }

    public void onDisplaySizeChanged(final int width, final int height) {
        mOutputWidth = width;
        mOutputHeight = height;
    }

    private void setTexelSize(final float w, final float h) {
    }

    protected void setFloatVec2(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform2fv(location, 1, FloatBuffer.wrap(arrayValue));
            }
        });
    }

    protected void setFloatDraw(final int loacation, final float value) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glVertexAttrib1f(loacation, value);
            }
        });
    }

    protected void setFloat(final int location, final float floatValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform1f(location, floatValue);
            }
        });
    }

    protected void setPoint(final int location, final PointF point) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                float[] vec2 = new float[2];
                vec2[0] = point.x;
                vec2[1] = point.y;
                GLES20.glUniform2fv(location, 1, vec2, 0);
            }
        });
    }

    protected void setFloatVec3(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform3fv(location, 1, FloatBuffer.wrap(arrayValue));
            }
        });
    }

    protected void runOnDraw(final Runnable runnable) {
        synchronized (mRunOnDraw) {
            mRunOnDraw.add(runnable);
        }
    }
}
