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
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.aniblitz.adapters.AnimeListAdapter;
import com.aniblitz.models.Anime;
import com.aniblitz.models.AnimeSource;
import com.aniblitz.models.Mirror;
public class AnimeListFragment extends Fragment implements OnItemClickListener {

	public int currentSkip = 0;
	public int currentLimit = 20;
	public boolean isLoading = false;
	public boolean loadmore = false;
	public boolean hasResults = false;
	private GridView gridView;
	private ArrayList<Anime> animes;
	private ArrayList<Anime> filteredAnimes;
	private ArrayList<AnimeSource> animeSources;
    private String fragmentName;
	private Resources r;
	int index = 0;
	App app;
	public Dialog busyDialog;
	public ArrayList<Mirror> mirrors;
	public int animeId;
	public AlertDialog alertProviders;
	private int animeSourceId;
	public AlertDialog alertType;
	public AnimeListFragment()
	{

	}
    public void clear()
    {
        this.animes = new ArrayList<Anime>();
        this.filteredAnimes = new ArrayList<Anime>();
        this.animeSources = new ArrayList<AnimeSource>();
    }

	public static AnimeListFragment newInstance(String fragmentName) {
		AnimeListFragment ttFrag = new AnimeListFragment();
	    Bundle args = new Bundle();
	    args.putString("fragmentName", fragmentName);
	    ttFrag.setArguments(args);
	    return ttFrag;
	}
	public AnimeListAdapter setAnimes(final ArrayList<Anime> animes, String fragmentName)
	{
		if(animes != null && animes.size() > 0)
		{
            if(this.getActivity() != null)
            {
                filteredAnimes = new ArrayList<Anime>();
                if(fragmentName.equals(getString(R.string.tab_all)))
                {
                    filteredAnimes = animes;
                }
                else if(fragmentName.equals(getString(R.string.tab_serie)))
                {
                    for(Anime anime:animes)
                    {
                        if(!anime.isCartoon() && !anime.isMovie())
                        {
                            filteredAnimes.add(anime);
                        }
                    }
                }
                else if(fragmentName.equals(getString(R.string.tab_movie)))
                {
                    for(Anime anime:animes)
                    {
                        if(anime.isMovie())
                        {
                            filteredAnimes.add(anime);
                        }

                    }
                }
                else if(fragmentName.equals(getString(R.string.tab_cartoon)))
                {
                    for(Anime anime:animes)
                    {
                        if(anime.isCartoon())
                        {
                            filteredAnimes.add(anime);
                        }
                    }
                }

				AnimeListAdapter adapter = new AnimeListAdapter(this.getActivity(), filteredAnimes);
				gridView.setAdapter(adapter);
				return adapter;
			}
		}
		return null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        fragmentName = this.getArguments().getString("fragmentName");
		app = (App)getActivity().getApplication();
		
	}
	 
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
		
    }

    @Override
    public void onResume() {
    	super.onResume();
        AnimeListAdapter adapter = new AnimeListAdapter(this.getActivity(), ((FragmentEvent)getActivity()).onFragmentResumed(fragmentName));
        gridView.setAdapter(adapter);
    }
    @Override
    public void onPause() {
    	super.onPause();
    }
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Anime anime = filteredAnimes.get(position);
		animeId = anime.getAnimeId();
		if(!anime.isMovie())
		{
			Intent intent = new Intent(this.getActivity(),EpisodesActivity.class);
	     	Bundle bundle = new Bundle();
	     	bundle.putParcelable("Anime", anime);
	     	intent.putExtras(bundle);
			startActivity(intent);
		}
		else
		{
			(new Utils.MirrorsTask(this.getActivity(),animeId)).execute();
		}
		
	}
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) { 
        final View rootView = inflater.inflate(R.layout.fragment_anime_list, container, false);
        r = getResources();
        fragmentName = getArguments().getString("fragmentName");
        gridView = (GridView)rootView.findViewById(R.id.gridView);
        gridView.setFastScrollEnabled(true);
        gridView.setOnItemClickListener(this);
		
        return rootView;
    }

private class ContentAdapter extends ArrayAdapter<String> implements SectionIndexer {
    	
    	private String mSections = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    	
		public ContentAdapter(Context context, int textViewResourceId,
				List<String> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public int getPositionForSection(int section) {
			// If there is no item for current section, previous section will be selected
			for (int i = section; i >= 0; i--) {
				for (int j = 0; j < getCount(); j++) {
					if (i == 0) {
						// For numeric section
						for (int k = 0; k <= 9; k++) {
							if (StringMatcher.match(String.valueOf(getItem(j).charAt(0)), String.valueOf(k)))
								return j;
						}
					} else {
						if (StringMatcher.match(String.valueOf(getItem(j).charAt(0)), String.valueOf(mSections.charAt(i))))
							return j;
					}
				}
			}
			return 0;
		}

		@Override
		public int getSectionForPosition(int position) {
			return 0;
		}

		@Override
		public Object[] getSections() {
			String[] sections = new String[mSections.length()];
			for (int i = 0; i < mSections.length(); i++)
				sections[i] = String.valueOf(mSections.charAt(i));
			return sections;
		}
    }
    public interface FragmentEvent
    {
        ArrayList<Anime> onFragmentResumed(String fragmentName);
    }
}
