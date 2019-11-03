package com.yyy.facedemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.idl.main.facesdk.FaceAuth;
import com.baidu.idl.main.facesdk.FaceDetect;
import com.baidu.idl.main.facesdk.FaceFeature;
import com.baidu.idl.main.facesdk.FaceLive;
import com.baidu.idl.main.facesdk.callback.Callback;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.model.BDFaceSDKConfig;
import com.baidu.idl.main.facesdk.utils.PreferencesUtil;
import com.yyy.facedemo.api.FaceApi;
import com.yyy.facedemo.been.Tokens;
import com.yyy.facedemo.db.DBManager;
import com.yyy.facedemo.listener.SdkInitListener;
import com.yyy.facedemo.model.GlobalSet;
import com.yyy.facedemo.model.SingleBaseConfig;
import com.yyy.facedemo.net.FastJsonConverterFactory;
import com.yyy.facedemo.net.GetRequest_Interface;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.baidu.idl.main.facesdk.model.BDFaceSDKCommon.BDFaceAnakinRunMode.BDFACE_ANAKIN_RUN_AT_SMALL_CORE;
import static com.baidu.idl.main.facesdk.model.BDFaceSDKCommon.BDFaceLogInfo.BDFACE_LOG_ALL_MESSAGE;

public class MainWin extends Activity {
    private static String TAG = "Main--";
    public static final int SDK_MODEL_LOAD_SUCCESS = 0;
    public static final int SDK_UNACTIVATION = 1;
    public static final int SDK_UNINIT = 2;
    public static final int SDK_INITING = 3;
    public static final int SDK_INITED = 4;
    public static final int SDK_INIT_FAIL = 5;
    public static final int SDK_INIT_SUCCESS = 6;

    private FaceAuth faceAuth;
    private FaceDetect faceDetect;
    private FaceFeature faceFeature;
    private FaceLive faceLiveness;
    private static volatile int initStatus = SDK_UNACTIVATION;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_lay);
        Log.i("first", "helloword");

        if (initStatus != SDK_MODEL_LOAD_SUCCESS) {
            init(this, new SdkInitListener() {
                @Override
                public void initStart() {
                    Log.i("first", "initStart()");
                }

                @Override
                public void initLicenseSuccess() {
                    Log.i("first", "initLicenseSuccess()");
                }

                @Override
                public void initLicenseFail(int errorCode, String msg) {
                    // 如果授权失败，跳转授权页面
                    Log.i("first", errorCode + msg);
                    //startActivity(new Intent(mContext, FaceAuthActicity.class));
                }

                @Override
                public void initModelSuccess() {
                    Log.i("first", "initModelSuccess()");
                }

                @Override
                public void initModelFail(int errorCode, String msg) {
                    Log.i("first", "initModelFail()");
                }
            });
        }

        //http请求
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://aip.baidubce.com/oauth/2.0/") //设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
                //.addConverterFactory(FastJsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        // 创建 网络请求接口 的实例
        GetRequest_Interface request = retrofit.create(GetRequest_Interface.class);
        //对 发送请求 进行封装
        //Call<ResponseBody> call = request.getCall();
        Call<Tokens> call = request.getToken("",
                "");

        call.enqueue(new retrofit2.Callback<Tokens>() {
            @Override
            public void onResponse(Call<Tokens> call, Response<Tokens> tokens) {
                Log.i(TAG, "onResponse");

                Tokens tokens1 = tokens.body();
                Log.i(TAG, tokens1.access_token);
            }

            @Override
            public void onFailure(Call<Tokens> call, Throwable t) {
                Log.i(TAG, "onFailure");
            }
        });

        //同步请求
