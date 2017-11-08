package com.example.admin.myapplication.model;

/**
 * Created by admin on 2017/11/8.
 */

public class RtProcessor {

    public final static int COLOR_FORMAT_GRAY = 0x10;
    public final static int COLOR_FORMAT_YUV_NV12 = 0x15;
    public final static int COLOR_FORMAT_YUV_NV21 = 0x16;
    public final static int COLOR_FORMAT_YUV_I420 = 0x17;
    public final static int COLOR_FORMAT_YUV_YV12 = 0x18;

    static {
        //System.loadLibrary("");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //Init realtime refocus environment. this function only be called once
    //
    //@param mainW         :main camera image width
    //@param mainH         :main camera image height
    //@param auxW          :aux camera image width
    //@param auxH          :aux camera image height
    //@param rotation      :the image rotation for rectification, must be 0, 90, 180 or 270 degree.
    //@return              :0 for success other failure
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public static native int init(int mianW, int mainH, int subW, int subH);

    /////////////////////////////////////////////////////////////////////////////////////////
    //Set it when camera focus finished.
    //@return              :0 for success other failure
    /////////////////////////////////////////////////////////////////////////////////////////
    public static native int aflocked();

    /////////////////////////////////////////////////////////////////////////////////////////
    //Do realtime refocus.
    //
    //@param mainImg       :main camera buffer.
    //@param auxImg        :aux camera buffer.
    //@param outImg        :output realtime refocused image buffer.
    //@param focusX        :user touch image coordinate X.
    //@param focusY        :user touch image coordinate Y.
    //@param fNum          :user set camera fNumber, fNum should be greater than 0.0f and less than or equal to 17.0f.
    //@return              :0 for success other failure.
    /////////////////////////////////////////////////////////////////////////////////////////
    public static native int process(byte[] mianBuffer, int mainW, int mainH, int mainFormat, int mainRoation, int mainSW, int mainSH,
                                     byte[] subBuffer, int subW, int subH, int subFormat, int subRotation, int subSW, int subSH);

    /////////////////////////////////////////////////////////////////////////////////////////
    //Dump realtime refocus image frames.
    //
    //@return              :0 for success other failure.
    /////////////////////////////////////////////////////////////////////////////////////////
    public static  native int dump();

    /////////////////////////////////////////////////////////////////////////////////////////
    //Destroy realtime refocus resource
    //
    //@return              :0 for success other failure.
    /////////////////////////////////////////////////////////////////////////////////////////
    public static native int uninit();

    /////////////////////////////////////////////////////////////////////////////////////////
    //Get version info of realtime refocus lib
    //
    //@return               :Return version info
    /////////////////////////////////////////////////////////////////////////////////////////
    public static native int getVersion();



}
