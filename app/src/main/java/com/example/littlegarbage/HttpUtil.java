package com.example.littlegarbage;

import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

public class HttpUtil {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");


    public static String sendOkHttpRequest(String garbage) {

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(400, TimeUnit.SECONDS)
                .writeTimeout(400, TimeUnit.SECONDS)
                .readTimeout(400, TimeUnit.SECONDS)
                .build();

        String url1 = "https://aiapi.jd.com/jdai/garbageTextSearch?appkey=f08733d22c104e5dc39f97a323359da9&timestamp=";
        long time = System.currentTimeMillis();
        String s1 = GetMD5.md5("1a8c89772abf812630f6687255d22a3b" + time);
        String url = url1 + time + "&sign=" + s1;

//        JSONObject json = new JSONObject();
//
//        try {
//            json.put("cityId","310000");
//            json.put("text",garbage);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

//        RequestBody body = FormBody.create(JSON, String.valueOf(json));

//        String json = "{ "+"cityId"+":"+310000+","+"text:"+garbage+"}";

//        RequestBody body = RequestBody.create(JSON, json);

        RequestBody body = new FormBody.Builder()
                .add("cityId",String.valueOf(310000))
                .add("text",garbage)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json;charset=UTF-8")
                .post(body)
                .build();

        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            if (response.code()==200) {
                String message=response.body().string();

                return message;
            } else {

                throw new IOException("Unexpected code " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


}
