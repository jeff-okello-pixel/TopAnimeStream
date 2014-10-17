package com.aniblitz;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.aniblitz.models.Anime;
import com.aniblitz.models.AnimeInformation;
import com.aniblitz.models.Episode;
import com.aniblitz.models.Genre;

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 6;
    private static final String DATABASE_NAME = "AnimeDB";

    private static final String TABLE_FAVORITES = "favorites";
    private static final String TABLE_WATCHED = "watched";
    private static final String TABLE_FLAGS = "flags";
    //Columns for flags
    private static final String KEY_ISPRO = "isPro";
    private static final String KEY_ISFIRSTTIME = "isFirstTime";
    //Columns for favorites
    private static final String KEY_ANIMEID = "animeId";
    private static final String KEY_NAME = "name";
    private static final String KEY_POSTER = "poster";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_GENRES = "genres";
    private static final String KEY_RATING = "rating";
    private static final String KEY_BACKDROP = "backdrop";
    
    //Columns for watched
    private static final String KEY_EPISODEID = "episodeId";
    private static final String KEY_EPISODENUMBER = "episodeNumber";
    private static final String KEY_LANGUAGEID = "languageId";
    
    private static final String[] FAVORITES_COLUMNS = {KEY_ANIMEID, KEY_NAME, KEY_POSTER, KEY_DESCRIPTION, KEY_GENRES, KEY_RATING, KEY_BACKDROP, KEY_LANGUAGEID};
    private static final String[] WATCHED_COLUMNS = {KEY_ANIMEID, KEY_NAME, KEY_POSTER, KEY_DESCRIPTION, KEY_EPISODEID, KEY_EPISODENUMBER, KEY_BACKDROP, KEY_GENRES, KEY_RATING, KEY_LANGUAGEID};
    private static final String[] FLAGS_COLUMNS = {KEY_ISPRO, KEY_ISFIRSTTIME};
    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION); 
    }
 
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_FAVORITES_TABLE = "CREATE TABLE " + TABLE_FAVORITES + " ( " +
        		KEY_ANIMEID + " TEXT, " +
        		KEY_NAME + " TEXT, "+
        		KEY_POSTER + " TEXT, "+
        		KEY_DESCRIPTION + " TEXT, " +
        		KEY_GENRES + " TEXT, " +
                KEY_RATING + " TEXT, " +
                KEY_BACKDROP + " TEXT, " +
        		KEY_LANGUAGEID + " TEXT)";
        db.execSQL(CREATE_FAVORITES_TABLE);
        
        String CREATE_WATCHED_TABLE = "CREATE TABLE " + TABLE_WATCHED + " ( " +
        		KEY_ANIMEID + " TEXT, " +
        		KEY_NAME + " TEXT, " +
        		KEY_POSTER + " TEXT, " +
        		KEY_DESCRIPTION + " TEXT, " +
        		KEY_EPISODEID + " TEXT, " +
        		KEY_EPISODENUMBER + " TEXT, " +
                KEY_BACKDROP + " TEXT, " +
                KEY_GENRES + " TEXT, " +
                KEY_RATING + " TEXT, " +
        		KEY_LANGUAGEID + " TEXT)";
        db.execSQL(CREATE_WATCHED_TABLE);

        String CREATE_FLAGS_TABLE = "CREATE TABLE " + TABLE_FLAGS + " ( " +
                KEY_ISPRO + " TEXT, " +
                KEY_ISFIRSTTIME + " TEXT)";
        db.execSQL(CREATE_FLAGS_TABLE);
    }
 
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WATCHED);
        this.onCreate(db);
    }
    public void setPro(boolean pro)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ISPRO, String.valueOf(pro));
        db.insert(TABLE_FLAGS,
                null, //nullColumnHack
                values);

        db.close();

        App.isPro = pro;
    }
    public boolean isPro()
    {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor =
                db.query(TABLE_FLAGS,
                        FLAGS_COLUMNS,
                        KEY_ISPRO + " = ?", //selections
                        new String[] { String.valueOf(true)}, //selections args
                        null, //group by
                        null, //having
                        null, //order by
                        null);//limit

        if (cursor != null)
        {
            if(cursor.getCount() > 0)
            {
                cursor.close();
                return true;
            }
            cursor.close();
        }

        return false;
    }
    public void addFavorite(int animeId, String name, String poster, String genres, String description, String rating, String backdrop, int languageId){
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_ANIMEID, String.valueOf(animeId));
        values.put(KEY_NAME, name);
        values.put(KEY_POSTER, poster);
        values.put(KEY_GENRES, genres);
        values.put(KEY_DESCRIPTION, description);
        values.put(KEY_RATING, rating);
        values.put(KEY_BACKDROP, backdrop);
        values.put(KEY_LANGUAGEID, languageId);
        db.insert(TABLE_FAVORITES,
                null, //nullColumnHack
                values);
        
        db.close();
    }
    
    public void removeFavorite(int animeId)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FAVORITES, KEY_ANIMEID + " = ?",  new String[] { String.valueOf(animeId) });
        db.close();
    }
    
    public ArrayList<Anime> getFavorites(String languageId)
    {
    	ArrayList<Anime> animes = new ArrayList<Anime>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor =
                db.query(TABLE_FAVORITES,
                FAVORITES_COLUMNS,
                KEY_LANGUAGEID + " = ?", //selections
                new String[] {languageId}, //selections args
                null, //group by
                null, //having
                null, //order by
                null);//limit
 
        if (cursor != null)
        {
        	cursor.moveToFirst();
        	for(int i = 0; i < cursor.getCount(); i++)
        	{
        		Anime anime = new Anime();
        		anime.setAnimeId(Integer.valueOf(cursor.getString(0)));
        		anime.setName(cursor.getString(1));
        		anime.setPosterPath(cursor.getString(2));
                ArrayList<AnimeInformation> animeInfos = new ArrayList<AnimeInformation>();
                animeInfos.add(new AnimeInformation(Integer.valueOf(languageId), cursor.getString(3)));
                anime.setAnimeInformations(animeInfos);
        		ArrayList<Genre> genres = new ArrayList<Genre>();
        		String[] genreArray = cursor.getString(4).split(", ");
        		for(String genreStr:genreArray)
        		{
        			Genre genre = new Genre();
        			genre.setName(genreStr);
        			genres.add(genre);
        		}
        		anime.setGenres(genres);
                anime.setRating(Double.valueOf(cursor.getString(5)));
                anime.setBackdropPath(cursor.getString(6));
        		animes.add(anime);
        		cursor.moveToNext();
        	}
        	cursor.close();
        }

        return animes;
    }
    
    public boolean isFavorite(int animeId, String languageId)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor =
                db.query(TABLE_FAVORITES,
                FAVORITES_COLUMNS,
                KEY_ANIMEID + " = ? AND " + KEY_LANGUAGEID + " = ?", //selections
                new String[] { String.valueOf(animeId), languageId}, //selections args
                null, //group by
                null, //having
                null, //order by
                null);//limit
 
        if (cursor != null)
        {
        	if(cursor.getCount() > 0)
        	{
        		cursor.close();
        		return true;
        	}
        	cursor.close();
        }
        
        return false;
    }
    
    public void addWatched(int animeId, String name, String poster, String description, int episodeId, String episodeNumber, String backdrop, String genres, String rating, int languageId){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ANIMEID, String.valueOf(animeId));
        values.put(KEY_NAME, name);
        values.put(KEY_POSTER, poster);
        values.put(KEY_DESCRIPTION, description);
        values.put(KEY_EPISODEID, String.valueOf(episodeId));
        values.put(KEY_EPISODENUMBER, episodeNumber);
        values.put(KEY_EPISODENUMBER, episodeNumber);
        values.put(KEY_BACKDROP, backdrop);
        values.put(KEY_GENRES, genres);
        values.put(KEY_RATING, rating);
        values.put(KEY_LANGUAGEID, String.valueOf(languageId));
        db.insert(TABLE_WATCHED,
                null, //nullColumnHack
                values);
        
        db.close();
    }
    
    public void removeWatched(int episodeId, String languageId)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_WATCHED, KEY_EPISODEID + " = ? AND " + KEY_LANGUAGEID + " = ?",  new String[] { String.valueOf(episodeId), languageId });
        db.close();
    }
    
    public ArrayList<Anime> GetHistory(String languageId)
    {
    	ArrayList<Anime> animes = new ArrayList<Anime>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor =
                db.query(TABLE_WATCHED,
                WATCHED_COLUMNS,
                KEY_LANGUAGEID + " = ?", //selections
                new String[] {languageId}, //selections args
                null, //group by
                null, //having
                null, //order by
                null);//limit
 
        if (cursor != null)
        {
        	cursor.moveToFirst();
        	for(int i = 0; i < cursor.getCount(); i++)
        	{
        		Anime anime = new Anime();
        		anime.setAnimeId(Integer.valueOf(cursor.getString(0)));
        		anime.setName(cursor.getString(1));
        		anime.setPosterPath(cursor.getString(2));
                ArrayList<AnimeInformation> animeInfos = new ArrayList<AnimeInformation>();
                animeInfos.add(new AnimeInformation(Integer.valueOf(languageId), cursor.getString(3)));
                anime.setAnimeInformations(animeInfos);
        		Episode episode = new Episode();
        		episode.setEpisodeId(Integer.valueOf(cursor.getString(4)));
        		episode.setEpisodeNumber(cursor.getString(5));
        		ArrayList<Episode> episodes = new ArrayList<Episode>();
        		episodes.add(episode);
        		anime.setEpisodes(episodes);
                anime.setBackdropPath(cursor.getString(6));
                ArrayList<Genre> genres = new ArrayList<Genre>();
                String[] genreArray = cursor.getString(7).split(", ");
                for(String genreStr:genreArray)
                {
                    Genre genre = new Genre();
                    genre.setName(genreStr);
                    genres.add(genre);
                }
                anime.setGenres(genres);
                anime.setRating(Double.valueOf(cursor.getString(8)));
        		animes.add(anime);
        		cursor.moveToNext();
        	}
        	cursor.close();
        }
        
        return animes;
    }

    public boolean isWatched(int episodeId, String languageId)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor =
                db.query(TABLE_WATCHED,
                WATCHED_COLUMNS,
                KEY_EPISODEID + " = ? AND " + KEY_LANGUAGEID + " = ?", //selections
                new String[] { String.valueOf(episodeId), languageId}, //selections args
                null, //group by
                null, //having
                null, //order by
                null);//limit
 
        if (cursor != null)
        {
        	if(cursor.getCount() > 0)
        	{
        		cursor.close();
        		return true;
        	}
        	cursor.close();
        }
        
        return false;
    }
}
