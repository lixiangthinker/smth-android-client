package com.tony.newsmth.net;

import android.content.Context;

import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by l00151177 on 2016/9/21.
 */
public class SmthApi {
    private static final String TAG = "SmthApi";
    private static SmthApi instance = null;
    public OkHttpClient mHttpClient;
    private ClearableCookieJar mCookieJar;

    // WWW service of SMTH, but actually most of services are actually from nForum
    private final String SMTH_WWW_URL = "http://www.newsmth.net";
    public static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.101 Safari/537.36";

    private Retrofit wRetrofit = null;
    private ISmthWwwService wService = null;

    public synchronized static SmthApi getInstance(Context context) {
        if (instance == null) {
            instance = new SmthApi(context.getApplicationContext());
        }
        return instance;
    }

    protected SmthApi(Context context) {
        mHttpClient = initSmthWwwHttpClient(context);
        wRetrofit = new Retrofit.Builder()
                .baseUrl(SMTH_WWW_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(mHttpClient)
                .build();
        wService = wRetrofit.create(ISmthWwwService.class);
    }

    private OkHttpClient initSmthWwwHttpClient(Context context) {
        OkHttpClient client;
        ClearableCookieJar cookieJar;
        // set your desired log level
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        // https://github.com/franmontiel/PersistentCookieJar
        // A persistent CookieJar implementation for OkHttp 3 based on SharedPreferences.
        cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));
        mCookieJar = cookieJar;
        //set cache path
        File httpCacheDirectory = new File(context.getCacheDir(), "Responses");
        int cacheSize = 250 * 1024 * 1024; // 100 MiB
        Cache cache = new Cache(httpCacheDirectory, cacheSize);

        client = new OkHttpClient().newBuilder()
                .addInterceptor(logging)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request()
                                .newBuilder()
                                .header("User-Agent", USER_AGENT)
                                .build();
                        return chain.proceed(request);
                    }
                })
                .cookieJar(cookieJar)
                .cache(cache)
                .readTimeout(15, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();
        return client;
    }

    public ClearableCookieJar getCookieJar() {
        return mCookieJar;
    }

    public ISmthWwwService getService() {
        return wService;
    }
}
