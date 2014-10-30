package com.aniblitz.managers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Base64;
import android.widget.Toast;

import com.aniblitz.App;
import com.aniblitz.AsyncTaskTools;
import com.aniblitz.R;
import com.aniblitz.Utils;
import com.aniblitz.models.Anime;
import com.aniblitz.models.Episode;
import com.aniblitz.models.Mirror;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.images.WebImage;
import com.google.sample.castcompanionlibrary.cast.exceptions.NoConnectionException;
import com.google.sample.castcompanionlibrary.cast.exceptions.TransientNetworkDisconnectionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Created by marcandre.therrien on 2014-10-30.
 */
public class Mp4Manager {
    public static Dialog chromecastDialog;
    public static Dialog qualityDialog;
    public static String ignitionKey = null;
    public static void getMp4(Mirror mirror, Activity act, Anime anime, Episode episode)
    {
        AsyncTaskTools.execute(new GetWebPageTask(mirror, act, anime, episode));
    }
    public static class GetWebPageTask extends AsyncTask<Void, Void, String> {
        Mirror mirror;
        private Dialog busyDialog;
        private Activity act;
        private Resources r;
        private Anime anime;
        private AlertDialog alertPlay;
        private Episode episode;
        private String providerName;
        private Document doc;
        public GetWebPageTask(Mirror mirror, Activity act, Anime anime, Episode episode)
        {
            this.mirror = mirror;
            this.act = act;
            this.anime = anime;
            this.episode = episode;
        }
        @Override
        protected void onPreExecute() {
            busyDialog = DialogManager.showBusyDialog(r.getString(R.string.loading_video), act);

        }


        @Override
        protected String doInBackground(Void... params) {

            try {
                providerName = mirror.getProvider().getName().toLowerCase();
                if (providerName.equals("animeultima")) {
                    doc = Jsoup.connect(mirror.getSource()).userAgent("Chrome").ignoreContentType(true).get();
                    String dailyMotionUrl = doc.baseUri().replace("swf/", "");
                    doc = Jsoup.connect(dailyMotionUrl).userAgent("Chrome").get();
                } else if (providerName.equals("ignition s") || providerName.equals("ignition hd")) {
                    if (ignitionKey != null)
                        return ignitionKey;
                    doc = Jsoup.connect(URLDecoder.decode("http://jkanime.net/naruto/1/", "UTF-8")).userAgent("Chrome").get();
                } else {
                    doc = Jsoup.connect(URLDecoder.decode(mirror.getSource(), "UTF-8")).userAgent("Chrome").get();
                }
                if (providerName.equals(r.getString(R.string.play).toLowerCase())) {
                    providerName = "vk";
                }
                String content = doc.html();

                //TODO check quality
            }
            catch(Exception e)
            {
                return null;
            }

            return "Success";
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (result == null) {
                    Toast.makeText(act, act.getString(R.string.error_loading_anime_details), Toast.LENGTH_LONG).show();
                }
                else
                {
                    if(providerName.equals("vk") || providerName.equals("vk_gk"))
                    {
                        final CharSequence[] items = new CharSequence[]{"720", "480", "360", "240" };
                        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(act);
                        alertBuilder.setTitle(act.getString(R.string.choose_quality));
                        alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                AsyncTaskTools.execute(new GetMp4(mirror, act, anime, episode, providerName, items[item].toString(), doc));
                            }
                        });

