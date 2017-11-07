package com.example.admin.myapplication.util;

import android.opengl.GLES20;

public class MagicFilterPrague extends MagicFilterDefault {
    private int widthLocation;
    private int heightLocation;
    private int L;
    private int A;
    private int B;

    private static final String mFragmentSource =
            "varying lowp vec2 v_TexCoord;\n" +
            "uniform sampler2D SamplerY;\n" +
            "uniform sampler2D SamplerU;\n" +
            "uniform sampler2D SamplerV;\n" +
            "\n" +
            "uniform lowp float width;\n" +
            "uniform lowp float height;\n" +
            "uniform lowp float L;\n" +
            "uniform lowp float A;\n" +
            "uniform lowp float B;\n" +
            "\n" +
            "lowp vec4 Crayon2(lowp vec2 v_TexCoord, lowp float xDistance, lowp float yDistance)\n" +
            "{\n" +
            "\tlowp float beta = L;\n" +
            "\tlowp float beta_a = A;\n" +
            "\tlowp float beta_b = B;\n" +
            "\n" +
            "    lowp vec3 yuv;\n" +
            "    lowp vec3 rgb;\n" +
            "    lowp vec3 rgb00;\n" +
            "    lowp vec3 rgb10;\n" +
            "    lowp vec3 rgb20;\n" +
            "    lowp vec3 rgb01;\n" +
            "\n" +
            "    yuv.x = texture2D(SamplerY,v_TexCoord).r;\n" +
            "    yuv.y = texture2D(SamplerU,v_TexCoord).r -0.5 ;\n" +
            "    yuv.z = texture2D(SamplerV,v_TexCoord).r -0.5 ;\n" +
            "    rgb = mat3( 1,1,1,0,-0.39465,2.03211,1.13983,-0.58060,0) * yuv;\n" +
            "\n" +
            "\tlowp vec2 pos = v_TexCoord.st;\n" +
            "\n" +
            "\tyuv.x = texture2D(SamplerY, v_TexCoord + vec2(-xDistance,yDistance)).r;\n" +
            "    yuv.y = texture2D(SamplerU, v_TexCoord + vec2(-xDistance,yDistance)).r -0.5 ;\n" +
            "    yuv.z = texture2D(SamplerV, v_TexCoord + vec2(-xDistance,yDistance)).r -0.5 ;\n" +
            "    rgb00 = mat3( 1,1,1,0,-0.39465,2.03211,1.13983,-0.58060,0) * yuv;\n" +
            "    lowp vec3 s00 = rgb00.rgb;\n" +
            "\n" +
            "    yuv.x = texture2D(SamplerY, v_TexCoord + vec2(xDistance,yDistance)).r;\n" +
            "    yuv.y = texture2D(SamplerU, v_TexCoord + vec2(xDistance,yDistance)).r -0.5 ;\n" +
            "    yuv.z = texture2D(SamplerV, v_TexCoord + vec2(xDistance,yDistance)).r -0.5 ;\n" +
            "    rgb10 = mat3( 1,1,1,0,-0.39465,2.03211,1.13983,-0.58060,0) * yuv;\n" +
            "\tlowp vec3 s10 = rgb10.rgb;\n" +
            "\n" +
            "    yuv.x = texture2D(SamplerY, v_TexCoord + vec2(-xDistance,-yDistance)).r;\n" +
            "    yuv.y = texture2D(SamplerU, v_TexCoord + vec2(-xDistance,-yDistance)).r -0.5 ;\n" +
            "    yuv.z = texture2D(SamplerV, v_TexCoord + vec2(-xDistance,-yDistance)).r -0.5 ;\n" +
            "    rgb20 = mat3( 1,1,1,0,-0.39465,2.03211,1.13983,-0.58060,0) * yuv;\n" +
            "\tlowp vec3 s20 = rgb20.rgb;\n" +
            "\n" +
            "    yuv.x = texture2D(SamplerY, v_TexCoord + vec2(-xDistance,-yDistance)).r;\n" +
            "    yuv.y = texture2D(SamplerU, v_TexCoord + vec2(-xDistance,-yDistance)).r -0.5 ;\n" +
            "    yuv.z = texture2D(SamplerV, v_TexCoord + vec2(-xDistance,-yDistance)).r -0.5 ;\n" +
            "    rgb01 = mat3( 1,1,1,0,-0.39465,2.03211,1.13983,-0.58060,0) * yuv;\n" +
            "    lowp vec3 s01 = rgb01.rgb;\n" +
            "\n" +
            "\tlowp vec3 color = rgb.rgb;\n" +
            "\tlowp vec3 maxcolor ;\n" +
            "\tlowp vec3 color_T ;\n" +
            "\tmaxcolor.rgb  = max(s00.rgb, s10.rgb);\n" +
            "\tmaxcolor.rgb  = max(maxcolor.rgb, s20.rgb);\n" +
            "\tmaxcolor.rgb  = max(maxcolor.rgb, s01.rgb);\n" +
            "\n" +
            "\tlowp float maxc = max(max(maxcolor.r,maxcolor.b),maxcolor.g);\n" +
            "    if(maxcolor.r>0.0) color_T.r = color.r/(maxcolor.r+(maxc-maxcolor.r)/4.0);\n" +
            "    if(maxcolor.g>0.0) color_T.g = color.g/(maxcolor.g+(maxc-maxcolor.g)/4.0);\n" +
            "    if(maxcolor.b>0.0) color_T.b = color.b/(maxcolor.b+(maxc-maxcolor.b)/4.0);\n" +
            "\n" +
            "    lowp float Y_c =0.299 * color_T.r + 0.587 * color_T.g + 0.114*color_T.b  ;\n" +
            "\n" +
            "    lowp float alpha = min(Y_c ,57.0/255.0)/(57.0/255.0);\n" +
            "\n" +
            "    color_T.rgb = alpha*color_T.rgb + (1.0-alpha) * color.rgb ;\n" +
            "\n" +
            "    lowp float YY = 0.299 * color_T.r + 0.587 * color_T.g + 0.114*color_T.b  ;\n" +
            "    lowp float II = 0.596 * color_T.r - 0.275 * color_T.g - 0.321*color_T.b  ;\n" +
            "    lowp float QQ = 0.212 * color_T.r - 0.523 * color_T.g + 0.311*color_T.b  ;\n" +
            "\n" +
            "    II = II * beta ;\n" +
            "    QQ = QQ * beta ;\n" +
            "    lowp float Y = max(0.0,min(1.0,pow(1.0*YY,5.0*beta_a)));\n" +
            "    lowp vec3 color_o ;\n" +
            "    color_o.r = Y + 0.956 *II +0.621 * QQ;\n" +
            "    color_o.g = Y - 0.272 *II -1.703 * QQ;\n" +
            "    color_o.b = Y - 1.106 *II +0.000 * QQ;\n" +
            "\n" +
            "    color.r = (1.0-beta_b)*  color.r +  (beta_b) *color_o.r ;\n" +
            "    color.g = (1.0-beta_b)*  color.g +  (beta_b) *color_o.g ;\n" +
            "    color.b = (1.0-beta_b)*  color.b +  (beta_b) *color_o.b ;\n" +
            "\n" +
            "   \tcolor.rgb  = min(color.rgb, 1.0);\n" +
            "    color.rgb  = max(color.rgb, 0.0);\n" +
            "\n" +
            "    return vec4(color.rgb ,1.0);\n" +
            "}\n" +
            "\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "    lowp float xDistance = 1.5 / height;\n" +
            "\tlowp float yDistance = 1.5 / width;\n" +
            "\n" +
            "\tlowp vec4 color ;\n" +
            " \tcolor = Crayon2(v_TexCoord, xDistance, yDistance);\n" +
            "\n" +
            "\tgl_FragColor = vec4(color.r, color.g, color.b, 1.0);\n" +
            "\n" +
            "}";

    public MagicFilterPrague() {
        super(mVertexSource, mFragmentSource);
    }

    @Override
    public void onInit() {
        super.onInit();
        widthLocation = GLES20.glGetUniformLocation(mProgramHandle, "width");
        heightLocation = GLES20.glGetUniformLocation(mProgramHandle, "height");
        L = GLES20.glGetUniformLocation(mProgramHandle, "L");
        A = GLES20.glGetUniformLocation(mProgramHandle, "A");
        B = GLES20.glGetUniformLocation(mProgramHandle, "B");
    }

    public void onDestroy() {
        super.onDestroy();
    }

    private void setTexelSize(final float w, final float h) {
        setFloat(widthLocation, w);
        setFloat(heightLocation, h);
        setFloat(L, 1.6f);
        setFloat(A, 0.2f);
        setFloat(B, 0.8f);
    }

    @Override
    public void onInputSizeChanged(final int width, final int height) {
        super.onInputSizeChanged(width, height);
        setTexelSize(width, height);
    }
}
