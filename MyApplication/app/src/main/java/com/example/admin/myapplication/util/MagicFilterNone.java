package com.example.admin.myapplication.util;

/**
 * Created by admin on 2017/07/10   .
 */

public class MagicFilterNone extends MagicFilterDefault {
    private static final String mFragmentSource = "" +
            "varying lowp vec2 v_TexCoord;\n" +
            "uniform sampler2D SamplerY;\n" +
            "uniform sampler2D SamplerU;\n" +
            "uniform sampler2D SamplerV;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "    lowp vec3 yuv;\n" +
            "    lowp vec3 rgb;\n" +
            "\n" +
            "    yuv.x = texture2D(SamplerY,v_TexCoord).r;\n" +
            "    yuv.y = texture2D(SamplerU,v_TexCoord).r -0.5 ;\n" +
            "    yuv.z = texture2D(SamplerV,v_TexCoord).r -0.5 ;\n" +
            "\n" +
            "    rgb = mat3( 1,1,1,0,-0.39465,2.03211,1.13983,-0.58060,0) * yuv;\n" +
            "\n" +
            "    gl_FragColor = vec4(rgb, 1);\n" +
            "}";

    public MagicFilterNone() {
        super(mVertexSource,mFragmentSource);
    }

    @Override
    public void onInit() {
        super.onInit();
    }


}