                        qualityDialog = alertBuilder.create();
                        qualityDialog.show();
                    }
                    else
                    {
                        AsyncTaskTools.execute(new GetMp4(mirror, act, anime, episode, providerName, null, doc));
                    }
                }


            } catch (Exception e)//catch all exception, handle orientation change
            {
                e.printStackTrace();
                Toast.makeText(act, act.getString(R.string.error_loading_anime_details), Toast.LENGTH_LONG).show();
            }
            DialogManager.dismissBusyDialog(busyDialog);

        }

    }

    public static class GetMp4 extends AsyncTask<Void, Void, String> {

        Mirror mirror;
        private Dialog busyDialog;
        private Activity act;
        private Resources r;
        private Anime anime;
        private AlertDialog alertPlay;
        private Episode episode;
        private String providerName;
        private String quality;
        private Document doc;
        public GetMp4(Mirror mirror, Activity act, Anime anime, Episode episode, String providerName, String quality, Document doc)
        {
            this.mirror = mirror;
            this.act = act;
            this.anime = anime;
            this.episode = episode;
            this.providerName = providerName;
            this.quality = quality;
            this.doc = doc;
            r = act.getResources();
        }


        @Override
        protected void onPreExecute()
        {
            busyDialog = DialogManager.showBusyDialog(r.getString(R.string.loading_video), act);

        };
        @Override
        protected String doInBackground(Void... params)
        {

            try {
                byte[] data = doc.html().getBytes("UTF-8");
                String base64 = Base64.encodeToString(data, Base64.DEFAULT);


                String URL = act.getString(R.string.anime_service_path) + "GetMp4Url?provider='" + URLEncoder.encode(providerName) + "'" + (quality != null ? "&quality='" + quality + "'" : "") + "&$format=json";
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost request = new HttpPost(URL);
					/*
					List<NameValuePair> listParam = new ArrayList<NameValuePair>();
					listParam.add(new BasicNameValuePair("html", base64));
		            UrlEncodedFormEntity ent = new UrlEncodedFormEntity(listParam);*/
                request.setEntity(new StringEntity(base64));
                HttpResponse response = httpClient.execute(request);
                HttpEntity entity = response.getEntity();
                InputStream instream = entity.getContent();
                try {
                    JSONObject jsonObj = new JSONObject(Utils.convertStreamToString(instream));
                    return jsonObj.getString("value");
                } catch (JSONException e1) {
                    e1.printStackTrace();
                    return null;
                } catch (Exception e1) {
                    e1.printStackTrace();
                    return null;
                }
            }catch (Exception e)
            {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(final String result)
        {
            Utils.dismissBusyDialog(busyDialog);
            if(result == null)
            {
                Intent i = new Intent(Intent.ACTION_VIEW);
                try {
                    i.setData(Uri.parse(URLDecoder.decode(mirror.getSource(), "UTF-8")));
                } catch (UnsupportedEncodingException e) {
                    return;
                }
                act.startActivity(i);
                //Toast.makeText(act, r.getString(R.string.error_loading_video) , Toast.LENGTH_LONG).show();

                return;
            }

            final CharSequence[] items = {act.getString(R.string.play_phone), act.getString(R.string.stream_chromecast), act.getString(R.string.cancel)};
            final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(act);
            alertBuilder.setTitle(act.getString(R.string.choose_option));
            alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    if(items[item].equals(act.getString(R.string.play_phone)))
                    {
                            /*
                            Intent intent = new Intent(act, VideoViewSubtitle.class);
                            intent.putExtra("VideoPath", result);
                            act.startActivity(intent);*/

                        Intent intent = null;
                        String providerName = mirror.getProvider().getName().toLowerCase();
                        if(providerName.equals("ignition s") || providerName.equals("ignition hd"))
                        {
                            String textToReplace = mirror.getSource().substring(mirror.getSource().indexOf("/1/") + 3);
                            ignitionKey = result;
                            String url = mirror.getSource().replace(textToReplace,result);
                            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            intent.setDataAndType(Uri.parse(url), "video/*");
                        }
                        else {
                            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(result));
                            intent.setDataAndType(Uri.parse(result), "video/*");
                        }
                        act.startActivity(Intent.createChooser(intent, "Complete action using"));
                    }
                    else if(items[item].equals(act.getString(R.string.stream_chromecast)))
                    {
                        String subtitle = "";
                        if(episode != null && episode.getEpisodeInformations().getEpisodeName() != null && !episode.getEpisodeInformations().getEpisodeName().equals(""))
                        {
                            subtitle = episode.getEpisodeInformations().getEpisodeName();
                        }
                        else if(episode != null)
                        {
                            subtitle = act.getString(R.string.episode) + episode.getEpisodeNumber();
                        }
                        else
                        {
                            subtitle = act.getString(R.string.tab_movie);
                        }

                        MediaInfo info = buildMediaInfo(anime.getName(), subtitle, anime.getGenresFormatted(), Uri.parse(result).toString(), anime.getPosterPath("185"), anime.getBackdropPath("500"));
                        loadRemoteMedia(act, 0, true, info);
                    }
                    else
                    {
                        alertPlay.dismiss();
                    }
                }
            });
            alertPlay = alertBuilder.create();
            alertPlay.show();

        }

    }

    public static MediaInfo buildMediaInfo(String title,
                                           String subTitle, String studio, String url, String imgUrl, String bigImageUrl) {
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, subTitle);
        movieMetadata.putString(MediaMetadata.KEY_TITLE, title);
        movieMetadata.putString(MediaMetadata.KEY_STUDIO, studio);
        movieMetadata.addImage(new WebImage(Uri.parse(imgUrl)));
        movieMetadata.addImage(new WebImage(Uri.parse(bigImageUrl)));

        return new MediaInfo.Builder(url)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType("video/mp4")
                .setMetadata(movieMetadata)
                .build();
    }
    private static void loadRemoteMedia(Context context, int position, boolean autoPlay, MediaInfo mediaInfo) {
        if(App.mCastMgr != null) {
            try {
                App.mCastMgr.checkConnectivity();
                App.mCastMgr.startCastControllerActivity(context, mediaInfo, position, autoPlay);
                return;
            } catch (TransientNetworkDisconnectionException e) {
                e.printStackTrace();
            } catch (NoConnectionException e) {
                e.printStackTrace();
            }
        }

        //Chromecast not connected/not available
        DialogManager.ShowChromecastConnectionErrorDialog(context);


    }
}
