package com.aniblitz;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aniblitz.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.aniblitz.adapters.AnimeListAdapter;
import com.aniblitz.adapters.ProviderListAdapter;
import com.aniblitz.interfaces.MovieLoadedEvent;
import com.aniblitz.models.Anime;
import com.aniblitz.models.AnimeSource;
import com.aniblitz.models.Mirror;
public class ProviderListFragment extends Fragment implements OnItemClickListener {

	private Resources r;
	App app;
	private ListView listView;
	private ArrayList<Mirror> mirrors;
    private SharedPreferences prefs;
    private String type;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (App)getActivity().getApplication();
        type = getArguments().getString("type");
		mirrors = getArguments().getParcelableArrayList("mirrors");
	}
    public static ProviderListFragment newInstance(String type, ArrayList<Mirror> mirrors) {
        ProviderListFragment frag = new ProviderListFragment();

        Bundle args = new Bundle();
        args.putString("type", type);
        args.putParcelableArrayList("mirrors", mirrors);
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
        r = getResources();

		listView = (ListView)rootView.findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
		if(savedInstanceState != null)
        {
            this.mirrors = savedInstanceState.getParcelableArrayList("mirrors");
            listView.setAdapter(new ProviderListAdapter(getActivity(), mirrors));
        }
        else
        {
            AsyncTaskTools.execute(new LoadProvidersTask());
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("mirrors", mirrors);
        super.onSaveInstanceState(outState);
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		(new Utils.GetMp4(mirrors.get(position), getActivity())).execute();
	}

    public class LoadProvidersTask extends AsyncTask<Void, Void, String> {
        private String URL;
        private ArrayList<Mirror> mirrors;
        private Dialog busyDialog;

        @Override
        protected void onPreExecute()
        {
            busyDialog = Utils.showBusyDialog(getString(R.string.loading_anime_details), getActivity());
            URL = new WcfDataServiceUtility(getString(R.string.anime_service_path)).getTableSpecificRow("AnimeSources",anime.getAnimeSources().get(0).getAnimeSourceId(),false).formatJson().expand("Mirrors/Provider,Mirrors/AnimeSource").build();

        };
        @Override
        protected String doInBackground(Void... params)
        {

            JSONObject json = Utils.GetJson(URL);
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
            if(result == null)
            {
                Toast.makeText(getActivity(), getString(R.string.error_loading_anime_details), Toast.LENGTH_LONG).show();
                return;
            }
            else
            {
                ArrayList<Mirror> filteredMirrors = new ArrayList<Mirror>();
                for(Mirror mirror:mirrors)
                {
                    if(String.valueOf(mirror.getAnimeSource().getLanguageId()).equals(prefs.getString("prefLanguage", "1")))
                    {
                        if(type.equals("Dubbed"))
                        {
                            if(mirror.getAnimeSource().isSubbed())
                                continue;
                        }
                        else if(type.equals("Subbed"))
                        {
                            if(!mirror.getAnimeSource().isSubbed())
                                continue;
                        }

                    }
                    else
                        continue;
                    filteredMirrors.add(mirror);
                }
                if(listView != null)
                    listView.setAdapter(new ProviderListAdapter(getActivity(), filteredMirrors));
            }

            Utils.dismissBusyDialog(busyDialog);


        }

    }

}
