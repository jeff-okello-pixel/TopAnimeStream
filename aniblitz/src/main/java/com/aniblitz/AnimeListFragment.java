package com.aniblitz;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.aniblitz.adapters.AnimeListAdapter;
import com.aniblitz.managers.AnimationManager;
import com.aniblitz.models.Anime;
import com.aniblitz.models.Mirror;
public class AnimeListFragment extends Fragment implements OnItemClickListener {

	public int currentSkip = 0;
	public int currentLimit = 40;
	public boolean isLoading = false;
	public boolean loadmore = false;
	public boolean hasResults = false;
	private GridView gridView;
	private ArrayList<Anime> animes;
    private ProgressBar progressBarLoadMore;
    private String fragmentName;
    public boolean isDesc;
	private Resources r;
	App app;
	public Dialog busyDialog;
	public ArrayList<Mirror> mirrors;
	public int animeId;
    private SharedPreferences prefs;
    private AnimeTask task;
    private AnimeListAdapter adapter;
    private TextView txtNoAnime;
    public AnimeListFragment()
	{

	}

	public static AnimeListFragment newInstance(String fragmentName, boolean isDesc) {
		AnimeListFragment ttFrag = new AnimeListFragment();
	    Bundle args = new Bundle();
	    args.putString("fragmentName", fragmentName);
        args.putBoolean("isDesc", isDesc);
	    ttFrag.setArguments(args);
	    return ttFrag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}
	 
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fragmentName = this.getArguments().getString("fragmentName");
        app = (App)getActivity().getApplication();

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

		Anime anime = (Anime)gridView.getAdapter().getItem(position);
		animeId = anime.getAnimeId();

        Intent intent = new Intent(this.getActivity(),AnimeDetailsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("Anime", anime);
        intent.putExtras(bundle);
        startActivity(intent);
        AnimationManager.ActivityStart(getActivity());
		
	}

