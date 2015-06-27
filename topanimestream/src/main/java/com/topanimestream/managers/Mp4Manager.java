package com.topanimestream.managers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.widget.Toast;

import com.fwwjt.pacjz173199.Prm;
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

import com.topanimestream.App;
import com.topanimestream.utilities.AsyncTaskTools;
import com.topanimestream.utilities.Utils;
import com.topanimestream.views.VideoActivity;
import com.topanimestream.models.Anime;
import com.topanimestream.models.Episode;
import com.topanimestream.models.Mirror;
import com.topanimestream.R;

public class Mp4Manager {
    public static Dialog chromecastDialog;
    public static Dialog qualityDialog;
    public static String ignitionKey = null;

    public static void getMp4(Mirror mirror, Activity act, Anime anime, Episode episode) {
        AsyncTaskTools.execute(new GetWebPageTask(mirror, act, anime, episode));
    }

    public static class GetWebPageTask extends AsyncTask<Void, Void, String> {
        Mirror mirror;
        private Dialog busyDialog;
        private Activity act;
        private Anime anime;
        private AlertDialog alertPlay;
        private Episode episode;
        private String providerName;
        private Document doc;

        public GetWebPageTask(Mirror mirror, Activity act, Anime anime, Episode episode) {
            this.mirror = mirror;
            this.act = act;
            this.anime = anime;
            this.episode = episode;
        }

