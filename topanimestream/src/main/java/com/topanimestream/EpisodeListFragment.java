package com.topanimestream;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import com.topanimestream.adapters.EpisodeListAdapter;
import com.topanimestream.models.Episode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EpisodeListFragment extends Fragment implements OnItemClickListener {

    private int currentSkip = 0;
    private int currentLimit = 100;
    private boolean isLoading = false;
    private boolean loadmore = false;
    private boolean hasResults = false;
    private EpisodeListAdapter adapter;
    private String fragmentName;
    private ArrayList<Episode> episodes;
    private TextView txtNoEpisode;
    private int animeId;
    App app;
    public Dialog busyDialog;
    private SharedPreferences prefs;
    private String animeName;
    private String animePoster;
    private String animeDescription;
    private String animeBackdrop;
    private String animeGenres;
    private String animeRating;
    private boolean isSubbed;
    private ListView listViewEpisodes;
    private EpisodesTask task;
    private ProgressBar progressBarLoadMore;

    public EpisodeListFragment() {

    }

    public static EpisodeListFragment newInstance(String fragmentName, int animeId, String animeName, String animeDescription, String animePoster, String animeBackdrop, String animeGenres, String animeRating) {
        EpisodeListFragment ttFrag = new EpisodeListFragment();
        Bundle args = new Bundle();
        args.putString("fragmentName", fragmentName);//Subbed, Dubbed
        args.putInt("animeId", animeId);
        args.putString("animeName", animeName);
        args.putString("animeDescription", animeDescription);
        args.putString("animePoster", animePoster);
        args.putString("animeBackdrop", animeBackdrop);
        args.putString("animeGenres", animeGenres);
        args.putString("animeRating", animeRating);
        ttFrag.setArguments(args);
        return ttFrag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (App) getActivity().getApplication();

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {

        final Episode episode = (Episode)listViewEpisodes.getAdapter().getItem(position);
        EpisodesContainerFragment.ProviderFragmentCoordinator providerFragmentCoordinator = (EpisodesContainerFragment.ProviderFragmentCoordinator) getActivity();
        providerFragmentCoordinator.onEpisodeSelected(episode, fragmentName);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final View rootView = inflater.inflate(R.layout.fragment_episode_list, container, false);
        progressBarLoadMore = (ProgressBar) rootView.findViewById(R.id.progressBarLoadMore);
        txtNoEpisode = (TextView) rootView.findViewById(R.id.txtNoEpisode);
        listViewEpisodes = (ListView) rootView.findViewById(R.id.listViewEpisodes);

        Bundle bundle = getArguments();
        fragmentName = bundle.getString("fragmentName");
        animeId = bundle.getInt("animeId");
        animeDescription = bundle.getString("animeDescription");
        animeName = bundle.getString("animeName");
        animePoster = bundle.getString("animePoster");
        animeBackdrop = bundle.getString("animeBackdrop");
        animeGenres = bundle.getString("animeGenres");
        animeRating = bundle.getString("animeRating");

        if (savedInstanceState != null) {
            episodes = savedInstanceState.getParcelableArrayList("episodes");
            this.animeName = savedInstanceState.getString("animeName");
            this.animeDescription = savedInstanceState.getString("animeDescription");
            this.animePoster = savedInstanceState.getString("animePoster");
            this.animeBackdrop = savedInstanceState.getString("animeBackdrop");
            this.animeId = savedInstanceState.getInt("animeId");
            if (episodes != null && episodes.size() > 0)
                listViewEpisodes.setAdapter(new EpisodeListAdapter(this.getActivity(), episodes, animeName, animeDescription, animePoster, animeBackdrop, animeGenres, animeRating));
            else {
                listViewEpisodes.setVisibility(View.GONE);
                txtNoEpisode.setVisibility(View.VISIBLE);
            }
        }

        if(fragmentName.equals("Subbed"))
            isSubbed = true;
        else
            isSubbed = false;

        listViewEpisodes.setFastScrollEnabled(true);
        listViewEpisodes.setOnItemClickListener(this);
        listViewEpisodes.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (app.IsNetworkConnected()) {
                    int lastInScreen = firstVisibleItem + visibleItemCount;

                    if ((lastInScreen >= totalItemCount - 6) && !(isLoading)) {
                        if (hasResults) {
                            currentSkip += currentLimit;
                            loadmore = true;

                            task = new EpisodesTask();
                            AsyncTaskTools.execute(task);
                        } else if (task == null) {
                            loadmore = false;
                            task = new EpisodesTask();
                            currentSkip = 0;
                            AsyncTaskTools.execute(task);
                        }
                    }
                }
            }
        });


        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(adapter != null)
            episodes = ((EpisodeListAdapter)listViewEpisodes.getAdapter()).getAllEpisodes();
        outState.putParcelableArrayList("episodes", episodes);
        outState.putInt("animeId", animeId);
        outState.putString("animeName", animeName);
        outState.putString("animeDescription", animeDescription);
        outState.putString("animePoster", animePoster);
        outState.putString("animeBackdrop", animeBackdrop);
        super.onSaveInstanceState(outState);
    }


    private class EpisodesTask extends AsyncTask<Void, Void, String> {
        private ArrayList<Episode> newEpisodes = new ArrayList<Episode>();
        public EpisodesTask() {

        }

        private String URL;

        @Override
        protected void onPreExecute() {
            Utils.lockScreen(getActivity());
            progressBarLoadMore.setVisibility(View.VISIBLE);
            isLoading = true;
            URL = new WcfDataServiceUtility(getString(R.string.anime_data_service_path)).getEntity("Episodes").filter("AnimeId%20eq%20" + animeId + "%20and%20Mirrors/any(m:m/AnimeSource/LanguageId%20eq%20" + prefs.getString("prefLanguage", "1") + "%20and%20m/AnimeSource/IsSubbed%20eq%20" + isSubbed + ")").expand("Mirrors/AnimeSource,Mirrors/Provider,EpisodeInformations").skip(currentSkip).top(currentLimit).formatJson().build();
            episodes = new ArrayList<Episode>();
        }

        @Override
        protected String doInBackground(Void... params) {
            hasResults = false;
            JSONObject json = Utils.GetJson(URL);
            if (!json.isNull("error")) {
                try {
                    int error = json.getInt("error");
                    if (error == 401) {
                        return "401";
                    }
                } catch (Exception e) {
                    return null;
                }
            }
            if (json == null) {
                return null;
            }
            JSONArray episodesArray = new JSONArray();

            try {
                episodesArray = json.getJSONArray("value");
            } catch (JSONException e) {
                return null;
            }

            if(episodesArray.length() > 0)
                hasResults = true;

            for (int i = 0; i < episodesArray.length(); i++) {
                JSONObject episodeJson;
                try {
                    episodeJson = episodesArray.getJSONObject(i);
                    Episode episode = new Episode(episodeJson, getActivity());

                    newEpisodes.add(episode);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }

            }
            return "Success";
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (result == null) {
                    Toast.makeText(getActivity(), getString(R.string.error_loading_episodes), Toast.LENGTH_LONG).show();
                } else if (result.equals("401")) {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.have_been_logged_out), Toast.LENGTH_LONG).show();
                    getActivity().startActivity(new Intent(getActivity(), LoginActivity.class));
                    getActivity().finish();
                } else {
                    if (loadmore) {

                        for (Episode episode : newEpisodes) {
                                adapter.add(episode);
                        }
                        adapter.update();
                    } else {
                        adapter = new EpisodeListAdapter(getActivity(), newEpisodes, animeName, animeDescription, animePoster, animeBackdrop, animeGenres, animeRating);
                        SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(adapter);
                        swingBottomInAnimationAdapter.setAbsListView(listViewEpisodes);
                        assert swingBottomInAnimationAdapter.getViewAnimator() != null;
                        swingBottomInAnimationAdapter.getViewAnimator().setInitialDelayMillis(300);
                        listViewEpisodes.setAdapter(swingBottomInAnimationAdapter);
                    }
                }


                isLoading = false;
                progressBarLoadMore.setVisibility(View.GONE);

                if (listViewEpisodes.getAdapter().getCount() == 0) {
                    txtNoEpisode.setVisibility(View.VISIBLE);
                    listViewEpisodes.setVisibility(View.GONE);
                } else {
                    txtNoEpisode.setVisibility(View.GONE);
                    listViewEpisodes.setVisibility(View.VISIBLE);
                }

            } catch (Exception e)//catch all exception, handle orientation change
            {
                e.printStackTrace();
            }
            try {
                Utils.unlockScreen(getActivity());//User pressed back before the request is finished
            }catch(Exception e)
            {}
        }
    }

}
