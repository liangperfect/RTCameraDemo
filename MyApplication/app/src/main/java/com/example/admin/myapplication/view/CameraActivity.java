package com.example.admin.myapplication.view;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.example.admin.myapplication.R;
import com.example.admin.myapplication.model.RtProcessor;
import com.example.admin.myapplication.presenter.MainCameraModle;
import com.example.admin.myapplication.util.CameraSettings;
import com.example.admin.myapplication.util.ImageHelper;
import com.example.admin.myapplication.util.MagicFilterView;
import com.example.admin.myapplication.util.PermissionsActivity;
import com.example.admin.myapplication.util.RtRefocusManager;

public class CameraActivity extends AppCompatActivity {

    private final String TAG = "CameraActivity";
    private boolean mHasCriticalPermissions;
    private Button btnSwitch;
    private RelativeLayout mRelativeLayout;
    private int mSubPreviewWidth = 1440;
    private int mSubPreviewHeight = 1080;

    //buffer data
    private ByteBuffer yBuffer = null;
    private ByteBuffer uBuffer = null;
    private ByteBuffer vBuffer = null;
    private MagicFilterView mMagicFilterView;

    private TextureView mTexture;
    private TextureView mSubTexture;
    private MainCameraModle mMainCameraModle;
    private MainCameraModle mSubCameraModle;
    private RtRefocusManager mRtRefocusManager;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (checkPermissions() || !mHasCriticalPermissions) {
            Log.v(TAG, "onCreate: Missing critical permissions.");
            finish();
            return;
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
        );
        setContentView(R.layout.activity_camera);
        getSupportActionBar().hide();
        initView();
    }

    private void initView() {
        mTexture = (TextureView) findViewById(R.id.texture);
        mSubTexture = (TextureView) findViewById(R.id.sub_texture);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.glsurface_content);
        btnSwitch = (Button) findViewById(R.id.btn_switch);
        mRtRefocusManager = new RtRefocusManager();
        mRtRefocusManager.init(1440, 1080, 1440, 1080);
        //添加一个点击对焦回调，然后保存对焦点，换算成preview surface的对焦掉
        mRtRefocusManager.aflocked();
        mMainCameraModle = new MainCameraModle();
        mMainCameraModle.setRtRefocusManager(mRtRefocusManager);
        mMainCameraModle.setCurrentCameraId(CameraSettings.MAIN_CAMERA_ID);
        mMainCameraModle.setIHandlePreviewFrame(new MainCameraModle.IHandlePreviewFrame() {

            @Override
            public void handPreviewFrame(byte[] data, int yv12, int mSubPreviewWidth, int mSubPreviewHeight) {
                ImageHelper.ImageData inData = new ImageHelper.ImageData(data, ImageFormat.YV12, mSubPreviewWidth, mSubPreviewHeight);
                ImageHelper.fillYUVBuffer(inData, yBuffer, uBuffer, vBuffer);
                mMagicFilterView.onFrame(data, yBuffer, uBuffer, vBuffer, yv12, mSubPreviewWidth, mSubPreviewHeight);
            }

            @Override
            public void initYUVBuffer(int width, int height) {
                initDataBuffer(width * height);
                mMagicFilterView = new MagicFilterView(CameraActivity.this, width, height);
                mRelativeLayout.addView(mMagicFilterView);
            }
        });
        mTexture.setSurfaceTextureListener(mMainCameraModle);
        mSubCameraModle = new MainCameraModle();
        mSubCameraModle.setRtRefocusManager(mRtRefocusManager);
        mSubCameraModle.setCurrentCameraId(CameraSettings.SUB_CAMERA_ID);
        mSubCameraModle.setCameraId(CameraSettings.SUB_CAMERA_ID);
        mSubTexture.setSurfaceTextureListener(mSubCameraModle);
        btnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取jni的值
                int jniResult1 = RtProcessor.getVersion();
                int jniResult2 = RtProcessor.aflocked();
                int jniResult3 = RtProcessor.dump();
                Toast.makeText(CameraActivity.this, "jniResult1:" + jniResult1 + "   jniResult2:" + jniResult2 + "   jniResult3:" + jniResult3, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Checks if any of the needed Android runtime permissions are missing.
     * If they are, then launch the permissions activity under one of the following conditions:
     * a) If critical permissions are missing, display permission request again
     * b) If non-critical permissions are missing, just display permission request once.
     * Critical permissions are: camera, microphone and storage. The app cannot run without them.
     * Non-critical permission is location.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean checkPermissions() {
        boolean requestPermission = false;
        if (checkSelfPermission(Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
                        PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {
            mHasCriticalPermissions = true;
        } else {
            mHasCriticalPermissions = false;
        }
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isRequestShown = prefs.getBoolean(CameraSettings.KEY_REQUEST_PERMISSION, false);
        if (!isRequestShown || !mHasCriticalPermissions) {
            Log.v(TAG, "Request permission");
            Intent intent = new Intent(this, PermissionsActivity.class);
            startActivity(intent);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(CameraSettings.KEY_REQUEST_PERMISSION, true);
            editor.apply();
            requestPermission = true;
        }
        return requestPermission;
    }

    //YUV buffer
    private void initDataBuffer(int yuvFrameSize) {
        // recycled by GC
        yBuffer = ByteBuffer.allocate(yuvFrameSize).order(ByteOrder.nativeOrder());
        yBuffer.position(0);
        uBuffer = ByteBuffer.allocate(yuvFrameSize >> 2).order(ByteOrder.nativeOrder());
        uBuffer.position(0);
        vBuffer = ByteBuffer.allocate(yuvFrameSize >> 2).order(ByteOrder.nativeOrder());
        vBuffer.position(0);
    }
}

