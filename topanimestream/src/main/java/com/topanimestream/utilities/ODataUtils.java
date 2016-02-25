package com.topanimestream.utilities;

import android.accounts.NetworkErrorException;
import android.content.Entity;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.topanimestream.App;
import com.topanimestream.R;
import com.topanimestream.models.OdataRequestInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class ODataUtils {

    public static <T> void PostWithEntityResponse(String url, String jsonBody, final Class<T> classType, final EntityCallback<T> callback)
    {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jsonBody);


        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", App.accessToken)
                .build();

        client.newCall(request).enqueue(new com.squareup.okhttp.Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Response response) throws IOException {

                try {
                    if (response.isSuccessful()) {
                        Gson gson = App.getGson();
                        String json = response.body().string();
                        final OdataRequestInfo info = gson.fromJson(json, OdataRequestInfo.class);
                        T result = gson.fromJson(json, classType);
                        callback.onSuccess(result, info);
                        return;
                    }
                }
                catch (Exception e)
                {
                    callback.onFailure(e);
                }
                callback.onFailure(new NetworkErrorException("Failed to post/fetch the data."));
            }
        });
    }
    public static void Post(String url, String jsonBody, final Callback callback)
    {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jsonBody);


        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", App.accessToken)
                .build();

        client.newCall(request).enqueue(new com.squareup.okhttp.Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Response response) throws IOException {

                try {
                    if (response.isSuccessful()) {
                        callback.onSuccess();
                        return;
                    }
                }
                catch (Exception e)
                {
                    callback.onFailure(e);
                }
                callback.onFailure(new NetworkErrorException("Failed to post/fetch the data."));
            }
        });
    }
    public static void DeleteEntity(final String url, final Callback callback)
    {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", App.accessToken)
                .delete()
                .build();

        OkHttpClient client = App.getHttpClient();

        client.newCall(request).enqueue(new com.squareup.okhttp.Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        callback.onSuccess();
                        return;
                    }

                }
                catch (Exception e)
                {
                    callback.onFailure(e);
                }
                callback.onFailure(new NetworkErrorException("Failed to fetch the data."));
            }
        });
    }
    public static <T> void GetEntity(String url, final Class<T> classType, final EntityCallback<T> callback)
    {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", App.accessToken)
                .build();

        OkHttpClient client = App.getHttpClient();

        client.newCall(request).enqueue(new com.squareup.okhttp.Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        Gson gson = App.getGson();
                        String json = response.body().string();
                        final OdataRequestInfo info = gson.fromJson(json, OdataRequestInfo.class);
                        T result = gson.fromJson(json, classType);
                        callback.onSuccess(result, info);
                        return;
                    }

                }
                catch (Exception e)
                {
                    callback.onFailure(e);
                }
                callback.onFailure(new NetworkErrorException("Failed to fetch the data."));
            }
        });
    }

    public static <T> void GetEntityList(String url, final Class<T> classType, final EntityCallback<ArrayList<T>> callback)
    {
        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", App.accessToken)
                .build();

        OkHttpClient client = App.getHttpClient();
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        client.newCall(request).enqueue(new com.squareup.okhttp.Callback() {
            @Override
            public void onFailure(Request request, final IOException e) {
                mainHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        callback.onFailure(e);
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        Gson gson = App.getGson();
                        JSONObject json = new JSONObject(response.body().string());
                        final OdataRequestInfo info = gson.fromJson(json.toString(), OdataRequestInfo.class);
                        JSONArray jsonArray = json.getJSONArray("value");
                        final ArrayList<T> genericList = new ArrayList<T>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            genericList.add(gson.fromJson(jsonArray.get(i).toString(), classType));
                        }

                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess(genericList, info);
                            }
                        });

                        return;
                    }

                    onFailure(request, new IOException("Failed to fetch the data."));
                } catch (final Exception e) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure(e);
                        }
                    });

                }
            }
        });
    }

    public interface EntityCallback<T>
    {
        void onSuccess(T entity, OdataRequestInfo info);
        void onFailure(Exception e);
    }

    public interface Callback
    {
        void onSuccess();
        void onFailure(Exception e);
    }

}
