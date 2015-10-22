package com.topanimestream.utilities;

import android.accounts.NetworkErrorException;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.topanimestream.App;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class OkHttpUtils {

    public static <T extends Object> void GetJson(String url, final Class<T> classType, final Callback<T> callback)
    {

        OkHttpClient client = App.getHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", App.accessToken)
                .build();
        try {
            client.newCall(request).enqueue(new com.squareup.okhttp.Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    callback.onFailure(e);
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if (response.isSuccessful()) {
                        Gson gson = new Gson();
                        T object = gson.fromJson(response.body().string(), classType);
                        callback.onSuccess(object);
                    }
                    onFailure(response.request(), new IOException("Couldn't connect to TopAnimeStream"));
                }
            });

        } catch (Exception e) {
            callback.onFailure(e);
        }
        callback.onFailure(new NetworkErrorException("Failed to fetch the data."));
    }

    public static <T extends Object> void GetJsonList(String url, Class<T> classType, Callback<ArrayList<T>> callback)
    {
        Gson gson = new Gson();
        OkHttpClient client = App.getHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", App.accessToken)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if(response.isSuccessful()) {
                JSONObject json = new JSONObject(response.body().string());
                JSONArray jsonArray = json.getJSONArray("value");
                ArrayList<T> genericList = new ArrayList<T>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    genericList.add(gson.fromJson(jsonArray.get(0).toString(), classType));
                }
                callback.onSuccess(genericList);
            }
        } catch (Exception e) {
            callback.onFailure(e);
        }
        callback.onFailure(new NetworkErrorException("Failed to fetch the data."));
    }

    public interface Callback<T>
    {
        void onSuccess(T obj);
        void onFailure(Exception e);
    }
}
