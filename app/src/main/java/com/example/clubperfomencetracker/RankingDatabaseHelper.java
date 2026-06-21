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
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_RANKINGS = "rankings";
    private static final String COLUMN_RANK = "rank";
    private static final String COLUMN_HANDLE = "handle";
    private static final String COLUMN_RATING = "rating";

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
                COLUMN_RATING + " INTEGER)";
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

    public void saveRankings(List<Ranking> rankings) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_RANKINGS, null, null);
            
            String sql = "INSERT INTO " + TABLE_RANKINGS + " (" + COLUMN_HANDLE + ", " + COLUMN_RANK + ", " + COLUMN_RATING + ") VALUES (?, ?, ?)";
            SQLiteStatement statement = db.compileStatement(sql);
            
            for (Ranking r : rankings) {
                statement.clearBindings();
                statement.bindString(1, r.getUserName());
                statement.bindLong(2, r.getRank());
                statement.bindLong(3, r.getScore());
                statement.executeInsert();
            }
            
            // Update last updated time
            ContentValues metaValues = new ContentValues();
            metaValues.put(COLUMN_KEY, "last_updated");
            metaValues.put(COLUMN_VALUE, String.valueOf(System.currentTimeMillis()));
            db.insertWithOnConflict(TABLE_METADATA, null, metaValues, SQLiteDatabase.CONFLICT_REPLACE);
            
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public List<Ranking> getAllRankings() {
        List<Ranking> rankings = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_RANKINGS, null, null, null, null, null, COLUMN_RANK + " ASC");

        if (cursor.moveToFirst()) {
            do {
                int rank = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RANK));
                String handle = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HANDLE));
                int rating = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RATING));
                rankings.add(new Ranking(rank, handle, rating, "", 0));
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