    @Override
    public void onSaveInstanceState (Bundle outState) {
        outState.putBoolean("isDesc", isDesc);
        super.onSaveInstanceState(outState);

    }
    public void refresh()
    {
        currentSkip = 0;
        if(adapter != null)
            adapter.clear();
        loadmore = false;
        isDesc = ((MainActivity)getActivity()).isDesc;
        AsyncTaskTools.execute(new AnimeTask());
    }
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) { 
        final View rootView = inflater.inflate(R.layout.fragment_anime_list, container, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        r = getResources();
        isDesc = ((MainActivity)getActivity()).isDesc;
        /*
        if(savedInstanceState != null)
            isDesc = savedInstanceState.getBoolean("isDesc");
        else
            isDesc = getArguments().getBoolean("isDesc");*/
        animes = new ArrayList<Anime>();
        fragmentName = getArguments().getString("fragmentName");
        txtNoAnime = (TextView) rootView.findViewById(R.id.txtNoAnime);
        progressBarLoadMore = (ProgressBar)rootView.findViewById(R.id.progressBarLoadMore);
        gridView = (GridView)rootView.findViewById(R.id.gridView);
        gridView.setFastScrollEnabled(true);
        gridView.setOnItemClickListener(this);
        gridView.setScrollingCacheEnabled(false);

        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if(app.IsNetworkConnected())
                {
                    int lastInScreen = firstVisibleItem + visibleItemCount;

                    if ((lastInScreen >= totalItemCount - 6) && !(isLoading)) {
                        if(hasResults)
                        {
                            currentSkip += currentLimit;
                            loadmore = true;

                            task = new AnimeTask();
                            AsyncTaskTools.execute(task);
                        }
                        else if(task == null)
                        {
                            loadmore = false;
                            task = new AnimeTask();
                            currentSkip = 0;
                            AsyncTaskTools.execute(task);
                        }
                    }
                }
            }
        });
        return rootView;
    }
    private class AnimeTask extends AsyncTask<Void, Void, String> {
        private ArrayList<Anime> newAnimes = new ArrayList<Anime>();

        public AnimeTask()
        {

        }
        private String URL;

        @Override
        protected void onPreExecute()
            {
                progressBarLoadMore.setVisibility(View.VISIBLE);
                isLoading = true;
                WcfDataServiceUtility wcfCall = new WcfDataServiceUtility(getString(R.string.anime_service_path)).getEntity("Animes").formatJson().expand("AnimeSources,AnimeSources/vks,Genres,AnimeInformations").orderby(isDesc ? "OriginalName%20desc" : "OriginalName").skip(currentSkip).top(currentLimit);
                String filter;
                if(!App.isVkOnly) {
                    filter = "AnimeSources/any(as:as/LanguageId%20eq%20" + prefs.getString("prefLanguage", "1") + ")";

                    if(fragmentName.equals(getString(R.string.tab_cartoon)))
                        filter = "AnimeSources/any(as:as/LanguageId%20eq%20" + prefs.getString("prefLanguage", "1") + ")%20and%20IsCartoon%20eq%20true";
                    else if(fragmentName.equals(getString(R.string.tab_movie)))
                        filter = "AnimeSources/any(as:as/LanguageId%20eq%20" + prefs.getString("prefLanguage", "1") + ")%20and%20IsMovie%20eq%20true";
                    else if(fragmentName.equals(getString(R.string.tab_serie)))
                        filter = "AnimeSources/any(as:as/LanguageId%20eq%20" + prefs.getString("prefLanguage", "1") + ")%20and%20IsMovie%20eq%20false";
                }else
                {
                    filter = "AnimeSources/any(as:as/LanguageId%20eq%20" + prefs.getString("prefLanguage", "1") + "%20and%20as/vks/any(vk:vk/Id%20gt%200))";
                    if(fragmentName.equals(getString(R.string.tab_cartoon)))
                        filter = "AnimeSources/any(as:as/LanguageId%20eq%20" + prefs.getString("prefLanguage", "1") + "%20and%20as/vks/any(vk:vk/Id%20gt%200))%20and%20IsCartoon%20eq%20true";
                    else if(fragmentName.equals(getString(R.string.tab_movie)))
                        filter = "AnimeSources/any(as:as/LanguageId%20eq%20" + prefs.getString("prefLanguage", "1") + "%20and%20as/vks/any(vk:vk/Id%20gt%200))%20and%20IsMovie%20eq%20true";
                    else if(fragmentName.equals(getString(R.string.tab_serie)))
                        filter = "AnimeSources/any(as:as/LanguageId%20eq%20" + prefs.getString("prefLanguage", "1") + "%20and%20as/vks/any(vk:vk/Id%20gt%200))%20and%20IsMovie%20eq%20false";
                }

                URL = wcfCall.filter(filter).build();
        };
        @Override
        protected String doInBackground(Void... params)
        {

            JSONObject json = Utils.GetJson(URL);
            JSONArray animeArray = new JSONArray();

            try {
                animeArray = json.getJSONArray("value");
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            hasResults = false;
            for(int i = 0;i<animeArray.length();i++)
            {
                hasResults = true;
                JSONObject animeJson;
                try {
                    animeJson = animeArray.getJSONObject(i);
                    newAnimes.add(new Anime(animeJson, getActivity()));
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

            return "Success";
        }

        @Override
        protected void onPostExecute(String result)
        {
            try {
                if (result == null) {
                    Toast.makeText(getActivity(), r.getString(R.string.error_loading_animes), Toast.LENGTH_LONG).show();
                } else {
                    if (loadmore) {

                        for (Anime anime : newAnimes) {
                            adapter.add(anime);
                        }
                        adapter.update();
                    } else {
                        adapter = new AnimeListAdapter(AnimeListFragment.this.getActivity(), newAnimes);
                    gridView.setAdapter(adapter);
                }


                }
                isLoading = false;
                progressBarLoadMore.setVisibility(View.GONE);

                if(gridView.getAdapter().getCount() == 0)
                {
                    txtNoAnime.setVisibility(View.VISIBLE);
                    gridView.setVisibility(View.GONE);
                }
                else
                {
                    txtNoAnime.setVisibility(View.GONE);
                    gridView.setVisibility(View.VISIBLE);
                }
            }
            catch(Exception e)//catch all exception... handle orientation change
            {
                e.printStackTrace();
            }
        }

    }
}