//        try {
//            Response<ResponseBody> response = call.execute();
//            System.out.print(response.body().source());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public void init(final Context context, final SdkInitListener listener) {
        faceAuth = new FaceAuth();
        faceDetect = new FaceDetect();
        faceDetect = new FaceDetect();
        faceFeature = new FaceFeature();
        faceLiveness = new FaceLive();

        faceAuth.setActiveLog(BDFACE_LOG_ALL_MESSAGE);
        faceAuth.setAnakinConfigure(BDFACE_ANAKIN_RUN_AT_SMALL_CORE, 2);
        PreferencesUtil.initPrefs(context.getApplicationContext());
        final String licenseOfflineKey = PreferencesUtil.getString("activate_offline_key", "HPYX-AEQ9-PGNU-GQTA");
        final String licenseOnlineKey = PreferencesUtil.getString("activate_online_key", "");

        // 如果licenseKey 不存在提示授权码为空，并跳转授权页面授权
        if (TextUtils.isEmpty(licenseOfflineKey) && TextUtils.isEmpty(licenseOnlineKey)) {
            Log.i("first----", "未授权设备，请完成授权激活");
            if (listener != null) {
                listener.initLicenseFail(-1, "授权码不存在，请重新输入！");
            }
            return;
        }
        // todo 增加判空处理
        if (listener != null) {
            listener.initStart();
        }

        if (!TextUtils.isEmpty(licenseOfflineKey)) {
            // 离线激活
            Log.i("first", "开始激活initLicenseOffLine");
            faceAuth.initLicenseOffLine(context, new Callback() {
                @Override
                public void onResponse(int code, String response) {
                    if (code == 0) {
                        initStatus = SDK_INIT_SUCCESS;
                        if (listener != null) {
                            listener.initLicenseSuccess();
                        }
                        initModel(context, listener);
                        return;
                    } else {
                        Log.i("first", "激活失败" + code);
                    }
                }
            });
        } else if (!TextUtils.isEmpty(licenseOnlineKey)) {

            // 在线激活
            Log.i("first", "开始激活initLicenseOnLine");
            faceAuth.initLicenseOnLine(context, licenseOnlineKey, new Callback() {
                @Override
                public void onResponse(int code, String response) {
                    if (code == 0) {
                        initStatus = SDK_INIT_SUCCESS;
                        if (listener != null) {
                            listener.initLicenseSuccess();
                        }
                        initModel(context, listener);
                        return;
                    }
                }
            });

        } else {
            Log.i("first", "bu激活");
            if (listener != null) {
                listener.initLicenseFail(-1, "授权码不存在，请重新输入！");
            }
        }

    }

    /**
     * 初始化模型，目前包含检查，活体，识别模型；因为初始化是顺序执行，可以在最好初始化回掉中返回状态结果
     *
     * @param context
     * @param listener
     */
    public void initModel(final Context context, final SdkInitListener listener) {
        Log.i("first", "模型初始化中，请稍后片刻");

        initConfig();

        final long startInitModelTime = System.currentTimeMillis();
        faceDetect.initModel(context,
                GlobalSet.DETECT_VIS_MODEL,
                GlobalSet.DETECT_NIR_MODE,
                GlobalSet.ALIGN_MODEL,
                new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        //  ToastUtils.toast(context, code + "  " + response);
                        if (code != 0 && listener != null) {
                            listener.initModelFail(code, response);
                        }
                    }
                });

        faceDetect.initQuality(context,
                GlobalSet.BLUR_MODEL,
                GlobalSet.OCCLUSION_MODEL, new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        if (code != 0 && listener != null) {
                            listener.initModelFail(code, response);
                        }
                    }
                });

        faceDetect.initAttrEmo(context, GlobalSet.ATTRIBUTE_MODEL, GlobalSet.EMOTION_MODEL, new Callback() {
            @Override
            public void onResponse(int code, String response) {
                if (code != 0 && listener != null) {
                    listener.initModelFail(code, response);
                }
            }
        });

        faceLiveness.initModel(context,
                GlobalSet.LIVE_VIS_MODEL,
                GlobalSet.LIVE_NIR_MODEL,
                GlobalSet.LIVE_DEPTH_MODEL,
                new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        //  ToastUtils.toast(context, code + "  " + response);
                        if (code != 0 && listener != null) {
                            listener.initModelFail(code, response);
                        }
                    }
                });

        faceFeature.initModel(context,
                GlobalSet.RECOGNIZE_IDPHOTO_MODEL,
                GlobalSet.RECOGNIZE_VIS_MODEL,
                "",
                new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        long endInitModelTime = System.currentTimeMillis();
                        Log.i("first", "init model time = " + (endInitModelTime - startInitModelTime));
                        if (code != 0) {
                            Log.i("first", "模型加载失败");
                            if (listener != null) {
                                listener.initModelFail(code, response);
                            }
                        } else {
                            initStatus = SDK_MODEL_LOAD_SUCCESS;
                            // 模型初始化成功，加载人脸数据
                            initDataBases(context);
                            Log.i("first", "模型加载完毕，欢迎使用");
                            if (listener != null) {
                                listener.initModelSuccess();
                            }
                        }
                    }
                });
    }

    /**
     * 初始化配置
     *
     * @return
     */
    public boolean initConfig() {
        if (faceDetect != null) {
            BDFaceSDKConfig config = new BDFaceSDKConfig();
            // TODO: 最小人脸个数检查，默认设置为1,用户根据自己需求调整
            config.maxDetectNum = 1;

            // TODO: 默认为80px。可传入大于30px的数值，小于此大小的人脸不予检测，生效时间第一次加载模型
            config.minFaceSize = SingleBaseConfig.getBaseConfig().getMinimumFace();
            // 是否进行属性检测，默认关闭
            config.isAttribute = SingleBaseConfig.getBaseConfig().isAttribute();

            // TODO: 模糊，遮挡，光照三个质量检测和姿态角查默认关闭，如果要开启，设置页启动
            config.isCheckBlur = config.isOcclusion
                    = config.isIllumination = config.isHeadPose
                    = SingleBaseConfig.getBaseConfig().isQualityControl();

            faceDetect.loadConfig(config);
            return true;
        }
        return false;
    }

    public void initDataBases(Context context) {
        // 初始化数据库
        DBManager.getInstance().init(context);
        // 数据变化，更新内存
        FaceApi.getInstance().initDatabases(true);
    }
}
