package com.example.clubperfomencetracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;
import java.util.List;

public class RankingDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "rankings.db";
    private static final int DATABASE_VERSION = 4; // Incremented version

    private static final String TABLE_RANKINGS = "rankings";
    private static final String COLUMN_RANK = "rank";
    private static final String COLUMN_HANDLE = "handle";
    private static final String COLUMN_RATING = "rating";
    private static final String COLUMN_TITLE = "title";

    private static final String TABLE_METADATA = "metadata";
    private static final String COLUMN_KEY = "key";
    private static final String COLUMN_VALUE = "value";

    public RankingDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createRankingsTable = "CREATE TABLE " + TABLE_RANKINGS + " (" +
                COLUMN_HANDLE + " TEXT PRIMARY KEY, " +
                COLUMN_RANK + " INTEGER, " +
                COLUMN_RATING + " INTEGER, " +
                COLUMN_TITLE + " TEXT)";
        db.execSQL(createRankingsTable);

        String createMetadataTable = "CREATE TABLE " + TABLE_METADATA + " (" +
                COLUMN_KEY + " TEXT PRIMARY KEY, " +
                COLUMN_VALUE + " TEXT)";
        db.execSQL(createMetadataTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RANKINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_METADATA);
        onCreate(db);
    }

    public void addOrUpdateRanking(Ranking r) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HANDLE, r.getUserName());
        values.put(COLUMN_RANK, r.getRank());
        values.put(COLUMN_RATING, r.getScore());
        values.put(COLUMN_TITLE, r.getRankTitle() != null ? r.getRankTitle() : "");
        db.insertWithOnConflict(TABLE_RANKINGS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void saveRankings(List<Ranking> rankings) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_RANKINGS, null, null);
            String sql = "INSERT INTO " + TABLE_RANKINGS + " (" + COLUMN_HANDLE + ", " + COLUMN_RANK + ", " + COLUMN_RATING + ", " + COLUMN_TITLE + ") VALUES (?, ?, ?, ?)";
            SQLiteStatement statement = db.compileStatement(sql);
            for (Ranking r : rankings) {
                statement.clearBindings();
                statement.bindString(1, r.getUserName());
                statement.bindLong(2, r.getRank());
                statement.bindLong(3, r.getScore());
                statement.bindString(4, r.getRankTitle() != null ? r.getRankTitle() : "");
                statement.executeInsert();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void updateLastUpdated() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues metaValues = new ContentValues();
        metaValues.put(COLUMN_KEY, "last_updated");
        metaValues.put(COLUMN_VALUE, String.valueOf(System.currentTimeMillis()));
        db.insertWithOnConflict(TABLE_METADATA, null, metaValues, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public List<Ranking> getAllRankings() {
        List<Ranking> rankings = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_RANKINGS, null, null, null, null, null, COLUMN_RATING + " DESC");

        if (cursor.moveToFirst()) {
            int rankCounter = 1;
            do {
                String handle = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HANDLE));
                int rating = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RATING));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                rankings.add(new Ranking(rankCounter++, handle, rating, title, "", 0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return rankings;
    }

    public String getLastUpdated() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_METADATA, new String[]{COLUMN_VALUE}, COLUMN_KEY + " = ?", new String[]{"last_updated"}, null, null, null);
        String lastUpdated = null;
        if (cursor.moveToFirst()) {
            lastUpdated = cursor.getString(0);
        }
        cursor.close();
        return lastUpdated;
    }
}