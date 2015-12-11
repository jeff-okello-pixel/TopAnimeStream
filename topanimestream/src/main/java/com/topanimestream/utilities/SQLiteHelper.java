package com.topanimestream.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import com.topanimestream.App;
import com.topanimestream.R;
import com.topanimestream.models.Anime;
import com.topanimestream.models.AnimeInformation;
import com.topanimestream.models.Episode;
import com.topanimestream.models.Genre;

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 8;
    private static final String DATABASE_NAME = "AnimeDB";

    private static final String TABLE_FAVORITES = "favorites";
    private static final String TABLE_WATCHED = "watched";
    private static final String TABLE_FLAGS = "flags";

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

    private static final String[] WATCHED_COLUMNS = {KEY_ANIMEID, KEY_NAME, KEY_POSTER, KEY_DESCRIPTION, KEY_EPISODEID, KEY_EPISODENUMBER, KEY_BACKDROP, KEY_GENRES, KEY_RATING, KEY_LANGUAGEID};

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

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


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WATCHED);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FLAGS);
        this.onCreate(db);
    }

    public void addWatched(Anime anime, int episodeId, String episodeNumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ANIMEID, String.valueOf(anime.getAnimeId()));
        values.put(KEY_NAME, anime.getName());
        values.put(KEY_POSTER, ImageUtils.resizeImage(App.getContext().getString(R.string.image_host_path) + anime.getPosterPath(), 185));
        values.put(KEY_DESCRIPTION, anime.getDescription(App.getContext()));
        values.put(KEY_EPISODEID, String.valueOf(episodeId));
        values.put(KEY_EPISODENUMBER, episodeNumber);
        values.put(KEY_BACKDROP, anime.getRelativeBackdropPath(null));
        values.put(KEY_GENRES, anime.getGenresFormatted());
        values.put(KEY_RATING, String.valueOf(anime.getRating()));
        values.put(KEY_LANGUAGEID, String.valueOf(0)); //was used in older version
        db.insert(TABLE_WATCHED,
                null, //nullColumnHack
                values);


    }

    public void removeWatched(int episodeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_WATCHED, KEY_EPISODEID + " = ?", new String[]{String.valueOf(episodeId)});

    }

    public ArrayList<Anime> GetHistory() {
        ArrayList<Anime> animes = new ArrayList<Anime>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor =
                db.query(TABLE_WATCHED,
                        WATCHED_COLUMNS,
                        null, //selections
                        null, //selections args
                        null, //group by
                        null, //having
                        null, //order by
                        null);//limit

        if (cursor != null) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                Anime anime = new Anime();
                anime.setAnimeId(Integer.valueOf(cursor.getString(0)));
                anime.setName(cursor.getString(1));
                anime.setPosterPath(cursor.getString(2));
                ArrayList<AnimeInformation> animeInfos = new ArrayList<AnimeInformation>();
                animeInfos.add(new AnimeInformation(cursor.getString(3)));
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
                for (String genreStr : genreArray) {
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

    public boolean isWatched(int episodeId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor =
                db.query(TABLE_WATCHED,
                        WATCHED_COLUMNS,
                        KEY_EPISODEID + " = ?", //selections
                        new String[]{String.valueOf(episodeId)}, //selections args
                        null, //group by
                        null, //having
                        null, //order by
                        null);//limit

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }

        return false;
    }
}
