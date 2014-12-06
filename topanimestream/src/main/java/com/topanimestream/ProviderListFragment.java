package com.topanimestream;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.topanimestream.R;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.topanimestream.adapters.ProviderListAdapter;
import com.topanimestream.managers.Mp4Manager;
import com.topanimestream.models.Anime;
import com.topanimestream.models.AnimeSource;
import com.topanimestream.models.Episode;
import com.topanimestream.models.Mirror;
public class ProviderListFragment extends Fragment implements OnItemClickListener {

	private Resources r;
    App app;
    private ListView listView;
    private int animeSourceId;
    private SharedPreferences prefs;
    private Episode episode;
    private ArrayList<Mirror> mirrors;
    private TextView txtNoProvider;
    private String type; //subbed dubbed
    private Anime anime;
    private ArrayList<Mirror> filteredMirrors;
    private Dialog qualityDialog;
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (App)getActivity().getApplication();
        animeSourceId = getArguments().getInt("animeSourceId");
        type = getArguments().getString("type");
        episode = getArguments().getParcelable("episode");
        if(episode != null)
          mirrors = episode.getMirrors();
        anime = getArguments().getParcelable("anime");
	}
    public static ProviderListFragment newInstance(int animeSourceId, Episode episode, String type, Anime anime) {
        ProviderListFragment frag = new ProviderListFragment();

        Bundle args = new Bundle();
        args.putInt("animeSourceId", animeSourceId);
        args.putString("type", type);
        args.putParcelable("episode", episode);
        args.putParcelable("anime", anime);
        frag.setArguments(args);

        return frag;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) { 
        final View rootView = inflater.inflate(R.layout.fragment_providers, container, false);
        filteredMirrors = new ArrayList<Mirror>();
        txtNoProvider = (TextView) rootView.findViewById(R.id.txtNoProvider);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		listView = (ListView)rootView.findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
		if(savedInstanceState != null)
        {
            mirrors = savedInstanceState.getParcelableArrayList("mirrors");
            episode = savedInstanceState.getParcelable("episode");
            filteredMirrors = mirrors;
            animeSourceId = savedInstanceState.getInt("animeSourceId");
            anime = savedInstanceState.getParcelable("anime");
            if(filteredMirrors != null)
                listView.setAdapter(new ProviderListAdapter(getActivity(), filteredMirrors));
            else
            {
                txtNoProvider.setVisibility(View.VISIBLE);
                listView.setVisibility(View.GONE);
            }
        }
        else
        {
            if(animeSourceId != -1) //is a movie
            {
                txtNoProvider.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                for(AnimeSource animeSource: anime.getAnimeSources())
                {
                    //we already know that this animesource correspond to the subbed/dubbed and language that we want (checked in EpisodesContainerFragment)
                    if(animeSource.getAnimeSourceId() == animeSourceId)
                    {
                        /*
                        if(animeSource.getVks().size() > 0)
                        {
                            //We have a vk source (yay!), we know it is the best provider so we only show the option to play the video
                            Mirror mirror = new Mirror(animeSource.getVks().get(0));
                            //Since we want to play the video, let's change the provider name to Play so the user doesn't see VK
                            mirror.getProvider().setName(getString(R.string.play));
                            mirrors = new ArrayList<Mirror>();
                            mirrors.add(mirror);
                            filteredMirrors = mirrors;
                            listView.setAdapter(new ProviderListAdapter(getActivity(), mirrors));
                        }
                        else
                        {*/
                            //We do not have anything in vk so we let the user choose the provider he wants
                            AsyncTaskTools.execute(new LoadProvidersTask());
                        //}
                    }
                }




            }
            else if(mirrors != null && !mirrors.isEmpty())//is not a movie
            {
                txtNoProvider.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                String language = prefs.getString("prefLanguage", "1");
                for(Mirror mirror: mirrors)
                {
                    if(!String.valueOf(mirror.getAnimeSource().getLanguageId()).equals(language))
                        continue;

                    if(type.equals("Subbed")) {
                        if (mirror.getAnimeSource().isSubbed())
                            filteredMirrors.add(mirror);
                    }
                    else {
                        if (!mirror.getAnimeSource().isSubbed())
                            filteredMirrors.add(mirror);
                    }
                }
                listView.setAdapter(new ProviderListAdapter(getActivity(), filteredMirrors));
            }
            else//is a movie, but there's no provider in this tab
            {
                txtNoProvider.setVisibility(View.VISIBLE);
                listView.setVisibility(View.GONE);
            }
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(animeSourceId != -1)
            outState.putParcelableArrayList("mirrors", mirrors);
        else if(mirrors != null && !mirrors.isEmpty())
            outState.putParcelableArrayList("mirrors", filteredMirrors);
        else
            outState.putParcelableArrayList("mirrors", null);
        outState.putParcelable("anime", anime);
        outState.putParcelable("episode", episode);
        outState.putInt("animeSourceId", animeSourceId);

        super.onSaveInstanceState(outState);
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        Mp4Manager.getMp4(filteredMirrors.get(position), getActivity(), anime, episode);
	}

    public class LoadProvidersTask extends AsyncTask<Void, Void, String> {
        private String URL;
        private Dialog busyDialog;

        @Override
        protected void onPreExecute()
        {
            busyDialog = Utils.showBusyDialog(getString(R.string.loading_anime_details), getActivity());
            URL = new WcfDataServiceUtility(getString(R.string.anime_data_service_path)).getEntitySpecificRow("AnimeSources", animeSourceId,false).formatJson().expand("Mirrors/Provider").build();
        };
        @Override
        protected String doInBackground(Void... params)
        {

            JSONObject json = Utils.GetJson(URL);
            if(json == null)
            {
                return null;
            }
            JSONArray mirrorArray = new JSONArray();
            try {
                mirrors = new ArrayList<Mirror>();
                mirrorArray = json.getJSONArray("Mirrors");
            } catch (JSONException e) {
                return null;
            }
            for(int i = 0;i<mirrorArray.length();i++)
            {
                try {
                    mirrors.add(new Mirror(mirrorArray.getJSONObject(i)));
                } catch (Exception e) {
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
                    Toast.makeText(getActivity(), getString(R.string.error_loading_anime_details), Toast.LENGTH_LONG).show();
                } else {
                    filteredMirrors = mirrors;
                    listView.setAdapter(new ProviderListAdapter(getActivity(), mirrors));
                }

                Utils.dismissBusyDialog(busyDialog);
            }catch(Exception e)//catch all exception, handle orientation change
            {
                e.printStackTrace();
            }


        }

    }

}
