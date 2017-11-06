package com.example.admin.myapplication.view;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
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
import android.widget.Toast;

import com.example.admin.myapplication.R;
import com.example.admin.myapplication.util.CameraSettings;
import com.example.admin.myapplication.util.ImageHelper;
import com.example.admin.myapplication.util.PermissionsActivity;

import java.lang.reflect.Method;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraActivity extends AppCompatActivity implements GLSurfaceView.Renderer, TextureView.SurfaceTextureListener {

    private final String TAG = "CameraActivity";
    private static final int CAMERA_HAL_API_VERSION_1_0 = 0x100;
    private boolean mHasCriticalPermissions;
    private int camId = 0;//camera IDs
    private Camera mCamera;
    private GLSurfaceView mGlSurfaceView;
    private Button btnSwitch;
    private byte[][] mMemory;
    private int count = 0;

    //camera parameters
    private int mFullPreviewWidth;
    private int mFullPreviewHeight;
    private int mFormat = ImageFormat.NV21;
    private int mSubPreviewWidth = 640;
    private int mSubPreviewHeight = 480;
    private String mCurrent = "none";

    private int mDisplayWidth = 0;
    private int mDisplayHeight = 0;
    private float mDisplayRatio = 1.00f;

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
        //openMainCamera();
        //startPreviewMainCamera();
    }

    private void openMainCamera() {

        mCamera = Camera.open(camId);
        /*
        try {
            Method openMethod = Class.forName("android.hardware.Camera").getMethod(
                    "openLegacy", int.class, int.class);
            mCamera = (Camera) openMethod.invoke(null, camId, CAMERA_HAL_API_VERSION_1_0);
        } catch (Exception e) {
            mCamera = Camera.open(camId);
        }
        */
        if (mCamera == null)
            return;
    }

    private void startPreviewMainCamera() {

        Camera.Parameters p = mCamera.getParameters();
        Camera.Size preview = p.getPreviewSize();
        mFormat = p.getPreviewFormat();
        mFullPreviewWidth = preview.width;
        mFullPreviewHeight = preview.height;
        mDisplayRatio = mFullPreviewWidth / (float) mFullPreviewHeight;
        // set new preview size, MUST stop preview, otherwise preview not
        // changed.
        // mCamera.stopPreview();
        Log.d(TAG, mFullPreviewWidth + " applyCameraPreviewWithBuffer, " + mFullPreviewHeight);
        if (ImageHelper.equals(mDisplayRatio, 4.0 / 3)) {
            p.setPreviewSize(640, 480);
        } else {
            p.setPreviewSize(640, 360);
        }
        p.setColorEffect("none");
        p.setPreviewFormat(ImageFormat.YV12);
        // p.setPreviewFormat(ImageFormat.NV21);
        mCamera.setParameters(p);

        preview = p.getPreviewSize();
        mSubPreviewWidth = preview.width;
        mSubPreviewHeight = preview.height;
        Log.d(TAG, "previewSize->width: " + mSubPreviewWidth + "  height:" + mSubPreviewHeight);
        final int format = p.getPreviewFormat();
        int length = preview.width * preview.height * ImageFormat.getBitsPerPixel(format) / 8;
        final Camera.Size size = preview;
        mMemory = new byte[2][length];
        mCamera.addCallbackBuffer(mMemory[0]);
        mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] bytes, Camera camera) {
                count++;
                if (1 == count) {
                    Log.d(TAG, "liang.chen->count");
                }
                Log.d(TAG, "liang.chen->setPreviewCallbackWithBuffer");
            }
        });

        mCamera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] bytes, Camera camera) {
                Log.d(TAG, "asdasda");
            }
        });
        Log.d(TAG, "startPreview");
        mCamera.startPreview();
    }

    private void initView() {
        mTexture = (TextureView) findViewById(R.id.texture);
        mTexture.setSurfaceTextureListener(this);
        mGlSurfaceView = (GLSurfaceView) findViewById(R.id.glsurfaceview);
        mGlSurfaceView.setRenderer(this);
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

    //glsurfaceview render interface
    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {

    }

    @Override
    public void onDrawFrame(GL10 gl10) {

    }


    //textureview callback interface
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
                //mCamera.setPreviewCallback(this);
                mCamera.setDisplayOrientation(90);

                /*a============================*/
                Camera.Parameters p = mCamera.getParameters();
                Camera.Size preview = p.getPreviewSize();
                preview = p.getPreviewSize();
                mSubPreviewWidth = preview.width;
                mSubPreviewHeight = preview.height;
                int length = preview.width * preview.height * ImageFormat.getBitsPerPixel(ImageFormat.YV12) / 8;
                final Camera.Size size = preview;
                mMemory = new byte[2][length];
                mCamera.addCallbackBuffer(mMemory[0]);
                mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] bytes, Camera camera) {

                        Log.d(TAG, "liang.chen->setPreviewCallbackWithBuffer");
                        mCamera.addCallbackBuffer(mMemory[0]);
                    }
                });
                /*============================*/
                mCamera.startPreview();
            } catch (Exception e) {
                return;
            }
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }
}
