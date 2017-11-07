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
import com.example.admin.myapplication.util.CameraSettings;
import com.example.admin.myapplication.util.ImageHelper;
import com.example.admin.myapplication.util.MagicFilterView;
import com.example.admin.myapplication.util.PermissionsActivity;

public class CameraActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    private final String TAG = "CameraActivity";
    private static final int CAMERA_HAL_API_VERSION_1_0 = 0x100;
    private boolean mHasCriticalPermissions;
    private int camId = 0;//camera IDs
    private Camera mCamera;
    private Button btnSwitch;
    private RelativeLayout mRelativeLayout;
    private byte[][] mMemory;
    private int count = 0;

    private int mFormat = ImageFormat.NV21;
    private int mSubPreviewWidth = 640;
    private int mSubPreviewHeight = 480;
    private String mCurrent = "none";

    //buffer data
    private ByteBuffer yBuffer = null;
    private ByteBuffer uBuffer = null;
    private ByteBuffer vBuffer = null;
    private MagicFilterView mMagicFilterView;

    private TextureView mTexture;

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
        mTexture.setSurfaceTextureListener(this);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.glsurface_content);
        btnSwitch = (Button) findViewById(R.id.btn_switch);
        btnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(CameraActivity.this, "Camera Activity btn ce shi", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        if (mCamera == null) {
            try {
                mCamera = Camera.open();
                Camera.Parameters parameters = mCamera.getParameters();
                if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                }
                //preview size: 1280x960;1280x720;640x480;768*432;480*360;640*360
                parameters.setPreviewSize(640, 480);
                //parameters.setPictureFormat(ImageFormat.NV21);
                mCamera.setParameters(parameters);
                mCamera.setPreviewTexture(surfaceTexture);
                mCamera.setDisplayOrientation(90);
                parameters.setPreviewFormat(ImageFormat.YV12);
                Camera.Parameters p = mCamera.getParameters();
                Camera.Size preview = p.getPreviewSize();
                preview = p.getPreviewSize();
                mSubPreviewWidth = preview.width;
                mSubPreviewHeight = preview.height;
                initDataBuffer(mSubPreviewHeight * mSubPreviewWidth);
                mMagicFilterView = new MagicFilterView(CameraActivity.this, mSubPreviewWidth, mSubPreviewHeight);
                mRelativeLayout.addView(mMagicFilterView);
                int length = preview.width * preview.height * ImageFormat.getBitsPerPixel(ImageFormat.YV12) / 8;
                final Camera.Size size = preview;
                mMemory = new byte[2][length];
                mCamera.addCallbackBuffer(mMemory[0]);
                mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        updateFrameData(data, yBuffer, uBuffer, vBuffer, ImageFormat.YV12, mSubPreviewWidth, mSubPreviewHeight);
                        if (mCamera != null) {
                            mCamera.addCallbackBuffer(mMemory[0]);
                        }
                    }
                });
                mCamera.startPreview();
            } catch (Exception e) {
                return;
            }
        }
    }

    private synchronized void updateFrameData(final byte[] data, ByteBuffer buffer, ByteBuffer buffer1, ByteBuffer buffer2, int yv12, int width, int height) {
        ImageHelper.ImageData inData = new ImageHelper.ImageData(data, ImageFormat.YV12, mSubPreviewWidth, mSubPreviewHeight);
        ImageHelper.fillYUVBuffer(inData, yBuffer, uBuffer, vBuffer);
        mMagicFilterView.onFrame(data, yBuffer, uBuffer, vBuffer, ImageFormat.YV12, mSubPreviewWidth, mSubPreviewHeight);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

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

