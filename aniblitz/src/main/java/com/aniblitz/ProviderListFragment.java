package com.aniblitz;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.aniblitz.R;

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
import com.aniblitz.models.Anime;
import com.aniblitz.models.AnimeSource;
import com.aniblitz.models.Mirror;
public class ProviderListFragment extends Fragment implements OnItemClickListener {

	private Resources r;
	App app;
	private ListView listView;
	private ArrayList<Mirror> mirrors;
    private SharedPreferences prefs;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (App)getActivity().getApplication();
		
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
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		listView = (ListView)rootView.findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
		
        return rootView;
    }
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		(new Utils.GetMp4(mirrors.get(position), getActivity())).execute();
	}
	
	public void setProviders(ArrayList<Mirror> lstMirror, String type)
	{
        mirrors = new ArrayList<Mirror>();
        final ArrayList<String> providers = new ArrayList<String>();
        for(Mirror mirror:lstMirror)
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
            mirrors.add(mirror);
        }
		listView.setAdapter(new ProviderListAdapter(getActivity(), mirrors));
	}


}