        @Override
        protected void onPreExecute() {
            busyDialog = DialogManager.showBusyDialog(act.getString(R.string.loading_video), act);
            if (!App.isPro) {
                Prm prm = new Prm(act, null, false);
                try {
                    prm.runAppWall();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
                if (providerName.equals(act.getString(R.string.play).toLowerCase())) {
                    providerName = "vk";
                }
            } catch (Exception e) {
                return null;
            }

            return "Success";
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (result == null) {
                    Toast.makeText(act, act.getString(R.string.error_loading_video), Toast.LENGTH_LONG).show();
                    AsyncTaskTools.execute(new Utils.ReportMirror(mirror.getMirrorId(), act));
                } else {
                    if (providerName.equals("vk") || providerName.equals("vk_gk") || providerName.equals("vkontakte")) {
                        String content = doc.html();
                        CharSequence[] items = null;
                        if (content.indexOf("url720") != -1)
                            items = new CharSequence[]{"720", "480", "360", "240"};
                        else if (content.indexOf("url480") != -1)
                            items = new CharSequence[]{"480", "360", "240"};
                        else if (content.indexOf("url360") != -1)
                            items = new CharSequence[]{"360", "240"};
                        else if (content.indexOf("url240") != -1)
                            items = new CharSequence[]{"240"};

                        if (items != null && items.length > 1) {
                            final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(act);
                            alertBuilder.setTitle(act.getString(R.string.choose_quality));
                            final CharSequence[] finalItems = items;
                            alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                    AsyncTaskTools.execute(new GetMp4(mirror, act, anime, episode, providerName, finalItems[item].toString(), doc, null));
                                }
                            });
                            qualityDialog = alertBuilder.create();
                            qualityDialog.show();
                        } else if (items != null) {
                            Toast.makeText(act, act.getString(R.string.only_240p_available), Toast.LENGTH_SHORT).show();
                            AsyncTaskTools.execute(new GetMp4(mirror, act, anime, episode, providerName, "240", doc, null));
                        } else {
                            throw new Exception("Vk not valid video");
                        }


                        DialogManager.dismissBusyDialog(busyDialog);
                    } else {
                        AsyncTaskTools.execute(new GetMp4(mirror, act, anime, episode, providerName, null, doc, busyDialog));
                    }
                }


            } catch (Exception e)//catch all exception, handle orientation change
            {
                e.printStackTrace();
                Toast.makeText(act, act.getString(R.string.error_loading_video), Toast.LENGTH_LONG).show();
                DialogManager.dismissBusyDialog(busyDialog);
                AsyncTaskTools.execute(new Utils.ReportMirror(mirror.getMirrorId(), act));
            }


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

        public GetMp4(Mirror mirror, Activity act, Anime anime, Episode episode, String providerName, String quality, Document doc, Dialog busyDialog) {
            this.mirror = mirror;
            this.act = act;
            this.anime = anime;
            this.episode = episode;
            this.providerName = providerName;
            this.quality = quality;
            this.doc = doc;
            this.busyDialog = busyDialog;
            r = act.getResources();
        }


        @Override
        protected void onPreExecute() {
            if (busyDialog == null) {
                busyDialog = DialogManager.showBusyDialog(act.getString(R.string.loading_video), act);
            }
        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                byte[] data = doc.html().getBytes("UTF-8");
                String base64 = Base64.encodeToString(data, Base64.DEFAULT);


                String URL = act.getString(R.string.anime_data_service_path) + "GetMp4Url?provider='" + URLEncoder.encode(providerName) + "'" + (quality != null ? "&quality='" + quality + "'" : "") + "&$format=json";
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost request = new HttpPost(URL);
                    /*
					List<NameValuePair> listParam = new ArrayList<NameValuePair>();
					listParam.add(new BasicNameValuePair("html", base64));
		            UrlEncodedFormEntity ent = new UrlEncodedFormEntity(listParam);*/
                request.setEntity(new StringEntity(base64));
                if (App.accessToken != null && !App.accessToken.equals(""))
                    request.setHeader("Authentication", App.accessToken);
                else
                    request.setHeader("Authentication", App.getContext().getString(R.string.urc));
                HttpResponse response = httpClient.execute(request);
                HttpEntity entity = response.getEntity();
                InputStream instream = entity.getContent();
                try {
                    JSONObject jsonObj = new JSONObject(Utils.convertStreamToString(instream));
                    return jsonObj.getString("vdalue");
                } catch (JSONException e1) {
                    e1.printStackTrace();
                    return null;
                } catch (Exception e1) {
                    e1.printStackTrace();
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(final String result) {
            DialogManager.dismissBusyDialog(busyDialog);
            if (App.isGooglePlayVersion) {
                Toast.makeText(act, act.getString(R.string.video_spanish_only), Toast.LENGTH_LONG).show();
            }
            if (result == null) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                try {
                    i.setData(Uri.parse(URLDecoder.decode(mirror.getSource(), "UTF-8")));
                } catch (UnsupportedEncodingException e) {
                    return;
                }
                act.startActivity(i);
                //Toast.makeText(act, r.getString(R.string.error_loading_video) , Toast.LENGTH_LONG).show();
                AsyncTaskTools.execute(new Utils.ReportMirror(mirror.getMirrorId(), act));
                return;
            }

            final CharSequence[] items = {act.getString(R.string.play_phone), act.getString(R.string.stream_chromecast), act.getString(R.string.cancel)};
            final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(act);
            alertBuilder.setTitle(act.getString(R.string.choose_option));
            alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    if (items[item].equals(act.getString(R.string.play_phone))) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
                        String playInternal = prefs.getString("prefPlayInternal", "undefined");
                        if (playInternal.equals("true"))
                            PlayInternalVideo(act, result, mirror.getMirrorId());
                        else if (playInternal.equals("false"))
                            PlayExternalVideo(act, result);
                        else
                            DialogManager.ShowChoosePlayerDialog(act, result, mirror.getMirrorId());
                    } else if (items[item].equals(act.getString(R.string.stream_chromecast))) {
                        if (App.isPro) {
                            String subtitle = "";
                            if (episode != null && episode.getEpisodeInformations() != null && episode.getEpisodeInformations().getEpisodeName() != null && !episode.getEpisodeInformations().getEpisodeName().equals("")) {
                                subtitle = episode.getEpisodeInformations().getEpisodeName();
                            } else if (episode != null) {
                                subtitle = act.getString(R.string.episode) + episode.getEpisodeNumber();
                            } else {
                                subtitle = act.getString(R.string.tab_movie);
                            }

                            MediaInfo info = buildMediaInfo(anime.getName(), subtitle, anime.getGenresFormatted(), Uri.parse(result).toString(), Utils.resizeImage(App.getContext().getString(R.string.image_host_path) + anime.getPosterPath(), App.ImageSize.w185.getValue()), Utils.resizeImage(App.getContext().getString(R.string.image_host_path) + anime.getBackdropPath(), App.ImageSize.w500.getValue()));
                            loadRemoteMedia(act, 0, true, info);
                        } else {
                            DialogManager.ShowChromecastNotPremiumErrorDialog(act);
                        }
                        alertPlay.dismiss();
                    } else {
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
        if (App.mCastMgr != null) {
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

    public static void PlayInternalVideo(Context context, String mp4Url, int mirrorId) {
        Intent intent = null;
        intent = new Intent(context, VideoActivity.class);
        intent.putExtra("Mp4Url", mp4Url);
        intent.putExtra("MirrorId", mirrorId);
        context.startActivity(intent);
    }

    public static void PlayExternalVideo(Context context, String mp4Url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(mp4Url), "video/*");
        context.startActivity(intent);
    }
}
